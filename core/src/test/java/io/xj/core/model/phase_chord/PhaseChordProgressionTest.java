package io.xj.core.model.phase_chord;// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.

import com.google.common.collect.ImmutableList;

import io.xj.core.model.chord.ChordProgression;
import io.xj.music.PitchClass;
import org.junit.Test;

import java.math.BigInteger;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class PhaseChordProgressionTest {

  /**
   Assert two lists of phase chords are equivalent

   @param o1 to compare
   @param o2 to compare
   */
  private static void assertEquivalent(List<PhaseChord> o1, List<PhaseChord> o2) {
    int size = o1.size();
    assertEquals(size, o2.size());
    for (int n = 0; n < size; n++)
      assertEquivalent(o1.get(n), o2.get(n));
  }

  /**
   Assert two phase chords are equivalent

   @param o1 to compare
   @param o2 to compare
   */
  private static void assertEquivalent(PhaseChord o1, PhaseChord o2) {
    assertEquals(o1.getPhaseId(), o2.getPhaseId());
    assertEquals(o1.getPosition(), o2.getPosition());
    assertEquals(o1.getName(), o2.getName());
  }

  @Test
  public void getDescriptor_basic() throws Exception {
    PhaseChordProgression subjectA = new PhaseChordProgression(BigInteger.valueOf(25), ImmutableList.of(
      new PhaseChord().setPosition(0).setName("D").setPhaseId(BigInteger.valueOf(25)),
      new PhaseChord().setPosition(2).setName("A").setPhaseId(BigInteger.valueOf(25)),
      new PhaseChord().setPosition(7).setName("B").setPhaseId(BigInteger.valueOf(25))
    ));
    PhaseChordProgression subjectB = new PhaseChordProgression(BigInteger.valueOf(29), ImmutableList.of(
      new PhaseChord().setPosition(4).setName("F").setPhaseId(BigInteger.valueOf(29)),
      new PhaseChord().setPosition(6).setName("C").setPhaseId(BigInteger.valueOf(29)),
      new PhaseChord().setPosition(11).setName("D").setPhaseId(BigInteger.valueOf(29))
    ));

    // Note: These two end up having identical descriptors, because the chords are positioned identically relative to each other
    assertEquals("Major:7|Major:2|Major", subjectA.getChordProgression().toString());
    assertEquals("Major:7|Major:2|Major", subjectB.getChordProgression().toString());
  }

  @Test
  public void getDescriptor_moreComplex() throws Exception {
    PhaseChordProgression subjectA = new PhaseChordProgression(BigInteger.valueOf(25), ImmutableList.of(
      new PhaseChord().setPosition(0).setName("D7+9").setPhaseId(BigInteger.valueOf(25)),
      new PhaseChord().setPosition(2).setName("Am7").setPhaseId(BigInteger.valueOf(25)),
      new PhaseChord().setPosition(7).setName("B7+5").setPhaseId(BigInteger.valueOf(25))
    ));
    PhaseChordProgression subjectB = new PhaseChordProgression(BigInteger.valueOf(29), ImmutableList.of(
      new PhaseChord().setPosition(4).setName("F7+9").setPhaseId(BigInteger.valueOf(29)),
      new PhaseChord().setPosition(6).setName("Cm7").setPhaseId(BigInteger.valueOf(29)),
      new PhaseChord().setPosition(11).setName("D7+5").setPhaseId(BigInteger.valueOf(29))
    ));

    // Note: These two end up having identical descriptors, because the chords are positioned identically relative to each other
    assertEquals("Major Seventh Add Ninth:7|Minor Seventh:2|Major Seventh", subjectA.getChordProgression().toString());
    assertEquals("Major Seventh Add Ninth:7|Minor Seventh:2|Major Seventh", subjectB.getChordProgression().toString());
  }

  @Test
  public void getRootChord() throws Exception {
    PhaseChordProgression subjectA = new PhaseChordProgression(BigInteger.valueOf(25), ImmutableList.of(
      new PhaseChord().setPosition(0).setName("D").setPhaseId(BigInteger.valueOf(25)),
      new PhaseChord().setPosition(2).setName("A").setPhaseId(BigInteger.valueOf(25)),
      new PhaseChord().setPosition(7).setName("B").setPhaseId(BigInteger.valueOf(25))
    ));
    PhaseChordProgression subjectB = new PhaseChordProgression(BigInteger.valueOf(29), ImmutableList.of(
      new PhaseChord().setPosition(4).setName("F").setPhaseId(BigInteger.valueOf(29)),
      new PhaseChord().setPosition(6).setName("C").setPhaseId(BigInteger.valueOf(29)),
      new PhaseChord().setPosition(11).setName("D").setPhaseId(BigInteger.valueOf(29))
    ));

    // Note: These two end up having identical descriptors, because the chords are positioned identically relative to each other
    assertEquals("D", subjectA.getRootChord().getName());
    assertEquals("F", subjectB.getRootChord().getName());
  }

  @Test
  public void createFromProgression() throws Exception {
    assertEquivalent(
      ImmutableList.of(
        new PhaseChord(BigInteger.valueOf(27), 0, "F Major Seventh Add Ninth"),
        new PhaseChord(BigInteger.valueOf(27), 4, "C Minor Seventh"),
        new PhaseChord(BigInteger.valueOf(27), 8, "D Major Seventh"),
        new PhaseChord(BigInteger.valueOf(27), 12, "Bb Minor Flat Nine")
      ),
      new PhaseChordProgression(
        new ChordProgression("0|Major Seventh Add Ninth:7|Minor Seventh:9|Major Seventh:5|Minor Flat Nine"),
        BigInteger.valueOf(27),
        PitchClass.F
      ).getChords());

    assertEquivalent(
      ImmutableList.of(
        new PhaseChord(BigInteger.valueOf(27), 0, "B Minor Flat Five"),
        new PhaseChord(BigInteger.valueOf(27), 4, "F# Major Seven"),
        new PhaseChord(BigInteger.valueOf(27), 8, "F Major Seven Flat Nine")
      ),
      new PhaseChordProgression(
        new ChordProgression("0|Minor Flat Five:7|Major Seven:6|Major Seven Flat Nine"),
        BigInteger.valueOf(27),
        PitchClass.B
      ).getChords());
  }

}
