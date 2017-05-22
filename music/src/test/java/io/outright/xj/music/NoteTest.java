// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.music;

import org.junit.Test;

import static io.outright.xj.music.PitchClass.C;
import static io.outright.xj.music.PitchClass.E;
import static io.outright.xj.music.PitchClass.G;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class NoteTest {
  @Test
  public void noteToString() throws Exception {
    assertEquals("C#5", Note.of("C#5").toString(AdjSymbol.Sharp));
    assertEquals("Db5", Note.of("C#5").toString(AdjSymbol.Flat));
  }

  @Test
  public void sameAs() throws Exception {
    assertTrue(Note.of("C5").sameAs(Note.of("C5")));
    assertTrue(Note.of("C#5").sameAs(Note.of("Db5")));
    assertFalse(Note.of("C#5").sameAs(Note.of("Db6")));
    assertFalse(Note.of("C#5").sameAs(Note.of("Eb5")));
  }


  @Test
  public void delta() throws Exception {
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
  public void conformedTo() throws Exception {
    assertEquals(G, Note.of("G#5").conformedTo(Chord.of("C major")).getPitchClass());
    assertEquals(G, Note.of("A5").conformedTo(Chord.of("C major")).getPitchClass());
    assertEquals(C, Note.of("A#5").conformedTo(Chord.of("C major")).getPitchClass());
    assertEquals(C, Note.of("B5").conformedTo(Chord.of("C major")).getPitchClass());
    assertEquals(E, Note.of("F5").conformedTo(Chord.of("C major")).getPitchClass());
  }

  @Test
  public void NamedTest() {
    Note note = Note.of("G");
    assertEquals(G, note.getPitchClass());
  }

  @Test
  public void OfPitchClassTest() {
    Note note = Note.of(C, 5);
    assertEquals(Integer.valueOf(5), note.getOctave());
    assertEquals(C, note.getPitchClass());
  }

}
