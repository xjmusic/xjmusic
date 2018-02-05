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
    assertEquals(2, new ChordProgression("Major:7|Minor").size());
    assertEquals(1, new ChordProgression("Minor Flat Nine").size());
    assertEquals(3, new ChordProgression("Major Seven:9|Major:7|Minor").size());
  }

  @Test
  public void reversed() throws Exception {
    assertEquals("10|Minor:5|Major:7|Minor:2|Major", new ChordProgression("2|Major:7|Minor:5|Major:10|Minor").reversed().toString());
    assertEquals("10|Minor", new ChordProgression("10|Minor").reversed().toString());
  }

  @Test
  public void reversed_retainsSpacingAndTotal() throws Exception {
    ChordProgression from = new ChordProgression("2|Major:7|Minor:5|Major:10|Minor");
    from.setTotal(16);
    from.setSpacing(2);

    ChordProgression subject = from.reversed();

    assertEquals(16, subject.getTotal());
    assertEquals(2, subject.getSpacing());
  }

  @Test
  public void phaseTotal() throws Exception {
    ChordProgression subject = new ChordProgression("2|Major:7|Minor:5|Major:10|Minor");
    subject.setTotal(2);
    assertEquals(2, subject.getTotal());
  }

  @Test
  public void chordSpacing() throws Exception {
    ChordProgression subject = new ChordProgression("2|Major:7|Minor:5|Major:10|Minor");
    subject.setSpacing(2);
    assertEquals(2, subject.getSpacing());
  }

  @Test
  public void hasSameTotalSpacingChords() throws Exception {
    ChordProgression subject = new ChordProgression("2|Major:7|Minor:5|Major:10|Minor");
    subject.setSpacing(2);
    subject.setTotal(2);
    ChordProgression same = new ChordProgression("5|Major:6|Major:10|Minor:4|Minor");
    same.setSpacing(2);
    same.setTotal(2);
    ChordProgression diff1 = new ChordProgression("6|Major:10|Minor:4|Minor");
    diff1.setSpacing(2);
    diff1.setTotal(2);
    ChordProgression diff2 = new ChordProgression("5|Major:6|Major:10|Minor:4|Minor");
    diff2.setSpacing(3);
    diff2.setTotal(2);
    ChordProgression diff3 = new ChordProgression("5|Major:6|Major:10|Minor:4|Minor");
    diff3.setSpacing(2);
    diff3.setTotal(4);

    assertTrue(subject.hasSameTotalSpacingChords(same));
    assertFalse(subject.hasSameTotalSpacingChords(diff1));
    assertFalse(subject.hasSameTotalSpacingChords(diff2));
    assertFalse(subject.hasSameTotalSpacingChords(diff3));
  }

}
