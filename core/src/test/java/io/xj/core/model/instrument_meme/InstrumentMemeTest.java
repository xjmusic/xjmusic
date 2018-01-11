// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.instrument_meme;

import io.xj.core.exception.BusinessException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;

public class InstrumentMemeTest {

  @Rule public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    new InstrumentMeme()
      .setInstrumentId(BigInteger.valueOf(23678))
      .setName("Miccheckonetwo")
      .validate();
  }

  @Test
  public void validate_failsWithoutInstrumentID() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Instrument ID is required");

    new InstrumentMeme()
      .setName("Miccheckonetwo")
      .validate();
  }

  @Test
  public void validate_failsWithoutName() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Name is required");

    new InstrumentMeme()
      .setInstrumentId(BigInteger.valueOf(23678))
      .validate();
  }

}
