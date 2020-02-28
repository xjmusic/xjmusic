// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.core.isometry;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class IsometryTest {

  @Test
  public void ofStemmed() {
    Isometry result = Isometry.ofStemmed(ImmutableList.of("Smooth", "Catlike"));

    assertArrayEquals(new String[]{"smooth", "catlik"}, result.getSources().toArray());
  }

  @Test
  public void ofPhonetic() {
    Isometry result = Isometry.ofPhonetic(ImmutableList.of("Kick", "Snare"));

    assertArrayEquals(new String[]{"KK", "SNR"}, result.getSources().toArray());
  }

  @Test
  public void add() {
    Isometry result = Isometry.ofStemmed(ImmutableList.of("Smooth"));
    result.addStem("Catlike");

    assertArrayEquals(new String[]{"smooth", "catlik"}, result.getSources().toArray());
  }

  @Test
  public void getSourceStems() {
    List<String> result = Isometry.ofStemmed(ImmutableList.of("Intensity", "Cool", "Dark")).getSources();

    assertArrayEquals(new String[]{"intens", "cool", "dark"}, result.toArray());
  }


  @Test
  public void getConstellation() {
    assertEquals("catlik_smooth", Isometry.ofStemmed(ImmutableList.of("Smooth", "Catlike")).getConstellation());
    assertEquals("cool_dark_intens", Isometry.ofStemmed(ImmutableList.of("Intensity", "Cool", "Dark")).getConstellation());
    assertEquals("bam_flam_shim_wham", Isometry.ofStemmed(ImmutableList.of("Wham", "Bam", "Shim", "Shim", "Shim", "Flam")).getConstellation());
  }


}
