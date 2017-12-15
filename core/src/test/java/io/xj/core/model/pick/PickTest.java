// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.pick;

import io.xj.core.exception.BusinessException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;

public class PickTest {

  @Rule public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    new Pick()
      .setArrangementId(BigInteger.valueOf(1269))
      .setMorphId(BigInteger.valueOf(6945))
      .setAudioId(BigInteger.valueOf(6329))
      .setStart(0.92)
      .setLength(2.7)
      .setAmplitude(0.84)
      .setPitch(42.9)
      .validate();
  }

  @Test
  public void validate_failsWithoutArrangementID() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Arrangement ID is required");

    new Pick()
      .setMorphId(BigInteger.valueOf(6945))
      .setAudioId(BigInteger.valueOf(6329))
      .setStart(0.92)
      .setLength(2.7)
      .setAmplitude(0.84)
      .setPitch(42.9)
      .validate();
  }

  @Test
  public void validate_withoutMorphID() throws Exception {
    new Pick()
      .setArrangementId(BigInteger.valueOf(1269))
      .setAudioId(BigInteger.valueOf(6329))
      .setStart(0.92)
      .setLength(2.7)
      .setAmplitude(0.84)
      .setPitch(42.9)
      .validate();
  }

  @Test
  public void validate_failsWithoutAudioID() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Audio ID is required");

    new Pick()
      .setArrangementId(BigInteger.valueOf(1269))
      .setMorphId(BigInteger.valueOf(6945))
      .setStart(0.92)
      .setLength(2.7)
      .setAmplitude(0.84)
      .setPitch(42.9)
      .validate();
  }

  @Test
  public void validate_failsWithoutStart() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Start is required");

    new Pick()
      .setArrangementId(BigInteger.valueOf(1269))
      .setMorphId(BigInteger.valueOf(6945))
      .setAudioId(BigInteger.valueOf(6329))
      .setLength(2.7)
      .setAmplitude(0.84)
      .setPitch(42.9)
      .validate();
  }

  @Test
  public void validate_failsWithoutLength() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Length is required");

    new Pick()
      .setArrangementId(BigInteger.valueOf(1269))
      .setMorphId(BigInteger.valueOf(6945))
      .setAudioId(BigInteger.valueOf(6329))
      .setStart(0.92)
      .setAmplitude(0.84)
      .setPitch(42.9)
      .validate();
  }

  @Test
  public void validate_failsWithoutAmplitude() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Amplitude is required");

    new Pick()
      .setArrangementId(BigInteger.valueOf(1269))
      .setMorphId(BigInteger.valueOf(6945))
      .setAudioId(BigInteger.valueOf(6329))
      .setStart(0.92)
      .setLength(2.7)
      .setPitch(42.9)
      .validate();
  }

  @Test
  public void validate_failsWithoutPitch() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Pitch is required");

    new Pick()
      .setArrangementId(BigInteger.valueOf(1269))
      .setMorphId(BigInteger.valueOf(6945))
      .setAudioId(BigInteger.valueOf(6329))
      .setStart(0.92)
      .setLength(2.7)
      .setAmplitude(0.84)
      .validate();
  }

}
