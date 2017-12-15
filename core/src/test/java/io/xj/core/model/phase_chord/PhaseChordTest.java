// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.phase_chord;

import io.xj.core.exception.BusinessException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;

public class PhaseChordTest {

  @Rule public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    new PhaseChord()
      .setName("C# minor")
      .setPhaseId(BigInteger.valueOf(1235))
      .setPosition(0.75)
      .validate();
  }

  @Test
  public void validate_failsWithoutName() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Name is required");

    new PhaseChord()
      .setPhaseId(BigInteger.valueOf(1235))
      .setPosition(0.75)
      .validate();
  }

  @Test
  public void validate_failsWithoutPhaseID() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Phase ID is required");

    new PhaseChord()
      .setName("C# minor")
      .setPosition(0.75)
      .validate();
  }

  @Test
  public void validate_failsWithoutPosition() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Position is required");

    new PhaseChord()
      .setName("C# minor")
      .setPhaseId(BigInteger.valueOf(1235))
      .validate();
  }

}
