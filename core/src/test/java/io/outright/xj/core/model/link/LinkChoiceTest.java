// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.core.model.link;// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.

import io.outright.xj.core.model.choice.Choice;

import org.jooq.types.ULong;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LinkChoiceTest {

  @Test
  public void nextPhaseOffset() throws Exception {
    Choice linkChoice = new Choice()
      .setIdeaId(ULong.valueOf(345).toBigInteger())
      .setPhaseOffset(ULong.valueOf(0).toBigInteger())
      .setTranspose(5)
      .setType(Choice.MAIN)
    .setAvailablePhaseOffsets("0,1");

    assertEquals(ULong.valueOf(1), linkChoice.nextPhaseOffset());
  }

  @Test
  public void nextPhaseOffset_endLoopsBackToZero() throws Exception {
    Choice linkChoice = new Choice()
      .setIdeaId(ULong.valueOf(345).toBigInteger())
      .setPhaseOffset(ULong.valueOf(3).toBigInteger())
      .setTranspose(5)
      .setType(Choice.MAIN)
    .setAvailablePhaseOffsets("0,1,2,3");

    assertEquals(ULong.valueOf(0), linkChoice.nextPhaseOffset());
  }

  @Test
  public void nextPhaseOffset_weirdIsOkay() throws Exception {
    Choice linkChoice = new Choice()
      .setIdeaId(ULong.valueOf(345).toBigInteger())
      .setPhaseOffset(ULong.valueOf(17).toBigInteger())
      .setTranspose(5)
      .setType(Choice.MAIN)
    .setAvailablePhaseOffsets("0,1,17,204,1407");

    assertEquals(ULong.valueOf(204), linkChoice.nextPhaseOffset());
  }

  @Test
  public void hasOneMorePhase() throws Exception {
    Choice linkChoice = new Choice()
      .setIdeaId(ULong.valueOf(345).toBigInteger())
      .setPhaseOffset(ULong.valueOf(0).toBigInteger())
      .setTranspose(5)
      .setType(Choice.MAIN)
    .setAvailablePhaseOffsets("0,1");

    assertTrue(linkChoice.hasOneMorePhase());
  }

  @Test
  public void hasOneMorePhase_true() throws Exception {
    Choice linkChoice = new Choice()
      .setIdeaId(ULong.valueOf(345).toBigInteger())
      .setPhaseOffset(ULong.valueOf(2).toBigInteger())
      .setTranspose(5)
      .setType(Choice.MAIN)
    .setAvailablePhaseOffsets("0,1,2,3");

    assertTrue(linkChoice.hasOneMorePhase());
  }

  @Test
  public void hasOneMorePhase_false() throws Exception {
    Choice linkChoice = new Choice()
      .setIdeaId(ULong.valueOf(21).toBigInteger())
      .setPhaseOffset(ULong.valueOf(3).toBigInteger())
      .setTranspose(5)
      .setType(Choice.MAIN)
    .setAvailablePhaseOffsets("0,1,2,3");

    assertFalse(linkChoice.hasOneMorePhase());
  }

  @Test
  public void hasTwoMorePhases() throws Exception {
    Choice linkChoice = new Choice()
      .setIdeaId(ULong.valueOf(64).toBigInteger())
      .setPhaseOffset(ULong.valueOf(0).toBigInteger())
      .setTranspose(5)
      .setType(Choice.MACRO)
    .setAvailablePhaseOffsets("0,1,2");

    assertTrue(linkChoice.hasTwoMorePhases());
  }

  @Test
  public void hasTwoMorePhases_true() throws Exception {
    Choice linkChoice = new Choice()
      .setIdeaId(ULong.valueOf(64).toBigInteger())
      .setPhaseOffset(ULong.valueOf(1).toBigInteger())
      .setTranspose(5)
      .setType(Choice.MACRO)
    .setAvailablePhaseOffsets("0,1,2,3");

    assertTrue(linkChoice.hasTwoMorePhases());
  }

  @Test
  public void hasTwoMorePhases_false() throws Exception {
    Choice linkChoice = new Choice()
      .setIdeaId(ULong.valueOf(64).toBigInteger())
      .setPhaseOffset(ULong.valueOf(2).toBigInteger())
      .setTranspose(5)
      .setType(Choice.MACRO)
    .setAvailablePhaseOffsets("0,1,2,3");

    assertFalse(linkChoice.hasTwoMorePhases());
  }

}
