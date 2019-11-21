//  Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.segment;

import com.google.common.collect.ImmutableList;
import io.xj.core.exception.CoreException;

import io.xj.core.model.SegmentChoiceArrangement;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.UUID;

import static io.xj.core.testing.Assert.assertSameItems;
import static org.junit.Assert.assertEquals;

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
    failure.expect(CoreException.class);
    failure.expectMessage("Segment ID is required");

    new SegmentChoiceArrangement()
      .setProgramVoiceId(UUID.randomUUID())
      .setSegmentChoiceId(UUID.randomUUID())
      .setInstrumentId(UUID.randomUUID())
      .validate();
  }

  @Test
  public void validate_failsWithoutVoiceID() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Voice ID is required");

    new SegmentChoiceArrangement()
      .setSegmentChoiceId(UUID.randomUUID())
      .setInstrumentId(UUID.randomUUID())
      .setSegmentId(UUID.randomUUID())
      .validate();
  }

  @Test
  public void validate_failsWithoutChoiceID() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Choice ID is required");

    new SegmentChoiceArrangement()
      .setProgramVoiceId(UUID.randomUUID())
      .setInstrumentId(UUID.randomUUID())
      .setSegmentId(UUID.randomUUID())
      .validate();
  }

  @Test
  public void validate_failsWithoutInstrumentID() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Instrument ID is required");

    new SegmentChoiceArrangement()
      .setProgramVoiceId(UUID.randomUUID())
      .setSegmentChoiceId(UUID.randomUUID())
      .setSegmentId(UUID.randomUUID())
      .validate();
  }

  @Test
  public void getPayloadAttributeNames() {
    assertSameItems(ImmutableList.of(), new SegmentChoiceArrangement().getResourceAttributeNames());
  }

}


