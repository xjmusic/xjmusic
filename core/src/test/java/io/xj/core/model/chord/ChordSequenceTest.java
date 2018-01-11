package io.xj.core.model.chord;// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.

import io.xj.core.model.phase_chord.PhaseChord;

import com.google.common.collect.ImmutableList;

import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ChordSequenceTest {

  @Test
  public void getDescriptor_basic() throws Exception {
    ChordSequence subjectA = new ChordSequence(BigInteger.valueOf(25), ImmutableList.of(
      new PhaseChord().setPosition(0).setName("D").setPhaseId(BigInteger.valueOf(25)),
      new PhaseChord().setPosition(2).setName("A").setPhaseId(BigInteger.valueOf(25)),
      new PhaseChord().setPosition(7).setName("B").setPhaseId(BigInteger.valueOf(25))
    ));
    ChordSequence subjectB = new ChordSequence(BigInteger.valueOf(29), ImmutableList.of(
      new PhaseChord().setPosition(4).setName("F").setPhaseId(BigInteger.valueOf(29)),
      new PhaseChord().setPosition(6).setName("C").setPhaseId(BigInteger.valueOf(29)),
      new PhaseChord().setPosition(11).setName("D").setPhaseId(BigInteger.valueOf(29))
    ));

    // Note: These two end up having identical descriptors, because the chords are positioned identically relative to each other
    assertEquals("Major:7|Major:2|Major", subjectA.getDescriptor());
    assertEquals("Major:7|Major:2|Major", subjectB.getDescriptor());
  }

  @Test
  public void getDescriptor_moreComplex() throws Exception {
    ChordSequence subjectA = new ChordSequence(BigInteger.valueOf(25), ImmutableList.of(
      new PhaseChord().setPosition(0).setName("D7+9").setPhaseId(BigInteger.valueOf(25)),
      new PhaseChord().setPosition(2).setName("Am7").setPhaseId(BigInteger.valueOf(25)),
      new PhaseChord().setPosition(7).setName("B7+5").setPhaseId(BigInteger.valueOf(25))
    ));
    ChordSequence subjectB = new ChordSequence(BigInteger.valueOf(29), ImmutableList.of(
      new PhaseChord().setPosition(4).setName("F7+9").setPhaseId(BigInteger.valueOf(29)),
      new PhaseChord().setPosition(6).setName("Cm7").setPhaseId(BigInteger.valueOf(29)),
      new PhaseChord().setPosition(11).setName("D7+5").setPhaseId(BigInteger.valueOf(29))
    ));

    // Note: These two end up having identical descriptors, because the chords are positioned identically relative to each other
    assertEquals("Major Seventh Add Ninth:7|Minor Seventh:2|Major Seventh", subjectA.getDescriptor());
    assertEquals("Major Seventh Add Ninth:7|Minor Seventh:2|Major Seventh", subjectB.getDescriptor());
  }

  @Test
  public void isRedundantSubsetOfDescriptor() throws Exception {
    assertTrue(ChordSequence.isRedundantSubsetOfDescriptor(
      "Major:7|Minor:5|Major",
      "Major:7|Minor:5|Major:10|Minor",
      1));
    assertFalse(ChordSequence.isRedundantSubsetOfDescriptor(
      "Major:7|Minor",
      "Major:7|Minor:5|Major:10|Minor",
      1));
    assertTrue(ChordSequence.isRedundantSubsetOfDescriptor(
      "Major:7|Minor",
      "Major:7|Minor:5|Major:10|Minor",
      2));
    assertTrue(ChordSequence.isRedundantSubsetOfDescriptor(
      "Minor:5|Major",
      "Major:7|Minor:5|Major:10|Minor",
      2));
    assertFalse(ChordSequence.isRedundantSubsetOfDescriptor(
      "Minor:5|Major",
      "Major:7|Minor:5|Major:10|Minor",
      1));
  }
}
