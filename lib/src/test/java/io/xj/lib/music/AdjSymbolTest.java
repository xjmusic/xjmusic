// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.music;

import org.junit.Test;

import static io.xj.lib.music.AdjSymbol.Flat;
import static io.xj.lib.music.AdjSymbol.None;
import static io.xj.lib.music.AdjSymbol.Sharp;
import static org.junit.Assert.assertEquals;

public class AdjSymbolTest {

  @Test
  public void TestAdjSymbolOf() {
    assertEquals(Sharp, AdjSymbol.of("C"));
    assertEquals(Flat, AdjSymbol.of("CMb5b7"));
    assertEquals(Sharp, AdjSymbol.of("C#"));
    assertEquals(Flat, AdjSymbol.of("Gb"));
    assertEquals(Flat, AdjSymbol.of("G♭M"));
    assertEquals(Sharp, AdjSymbol.of("A#m"));
    assertEquals(Sharp, AdjSymbol.of("A♯M♯5"));
    assertEquals(Flat, AdjSymbol.of("C minor"));
    assertEquals(Flat, AdjSymbol.of("C dim"));
    assertEquals(Sharp, AdjSymbol.of("CM M9 m7")); // More Sharpish than Flattish
    assertEquals(Flat, AdjSymbol.of("Cm m9 M7"));  // More Flattish than Sharpish
    assertEquals(Sharp, AdjSymbol.of("C major"));
  }

  @Test
  public void TestAdjSymbolBegin() {
    assertEquals(None, AdjSymbol.firstOf("C".substring(1)));
    assertEquals(None, AdjSymbol.firstOf("CMb5b7".substring(1)));
    assertEquals(Sharp, AdjSymbol.firstOf("C#".substring(1)));
    assertEquals(Flat, AdjSymbol.firstOf("Gb".substring(1)));
    assertEquals(Flat, AdjSymbol.firstOf("G♭M".substring(1)));
    assertEquals(Sharp, AdjSymbol.firstOf("A#m".substring(1)));
    assertEquals(Sharp, AdjSymbol.firstOf("A♯M♯5".substring(1)));
  }


}

