// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.entity;

import io.xj.lib.util.ValueException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class SegmentChordTest {

  @Rule
  public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    new SegmentChord()
      .setName("C# minor")
      .setSegmentId(UUID.randomUUID())
      .setPosition(7.0)
      .validate();
  }

  @Test
  public void validate_failsWithoutName() throws Exception {
    failure.expect(ValueException.class);
    failure.expectMessage("Name is required");

    new SegmentChord()
      .setSegmentId(UUID.randomUUID())
      .setPosition(7.0)
      .validate();
  }

  @Test
  public void validate_failsWithoutSegmentID() throws Exception {
    failure.expect(ValueException.class);
    failure.expectMessage("Segment ID is required");

    new SegmentChord()
      .setName("C# minor")
      .setPosition(7.0)
      .validate();
  }

  @Test
  public void validate_failsWithoutPosition() throws Exception {
    failure.expect(ValueException.class);
    failure.expectMessage("Position is required");

    new SegmentChord()
      .setName("C# minor")
      .setSegmentId(UUID.randomUUID())
      .validate();
  }

  /**
   [#154976066] Architect wants to limit the floating point precision of chord and event position, in order to limit obsession over the position of things.
   */
  @Test
  public void position_rounded() {
    assertEquals(1.25, new SegmentChord().setPosition(1.25179957).getPosition(), 0.0000001);
  }


}

