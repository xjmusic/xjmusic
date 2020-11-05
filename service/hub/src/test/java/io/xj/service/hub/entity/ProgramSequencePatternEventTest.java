// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.entity;

import io.xj.lib.util.ValueException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class ProgramSequencePatternEventTest {

  @Rule
  public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    new ProgramSequencePatternEvent()
      .setProgramId(UUID.randomUUID())
      .setProgramSequencePatternId(UUID.randomUUID())
      .setProgramVoiceTrackId(UUID.randomUUID())
      .setPosition(0.75)
      .setDuration(3.45)
      .setNote("D6")
      .setVelocity(0.9)
      .validate();
  }

  @Test
  public void validate_failsWithoutDuration() throws Exception {
    failure.expect(ValueException.class);
    failure.expectMessage("Duration is required");

    new ProgramSequencePatternEvent()
      .setProgramId(UUID.randomUUID())
      .setProgramSequencePatternId(UUID.randomUUID())
      .setProgramVoiceTrackId(UUID.randomUUID())
      .setPosition(0.75)
      .setNote("D6")
      .setVelocity(0.9)
      .validate();
  }

  @Test
  public void validate_failsWithoutPatternID() throws Exception {
    failure.expect(ValueException.class);
    failure.expectMessage("Pattern ID is required");

    new ProgramSequencePatternEvent()
      .setProgramId(UUID.randomUUID())
      .setProgramVoiceTrackId(UUID.randomUUID())
      .setPosition(0.75)
      .setDuration(3.45)
      .setNote("D6")
      .setVelocity(0.9)
      .validate();
  }

  @Test
  public void validate_failsWithoutTrackID() throws Exception {
    failure.expect(ValueException.class);
    failure.expectMessage("Track ID is required");

    new ProgramSequencePatternEvent()
      .setProgramId(UUID.randomUUID())
      .setProgramSequencePatternId(UUID.randomUUID())
      .setPosition(0.75)
      .setDuration(3.45)
      .setNote("D6")
      .setVelocity(0.9)
      .validate();
  }

  @Test
  public void validate_failsWithoutPosition() throws Exception {
    failure.expect(ValueException.class);
    failure.expectMessage("Position is required");

    new ProgramSequencePatternEvent()
      .setProgramId(UUID.randomUUID())
      .setProgramSequencePatternId(UUID.randomUUID())
      .setProgramVoiceTrackId(UUID.randomUUID())
      .setDuration(3.45)
      .setNote("D6")
      .setVelocity(0.9)
      .validate();
  }

  @Test
  public void validate_failsWithoutNote() throws Exception {
    failure.expect(ValueException.class);
    failure.expectMessage("Note is required");

    new ProgramSequencePatternEvent()
      .setProgramId(UUID.randomUUID())
      .setProgramSequencePatternId(UUID.randomUUID())
      .setProgramVoiceTrackId(UUID.randomUUID())
      .setPosition(0.75)
      .setDuration(3.45)
      .setVelocity(0.9)
      .validate();
  }

  @Test
  public void validate_failsWithoutVelocity() throws Exception {
    failure.expect(ValueException.class);
    failure.expectMessage("Velocity is required");

    new ProgramSequencePatternEvent()
      .setProgramId(UUID.randomUUID())
      .setProgramSequencePatternId(UUID.randomUUID())
      .setProgramVoiceTrackId(UUID.randomUUID())
      .setPosition(0.75)
      .setDuration(3.45)
      .setNote("D6")
      .validate();
  }

  /**
   [#175602029] ProgramSequencePatternEvent Position persists exact floating point
   <p>
   DEPRECATED: [#154976066] Architect wants to limit the floating point precision of chord and event position, in order to limit obsession over the position of things.
   */
  @Test
  public void exactPosition() {
    assertEquals(1.25179957, new ProgramSequencePatternEvent().setPosition(1.25179957).getPosition(), 0.0000001);
  }

  /**
   [#175602029] ProgramSequencePatternEvent Duration persists exact floating point
   <p>
   DEPRECATED: [#154976066] Architect wants to limit the floating point precision of chord and event position, in order to limit obsession over the position of things.
   */
  @Test
  public void exactDuration() {
    assertEquals(1.25179957, new ProgramSequencePatternEvent().setDuration(1.25179957).getDuration(), 0.0000001);
  }

}
