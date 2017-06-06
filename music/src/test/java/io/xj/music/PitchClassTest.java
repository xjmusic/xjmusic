// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.music;

import org.junit.Assert;
import org.junit.Test;

import static io.xj.music.AdjSymbol.Flat;
import static io.xj.music.AdjSymbol.None;
import static io.xj.music.AdjSymbol.Sharp;
import static org.junit.Assert.assertEquals;

public class PitchClassTest {


  @Test
  public void NameOfTest() throws Exception {
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
    assertPitchClassOf("z", PitchClass.None, "-", "-");
    assertPitchClassOf("zzzz", PitchClass.None, "-", "-");
  }

  @Test
  public void NameStepTest() {
    //assertEquals(false, true)
  }

  @Test
  public void StringOfTest() {
    Assert.assertEquals("C#", PitchClass.Cs.toString(Sharp));
    Assert.assertEquals("Db", PitchClass.Cs.toString(Flat));
    Assert.assertEquals("-", PitchClass.Cs.toString(None));
  }


  @Test
  public void PitchClass_DiffTest() {
    Assert.assertEquals(5, PitchClass.Cs.delta(PitchClass.Fs));
    Assert.assertEquals(-5, PitchClass.Fs.delta(PitchClass.Cs));
    Assert.assertEquals(2, PitchClass.Gs.delta(PitchClass.As));
    Assert.assertEquals(-3, PitchClass.C.delta(PitchClass.A));
    Assert.assertEquals(4, PitchClass.D.delta(PitchClass.Fs));
    Assert.assertEquals(-6, PitchClass.F.delta(PitchClass.B));
  }

  private void assertPitchClassOf(String name, PitchClass expectPitchClass, String expectStringSharp, String expectStringFlat) throws Exception {
    PitchClass pitchClass = PitchClass.of(name);
    assertEquals(expectPitchClass, pitchClass);
    assertEquals(expectStringSharp, pitchClass.toString(Sharp));
    assertEquals(expectStringFlat, pitchClass.toString(Flat));
  }

}
