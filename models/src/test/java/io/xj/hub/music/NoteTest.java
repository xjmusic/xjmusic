// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.music;

import org.junit.jupiter.api.Test;

import jakarta.annotation.Nullable;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NoteTest {

  public static void assertNote(String expect, @Nullable Note actual) {
    assertNotNull(actual);
    assertEquals(Note.of(expect).getPitchClass(), actual.getPitchClass());
    assertEquals(Note.of(expect).getOctave(), actual.getOctave());
  }

  /**
   [#303] Craft calculates drum audio pitch to conform to the allowable note closest to the original note, slightly favoring down-pitching versus up-pitching.
   */
  @Test
  public void setOctaveNearest() {
    assertEquals(Integer.valueOf(3), Note.of("C7").setOctaveNearest(Note.of("B2")).getOctave());
    assertEquals(Integer.valueOf(2), Note.of("F7").setOctaveNearest(Note.of("B2")).getOctave());
    assertEquals(Integer.valueOf(3), Note.of("E7").setOctaveNearest(Note.of("B2")).getOctave());
  }

  @Test
  public void noteToString() {
    assertEquals("C#5", Note.of("C#5").toString(Accidental.Sharp));
    assertEquals("Db5", Note.of("C#5").toString(Accidental.Flat));
  }

  @Test
  public void sameAs() {
    assertTrue(Note.of("C5").sameAs(Note.of("C5")));
    assertTrue(Note.of("C#5").sameAs(Note.of("Db5")));
    assertFalse(Note.of("C#5").sameAs(Note.of("Db6")));
    assertFalse(Note.of("C#5").sameAs(Note.of("Eb5")));
  }

  @Test
  public void delta() {
    // run from -20 to +20
    assertEquals(Integer.valueOf(-20), Note.of("C5").delta(Note.of("E3")));
    assertEquals(Integer.valueOf(-19), Note.of("C5").delta(Note.of("F3")));
    assertEquals(Integer.valueOf(-18), Note.of("C5").delta(Note.of("Gb3")));
    assertEquals(Integer.valueOf(-17), Note.of("C5").delta(Note.of("G3")));
    assertEquals(Integer.valueOf(-16), Note.of("C5").delta(Note.of("Ab3")));
    assertEquals(Integer.valueOf(-15), Note.of("C5").delta(Note.of("A3")));
    assertEquals(Integer.valueOf(-14), Note.of("C5").delta(Note.of("Bb3")));
    assertEquals(Integer.valueOf(-13), Note.of("C5").delta(Note.of("B3")));
    assertEquals(Integer.valueOf(-12), Note.of("C5").delta(Note.of("C4")));
    assertEquals(Integer.valueOf(-11), Note.of("C5").delta(Note.of("Db4")));
    assertEquals(Integer.valueOf(-10), Note.of("C5").delta(Note.of("D4")));
    assertEquals(Integer.valueOf(-9), Note.of("C5").delta(Note.of("Eb4")));
    assertEquals(Integer.valueOf(-8), Note.of("C5").delta(Note.of("E4")));
    assertEquals(Integer.valueOf(-7), Note.of("C5").delta(Note.of("F4")));
    assertEquals(Integer.valueOf(-6), Note.of("C5").delta(Note.of("Gb4")));
    assertEquals(Integer.valueOf(-5), Note.of("C5").delta(Note.of("G4")));
    assertEquals(Integer.valueOf(-4), Note.of("C5").delta(Note.of("Ab4")));
    assertEquals(Integer.valueOf(-3), Note.of("C5").delta(Note.of("A4")));
    assertEquals(Integer.valueOf(-2), Note.of("C5").delta(Note.of("Bb4")));
    assertEquals(Integer.valueOf(-1), Note.of("C5").delta(Note.of("B4")));
    assertEquals(Integer.valueOf(0), Note.of("C5").delta(Note.of("C5")));
    assertEquals(Integer.valueOf(1), Note.of("C5").delta(Note.of("C#5")));
    assertEquals(Integer.valueOf(2), Note.of("C5").delta(Note.of("D5")));
    assertEquals(Integer.valueOf(3), Note.of("C5").delta(Note.of("D#5")));
    assertEquals(Integer.valueOf(4), Note.of("C5").delta(Note.of("E5")));
    assertEquals(Integer.valueOf(5), Note.of("C5").delta(Note.of("F5")));
    assertEquals(Integer.valueOf(6), Note.of("C5").delta(Note.of("F#5")));
    assertEquals(Integer.valueOf(7), Note.of("C5").delta(Note.of("G5")));
    assertEquals(Integer.valueOf(8), Note.of("C5").delta(Note.of("G#5")));
    assertEquals(Integer.valueOf(9), Note.of("C5").delta(Note.of("A5")));
    assertEquals(Integer.valueOf(10), Note.of("C5").delta(Note.of("A#5")));
    assertEquals(Integer.valueOf(11), Note.of("C5").delta(Note.of("B5")));
    assertEquals(Integer.valueOf(12), Note.of("C5").delta(Note.of("C6")));
    assertEquals(Integer.valueOf(13), Note.of("C5").delta(Note.of("C#6")));
    assertEquals(Integer.valueOf(14), Note.of("C5").delta(Note.of("D6")));
    assertEquals(Integer.valueOf(15), Note.of("C5").delta(Note.of("D#6")));
    assertEquals(Integer.valueOf(16), Note.of("C5").delta(Note.of("E6")));
    assertEquals(Integer.valueOf(17), Note.of("C5").delta(Note.of("F6")));
    assertEquals(Integer.valueOf(18), Note.of("C5").delta(Note.of("F#6")));
    assertEquals(Integer.valueOf(19), Note.of("C5").delta(Note.of("G6")));
    assertEquals(Integer.valueOf(20), Note.of("C5").delta(Note.of("G#6")));
    // spot checks relative to A4 (re: tuning)
    assertEquals(Integer.valueOf(6), Note.of("A4").delta(Note.of("D#5")));
  }

  @Test
  public void NamedTest() {
    Note note = Note.of("G");
    assertEquals(PitchClass.G, note.getPitchClass());
  }

  @Test
  public void OfPitchClassTest() {
    Note note = Note.of(PitchClass.C, 5);
    assertEquals(Integer.valueOf(5), note.getOctave());
    assertEquals(PitchClass.C, note.getPitchClass());
  }

  @Test
  public void compareTo() {
    assertEquals(
      "C1,D1,D#1,E1,F#1,C2,D2,D#2,E2,F#2,C3,D3,D#3,E3",
      List.of(Note.of("C1"),
          Note.of("F#1"),
          Note.of("E1"),
          Note.of("C3"),
          Note.of("D#3"),
          Note.of("D1"),
          Note.of("D2"),
          Note.of("D3"),
          Note.of("E2"),
          Note.of("C2"),
          Note.of("D#2"),
          Note.of("E3"),
          Note.of("D#1"),
          Note.of("F#2"))
        .stream()
        .sorted(Note::compareTo)
        .map(note -> note.toString(Accidental.Sharp))
        .collect(Collectors.joining(","))
    );
  }

  @Test
  public void isLower() {
    assertTrue(Note.of("A5").isLower(Note.of("A6")));
    assertTrue(Note.of("A5").isLower(Note.of("A#5")));
    assertFalse(Note.of("A5").isLower(Note.of("Ab5")));
  }

  @Test
  public void isHigher() {
    assertTrue(Note.of("G6").isHigher(Note.of("F6")));
    assertTrue(Note.of("A5").isHigher(Note.of("A4")));
    assertTrue(Note.of("A5").isHigher(Note.of("Ab5")));
    assertFalse(Note.of("A5").isHigher(Note.of("A#5")));
  }

  @Test
  public void atonal() {
    assertEquals(PitchClass.None, Note.atonal().getPitchClass());
  }

  @Test
  public void isAtonal() {
    assertTrue(Note.atonal().isAtonal());
    assertTrue(Note.of("X0").isAtonal());
  }

  /**
   NC sections should not cache notes from the previous section https://github.com/xjmusic/workstation/issues/221
   */
  @Test
  public void onlyValid() {
    assertEquals(PitchClass.G, Note.ofValid("G6").findAny().orElseThrow().getPitchClass());
    assertEquals(PitchClass.Gs, Note.ofValid("G#6").findAny().orElseThrow().getPitchClass());
    assertEquals(PitchClass.None, Note.ofValid("X").findAny().orElseThrow().getPitchClass());
    assertFalse(Note.ofValid("(None)").findAny().isPresent());
    assertFalse(Note.ofValid("abc").findAny().isPresent());
  }

  /**
   NC sections should not cache notes from the previous section https://github.com/xjmusic/workstation/issues/221
   */
  @Test
  public void isValid() {
    assertTrue(Note.isValid("G6"));
    assertTrue(Note.isValid("G#6"));
    assertTrue(Note.isValid("X"));
    assertFalse(Note.isValid("(None)"));
    assertFalse(Note.isValid("abc"));
  }

  @Test
  public void containsAnyValidNotes() {
    assertTrue(Note.containsAnyValidNotes("C, D, E"));
    assertTrue(Note.containsAnyValidNotes("X")); // drum event note
    assertTrue(Note.containsAnyValidNotes("C3, D3, E3"));
    assertFalse(Note.containsAnyValidNotes("Y, Z"));
    assertFalse(Note.containsAnyValidNotes("(None)")); // NC voicing
  }

  @Test
  public void median() {
    assertNull(Note.median(null, null));
    assertNote("C5", Note.median(Note.of("C5"), null));
    assertNote("G#5", Note.median(null, Note.of("G#5")));
    assertNote("E5", Note.median(Note.of("C5"), Note.of("G#5")));
  }

  @Test
  public void nextUp() {
    assertEquals(PitchClass.None, Note.of("X").nextUp(PitchClass.C).getPitchClass());
    assertNote("C4", Note.of("B3").nextUp(PitchClass.C));
  }

  @Test
  public void nextDown() {
    assertEquals(PitchClass.None, Note.of("X").nextDown(PitchClass.C).getPitchClass());
    assertNote("C4", Note.of("D4").nextDown(PitchClass.C));
  }

}
