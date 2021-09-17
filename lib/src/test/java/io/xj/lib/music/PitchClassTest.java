// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.music;

import org.junit.Test;

import static io.xj.lib.music.AdjSymbol.Flat;
import static io.xj.lib.music.AdjSymbol.None;
import static io.xj.lib.music.AdjSymbol.Sharp;
import static org.junit.Assert.assertEquals;

public class PitchClassTest {


  @Test
  public void NameOfTest() {
    assertPitchClassOf("C", PitchClass.C, "C", "C");
    assertPitchClassOf("C#", PitchClass.Cs, "C#", "Db");
    assertPitchClassOf("Cb", PitchClass.B, "B", "B");
    assertPitchClassOf("D", PitchClass.D, "D", "D");
    assertPitchClassOf("D#", PitchClass.Ds, "D#", "Eb");
    assertPitchClassOf("D♭", PitchClass.Cs, "C#", "Db");
    assertPitchClassOf("E", PitchClass.E, "E", "E");
    assertPitchClassOf("E#", PitchClass.F, "F", "F");
    assertPitchClassOf("E♭", PitchClass.Ds, "D#", "Eb");
    assertPitchClassOf("F", PitchClass.F, "F", "F");
    assertPitchClassOf("F#", PitchClass.Fs, "F#", "Gb");
    assertPitchClassOf("F♭", PitchClass.E, "E", "E");
    assertPitchClassOf("G", PitchClass.G, "G", "G");
    assertPitchClassOf("G♯", PitchClass.Gs, "G#", "Ab");
    assertPitchClassOf("Gb", PitchClass.Fs, "F#", "Gb");
    assertPitchClassOf("A", PitchClass.A, "A", "A");
    assertPitchClassOf("A#", PitchClass.As, "A#", "Bb");
    assertPitchClassOf("Ab", PitchClass.Gs, "G#", "Ab");
    assertPitchClassOf("B", PitchClass.B, "B", "B");
    assertPitchClassOf("B#", PitchClass.C, "C", "C");
    assertPitchClassOf("E♭", PitchClass.Ds, "D#", "Eb");
    assertPitchClassOf("Bb", PitchClass.As, "A#", "Bb");
    assertPitchClassOf("z", PitchClass.None, "X", "X");
    assertPitchClassOf("zzzz", PitchClass.None, "X", "X");
  }

  @Test
  public void NameStepTest() {
    //assertEquals(false, true)
  }

  @Test
  public void pitchClass_toString() {
    assertEquals("C#", PitchClass.Cs.toString(Sharp));
    assertEquals("Db", PitchClass.Cs.toString(Flat));
    assertEquals("X", PitchClass.Cs.toString(None));
  }

  @Test
  public void delta() {
    assertEquals(5, PitchClass.Cs.delta(PitchClass.Fs));
    assertEquals(-5, PitchClass.Fs.delta(PitchClass.Cs));
    assertEquals(2, PitchClass.Gs.delta(PitchClass.As));
    assertEquals(-3, PitchClass.C.delta(PitchClass.A));
    assertEquals(4, PitchClass.D.delta(PitchClass.Fs));
    assertEquals(-6, PitchClass.F.delta(PitchClass.B));
    assertEquals(0, PitchClass.Cs.delta(PitchClass.None));
    assertEquals(0, PitchClass.None.delta(PitchClass.Cs));
    assertEquals(0, PitchClass.None.delta(PitchClass.None));
  }

  private void assertPitchClassOf(String name, PitchClass expectPitchClass, String expectStringSharp, String expectStringFlat) {
    PitchClass pitchClass = PitchClass.of(name);
    assertEquals(expectPitchClass, pitchClass);
    assertEquals(expectStringSharp, pitchClass.toString(Sharp));
    assertEquals(expectStringFlat, pitchClass.toString(Flat));
  }

}
