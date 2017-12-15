// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.phase;

import io.xj.core.exception.BusinessException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;

public class PhaseTest {

  @Rule public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    new Phase()
      .setPatternId(BigInteger.valueOf(9812))
      .setName("Mic Check One Two")
      .setKey("D major")
      .setTotal(64)
      .setOffset(BigInteger.valueOf(14))
      .setDensity(0.6)
      .setTempo(140.5)
      .validate();
  }

  @Test
  public void validate_withMinimalAttributes() throws Exception {
    new Phase()
      .setPatternId(BigInteger.valueOf(9812))
      .setOffset(BigInteger.valueOf(14))
      .validate();
  }

  @Test
  public void validate_failsWithoutPatternID() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Pattern ID is required");

    new Phase()
      .setOffset(BigInteger.valueOf(14))
      .validate();
  }

  @Test
  public void validate_failsWithoutOffset() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Offset is required");

    new Phase()
      .setPatternId(BigInteger.valueOf(9812))
      .validate();
  }


}
