// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.lib.music;

import org.junit.Test;

import static io.xj.lib.music.Accidental.Flat;
import static io.xj.lib.music.Accidental.None;
import static io.xj.lib.music.Accidental.Sharp;
import static org.junit.Assert.assertEquals;

public class AccidentalTest {

  @Test
  public void TestAdjSymbolOf() {
    assertEquals(Sharp, Accidental.of("C"));
    assertEquals(Flat, Accidental.of("CMb5b7"));
    assertEquals(Sharp, Accidental.of("C#"));
    assertEquals(Flat, Accidental.of("Gb"));
    assertEquals(Flat, Accidental.of("G♭M"));
    assertEquals(Sharp, Accidental.of("A#m"));
    assertEquals(Sharp, Accidental.of("A♯M♯5"));
    assertEquals(Flat, Accidental.of("C minor"));
    assertEquals(Flat, Accidental.of("C dim"));
    assertEquals(Sharp, Accidental.of("CM M9 m7")); // More Sharpish than Flattish
    assertEquals(Flat, Accidental.of("Cm m9 M7"));  // More Flattish than Sharpish
    assertEquals(Sharp, Accidental.of("C major"));
  }

  @Test
  public void TestAdjSymbolBegin() {
    assertEquals(None, Accidental.firstOf("C".substring(1)));
    assertEquals(None, Accidental.firstOf("CMb5b7".substring(1)));
    assertEquals(Sharp, Accidental.firstOf("C#".substring(1)));
    assertEquals(Flat, Accidental.firstOf("Gb".substring(1)));
    assertEquals(Flat, Accidental.firstOf("G♭M".substring(1)));
    assertEquals(Sharp, Accidental.firstOf("A#m".substring(1)));
    assertEquals(Sharp, Accidental.firstOf("A♯M♯5".substring(1)));
  }


  @Test
  public void replaceWithExplicit() {
    assertEquals("Cs", Accidental.replaceWithExplicit("C#"));
    assertEquals("Cs", Accidental.replaceWithExplicit("C♯"));
    assertEquals("Cb", Accidental.replaceWithExplicit("Cb"));
    assertEquals("Cb", Accidental.replaceWithExplicit("C♭"));
  }
}

