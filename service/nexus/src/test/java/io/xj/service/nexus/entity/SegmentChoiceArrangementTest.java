// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.entity;

import io.xj.lib.util.ValueException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.UUID;

/**
 [#166132897] Segment model handles all of its own entities
 [#166273140] Segment Child Entities are identified and related by UUID (not id)
 */
public class SegmentChoiceArrangementTest {

  @Rule
  public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    new SegmentChoiceArrangement()
      .setProgramVoiceId(UUID.randomUUID())
      .setSegmentChoiceId(UUID.randomUUID())
      .setInstrumentId(UUID.randomUUID())
      .setSegmentId(UUID.randomUUID())
      .validate();
  }

  @Test
  public void validate_failsWithoutSegmentID() throws Exception {
    failure.expect(ValueException.class);
    failure.expectMessage("Segment ID is required");

    new SegmentChoiceArrangement()
      .setProgramVoiceId(UUID.randomUUID())
      .setSegmentChoiceId(UUID.randomUUID())
      .setInstrumentId(UUID.randomUUID())
      .validate();
  }

  @Test
  public void validate_failsWithoutVoiceID() throws Exception {
    failure.expect(ValueException.class);
    failure.expectMessage("Voice ID is required");

    new SegmentChoiceArrangement()
      .setSegmentChoiceId(UUID.randomUUID())
      .setInstrumentId(UUID.randomUUID())
      .setSegmentId(UUID.randomUUID())
      .validate();
  }

  @Test
  public void validate_failsWithoutChoiceID() throws Exception {
    failure.expect(ValueException.class);
    failure.expectMessage("Choice ID is required");

    new SegmentChoiceArrangement()
      .setProgramVoiceId(UUID.randomUUID())
      .setInstrumentId(UUID.randomUUID())
      .setSegmentId(UUID.randomUUID())
      .validate();
  }

  @Test
  public void validate_failsWithoutInstrumentID() throws Exception {
    failure.expect(ValueException.class);
    failure.expectMessage("Instrument ID is required");

    new SegmentChoiceArrangement()
      .setProgramVoiceId(UUID.randomUUID())
      .setSegmentChoiceId(UUID.randomUUID())
      .setSegmentId(UUID.randomUUID())
      .validate();
  }


}


