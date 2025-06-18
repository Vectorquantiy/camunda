/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.zeebe.broker.partitioning.scaling.snapshot;

import java.util.Optional;
import java.util.UUID;

public sealed interface SnapshotRequest {

  int partitionId();

  SnapshotRequest withPartitionId(int partitionId);

  default boolean requiresPartitionId() {
    return true;
  }

  default boolean addressesSpecificPartition() {
    return true;
  }

  record GetSnapshotChunk(
      int partitionId, UUID transferId, Optional<String> snapshotId, Optional<String> lastChunkName)
      implements SnapshotRequest {
    @Override
    public GetSnapshotChunk withPartitionId(final int partitionId) {
      return new GetSnapshotChunk(partitionId, transferId, snapshotId, lastChunkName);
    }
  }

  record DeleteSnapshotForBootstrapRequest(int partitionId) implements SnapshotRequest {

    @Override
    public DeleteSnapshotForBootstrapRequest withPartitionId(final int partitionId) {
      return new DeleteSnapshotForBootstrapRequest(partitionId);
    }
  }
}
