/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.zeebe.broker.partitioning.scaling.snapshot.sbe;

import org.agrona.MutableDirectBuffer;

public interface SbeSerializer<T> {
  int size(final T message);

  int serialize(final T message, final MutableDirectBuffer buffer, final int offset);
}
