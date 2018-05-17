// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.pattern_event;

import io.xj.core.exception.BusinessException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;

import static org.junit.Assert.assertEquals;

public class PatternEventTest {

  @Rule public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    new PatternEvent()
      .setVoiceId(BigInteger.valueOf(1235L))
      .setPatternId(BigInteger.valueOf(1235L))
      .setPosition(0.75)
      .setDuration(3.45)
      .setInflection("SMACK")
      .setNote("D6")
      .setTonality(0.6)
      .setVelocity(0.9)
      .validate();
  }

  @Test
  public void validate_failsWithoutDuration() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Duration is required");

    new PatternEvent()
      .setVoiceId(BigInteger.valueOf(1235L))
      .setPatternId(BigInteger.valueOf(1235L))
      .setPosition(0.75)
      .setInflection("SMACK")
      .setNote("D6")
      .setTonality(0.6)
      .setVelocity(0.9)
      .validate();
  }

  @Test
  public void validate_failsWithoutVoiceID() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Voice ID is required");

    new PatternEvent()
      .setPatternId(BigInteger.valueOf(1235L))
      .setPosition(0.75)
      .setDuration(3.45)
      .setInflection("SMACK")
      .setNote("D6")
      .setTonality(0.6)
      .setVelocity(0.9)
      .validate();
  }

  @Test
  public void validate_failsWithoutPatternID() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Pattern ID is required");

    new PatternEvent()
      .setVoiceId(BigInteger.valueOf(1235L))
      .setPosition(0.75)
      .setDuration(3.45)
      .setInflection("SMACK")
      .setNote("D6")
      .setTonality(0.6)
      .setVelocity(0.9)
      .validate();
  }

  @Test
  public void validate_failsWithoutPosition() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Position is required");

    new PatternEvent()
      .setPatternId(BigInteger.valueOf(1235L))
      .setVoiceId(BigInteger.valueOf(1235L))
      .setDuration(3.45)
      .setInflection("SMACK")
      .setNote("D6")
      .setTonality(0.6)
      .setVelocity(0.9)
      .validate();
  }

  @Test
  public void validate_failsWithoutInflection() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Inflection is required");

    new PatternEvent()
      .setPatternId(BigInteger.valueOf(1235L))
      .setVoiceId(BigInteger.valueOf(1235L))
      .setPosition(0.75)
      .setDuration(3.45)
      .setNote("D6")
      .setTonality(0.6)
      .setVelocity(0.9)
      .validate();
  }

  @Test
  public void validate_failsWithoutNote() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Note is required");

    new PatternEvent()
      .setPatternId(BigInteger.valueOf(1235L))
      .setVoiceId(BigInteger.valueOf(1235L))
      .setPosition(0.75)
      .setDuration(3.45)
      .setInflection("SMACK")
      .setTonality(0.6)
      .setVelocity(0.9)
      .validate();
  }

  @Test
  public void validate_failsWithoutTonality() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Tonality is required");

    new PatternEvent()
      .setPatternId(BigInteger.valueOf(1235L))
      .setVoiceId(BigInteger.valueOf(1235L))
      .setPosition(0.75)
      .setDuration(3.45)
      .setInflection("SMACK")
      .setNote("D6")
      .setVelocity(0.9)
      .validate();
  }

  @Test
  public void validate_failsWithoutVelocity() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Velocity is required");

    new PatternEvent()
      .setPatternId(BigInteger.valueOf(1235L))
      .setVoiceId(BigInteger.valueOf(1235L))
      .setPosition(0.75)
      .setDuration(3.45)
      .setInflection("SMACK")
      .setNote("D6")
      .setTonality(0.6)
      .validate();
  }

  /**
   [#154976066] Architect wants to limit the floating point precision of chord and event position, in order to limit obsession over the position of things.
   */
  @Test
  public void position_rounded() throws Exception {
    assertEquals(1.25, new PatternEvent().setPosition(1.25179957).getPosition(), 0.0000001);
  }

}
