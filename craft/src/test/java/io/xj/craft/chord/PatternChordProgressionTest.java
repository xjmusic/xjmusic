// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.craft.chord;// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.

import io.xj.core.model.pattern_chord.PatternChord;
import io.xj.craft.chord.ChordProgression;
import io.xj.craft.chord.PatternChordProgression;
import io.xj.music.PitchClass;

import com.google.common.collect.ImmutableList;

import org.junit.Test;

import java.math.BigInteger;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class PatternChordProgressionTest {

  /**
   Assert two lists of pattern chords are equivalent

   @param o1 to compare
   @param o2 to compare
   */
  private static void assertEquivalent(List<PatternChord> o1, List<PatternChord> o2) {
    int size = o1.size();
    assertEquals(size, o2.size());
    for (int n = 0; n < size; n++)
      assertEquivalent(o1.get(n), o2.get(n));
  }

  /**
   Assert two pattern chords are equivalent

   @param o1 to compare
   @param o2 to compare
   */
  private static void assertEquivalent(PatternChord o1, PatternChord o2) {
    assertEquals(o1.getPatternId(), o2.getPatternId());
    assertEquals(o1.getPosition(), o2.getPosition());
    assertEquals(o1.getName(), o2.getName());
  }

  @Test
  public void getDescriptor_basic() {
    PatternChordProgression subjectA = new PatternChordProgression(BigInteger.valueOf(25), ImmutableList.of(
      new PatternChord().setPosition(0.0).setName("D").setPatternId(BigInteger.valueOf(25)),
      new PatternChord().setPosition(2.0).setName("A").setPatternId(BigInteger.valueOf(25)),
      new PatternChord().setPosition(7.0).setName("B").setPatternId(BigInteger.valueOf(25))
    ));
    PatternChordProgression subjectB = new PatternChordProgression(BigInteger.valueOf(29), ImmutableList.of(
      new PatternChord().setPosition(4.0).setName("F").setPatternId(BigInteger.valueOf(29)),
      new PatternChord().setPosition(6.0).setName("C").setPatternId(BigInteger.valueOf(29)),
      new PatternChord().setPosition(11.0).setName("D").setPatternId(BigInteger.valueOf(29))
    ));

    // Note: These two end up having identical descriptors, because the chords are positioned identically relative to each other
    assertEquals("Major:7|Major:2|Major", subjectA.getChordProgression().toString());
    assertEquals("Major:7|Major:2|Major", subjectB.getChordProgression().toString());
  }

  @Test
  public void getDescriptor_moreComplex() {
    PatternChordProgression subjectA = new PatternChordProgression(BigInteger.valueOf(25), ImmutableList.of(
      new PatternChord().setPosition(0.0).setName("D7+9").setPatternId(BigInteger.valueOf(25)),
      new PatternChord().setPosition(2.0).setName("Am7").setPatternId(BigInteger.valueOf(25)),
      new PatternChord().setPosition(7.0).setName("B7+5").setPatternId(BigInteger.valueOf(25))
    ));
    PatternChordProgression subjectB = new PatternChordProgression(BigInteger.valueOf(29), ImmutableList.of(
      new PatternChord().setPosition(4.0).setName("F7+9").setPatternId(BigInteger.valueOf(29)),
      new PatternChord().setPosition(6.0).setName("Cm7").setPatternId(BigInteger.valueOf(29)),
      new PatternChord().setPosition(11.0).setName("D7+5").setPatternId(BigInteger.valueOf(29))
    ));

    // Note: These two end up having identical descriptors, because the chords are positioned identically relative to each other
    assertEquals("Major Seventh Add Ninth:7|Minor Seventh:2|Major Seventh", subjectA.getChordProgression().toString());
    assertEquals("Major Seventh Add Ninth:7|Minor Seventh:2|Major Seventh", subjectB.getChordProgression().toString());
  }

  @Test
  public void getRootChord() {
    PatternChordProgression subjectA = new PatternChordProgression(BigInteger.valueOf(25), ImmutableList.of(
      new PatternChord().setPosition(0.0).setName("D").setPatternId(BigInteger.valueOf(25)),
      new PatternChord().setPosition(2.0).setName("A").setPatternId(BigInteger.valueOf(25)),
      new PatternChord().setPosition(7.0).setName("B").setPatternId(BigInteger.valueOf(25))
    ));
    PatternChordProgression subjectB = new PatternChordProgression(BigInteger.valueOf(29), ImmutableList.of(
      new PatternChord().setPosition(4.0).setName("F").setPatternId(BigInteger.valueOf(29)),
      new PatternChord().setPosition(6.0).setName("C").setPatternId(BigInteger.valueOf(29)),
      new PatternChord().setPosition(11.0).setName("D").setPatternId(BigInteger.valueOf(29))
    ));

    // Note: These two end up having identical descriptors, because the chords are positioned identically relative to each other
    assertEquals("D", subjectA.getRootChord().getName());
    assertEquals("F", subjectB.getRootChord().getName());
  }

  @Test
  public void createFromProgression() {
    assertEquivalent(
      ImmutableList.of(
        new PatternChord(BigInteger.valueOf(27), 0.0, "F Major Seventh Add Ninth"),
        new PatternChord(BigInteger.valueOf(27), 4.0, "C Minor Seventh"),
        new PatternChord(BigInteger.valueOf(27), 8.0, "D Major Seventh"),
        new PatternChord(BigInteger.valueOf(27), 12.0, "Bb Minor Flat Nine")
      ),
      new PatternChordProgression(
        new ChordProgression("0|Major Seventh Add Ninth:7|Minor Seventh:9|Major Seventh:5|Minor Flat Nine"),
        BigInteger.valueOf(27),
        PitchClass.F,
        4.0
      ).getChords());

    assertEquivalent(
      ImmutableList.of(
        new PatternChord(BigInteger.valueOf(27), 0.0, "B Minor Flat Five"),
        new PatternChord(BigInteger.valueOf(27), 4.0, "F# Major Seven"),
        new PatternChord(BigInteger.valueOf(27), 8.0, "F Major Seven Flat Nine")
      ),
      new PatternChordProgression(
        new ChordProgression("0|Minor Flat Five:7|Major Seven:6|Major Seven Flat Nine"),
        BigInteger.valueOf(27),
        PitchClass.B,
        4.0
      ).getChords());
  }

}
