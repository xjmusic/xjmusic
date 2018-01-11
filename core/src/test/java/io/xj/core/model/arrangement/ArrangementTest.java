// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.arrangement;

import io.xj.core.exception.BusinessException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;

public class ArrangementTest {

  @Rule public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    new Arrangement()
      .setVoiceId(BigInteger.valueOf(354))
      .setChoiceId(BigInteger.valueOf(879))
      .setInstrumentId(BigInteger.valueOf(432))
      .validate();
  }

  @Test
  public void validate_failsWithoutVoiceID() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Voice ID is required");

    new Arrangement()
      .setChoiceId(BigInteger.valueOf(879))
      .setInstrumentId(BigInteger.valueOf(432))
      .validate();
  }

  @Test
  public void validate_failsWithoutChoiceID() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Choice ID is required");

    new Arrangement()
      .setVoiceId(BigInteger.valueOf(354))
      .setInstrumentId(BigInteger.valueOf(432))
      .validate();
  }

  @Test
  public void validate_failsWithoutInstrumentID() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Instrument ID is required");

    new Arrangement()
      .setVoiceId(BigInteger.valueOf(354))
      .setChoiceId(BigInteger.valueOf(879))
      .validate();
  }

}
