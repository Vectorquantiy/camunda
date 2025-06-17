/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.exporter.tasks.archiver;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.camunda.exporter.config.ExporterConfiguration.HistoryConfiguration;
import io.camunda.exporter.metrics.CamundaExporterMetrics;
import io.camunda.exporter.tasks.util.DateOfArchivedDocumentsUtil;
import io.camunda.exporter.tasks.util.OpensearchRepository;
import io.camunda.search.schema.config.RetentionConfiguration;
import io.camunda.webapps.schema.descriptors.AbstractIndexDescriptor;
import io.camunda.webapps.schema.descriptors.template.BatchOperationTemplate;
import io.camunda.webapps.schema.descriptors.template.ListViewTemplate;
import io.camunda.zeebe.exporter.api.ExporterException;
import io.micrometer.core.instrument.Timer;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.WillCloseWhenClosed;
import org.opensearch.client.json.JsonData;
import org.opensearch.client.opensearch.OpenSearchAsyncClient;
import org.opensearch.client.opensearch._types.Conflicts;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.SortOrder;
import org.opensearch.client.opensearch._types.Time;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch._types.query_dsl.QueryBuilders;
import org.opensearch.client.opensearch._types.query_dsl.TermsQuery;
import org.opensearch.client.opensearch.cat.indices.IndicesRecord;
import org.opensearch.client.opensearch.core.DeleteByQueryRequest;
import org.opensearch.client.opensearch.core.DeleteByQueryResponse;
import org.opensearch.client.opensearch.core.ReindexRequest;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.reindex.Source;
import org.opensearch.client.opensearch.core.search.Hit;
import org.opensearch.client.opensearch.generic.OpenSearchGenericClient;
import org.opensearch.client.opensearch.generic.Requests;
import org.slf4j.Logger;

public final class OpenSearchArchiverRepository extends OpensearchRepository
    implements ArchiverRepository {
  private static final Time REINDEX_SCROLL_TIMEOUT = Time.of(t -> t.time("30s"));
  private static final long AUTO_SLICES = 0; // see OS docs; 0 means auto
  private static final String INDEX_WILDCARD = ".+-\\d+\\.\\d+\\.\\d+_.+$";
  private static final String ALL_INDICES_PATTERN = ".*";

  private final int partitionId;
  private final HistoryConfiguration config;
  private final RetentionConfiguration retention;
  private final String indexPrefix;
  private final String processInstanceIndex;
  private final String batchOperationIndex;
  private final CamundaExporterMetrics metrics;
  private final OpenSearchGenericClient genericClient;
  private String lastHistoricalArchiverDate = null;

  public OpenSearchArchiverRepository(
      final int partitionId,
      final HistoryConfiguration config,
      final RetentionConfiguration retention,
      final String indexPrefix,
      final String processInstanceIndex,
      final String batchOperationIndex,
      @WillCloseWhenClosed final OpenSearchAsyncClient client,
      final Executor executor,
      final CamundaExporterMetrics metrics,
      final Logger logger) {
    super(client, executor, logger);
    this.partitionId = partitionId;
    this.config = config;
    this.retention = retention;
    this.indexPrefix = indexPrefix;
    this.processInstanceIndex = processInstanceIndex;
    this.batchOperationIndex = batchOperationIndex;
    this.metrics = metrics;

    genericClient = new OpenSearchGenericClient(client._transport(), client._transportOptions());
  }

  @Override
  public CompletableFuture<ArchiveBatch> getProcessInstancesNextBatch() {
    final var request = createFinishedInstancesSearchRequest();

    final var timer = Timer.start();
    return sendRequestAsync(() -> client.search(request, Object.class))
        .whenCompleteAsync((ignored, error) -> metrics.measureArchiverSearch(timer), executor)
        .thenComposeAsync(
            (response) -> createArchiveBatch(response, ListViewTemplate.END_DATE), executor);
  }

  @Override
  public CompletableFuture<ArchiveBatch> getBatchOperationsNextBatch() {
    final var searchRequest = createFinishedBatchOperationsSearchRequest();

    final var timer = Timer.start();
    return sendRequestAsync(() -> client.search(searchRequest, Object.class))
        .whenCompleteAsync((ignored, error) -> metrics.measureArchiverSearch(timer), executor)
        .thenComposeAsync(
            (response) -> createArchiveBatch(response, BatchOperationTemplate.END_DATE), executor);
  }

  @Override
  public CompletableFuture<Void> setIndexLifeCycle(final String... destinationIndexName) {
    if (!retention.isEnabled() || destinationIndexName.length == 0) {
      return CompletableFuture.completedFuture(null);
    }

    return CompletableFuture.allOf(
        Arrays.stream(destinationIndexName)
            .map(this::applyPolicyToIndex)
            .toArray(CompletableFuture[]::new));
  }

  @Override
  public CompletableFuture<Void> setLifeCycleToAllIndexes() {
    if (!retention.isEnabled()) {
      return CompletableFuture.completedFuture(null);
    }

    final var formattedPrefix = AbstractIndexDescriptor.formatIndexPrefix(indexPrefix);
    final var indexWildCard = "^" + formattedPrefix + INDEX_WILDCARD;

    try {
      return fetchIndexMatchingIndexes(indexWildCard)
          .thenComposeAsync(indices -> setIndexLifeCycle(indices.toArray(String[]::new)), executor);
    } catch (final IOException e) {
      return CompletableFuture.failedFuture(new ExporterException("Failed to fetch indexes:", e));
    }
  }

  @Override
  public CompletableFuture<Void> deleteDocuments(
      final String sourceIndexName,
      final String idFieldName,
      final List<String> processInstanceKeys) {
    final TermsQuery termsQuery = buildIdTermsQuery(idFieldName, processInstanceKeys);
    final var request =
        new DeleteByQueryRequest.Builder()
            .index(sourceIndexName)
            .slices(AUTO_SLICES)
            .conflicts(Conflicts.Proceed)
            .query(q -> q.terms(termsQuery))
            .build();

    final var timer = Timer.start();
    return sendRequestAsync(() -> client.deleteByQuery(request))
        .whenCompleteAsync((ignored, error) -> metrics.measureArchiverDelete(timer), executor)
        .thenApplyAsync(DeleteByQueryResponse::total, executor)
        .thenApplyAsync(ok -> null, executor);
  }

  @Override
  public CompletableFuture<Void> reindexDocuments(
      final String sourceIndexName,
      final String destinationIndexName,
      final String idFieldName,
      final List<String> processInstanceKeys) {
    final var source =
        new Source.Builder()
            .index(sourceIndexName)
            .query(q -> q.terms(buildIdTermsQuery(idFieldName, processInstanceKeys)))
            .build();
    final var request =
        new ReindexRequest.Builder()
            .source(source)
            .dest(dest -> dest.index(destinationIndexName))
            .conflicts(Conflicts.Proceed)
            .scroll(REINDEX_SCROLL_TIMEOUT)
            .slices(AUTO_SLICES)
            .build();

    final var timer = Timer.start();
    return sendRequestAsync(() -> client.reindex(request))
        .whenCompleteAsync((ignored, error) -> metrics.measureArchiverReindex(timer), executor)
        .thenApplyAsync(ignored -> null, executor);
  }

  private CompletableFuture<List<String>> fetchIndexMatchingIndexes(final String indexWildCard)
      throws IOException {
    final var pattern = Pattern.compile(indexWildCard);
    return client
        .cat()
        .indices()
        .thenApply(
            r ->
                r.valueBody().stream()
                    .map(IndicesRecord::index)
                    .filter(Objects::nonNull)
                    .filter(index -> pattern.matcher(index).matches())
                    .toList());
  }

  private CompletableFuture<Void> applyPolicyToIndex(final String index) {
    final AddPolicyRequestBody value = new AddPolicyRequestBody(retention.getPolicyName());
    final var request = Requests.builder().method("POST").endpoint("_plugins/_ism/add/" + index);
    return sendRequestAsync(
            () ->
                genericClient.executeAsync(
                    request.json(value, genericClient._transport().jsonpMapper()).build()))
        .thenComposeAsync(
            response -> {
              if (response.getStatus() >= 400) {
                return CompletableFuture.failedFuture(
                    new ExporterException(
                        "Failed to set index lifecycle policy for index: "
                            + index
                            + ".\n"
                            + "Status: "
                            + response.getStatus()
                            + ", Reason: "
                            + response.getReason()));
              }
              return CompletableFuture.completedFuture(null);
            },
            executor);
  }

  private SearchRequest createFinishedBatchOperationsSearchRequest() {
    final var endDateQ =
        QueryBuilders.range()
            .field(BatchOperationTemplate.END_DATE)
            .lte(JsonData.of(config.getArchivingTimePoint()))
            .build();

    return createSearchRequest(
        batchOperationIndex, endDateQ.toQuery(), BatchOperationTemplate.END_DATE);
  }

  private CompletableFuture<ArchiveBatch> createArchiveBatch(
      final SearchResponse<?> response, final String field) {
    final var hits = response.hits().hits();
    if (hits.isEmpty()) {
      return CompletableFuture.completedFuture(new ArchiveBatch(null, List.of()));
    }
    final var endDate = hits.getFirst().fields().get(field).toJson().asJsonArray().getString(0);

    final CompletableFuture<String> dateFuture;
    try {
      dateFuture =
          (lastHistoricalArchiverDate == null)
              ? fetchLastHistoricalArchiverDate()
              : CompletableFuture.completedFuture(lastHistoricalArchiverDate);
    } catch (final IOException e) {
      return CompletableFuture.failedFuture(new ExporterException("Failed to fetch indexes:", e));
    }

    return dateFuture.thenApply(
        date -> {
          lastHistoricalArchiverDate =
              DateOfArchivedDocumentsUtil.calculateDateOfArchiveIndexForBatch(
                  endDate, date, config.getRolloverInterval(), config.getElsRolloverDateFormat());

          final var ids =
              hits.stream()
                  .takeWhile(
                      hit ->
                          hit.fields()
                              .get(field)
                              .toJson()
                              .asJsonArray()
                              .getString(0)
                              .equals(endDate))
                  .map(Hit::id)
                  .toList();

          return new ArchiveBatch(lastHistoricalArchiverDate, ids);
        });
  }

  private TermsQuery buildIdTermsQuery(final String idFieldName, final List<String> idValues) {
    return QueryBuilders.terms()
        .field(idFieldName)
        .terms(terms -> terms.value(idValues.stream().map(FieldValue::of).toList()))
        .build();
  }

  private <T> CompletableFuture<T> sendRequestAsync(final RequestSender<T> sender) {
    try {
      return sender.sendRequest();
    } catch (final IOException e) {
      return CompletableFuture.failedFuture(
          new ExporterException(
              "Failed to send request, likely because we failed to parse the request", e));
    }
  }

  private SearchRequest createFinishedInstancesSearchRequest() {
    final var endDateQ =
        QueryBuilders.range()
            .field(ListViewTemplate.END_DATE)
            .lte(JsonData.of(config.getArchivingTimePoint()))
            .build();
    final var isProcessInstanceQ =
        QueryBuilders.term()
            .field(ListViewTemplate.JOIN_RELATION)
            .value(FieldValue.of(ListViewTemplate.PROCESS_INSTANCE_JOIN_RELATION))
            .build();
    final var partitionQ =
        QueryBuilders.term()
            .field(ListViewTemplate.PARTITION_ID)
            .value(FieldValue.of(partitionId))
            .build();
    final var combinedQuery =
        QueryBuilders.bool()
            .must(endDateQ.toQuery(), isProcessInstanceQ.toQuery(), partitionQ.toQuery())
            .build();

    return createSearchRequest(
        processInstanceIndex, combinedQuery.toQuery(), ListViewTemplate.END_DATE);
  }

  private SearchRequest createSearchRequest(
      final String indexName, final Query filterQuery, final String sortField) {
    logger.trace(
        "Create search request against index '{}', with filter '{}' and sortField '{}'",
        indexName,
        filterQuery.toString(),
        sortField);

    return new SearchRequest.Builder()
        .index(indexName)
        .requestCache(false)
        .allowNoIndices(true)
        .ignoreUnavailable(true)
        .source(source -> source.fetch(false))
        .fields(fields -> fields.field(sortField).format(config.getElsRolloverDateFormat()))
        .query(query -> query.bool(q -> q.filter(filterQuery)))
        .sort(sort -> sort.field(field -> field.field(sortField).order(SortOrder.Asc)))
        .size(config.getRolloverBatchSize())
        .build();
  }

  private CompletableFuture<String> fetchLastHistoricalArchiverDate() throws IOException {
    final DateTimeFormatter formatterWithHour = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH");
    final DateTimeFormatter formatterWithoutHour = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    final LocalDateTime[] latest = {null};
    final String[] latestDate = {null};
    final Pattern indexDatePattern = Pattern.compile("_(\\d{4}-\\d{2}-\\d{2}(?:-\\d{2})?)");

    return fetchIndexMatchingIndexes(ALL_INDICES_PATTERN)
        .thenApply(
            indexes ->
                // we want to filter out zeebe records since the
                // rollover interval does not apply to these.
                indexes.stream().filter(index -> !index.contains("zeebe-record")).toList())
        .thenApply(
            indexes -> {
              for (final String index : indexes) {
                final Matcher matcher = indexDatePattern.matcher(index);
                if (matcher.find()) {
                  final String dateStr = matcher.group(1);
                  final LocalDateTime dateTime;

                  if (dateStr.length() == 13) { // e.g., 2025-06-16-10
                    dateTime = LocalDateTime.parse(dateStr, formatterWithHour);
                  } else { // e.g., 2025-06-16
                    dateTime = LocalDate.parse(dateStr, formatterWithoutHour).atStartOfDay();
                  }

                  if (latest[0] == null || dateTime.isAfter(latest[0])) {
                    latest[0] = dateTime;
                    latestDate[0] = dateStr;
                  }
                }
              }
              // if no date historical index exists we want to return null
              return latestDate[0];
            });
  }

  private record AddPolicyRequestBody(@JsonProperty("policy_id") String policyId) {}

  @FunctionalInterface
  private interface RequestSender<T> {
    CompletableFuture<T> sendRequest() throws IOException;
  }
}
