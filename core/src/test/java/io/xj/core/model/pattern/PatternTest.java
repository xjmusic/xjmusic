// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.pattern;

import io.xj.core.exception.BusinessException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;

public class PatternTest {

  @Rule public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    new Pattern()
      .setSequenceId(BigInteger.valueOf(9812L))
      .setName("Mic Check One Two")
      .setKey("D major")
      .setTypeEnum(PatternType.Macro)
      .setTotal(64)
      .setOffset(BigInteger.valueOf(14L))
      .setDensity(0.6)
      .setTempo(140.5)
      .validate();
  }

  @Test
  public void validate_withMinimalAttributes() throws Exception {
    new Pattern()
      .setSequenceId(BigInteger.valueOf(9812L))
      .setOffset(BigInteger.valueOf(14L))
      .setTypeEnum(PatternType.Macro)
      .validate();
  }

  @Test
  public void validate_failsWithoutSequenceID() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Sequence ID is required");

    new Pattern()
      .setOffset(BigInteger.valueOf(14L))
      .setTypeEnum(PatternType.Macro)
      .validate();
  }

  @Test
  public void validate_failsWithoutType() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Type is required");

    new Pattern()
      .setSequenceId(BigInteger.valueOf(9812L))
      .setOffset(BigInteger.valueOf(14L))
      .validate();
  }

  @Test
  public void validate_failsWithoutOffset() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Offset is required");

    new Pattern()
      .setSequenceId(BigInteger.valueOf(9812L))
      .setTypeEnum(PatternType.Macro)
      .validate();
  }


}
