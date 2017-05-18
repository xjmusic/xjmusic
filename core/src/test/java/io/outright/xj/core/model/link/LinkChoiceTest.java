// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.core.model.link;// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.

import io.outright.xj.core.model.choice.Choice;

import org.jooq.types.ULong;

import com.google.common.collect.ImmutableList;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LinkChoiceTest {

  @Test
  public void nextPhaseOffset() throws Exception {
    LinkChoice linkChoice = new LinkChoice(
      ULong.valueOf(345),
      ULong.valueOf(0),
      5,
      Choice.MAIN, ImmutableList.of(
      ULong.valueOf(0),
      ULong.valueOf(1)
    ));

    assertEquals(ULong.valueOf(1), linkChoice.nextPhaseOffset());
  }

  @Test
  public void nextPhaseOffset_endLoopsBackToZero() throws Exception {
    LinkChoice linkChoice = new LinkChoice(
      ULong.valueOf(345),
      ULong.valueOf(3),
      5,
      Choice.MAIN, ImmutableList.of(
      ULong.valueOf(0),
      ULong.valueOf(1),
      ULong.valueOf(2),
      ULong.valueOf(3)
    ));

    assertEquals(ULong.valueOf(0), linkChoice.nextPhaseOffset());
  }

  @Test
  public void nextPhaseOffset_weirdIsOkay() throws Exception {
    LinkChoice linkChoice = new LinkChoice(
      ULong.valueOf(345),
      ULong.valueOf(17),
      5,
      Choice.MAIN, ImmutableList.of(
      ULong.valueOf(0),
      ULong.valueOf(1),
      ULong.valueOf(17),
      ULong.valueOf(204),
      ULong.valueOf(1407)
    ));

    assertEquals(ULong.valueOf(204), linkChoice.nextPhaseOffset());
  }

  @Test
  public void hasOneMorePhase() throws Exception {
    LinkChoice linkChoice = new LinkChoice(
      ULong.valueOf(345),
      ULong.valueOf(0),
      5,
      Choice.MAIN, ImmutableList.of(
      ULong.valueOf(0),
      ULong.valueOf(1)
    ));

    assertTrue(linkChoice.hasOneMorePhase());
  }

  @Test
  public void hasOneMorePhase_true() throws Exception {
    LinkChoice linkChoice = new LinkChoice(
      ULong.valueOf(345),
      ULong.valueOf(2),
      5,
      Choice.MAIN, ImmutableList.of(
      ULong.valueOf(0),
      ULong.valueOf(1),
      ULong.valueOf(2),
      ULong.valueOf(3)
    ));

    assertTrue(linkChoice.hasOneMorePhase());
  }

  @Test
  public void hasOneMorePhase_false() throws Exception {
    LinkChoice linkChoice = new LinkChoice(
      ULong.valueOf(21),
      ULong.valueOf(3),
      5,
      Choice.MAIN, ImmutableList.of(
      ULong.valueOf(0),
      ULong.valueOf(1),
      ULong.valueOf(2),
      ULong.valueOf(3)
    ));

    assertFalse(linkChoice.hasOneMorePhase());
  }

  @Test
  public void hasTwoMorePhases() throws Exception {
    LinkChoice linkChoice = new LinkChoice(
      ULong.valueOf(64),
      ULong.valueOf(0),
      5,
      Choice.MACRO, ImmutableList.of(
      ULong.valueOf(0),
      ULong.valueOf(1),
      ULong.valueOf(2)
    ));

    assertTrue(linkChoice.hasTwoMorePhases());
  }

  @Test
  public void hasTwoMorePhases_true() throws Exception {
    LinkChoice linkChoice = new LinkChoice(
      ULong.valueOf(64),
      ULong.valueOf(1),
      5,
      Choice.MACRO, ImmutableList.of(
      ULong.valueOf(0),
      ULong.valueOf(1),
      ULong.valueOf(2),
      ULong.valueOf(3)
    ));

    assertTrue(linkChoice.hasTwoMorePhases());
  }

  @Test
  public void hasTwoMorePhases_false() throws Exception {
    LinkChoice linkChoice = new LinkChoice(
      ULong.valueOf(64),
      ULong.valueOf(2),
      5,
      Choice.MACRO, ImmutableList.of(
      ULong.valueOf(0),
      ULong.valueOf(1),
      ULong.valueOf(2),
      ULong.valueOf(3)
    ));

    assertFalse(linkChoice.hasTwoMorePhases());
  }

}
