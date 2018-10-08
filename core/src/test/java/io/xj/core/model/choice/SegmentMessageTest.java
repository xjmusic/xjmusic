// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.choice;

import io.xj.core.exception.BusinessException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;

import static org.junit.Assert.assertEquals;

public class SegmentMessageTest {

  @Rule public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    new Choice()
      .setSegmentId(BigInteger.valueOf(352L))
      .setSequenceId(BigInteger.valueOf(125L))
      .setTranspose(5)
      .setType("Macro")
      .setSequencePatternOffset(BigInteger.valueOf(4L))
      .validate();
  }

  @Test
  public void validate_failsWithoutSegmentID() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Segment ID is required");

    new Choice()
      .setSequenceId(BigInteger.valueOf(125L))
      .setTranspose(5)
      .setType("Macro")
      .setSequencePatternOffset(BigInteger.valueOf(4L))
      .validate();
  }

  @Test
  public void validate_failsWithoutSequenceID() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Sequence ID is required");

    new Choice()
      .setSegmentId(BigInteger.valueOf(352L))
      .setTranspose(5)
      .setType("Macro")
      .setSequencePatternOffset(BigInteger.valueOf(4L))
      .validate();
  }

  @Test
  public void validate_transposeZeroByDefault() throws Exception {
    Choice result = new Choice()
      .setSegmentId(BigInteger.valueOf(352L))
      .setSequenceId(BigInteger.valueOf(125L))
      .setType("Macro")
      .setSequencePatternOffset(BigInteger.valueOf(4L));

    result.validate();

    assertEquals(Integer.valueOf(0), result.getTranspose());
  }

  @Test
  public void validate_failsWithoutType() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Type is required");

    new Choice()
      .setSegmentId(BigInteger.valueOf(352L))
      .setSequenceId(BigInteger.valueOf(125L))
      .setTranspose(5)
      .setSequencePatternOffset(BigInteger.valueOf(4L))
      .validate();
  }

  @Test
  public void validate_failsWithInvalidType() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("'bung' is not a valid type");

    new Choice()
      .setSegmentId(BigInteger.valueOf(352L))
      .setSequenceId(BigInteger.valueOf(125L))
      .setTranspose(5)
      .setType("bung")
      .setSequencePatternOffset(BigInteger.valueOf(4L))
      .validate();
  }

  @Test
  public void validate_failsWithoutPatternOffset() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Pattern Offset is required");

    new Choice()
      .setSegmentId(BigInteger.valueOf(352L))
      .setSequenceId(BigInteger.valueOf(125L))
      .setTranspose(5)
      .setType("Macro")
      .validate();
  }

}
