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
  public void nextPatternOffset() throws Exception {
    Choice segmentChoice = new Choice()
      .setSequenceId(BigInteger.valueOf(345L))
      .setPatternOffset(BigInteger.valueOf(0L))
      .setTranspose(5)
      .setType("Main")
      .setAvailablePatternOffsets("0,1");

    assertEquals(BigInteger.valueOf(1L), segmentChoice.nextPatternOffset());
  }

  @Test
  public void nextPatternOffset_endLoopsBackToZero() throws Exception {
    Choice segmentChoice = new Choice()
      .setSequenceId(BigInteger.valueOf(345L))
      .setPatternOffset(BigInteger.valueOf(3L))
      .setTranspose(5)
      .setType("Main")
      .setAvailablePatternOffsets("0,1,2,3");

    assertEquals(BigInteger.valueOf(0L), segmentChoice.nextPatternOffset());
  }

  @Test
  public void nextPatternOffset_weirdIsOkay() throws Exception {
    Choice segmentChoice = new Choice()
      .setSequenceId(BigInteger.valueOf(345L))
      .setPatternOffset(BigInteger.valueOf(17L))
      .setTranspose(5)
      .setType("Main")
      .setAvailablePatternOffsets("0,1,17,204,1407");

    assertEquals(BigInteger.valueOf(204L), segmentChoice.nextPatternOffset());
  }

  @Test
  public void hasOneMorePattern() throws Exception {
    Choice segmentChoice = new Choice()
      .setSequenceId(BigInteger.valueOf(345L))
      .setPatternOffset(BigInteger.valueOf(0L))
      .setTranspose(5)
      .setType("Main")
      .setAvailablePatternOffsets("0,1");

    assertTrue(segmentChoice.hasOneMorePattern());
  }

  @Test
  public void hasOneMorePattern_true() throws Exception {
    Choice segmentChoice = new Choice()
      .setSequenceId(BigInteger.valueOf(345L))
      .setPatternOffset(BigInteger.valueOf(2L))
      .setTranspose(5)
      .setType("Main")
      .setAvailablePatternOffsets("0,1,2,3");

    assertTrue(segmentChoice.hasOneMorePattern());
  }

  @Test
  public void hasOneMorePattern_false() throws Exception {
    Choice segmentChoice = new Choice()
      .setSequenceId(BigInteger.valueOf(21L))
      .setPatternOffset(BigInteger.valueOf(3L))
      .setTranspose(5)
      .setType("Main")
      .setAvailablePatternOffsets("0,1,2,3");

    assertFalse(segmentChoice.hasOneMorePattern());
  }

  @Test
  public void hasTwoMorePatterns() throws Exception {
    Choice segmentChoice = new Choice()
      .setSequenceId(BigInteger.valueOf(64L))
      .setPatternOffset(BigInteger.valueOf(0L))
      .setTranspose(5)
      .setType("Macro")
      .setAvailablePatternOffsets("0,1,2");

    assertTrue(segmentChoice.hasTwoMorePatterns());
  }

  @Test
  public void hasTwoMorePatterns_true() throws Exception {
    Choice segmentChoice = new Choice()
      .setSequenceId(BigInteger.valueOf(64L))
      .setPatternOffset(BigInteger.valueOf(1L))
      .setTranspose(5)
      .setType("Macro")
      .setAvailablePatternOffsets("0,1,2,3");

    assertTrue(segmentChoice.hasTwoMorePatterns());
  }

  @Test
  public void hasTwoMorePatterns_false() throws Exception {
    Choice segmentChoice = new Choice()
      .setSequenceId(BigInteger.valueOf(64L))
      .setPatternOffset(BigInteger.valueOf(2L))
      .setTranspose(5)
      .setType("Macro")
      .setAvailablePatternOffsets("0,1,2,3");

    assertFalse(segmentChoice.hasTwoMorePatterns());
  }

}
