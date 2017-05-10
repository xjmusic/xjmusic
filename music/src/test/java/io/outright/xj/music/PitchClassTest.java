// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.music;

import org.junit.Test;

import static io.outright.xj.music.AdjSymbol.Flat;
import static io.outright.xj.music.AdjSymbol.None;
import static io.outright.xj.music.AdjSymbol.Sharp;
import static io.outright.xj.music.PitchClass.A;
import static io.outright.xj.music.PitchClass.As;
import static io.outright.xj.music.PitchClass.B;
import static io.outright.xj.music.PitchClass.C;
import static io.outright.xj.music.PitchClass.Cs;
import static io.outright.xj.music.PitchClass.D;
import static io.outright.xj.music.PitchClass.Ds;
import static io.outright.xj.music.PitchClass.E;
import static io.outright.xj.music.PitchClass.F;
import static io.outright.xj.music.PitchClass.Fs;
import static io.outright.xj.music.PitchClass.G;
import static io.outright.xj.music.PitchClass.Gs;
import static org.junit.Assert.assertEquals;

public class PitchClassTest {


  @Test
  public void NameOfTest() throws Exception {
    assertPitchClassOf("C", C, "C", "C");
    assertPitchClassOf("C#", Cs, "C#", "Db");
    assertPitchClassOf("Cb", B, "B", "B");
    assertPitchClassOf("D", D, "D", "D");
    assertPitchClassOf("D#", Ds, "D#", "Eb");
    assertPitchClassOf("D♭", Cs, "C#", "Db");
    assertPitchClassOf("E", E, "E", "E");
    assertPitchClassOf("E#", F, "F", "F");
    assertPitchClassOf("E♭", Ds, "D#", "Eb");
    assertPitchClassOf("F", F, "F", "F");
    assertPitchClassOf("F#", Fs, "F#", "Gb");
    assertPitchClassOf("F♭", E, "E", "E");
    assertPitchClassOf("G", G, "G", "G");
    assertPitchClassOf("G♯", Gs, "G#", "Ab");
    assertPitchClassOf("Gb", Fs, "F#", "Gb");
    assertPitchClassOf("A", A, "A", "A");
    assertPitchClassOf("A#", As, "A#", "Bb");
    assertPitchClassOf("Ab", Gs, "G#", "Ab");
    assertPitchClassOf("B", B, "B", "B");
    assertPitchClassOf("B#", C, "C", "C");
    assertPitchClassOf("E♭", Ds, "D#", "Eb");
    assertPitchClassOf("Bb", As, "A#", "Bb");
    assertPitchClassOf("z", PitchClass.None, "-", "-");
    assertPitchClassOf("zzzz", PitchClass.None, "-", "-");
  }

  @Test
  public void NameStepTest() {
    //assertEquals(false, true)
  }

  @Test
  public void StringOfTest() {
    assertEquals("C#", Cs.toString(Sharp));
    assertEquals("Db", Cs.toString(Flat));
    assertEquals("-", Cs.toString(None));
  }


  @Test
  public void PitchClass_DiffTest() {
    assertEquals(5, Cs.delta(Fs));
    assertEquals(-5, Fs.delta(Cs));
    assertEquals(2, Gs.delta(As));
    assertEquals(-3, C.delta(A));
    assertEquals(4, D.delta(Fs));
    assertEquals(-6, F.delta(B));
  }

  private void assertPitchClassOf(String name, PitchClass expectPitchClass, String expectStringSharp, String expectStringFlat) throws Exception {
    PitchClass pitchClass = PitchClass.of(name);
    assertEquals(expectPitchClass, pitchClass);
    assertEquals(expectStringSharp, pitchClass.toString(Sharp));
    assertEquals(expectStringFlat, pitchClass.toString(Flat));
  }

}
