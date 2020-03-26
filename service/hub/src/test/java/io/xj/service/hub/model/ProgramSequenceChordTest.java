// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.model;

import io.xj.lib.util.ValueException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ProgramSequenceChordTest {

  @Rule
  public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    new ProgramSequenceChord()
      .setProgramId(UUID.randomUUID())
      .setName("C# minor")
      .setProgramSequenceId(UUID.randomUUID())
      .setPosition(7.0)
      .validate();
  }

  @Test
  public void validate_failsWithoutProgram() throws Exception {
    failure.expect(ValueException.class);
    failure.expectMessage("Program ID is required");

    new ProgramSequenceChord()
      .setName("C# minor")
      .setProgramSequenceId(UUID.randomUUID())
      .setPosition(7.0)
      .validate();
  }

  @Test
  public void validate_failsWithoutName() throws Exception {
    failure.expect(ValueException.class);
    failure.expectMessage("Name is required");

    new ProgramSequenceChord()
      .setProgramId(UUID.randomUUID())
      .setProgramSequenceId(UUID.randomUUID())
      .setPosition(7.0)
      .validate();
  }

  @Test
  public void validate_failsWithoutSequenceUUID() throws Exception {
    failure.expect(ValueException.class);
    failure.expectMessage("Sequence ID is required");

    new ProgramSequenceChord()
      .setProgramId(UUID.randomUUID())
      .setName("C# minor")
      .setPosition(7.0)
      .validate();
  }

  @Test
  public void validate_failsWithoutPosition() throws Exception {
    failure.expect(ValueException.class);
    failure.expectMessage("Position is required");

    new ProgramSequenceChord()
      .setProgramId(UUID.randomUUID())
      .setName("C# minor")
      .setProgramSequenceId(UUID.randomUUID())
      .validate();
  }

  /**
   [#154976066] Architect wants to limit the floating point precision of chord and chord position, in order to limit obsession over the position of things.
   */
  @Test
  public void position_rounded() {
    assertEquals(1.25, new ProgramSequenceChord().setPosition(1.25179957).getPosition(), 0.0000001);
  }

  @Test
  public void isNoChord() {
    assertFalse(new ProgramSequenceChord().setName("C#m7").isNoChord());
    assertTrue(new ProgramSequenceChord().setName("NC").isNoChord());
  }

}
