// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.phase_meme;

import io.xj.core.exception.BusinessException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;

public class PhaseMemeTest {

  @Rule public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    new PhaseMeme()
      .setPhaseId(BigInteger.valueOf(23678))
      .setName("Miccheckonetwo")
      .validate();
  }

  @Test
  public void validate_failsWithoutPhaseID() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Phase ID is required");

    new PhaseMeme()
      .setName("Miccheckonetwo")
      .validate();
  }

  @Test
  public void validate_failsWithoutName() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Name is required");

    new PhaseMeme()
      .setPhaseId(BigInteger.valueOf(23678))
      .validate();
  }

}
