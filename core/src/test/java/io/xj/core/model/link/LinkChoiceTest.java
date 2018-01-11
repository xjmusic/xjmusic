// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.link;

import io.xj.core.model.choice.Choice;

import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LinkChoiceTest {

  @Test
  public void nextPhaseOffset() throws Exception {
    Choice linkChoice = new Choice()
      .setPatternId(BigInteger.valueOf(345))
      .setPhaseOffset(BigInteger.valueOf(0))
      .setTranspose(5)
      .setType("Main")
      .setAvailablePhaseOffsets("0,1");

    assertEquals(BigInteger.valueOf(1), linkChoice.nextPhaseOffset());
  }

  @Test
  public void nextPhaseOffset_endLoopsBackToZero() throws Exception {
    Choice linkChoice = new Choice()
      .setPatternId(BigInteger.valueOf(345))
      .setPhaseOffset(BigInteger.valueOf(3))
      .setTranspose(5)
      .setType("Main")
      .setAvailablePhaseOffsets("0,1,2,3");

    assertEquals(BigInteger.valueOf(0), linkChoice.nextPhaseOffset());
  }

  @Test
  public void nextPhaseOffset_weirdIsOkay() throws Exception {
    Choice linkChoice = new Choice()
      .setPatternId(BigInteger.valueOf(345))
      .setPhaseOffset(BigInteger.valueOf(17))
      .setTranspose(5)
      .setType("Main")
      .setAvailablePhaseOffsets("0,1,17,204,1407");

    assertEquals(BigInteger.valueOf(204), linkChoice.nextPhaseOffset());
  }

  @Test
  public void hasOneMorePhase() throws Exception {
    Choice linkChoice = new Choice()
      .setPatternId(BigInteger.valueOf(345))
      .setPhaseOffset(BigInteger.valueOf(0))
      .setTranspose(5)
      .setType("Main")
      .setAvailablePhaseOffsets("0,1");

    assertTrue(linkChoice.hasOneMorePhase());
  }

  @Test
  public void hasOneMorePhase_true() throws Exception {
    Choice linkChoice = new Choice()
      .setPatternId(BigInteger.valueOf(345))
      .setPhaseOffset(BigInteger.valueOf(2))
      .setTranspose(5)
      .setType("Main")
      .setAvailablePhaseOffsets("0,1,2,3");

    assertTrue(linkChoice.hasOneMorePhase());
  }

  @Test
  public void hasOneMorePhase_false() throws Exception {
    Choice linkChoice = new Choice()
      .setPatternId(BigInteger.valueOf(21))
      .setPhaseOffset(BigInteger.valueOf(3))
      .setTranspose(5)
      .setType("Main")
      .setAvailablePhaseOffsets("0,1,2,3");

    assertFalse(linkChoice.hasOneMorePhase());
  }

  @Test
  public void hasTwoMorePhases() throws Exception {
    Choice linkChoice = new Choice()
      .setPatternId(BigInteger.valueOf(64))
      .setPhaseOffset(BigInteger.valueOf(0))
      .setTranspose(5)
      .setType("Macro")
      .setAvailablePhaseOffsets("0,1,2");

    assertTrue(linkChoice.hasTwoMorePhases());
  }

  @Test
  public void hasTwoMorePhases_true() throws Exception {
    Choice linkChoice = new Choice()
      .setPatternId(BigInteger.valueOf(64))
      .setPhaseOffset(BigInteger.valueOf(1))
      .setTranspose(5)
      .setType("Macro")
      .setAvailablePhaseOffsets("0,1,2,3");

    assertTrue(linkChoice.hasTwoMorePhases());
  }

  @Test
  public void hasTwoMorePhases_false() throws Exception {
    Choice linkChoice = new Choice()
      .setPatternId(BigInteger.valueOf(64))
      .setPhaseOffset(BigInteger.valueOf(2))
      .setTranspose(5)
      .setType("Macro")
      .setAvailablePhaseOffsets("0,1,2,3");

    assertFalse(linkChoice.hasTwoMorePhases());
  }

}
