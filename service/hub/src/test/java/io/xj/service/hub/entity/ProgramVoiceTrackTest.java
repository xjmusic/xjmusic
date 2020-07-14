// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.entity;

import io.xj.lib.util.ValueException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class ProgramVoiceTrackTest {
  @Rule
  public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    ProgramVoiceTrack subject = new ProgramVoiceTrack()
      .setProgramId(UUID.randomUUID())
      .setProgramVoiceId(UUID.randomUUID())
      .setName("Mic Check One Two");
    subject.validate();

    assertEquals("MICCHECKONETWO", subject.getName());
  }

  @Test
  public void validate_failsWithoutVoiceId() throws Exception {
    failure.expect(ValueException.class);
    failure.expectMessage("Voice ID is required");

    new ProgramVoiceTrack()
      .setProgramId(UUID.randomUUID())
      .setName("Thing")
      .validate();
  }

  @Test
  public void validate_failsWithoutName() throws Exception {
    failure.expect(ValueException.class);
    failure.expectMessage("Name is required");

    new ProgramVoiceTrack()
      .setProgramId(UUID.randomUUID())
      .setProgramVoiceId(UUID.randomUUID())
      .validate();
  }

  @Test
  public void validate_failsWithoutProgramId() throws Exception {
    failure.expect(ValueException.class);
    failure.expectMessage("Program ID is required");

    new ProgramVoiceTrack()
      .setProgramVoiceId(UUID.randomUUID())
      .setName("Mic Check One Two")
      .validate();
  }

}
