/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.operate.schema.util.camunda.exporter;

import io.camunda.exporter.DefaultExporterResourceProvider;
import io.camunda.exporter.ExporterMetadata;
import io.camunda.exporter.adapters.ClientAdapter;
import io.camunda.exporter.config.ConnectionTypes;
import io.camunda.exporter.config.ExporterConfiguration;
import io.camunda.search.schema.SchemaManager;
import io.camunda.search.schema.config.SearchEngineConfiguration;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

public class SchemaWithExporter {

  private final SchemaManager schemaManager;

  public SchemaWithExporter(final String prefix, final boolean isElasticsearch) {
    final var config = new ExporterConfiguration();
    config.getConnect().setIndexPrefix(prefix);
    config
        .getConnect()
        .setType(
            isElasticsearch
                ? ConnectionTypes.ELASTICSEARCH.getType()
                : ConnectionTypes.OPENSEARCH.getType());

    final var clientAdapter = ClientAdapter.of(config.getConnect());
    final var provider = new DefaultExporterResourceProvider();
    provider.init(
        config,
        clientAdapter.getExporterEntityCacheProvider(),
        new SimpleMeterRegistry(),
        new ExporterMetadata(clientAdapter.objectMapper()),
        clientAdapter.objectMapper());

    schemaManager =
        new SchemaManager(
            clientAdapter.getSearchEngineClient(),
            provider.getIndexDescriptors(),
            provider.getIndexTemplateDescriptors(),
            SearchEngineConfiguration.of(b -> b.connect(config.getConnect())),
            clientAdapter.objectMapper());
  }

  public void createSchema() {
    schemaManager.startup();
  }
}
