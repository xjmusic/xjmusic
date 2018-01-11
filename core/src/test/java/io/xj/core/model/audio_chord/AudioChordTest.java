// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.audio_chord;

import io.xj.core.exception.BusinessException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;

public class AudioChordTest {

  @Rule public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    new AudioChord()
      .setName("C# minor")
      .setAudioId(BigInteger.valueOf(1235))
      .setPosition(7)
      .validate();
  }

  @Test
  public void validate_failsWithoutName() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Name is required");

    new AudioChord()
      .setAudioId(BigInteger.valueOf(1235))
      .setPosition(7)
      .validate();
  }

  @Test
  public void validate_failsWithoutAudioID() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Audio ID is required");

    new AudioChord()
      .setName("C# minor")
      .setPosition(7)
      .validate();
  }

  @Test
  public void validate_failsWithoutPosition() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Position is required");

    new AudioChord()
      .setName("C# minor")
      .setAudioId(BigInteger.valueOf(1235))
      .validate();
  }


}
