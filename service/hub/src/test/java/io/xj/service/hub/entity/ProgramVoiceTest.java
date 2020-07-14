// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.entity;

import io.xj.lib.util.ValueException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.UUID;

public class ProgramVoiceTest {

  @Rule
  public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    new ProgramVoice()
      .setType("Harmonic")
      .setName("Mic Check One Two")
      .setProgramId(UUID.randomUUID())
      .validate();
  }

  @Test
  public void validate_failsWithoutProgram() throws Exception {
    failure.expect(ValueException.class);
    failure.expectMessage("Program ID is required");

    new ProgramVoice()
      .setType("Harmonic")
      .setName("Mic Check One Two")
      .validate();
  }

  @Test
  public void validate_failsWithoutType() throws Exception {
    failure.expect(ValueException.class);
    failure.expectMessage("Type is required");

    new ProgramVoice()
      .setName("Mic Check One Two")
      .setProgramId(UUID.randomUUID())
      .validate();
  }

  @Test
  public void validate_failsWithInvalidType() throws Exception {
    failure.expect(ValueException.class);
    failure.expectMessage("'chimney' is not a valid type");

    new ProgramVoice()
      .setType("chimney")
      .setName("Mic Check One Two")
      .setProgramId(UUID.randomUUID())
      .validate();
  }

  @Test
  public void validate_failsWithoutName() throws Exception {
    failure.expect(ValueException.class);
    failure.expectMessage("Name is required");

    new ProgramVoice()
      .setType("Harmonic")
      .setProgramId(UUID.randomUUID())
      .validate();
  }
}
