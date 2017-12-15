// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.voice_event;

import io.xj.core.exception.BusinessException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;

public class VoiceEventTest {

  @Rule public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    new VoiceEvent()
      .setVoiceId(BigInteger.valueOf(1235))
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

    new VoiceEvent()
      .setVoiceId(BigInteger.valueOf(1235))
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

    new VoiceEvent()
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

    new VoiceEvent()
      .setVoiceId(BigInteger.valueOf(1235))
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

    new VoiceEvent()
      .setVoiceId(BigInteger.valueOf(1235))
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

    new VoiceEvent()
      .setVoiceId(BigInteger.valueOf(1235))
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

    new VoiceEvent()
      .setVoiceId(BigInteger.valueOf(1235))
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

    new VoiceEvent()
      .setVoiceId(BigInteger.valueOf(1235))
      .setPosition(0.75)
      .setDuration(3.45)
      .setInflection("SMACK")
      .setNote("D6")
      .setTonality(0.6)
      .validate();
  }


}
