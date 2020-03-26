// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.service.hub.model;

import io.xj.lib.util.ValueException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.UUID;

/**
 [#166132897] Segment model handles all of its own entities
 [#166273140] Segment Child Entities are identified and related by UUID (not id)
 */
public class SegmentChoiceTest {
  @Rule
  public ExpectedException failure = ExpectedException.none();
  SegmentChoice subject;

  @Before
  public void setUp() throws Exception {
    subject = new SegmentChoice();
  }

  @Test
  public void validate() throws ValueException {
    subject
      .setType("Main")
      .setProgramId(UUID.randomUUID())
      .setSegmentId(UUID.randomUUID())
      .setProgramSequenceBindingId(UUID.randomUUID())
      .setTranspose(-2)
      .validate();
  }

  @Test
  public void validate_okWithTypeEnum() throws ValueException {
    subject
      .setTypeEnum(ProgramType.Main)
      .setProgramId(UUID.randomUUID())
      .setSegmentId(UUID.randomUUID())
      .setProgramSequenceBindingId(UUID.randomUUID())
      .setTranspose(-2)
      .validate();
  }

  @Test
  public void validate_okWithoutTranspose() throws ValueException {
    subject
      .setType("Main")
      .setProgramId(UUID.randomUUID())
      .setSegmentId(UUID.randomUUID())
      .setProgramSequenceBindingId(UUID.randomUUID())
      .validate();
  }


  @Test
  public void validate_exceptionWithoutSegmentId() throws ValueException {
    failure.expect(ValueException.class);
    failure.expectMessage("Segment ID is required");

    subject
      .setType("Main")
      .setProgramId(UUID.randomUUID())
      .setProgramSequenceBindingId(UUID.randomUUID())
      .setTranspose(-2)
      .validate();
  }

  @Test
  public void validate_exceptionWithoutProgramId() throws ValueException {
    failure.expect(ValueException.class);
    failure.expectMessage("Program ID is required");

    subject
      .setType("Main")
      .setSegmentId(UUID.randomUUID())
      .setProgramSequenceBindingId(UUID.randomUUID())
      .setTranspose(-2)
      .validate();
  }


}
