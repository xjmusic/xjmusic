// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.phase_chord;

import io.xj.core.exception.BusinessException;

import io.xj.core.model.phase_event.PhaseEvent;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;

import static org.junit.Assert.assertEquals;

public class PhaseChordTest {

  @Rule public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    new PhaseChord()
      .setName("C# minor")
      .setPhaseId(BigInteger.valueOf(1235))
      .setPosition(7.0)
      .validate();
  }

  @Test
  public void validate_failsWithoutName() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Name is required");

    new PhaseChord()
      .setPhaseId(BigInteger.valueOf(1235))
      .setPosition(7.0)
      .validate();
  }

  @Test
  public void validate_failsWithoutPhaseID() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Phase ID is required");

    new PhaseChord()
      .setName("C# minor")
      .setPosition(7.0)
      .validate();
  }

  @Test
  public void validate_failsWithoutPosition() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Position is required");

    new PhaseChord()
      .setName("C# minor")
      .setPhaseId(BigInteger.valueOf(1235))
      .validate();
  }

  /**
   [#154976066] Architect wants to limit the floating point precision of chord and chord position, in order to limit obsession over the position of things.
   */
  @Test
  public void position_rounded() throws Exception {
    assertEquals(1.25, new PhaseChord().setPosition(1.25179957).getPosition(), 0.0000001);
  }

}
