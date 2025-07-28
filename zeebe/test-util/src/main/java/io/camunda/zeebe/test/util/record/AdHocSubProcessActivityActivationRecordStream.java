/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.zeebe.test.util.record;

import io.camunda.zeebe.protocol.record.Record;
import io.camunda.zeebe.protocol.record.intent.ProcessInstanceIntent;
import io.camunda.zeebe.protocol.record.value.AdHocSubProcessInstructionRecordValue;
import java.util.stream.Stream;

public class AdHocSubProcessActivityActivationRecordStream
    extends ExporterRecordStream<
        AdHocSubProcessInstructionRecordValue, AdHocSubProcessActivityActivationRecordStream> {

  public AdHocSubProcessActivityActivationRecordStream(
      final Stream<Record<AdHocSubProcessInstructionRecordValue>> wrappedStream) {
    super(wrappedStream);
  }

  @Override
  protected AdHocSubProcessActivityActivationRecordStream supply(
      final Stream<Record<AdHocSubProcessInstructionRecordValue>> wrappedStream) {
    return new AdHocSubProcessActivityActivationRecordStream(wrappedStream);
  }

  public AdHocSubProcessActivityActivationRecordStream withAdHocSubProcessInstanceKey(
      final String adHocSubProcessInstanceKey) {
    return valueFilter(
        record -> record.getAdHocSubProcessInstanceKey().equals(adHocSubProcessInstanceKey));
  }

  public AdHocSubProcessActivityActivationRecordStream limitToAdHocSubProcessInstanceCompleted() {
    return limit(
        r ->
            r.getIntent() == ProcessInstanceIntent.ELEMENT_COMPLETED
                && r.getKey() == Long.parseLong(r.getValue().getAdHocSubProcessInstanceKey()));
  }
}
