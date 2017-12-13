// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.morph;

import io.xj.core.exception.BusinessException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;

import static org.junit.Assert.assertNull;

public class MorphTest {

  @Rule public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    new Morph()
      .setArrangementId(BigInteger.valueOf(987))
      .setPosition(3.5)
      .setDuration(1.5)
      .setNote("G5")
      .validate();
  }

  @Test
  public void validate_failsWithoutArrangementID() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Arrangement ID is required");

    new Morph()
      .setPosition(3.5)
      .setDuration(1.5)
      .setNote("G5")
      .validate();
  }

  @Test
  public void validate_failsWithoutPosition() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Position is required");

    new Morph()
      .setArrangementId(BigInteger.valueOf(987))
      .setDuration(1.5)
      .setNote("G5")
      .validate();
  }

  @Test
  public void validate_failsWithoutDuration() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Duration is required");

    new Morph()
      .setArrangementId(BigInteger.valueOf(987))
      .setPosition(3.5)
      .setNote("G5")
      .validate();
  }

  @Test
  public void validate_failsWithoutNote() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Note is required");

    new Morph()
      .setArrangementId(BigInteger.valueOf(987))
      .setPosition(3.5)
      .setDuration(1.5)
      .validate();
  }

  @Test
  public void setFromRecord_nullPassesThrough() throws Exception {
    assertNull(new Morph().setFromRecord(null));
  }

}
