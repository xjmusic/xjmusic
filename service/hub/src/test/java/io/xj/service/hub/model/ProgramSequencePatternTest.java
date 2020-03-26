// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.model;

import io.xj.lib.util.ValueException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.UUID;

public class ProgramSequencePatternTest {
  @Rule
  public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    new ProgramSequencePattern()
      .setProgramId(UUID.randomUUID())
      .setProgramSequenceId(UUID.randomUUID())
      .setProgramVoiceId(UUID.randomUUID())
      .setName("Mic Check One Two")
      .setTypeEnum(ProgramSequencePatternType.Loop)
      .setTotal(64)
      .validate();
  }

  @Test
  public void validate_withMinimalAttributes() throws Exception {
    new ProgramSequencePattern()
      .setProgramId(UUID.randomUUID())
      .setProgramSequenceId(UUID.randomUUID())
      .setProgramVoiceId(UUID.randomUUID())
      .setTotal(64)
      .setTypeEnum(ProgramSequencePatternType.Loop)
      .setName("Mic Check One Two")
      .validate();
  }

  @Test
  public void validate_failsWithoutVoiceId() throws Exception {
    failure.expect(ValueException.class);
    failure.expectMessage("Voice ID is required");

    new ProgramSequencePattern()
      .setName("Test54")
      .setProgramId(UUID.randomUUID())
      .setProgramSequenceId(UUID.randomUUID())
      .setTypeEnum(ProgramSequencePatternType.Loop)
      .setTotal(64)
      .validate();
  }

  @Test
  public void validate_failsWithoutSequenceId() throws Exception {
    failure.expect(ValueException.class);
    failure.expectMessage("Sequence ID is required");

    new ProgramSequencePattern()
      .setProgramId(UUID.randomUUID())
      .setProgramVoiceId(UUID.randomUUID())
      .setName("Mic Check One Two")
      .setTotal(64)
      .setTypeEnum(ProgramSequencePatternType.Loop)
      .validate();
  }

  @Test
  public void validate_failsWithoutType() throws Exception {
    failure.expect(ValueException.class);
    failure.expectMessage("Type is required");

    new ProgramSequencePattern()
      .setProgramId(UUID.randomUUID())
      .setProgramSequenceId(UUID.randomUUID())
      .setName("Mic Check One Two")
      .setProgramVoiceId(UUID.randomUUID())
      .setTotal(64)
      .validate();
  }

  @Test
  public void validate_failsWithoutTotal() throws Exception {
    failure.expect(ValueException.class);
    failure.expectMessage("Total is required");

    new ProgramSequencePattern()
      .setProgramId(UUID.randomUUID())
      .setProgramSequenceId(UUID.randomUUID())
      .setName("Mic Check One Two")
      .setProgramVoiceId(UUID.randomUUID())
      .setTypeEnum(ProgramSequencePatternType.Loop)
      .validate();
  }

  @Test
  public void validate_failsWithoutName() throws Exception {
    failure.expect(ValueException.class);
    failure.expectMessage("Name is required");

    new ProgramSequencePattern()
      .setProgramSequenceId(UUID.randomUUID())
      .setProgramId(UUID.randomUUID())
      .setProgramVoiceId(UUID.randomUUID())
      .setTypeEnum(ProgramSequencePatternType.Loop)
      .setTotal(64)
      .validate();
  }

  @Test
  public void validate_failsWithoutProgramId() throws Exception {
    failure.expect(ValueException.class);
    failure.expectMessage("Program ID is required");

    new ProgramSequencePattern()
      .setProgramSequenceId(UUID.randomUUID())
      .setProgramVoiceId(UUID.randomUUID())
      .setName("Mic Check One Two")
      .setTypeEnum(ProgramSequencePatternType.Loop)
      .setTotal(64)
      .validate();
  }
}
