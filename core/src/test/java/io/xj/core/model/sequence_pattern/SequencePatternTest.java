// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.sequence_pattern;

import io.xj.core.exception.BusinessException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;

import static org.junit.Assert.assertNotNull;

public class SequencePatternTest {

  @Rule
  public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    new SequencePattern()
      .setSequenceId(BigInteger.valueOf(9812L))
      .setPatternId(BigInteger.valueOf(357L))
      .setOffset(BigInteger.valueOf(14L))
      .validate();
  }

  @Test
  public void validate_failsWithoutSequenceID() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Sequence ID is required");

    new SequencePattern()
      .setPatternId(BigInteger.valueOf(357L))
      .setOffset(BigInteger.valueOf(14L))
      .validate();
  }

  @Test
  public void validate_failsWithoutPatternID() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Pattern ID is required");

    new SequencePattern()
      .setSequenceId(BigInteger.valueOf(9812L))
      .setOffset(BigInteger.valueOf(14L))
      .validate();
  }

  @Test
  public void validate_failsWithoutOffset() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Offset is required");

    new SequencePattern()
      .setSequenceId(BigInteger.valueOf(9812L))
      .setPatternId(BigInteger.valueOf(357L))
      .validate();
  }


}
