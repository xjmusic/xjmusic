// Copyright (c) 1999-2022, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.meme;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class IsometryTest {

  @Test
  public void of() {
    Isometry result = Isometry.of(ImmutableList.of("Smooth", "Catlike"));

    assertArrayEquals(new String[]{"Catlike", "Smooth"}, result.getSources().stream().sorted().toArray());
  }

  @Test
  public void ofPhonetic() {
    Isometry result = Isometry.ofPhonetic(ImmutableList.of("Kick", "Snare"));

    assertArrayEquals(new String[]{"KK", "SNR"}, result.getSources().stream().sorted().toArray());
  }

  @Test
  public void add() {
    Isometry result = Isometry.of(ImmutableList.of("Smooth"));
    result.add("Catlike");

    assertArrayEquals(new String[]{"Catlike", "Smooth"}, result.getSources().stream().sorted().toArray());
  }

  @Test
  public void getSourceStems() {
    Set<String> result = Isometry.of(ImmutableList.of("Intensity", "Cool", "Dark")).getSources();

    assertArrayEquals(new String[]{"Cool", "Dark", "Intensity"}, result.stream().sorted().toArray());
  }

  @Test
  public void getConstellation() {
    assertEquals("Catlike_Smooth", Isometry.of(ImmutableList.of("Smooth", "Catlike")).getConstellation());
    assertEquals("Cool_Dark_Intensity", Isometry.of(ImmutableList.of("Intensity", "Cool", "Dark")).getConstellation());
    assertEquals("Bam_Flam_Shim_Wham", Isometry.of(ImmutableList.of("Wham", "Bam", "Shim", "Shim", "Shim", "Flam")).getConstellation());
  }

  @Test
  public void getConstellation_withNotMeme() {
    assertEquals("!Clumsy_Catlike_Smooth", Isometry.of(ImmutableList.of("Smooth", "Catlike", "!Clumsy")).getConstellation());
  }

}
