// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.pattern_chord;

import io.xj.core.exception.BusinessException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PatternChordTest {

  @Rule public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    new PatternChord()
      .setName("C# minor")
      .setPatternId(BigInteger.valueOf(1235L))
      .setPosition(7.0)
      .validate();
  }

  @Test
  public void validate_failsWithoutName() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Name is required");

    new PatternChord()
      .setPatternId(BigInteger.valueOf(1235L))
      .setPosition(7.0)
      .validate();
  }

  @Test
  public void validate_failsWithoutPatternID() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Pattern ID is required");

    new PatternChord()
      .setName("C# minor")
      .setPosition(7.0)
      .validate();
  }

  @Test
  public void validate_failsWithoutPosition() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Position is required");

    new PatternChord()
      .setName("C# minor")
      .setPatternId(BigInteger.valueOf(1235L))
      .validate();
  }

  /**
   [#154976066] Architect wants to limit the floating point precision of chord and chord position, in order to limit obsession over the position of things.
   */
  @Test
  public void position_rounded() throws Exception {
    assertEquals(1.25, new PatternChord().setPosition(1.25179957).getPosition(), 0.0000001);
  }

  @Test
  public void isNoChord() {
    assertFalse(new PatternChord(BigInteger.valueOf(1), 0.0, "C#m7").isNoChord());
    assertTrue(new PatternChord(BigInteger.valueOf(1), 0.0, "NC").isNoChord());
  }

}
