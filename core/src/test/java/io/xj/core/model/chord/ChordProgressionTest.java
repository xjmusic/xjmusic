package io.xj.core.model.chord;// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ChordProgressionTest {

  @Test
  public void isRedundantSubsetOfDescriptor() throws Exception {
    assertTrue(
      new ChordProgression("Major:7|Minor:5|Major:10|Minor")
        .isRedundantSubset(new ChordProgression("Major:7|Minor:5|Major"),
          1));

    assertFalse(
      new ChordProgression("Major:7|Minor:5|Major:10|Minor")
        .isRedundantSubset(new ChordProgression("Major:7|Minor"),
          1));

    assertTrue(
      new ChordProgression("Major:7|Minor:5|Major:10|Minor")
        .isRedundantSubset(new ChordProgression("Major:7|Minor"),
          2));

    assertTrue(
      new ChordProgression("Major:7|Minor:5|Major:10|Minor")
        .isRedundantSubset(new ChordProgression("Minor:5|Major"),
          2));

    assertFalse(
      new ChordProgression("Major:7|Minor:5|Major:10|Minor")
        .isRedundantSubset(new ChordProgression("Minor:5|Major"),
          1));

  }

  @Test
  public void size() throws Exception {
    assertEquals(4, new ChordProgression("Major:7|Minor:5|Major:10|Minor").size());
    assertEquals(2,new ChordProgression("Major:7|Minor").size());
    assertEquals(1,new ChordProgression("Minor Flat Nine").size());
    assertEquals(3,new ChordProgression("Major Seven:9|Major:7|Minor").size());
  }

}
