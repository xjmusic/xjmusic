// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.segment;

import io.xj.core.model.choice.Choice;

import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SegmentChoiceTest {

  @Test
  public void nextPatternOffset() {
    Choice segmentChoice = new Choice()
      .setSequenceId(BigInteger.valueOf(345L))
      .setSequencePatternOffset(BigInteger.valueOf(0L))
      .setTranspose(5)
      .setType("Main")
      .setAvailablePatternOffsets("0,1");

    assertEquals(BigInteger.valueOf(1L), segmentChoice.nextPatternOffset());
  }

  @Test
  public void nullPatternOffset() {
    Choice segmentChoice = new Choice()
      .setSequenceId(BigInteger.valueOf(345L))
      .setSequencePatternOffset(BigInteger.valueOf(0L))
      .setTranspose(5)
      .setType("Main")
      .setAvailablePatternOffsets(null);

    assertEquals(BigInteger.valueOf(0L), segmentChoice.nextPatternOffset());
  }

  @Test
  public void nextPatternOffset_endLoopsBackToZero() {
    Choice segmentChoice = new Choice()
      .setSequenceId(BigInteger.valueOf(345L))
      .setSequencePatternOffset(BigInteger.valueOf(3L))
      .setTranspose(5)
      .setType("Main")
      .setAvailablePatternOffsets("0,1,2,3");

    assertEquals(BigInteger.valueOf(0L), segmentChoice.nextPatternOffset());
  }

  @Test
  public void nextPatternOffset_weirdIsOkay() {
    Choice segmentChoice = new Choice()
      .setSequenceId(BigInteger.valueOf(345L))
      .setSequencePatternOffset(BigInteger.valueOf(17L))
      .setTranspose(5)
      .setType("Main")
      .setAvailablePatternOffsets("0,1,17,204,1407");

    assertEquals(BigInteger.valueOf(204L), segmentChoice.nextPatternOffset());
  }

  @Test
  public void hasOneMorePattern() {
    Choice segmentChoice = new Choice()
      .setSequenceId(BigInteger.valueOf(345L))
      .setSequencePatternOffset(BigInteger.valueOf(0L))
      .setTranspose(5)
      .setType("Main")
      .setAvailablePatternOffsets("0,1");

    assertTrue(segmentChoice.hasOneMorePattern());
  }

  @Test
  public void hasOneMorePattern_true() {
    Choice segmentChoice = new Choice()
      .setSequenceId(BigInteger.valueOf(345L))
      .setSequencePatternOffset(BigInteger.valueOf(2L))
      .setTranspose(5)
      .setType("Main")
      .setAvailablePatternOffsets("0,1,2,3");

    assertTrue(segmentChoice.hasOneMorePattern());
  }

  @Test
  public void hasOneMorePattern_false() {
    Choice segmentChoice = new Choice()
      .setSequenceId(BigInteger.valueOf(21L))
      .setSequencePatternOffset(BigInteger.valueOf(3L))
      .setTranspose(5)
      .setType("Main")
      .setAvailablePatternOffsets("0,1,2,3");

    assertFalse(segmentChoice.hasOneMorePattern());
  }

  @Test
  public void hasTwoMorePatterns() {
    Choice segmentChoice = new Choice()
      .setSequenceId(BigInteger.valueOf(64L))
      .setSequencePatternOffset(BigInteger.valueOf(0L))
      .setTranspose(5)
      .setType("Macro")
      .setAvailablePatternOffsets("0,1,2");

    assertTrue(segmentChoice.hasTwoMorePatterns());
  }

  @Test
  public void hasTwoMorePatterns_true() {
    Choice segmentChoice = new Choice()
      .setSequenceId(BigInteger.valueOf(64L))
      .setSequencePatternOffset(BigInteger.valueOf(1L))
      .setTranspose(5)
      .setType("Macro")
      .setAvailablePatternOffsets("0,1,2,3");

    assertTrue(segmentChoice.hasTwoMorePatterns());
  }

  @Test
  public void hasTwoMorePatterns_false() {
    Choice segmentChoice = new Choice()
      .setSequenceId(BigInteger.valueOf(64L))
      .setSequencePatternOffset(BigInteger.valueOf(2L))
      .setTranspose(5)
      .setType("Macro")
      .setAvailablePatternOffsets("0,1,2,3");

    assertFalse(segmentChoice.hasTwoMorePatterns());
  }

}
