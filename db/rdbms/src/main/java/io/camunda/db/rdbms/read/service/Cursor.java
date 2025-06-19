/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.db.rdbms.read.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.camunda.db.rdbms.sql.columns.SearchColumn;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.stream.IntStream;

public class Cursor<T> {
  static final JsonMapper MAPPER =
      JsonMapper.builder()
          .addModule(new JavaTimeModule())
          .disable((SerializationFeature.WRITE_DATES_AS_TIMESTAMPS))
          .build();

  public static <T> String encode(final T entity, final List<SearchColumn<T>> columns) {
    if (columns == null || columns.isEmpty()) {
      return null;
    }
    if (entity == null) {
      return null;
    }

    try {
      final var fields = columns.stream().map(s -> s.getPropertyValue(entity)).toArray();
      final var value = MAPPER.writeValueAsString(fields);

      return Base64.getEncoder().encodeToString(value.getBytes());
    } catch (final JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  public static <T> Object[] decode(final String cursor, final List<SearchColumn<T>> columns) {
    if (columns == null || columns.isEmpty()) {
      return null;
    }

    try {
      final var decodedCursor = java.util.Base64.getDecoder().decode(cursor);
      final var values =
          MAPPER.readValue(
              decodedCursor,
              new TypeReference<Object[]>() {}); // new TypeReference<List<Object>>() {}

      return IntStream.range(0, columns.size())
          .mapToObj(i -> columns.get(i).convertToPropertyValue(values[i]))
          .toArray();

    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }
}
