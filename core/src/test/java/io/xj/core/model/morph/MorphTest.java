// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.morph;

import io.xj.core.exception.CoreException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;

public class MorphTest {

  @Rule
  public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    new Morph()
      .setArrangementId(BigInteger.valueOf(987L))
      .setPosition(3.5)
      .setDuration(1.5)
      .setNote("G5")
      .validate();
  }

  @Test
  public void validate_failsWithoutArrangementID() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Arrangement ID is required");

    new Morph()
      .setPosition(3.5)
      .setDuration(1.5)
      .setNote("G5")
      .validate();
  }

  @Test
  public void validate_failsWithoutPosition() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Position is required");

    new Morph()
      .setArrangementId(BigInteger.valueOf(987L))
      .setDuration(1.5)
      .setNote("G5")
      .validate();
  }

  @Test
  public void validate_failsWithoutDuration() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Duration is required");

    new Morph()
      .setArrangementId(BigInteger.valueOf(987L))
      .setPosition(3.5)
      .setNote("G5")
      .validate();
  }

  @Test
  public void validate_failsWithoutNote() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Note is required");

    new Morph()
      .setArrangementId(BigInteger.valueOf(987L))
      .setPosition(3.5)
      .setDuration(1.5)
      .validate();
  }


}
