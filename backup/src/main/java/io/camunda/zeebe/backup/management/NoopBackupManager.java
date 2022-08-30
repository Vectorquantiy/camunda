/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.1. You may not use this file
 * except in compliance with the Zeebe Community License 1.1.
 */
package io.camunda.zeebe.backup.management;

import io.camunda.zeebe.backup.api.BackupManager;
import io.camunda.zeebe.backup.api.BackupStatus;
import io.camunda.zeebe.backup.processing.state.CheckpointState;
import io.camunda.zeebe.scheduler.future.ActorFuture;
import io.camunda.zeebe.scheduler.future.CompletableActorFuture;
import io.opentelemetry.api.OpenTelemetry;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NoopBackupManager implements BackupManager {

  private static final Logger LOG = LoggerFactory.getLogger(NoopBackupManager.class);
  private final String errorMessage;
  private final OpenTelemetry openTelemetry;

  /**
   * @param errorMessage reason for installing NoopBackupManager. All operations will fail with this
   *     message.
   */
  public NoopBackupManager(final String errorMessage, final OpenTelemetry openTelemetry) {
    this.errorMessage = errorMessage;
    this.openTelemetry = openTelemetry;
  }

  @Override
  public ActorFuture<Void> takeBackup(final long checkpointId, final long checkpointPosition) {
    LOG.warn("Attempted to take backup, but cannot take backup. {}", errorMessage);
    return CompletableActorFuture.completedExceptionally(new Exception(errorMessage), openTelemetry);
  }

  @Override
  public ActorFuture<BackupStatus> getBackupStatus(final long checkpointId) {
    return CompletableActorFuture.completedExceptionally(
        new UnsupportedOperationException(errorMessage), openTelemetry);
  }

  @Override
  public ActorFuture<Collection<BackupStatus>> listBackups() {
    return CompletableActorFuture.completedExceptionally(
        new UnsupportedOperationException(errorMessage), openTelemetry);
  }

  @Override
  public ActorFuture<Void> deleteBackup(final long checkpointId) {
    return CompletableActorFuture.completedExceptionally(
        new UnsupportedOperationException(errorMessage), openTelemetry);
  }

  @Override
  public ActorFuture<Void> closeAsync() {
    return CompletableActorFuture.completed(null);
  }

  @Override
  public void failInProgressBackup(final long lastCheckpointId) {
    if (lastCheckpointId == CheckpointState.NO_CHECKPOINT) {
      return;
    }
    LOG.warn("Attempted to update in progress backup, but cannot do it. {}", errorMessage);
  }
}
