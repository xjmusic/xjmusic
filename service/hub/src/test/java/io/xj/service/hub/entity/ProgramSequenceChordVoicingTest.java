// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.entity;

import io.xj.lib.util.ValueException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.UUID;

public class ProgramSequenceChordVoicingTest {

  @Rule
  public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    new ProgramSequenceChordVoicing()
      .setProgramId(UUID.randomUUID())
      .setProgramSequenceChordId(UUID.randomUUID())
      .setType(InstrumentType.Harmonic)
      .setNotes("G,B,Db,F")
      .validate();
  }

  @Test
  public void validate_failsWithoutProgram() throws Exception {
    failure.expect(ValueException.class);
    failure.expectMessage("Program ID is required");

    new ProgramSequenceChordVoicing()
      .setProgramSequenceChordId(UUID.randomUUID())
      .setType(InstrumentType.Harmonic)
      .setNotes("G,B,Db,F")
      .validate();
  }

  @Test
  public void validate_failsWithoutSequenceChordId() throws Exception {
    failure.expect(ValueException.class);
    failure.expectMessage("Sequence Chord ID is required");

    new ProgramSequenceChordVoicing()
      .setProgramId(UUID.randomUUID())
      .setType(InstrumentType.Harmonic)
      .setNotes("G,B,Db,F")
      .validate();
  }

  @Test
  public void validate_failsWithoutNotes() throws Exception {
    failure.expect(ValueException.class);
    failure.expectMessage("Notes are required");

    new ProgramSequenceChordVoicing()
      .setProgramId(UUID.randomUUID())
      .setType(InstrumentType.Harmonic)
      .setProgramSequenceChordId(UUID.randomUUID())
      .validate();
  }

  @Test
  public void validate_failsWithoutType() throws Exception {
    failure.expect(ValueException.class);
    failure.expectMessage("Voice type is required");

    new ProgramSequenceChordVoicing()
      .setProgramId(UUID.randomUUID())
      .setNotes("G,B,Db,F")
      .setProgramSequenceChordId(UUID.randomUUID())
      .validate();
  }
}
