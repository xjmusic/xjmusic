// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.choice;

import io.xj.core.exception.BusinessException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;

import static org.junit.Assert.assertEquals;

public class LinkMessageTest {

  @Rule public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    new Choice()
      .setLinkId(BigInteger.valueOf(352))
      .setPatternId(BigInteger.valueOf(125))
      .setTranspose(5)
      .setType("Macro")
      .setPhaseOffset(BigInteger.valueOf(4))
      .validate();
  }

  @Test
  public void validate_failsWithoutLinkID() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Link ID is required");

    new Choice()
      .setPatternId(BigInteger.valueOf(125))
      .setTranspose(5)
      .setType("Macro")
      .setPhaseOffset(BigInteger.valueOf(4))
      .validate();
  }

  @Test
  public void validate_failsWithoutPatternID() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Pattern ID is required");

    new Choice()
      .setLinkId(BigInteger.valueOf(352))
      .setTranspose(5)
      .setType("Macro")
      .setPhaseOffset(BigInteger.valueOf(4))
      .validate();
  }

  @Test
  public void validate_transposeZeroByDefault() throws Exception {
    Choice result = new Choice()
      .setLinkId(BigInteger.valueOf(352))
      .setPatternId(BigInteger.valueOf(125))
      .setType("Macro")
      .setPhaseOffset(BigInteger.valueOf(4));

    result.validate();

    assertEquals(Integer.valueOf(0), result.getTranspose());
  }

  @Test
  public void validate_failsWithoutType() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Type is required");

    new Choice()
      .setLinkId(BigInteger.valueOf(352))
      .setPatternId(BigInteger.valueOf(125))
      .setTranspose(5)
      .setPhaseOffset(BigInteger.valueOf(4))
      .validate();
  }

  @Test
  public void validate_failsWithInvalidType() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("'bung' is not a valid type");

    new Choice()
      .setLinkId(BigInteger.valueOf(352))
      .setPatternId(BigInteger.valueOf(125))
      .setTranspose(5)
      .setType("bung")
      .setPhaseOffset(BigInteger.valueOf(4))
      .validate();
  }

  @Test
  public void validate_failsWithoutPhaseOffset() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Phase Offset is required");

    new Choice()
      .setLinkId(BigInteger.valueOf(352))
      .setPatternId(BigInteger.valueOf(125))
      .setTranspose(5)
      .setType("Macro")
      .validate();
  }

}
