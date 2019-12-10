// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.craft.chord;// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.

import com.google.common.collect.ImmutableSet;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ChordProgressionTest  {

  @Test
  public void isRedundantSubsetOfDescriptor() {
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
  public void size() {
    assertEquals(4, new ChordProgression("Major:7|Minor:5|Major:10|Minor").size());
    assertEquals(2, new ChordProgression("Major:7|Minor").size());
    assertEquals(1, new ChordProgression("Minor Flat Nine").size());
    assertEquals(3, new ChordProgression("Major Seven:9|Major:7|Minor").size());
  }

  @Test
  public void reversed() {
    assertEquals("10|Minor:5|Major:7|Minor:2|Major", new ChordProgression("2|Major:7|Minor:5|Major:10|Minor").reversed().toString());
    assertEquals("10|Minor", new ChordProgression("10|Minor").reversed().toString());
  }

  @Test
  public void isEquivalent() {
    ChordProgression subject = new ChordProgression("2|Major:7|Minor:5|Major:10|Minor");
    ChordProgression diff1 = new ChordProgression("6|Major:10|Minor:4|Minor");
    ChordProgression same1 = new ChordProgression("5|Major:6|Major:10|Minor:4|Minor");
    ChordProgression same2 = new ChordProgression("5|Major:6|Major:10|Minor:4|Minor");
    ChordProgression same3 = new ChordProgression("5|Major:6|Major:10|Minor:4|Minor");

    assertFalse(subject.isEquivalent(diff1));
    assertTrue(subject.isEquivalent(same1));
    assertTrue(subject.isEquivalent(same2));
    assertTrue(subject.isEquivalent(same3));
  }


  @Test
  public void scorePotentialSplice() {
    ChordProgression from = new ChordProgression("0|Major:1|Major:2|Major:3|Major:4|Major:5|Major");
    ChordProgression to = new ChordProgression("11|Major:10|Major:8|Major:7|Major:6|Major:5|Major");
    assertEquals(0.358, from.scorePotentialSplice(to, 8, 3), 0.1);
    assertEquals(0.516, from.scorePotentialSplice(to, 8, 4), 0.1);
    assertEquals(0.0, from.scorePotentialSplice(to, 8, 5), 0.1);
  }

  @Test
  public void spliceAtCollision_v1() {
    ChordProgression from = new ChordProgression("9|Major Seventh:2|Minor Seventh:7|Major Seventh Add Ninth:3|Major:9|Major:4|Major:1|Major:0|Major:4|Major:0|Major:9|Minor Sixth:4|Minor:4|Minor Seventh:4|Minor Sixth:2|Major:10|Diminished Major");
    ChordProgression to = new ChordProgression("1|Minor:6|Minor:9|Major:11|Minor Seventh:4|Major:1|Minor:7|Major:6|Major:11|Major:6|Major:11|Major:6|Major:11|Major:4|Major:1|Minor:7|Major");
    ChordProgression result = from.spliceAtCollision(to, ImmutableSet.of(4, 8, 16), 1);
    assertEquals("9|Major Seventh:2|Minor Seventh:7|Major Seventh Add Ninth:3|Major:9|Major:4|Major:1|Minor:7|Major", result.toString());
  }

  @Test
  public void spliceAtCollision_v2() {
    ChordProgression from = new ChordProgression("0|Minor Seventh:5|Major:2|Minor Sixth:9|Minor:5|Major:7|Minor Seventh:10|Major Add Ninth:8|Major Ninth:2|Minor Seventh:7|Major Seventh:9|Minor Seventh:7|Minor Seventh:9|Minor Seventh:2|Major Seventh:1|Minor Seventh:6|Minor Seventh");
    ChordProgression to = new ChordProgression("9|Major Seventh:2|Major Seventh:1|Minor Seventh:6|Minor Seventh:9|Major Seventh:2|Minor:10|Major:10|Major:5|Major:2|Minor:10|Major:10|Major:10|Major:10|Major:6|Major:9|Major Ninth");
    ChordProgression result = from.spliceAtCollision(to, ImmutableSet.of(4, 8, 16), 1);
    assertEquals("0|Minor Seventh:5|Major:2|Minor Sixth:9|Minor:5|Major:7|Minor Seventh:10|Major:10|Major:5|Major:2|Minor:10|Major:10|Major:10|Major:10|Major:6|Major:9|Major Ninth", result.toString());
  }

  @Test
  public void spliceAtCollision_v3() {
    ChordProgression from = new ChordProgression("7|Minor:5|Major:2|Minor Sixth:9|Minor:5|Major:2|Minor:7|Major:1|Minor Seventh Omit Fifth:0|Major:7|Major:1|Minor Seventh Omit Fifth:0|Major:0|Major:7|Minor:5|Major:5|Major");
    ChordProgression to = new ChordProgression("7|Major Seventh:3|Major Sixth:5|Major Sixth:5|Minor Sixth:7|Major Seventh:3|Major Sixth:5|Major Sixth:5|Minor Sixth:7|Major Seventh:3|Major Sixth:5|Major Sixth:5|Minor Sixth:7|Major Seventh:3|Major Sixth:5|Major Sixth:0|Minor Seventh");
    ChordProgression result = from.spliceAtCollision(to, ImmutableSet.of(4, 8, 16), 1);
    assertEquals("7|Minor:5|Major:2|Minor Sixth:9|Minor:5|Major:3|Major Sixth:5|Major Sixth:0|Minor Seventh", result.toString());
  }

  @Test
  public void spliceAtCollision_v4() {
    ChordProgression from = new ChordProgression("4|Major:11|Major:3|Minor:4|Major:4|Major:4|Major:7|Major:3|Diminished Major:7|Major:3|Diminished Major:7|Major:3|Diminished Major:7|Major:3|Diminished Major:7|Major:3|Diminished Major");
    ChordProgression to = new ChordProgression("7|Major:10|Major:8|Major:2|Major:9|Major:10|Major:8|Major:7|Major:3|Major:9|Major:0|Major:0|Major:11|Major:6|Major:8|Major:2|Major");
    ChordProgression result = from.spliceAtCollision(to, ImmutableSet.of(4, 8, 16), 1);
    assertEquals("4|Major:11|Major:3|Minor:4|Major:4|Major:4|Major:8|Major:2|Major", result.toString());
  }

  @Test
  public void spliceAtCollision_v5() {
    ChordProgression from = new ChordProgression("2|Major:8|Minor Seventh Omit Fifth:7|Major:0|Major:6|Major:0|Major:11|Major:6|Major:11|Major:0|Major:4|Major:6|Major:4|Major:7|Major:5|Major:2|Minor Seventh");
    ChordProgression to = new ChordProgression("0|Major:2|Major:0|Major:2|Minor:10|Major:6|Major:9|Major:11|Major:6|Major:4|Major:7|Major:3|Major:4|Major:1|Major:7|Major:1|Major");
    ChordProgression result = from.spliceAtCollision(to, ImmutableSet.of(4, 8, 16), 1);
    assertEquals("2|Major:8|Minor Seventh Omit Fifth:7|Major:1|Major", result.toString());
  }

  @Test
  public void spliceAtCollision_v6() {
    ChordProgression from = new ChordProgression("2|Major:10|Major:6|Major:11|Major:7|Major:9|Minor:9|Minor Seventh:7|Minor Seventh:2|Minor Seventh:7|Major Seventh:3|Major Sixth:5|Major Sixth:5|Major Seventh:0|Minor Seventh:5|Major Sixth:5|Major Seventh");
    ChordProgression to = new ChordProgression("2|Major:8|Major:2|Major:8|Major:7|Major:6|Major:11|Major:3|Major:9|Major Seventh:2|Minor Seventh:7|Major Seventh Add Ninth:0|Major:9|Major Seventh:2|Minor Seventh:7|Major Seventh Add Ninth:0|Minor Seventh");
    ChordProgression result = from.spliceAtCollision(to, ImmutableSet.of(4, 8, 16), 1);
    assertEquals("2|Major:10|Major:6|Major:11|Major:7|Major:6|Major:11|Major:3|Major:9|Major Seventh:2|Minor Seventh:7|Major Seventh Add Ninth:0|Major:9|Major Seventh:2|Minor Seventh:7|Major Seventh Add Ninth:0|Minor Seventh", result.toString());
  }

  @Test
  public void spliceAtCollision_v7() {
    ChordProgression from = new ChordProgression("5|Major:2|Minor Sixth:9|Minor:0|Major:0|Major:5|Major:9|Major:11|Major:3|Major:4|Major:6|Major:9|Major:4|Major:0|Major:6|Major:1|Major");
    ChordProgression to = new ChordProgression("5|Major:3|Major:2|Major:4|Minor Seventh:2|Minor Seventh:7|Major Seventh Add Ninth:0|Minor Seventh:5|Major Sixth:0|Minor Seventh:0|Major:2|Major:11|Major:0|Major:1|Major:4|Major:0|Major");
    ChordProgression result = from.spliceAtCollision(to, ImmutableSet.of(4, 8, 16), 1);
    assertEquals("5|Major:2|Minor Sixth:9|Minor:0|Major:0|Major:5|Major:9|Major:11|Major:3|Major:4|Major:6|Major:9|Major:4|Major:1|Major:4|Major:0|Major", result.toString());
  }

  @Test
  public void spliceAtCollision_v8() {
    ChordProgression from = new ChordProgression("0|Minor Seventh:0|Major:0|Major:4|Major:7|Major:3|Diminished Major:7|Major:10|Major:10|Major:9|Major:0|Major:9|Major:4|Major:0|Major:7|Minor Seventh:10|Major Add Ninth");
    ChordProgression to = new ChordProgression("3|Major:7|Major:1|Major:0|Major:11|Major:6|Major:0|Major:7|Minor:5|Major:0|Major:0|Major:7|Major:2|Major:7|Major:1|Major:6|Major");
    ChordProgression result = from.spliceAtCollision(to, ImmutableSet.of(4, 8, 16), 1);
    assertEquals("0|Minor Seventh:0|Major:0|Major:7|Major:2|Major:7|Major:1|Major:6|Major", result.toString());
  }

  @Test
  public void spliceAtCollision_v9() {
    ChordProgression from = new ChordProgression("10|Major:5|Major:10|Major:9|Major:3|Major:9|Major:10|Major:5|Major:2|Major:0|Major:11|Major:4|Major:4|Major:2|Major:7|Major:2|Major");
    ChordProgression to = new ChordProgression("5|Major:9|Minor:9|Minor Seventh:7|Minor Seventh:10|Major Add Ninth:8|Major Ninth:2|Minor Seventh:7|Major Seventh Add Ninth:0|Major:6|Major:2|Major:10|Major:5|Major:2|Major:8|Minor Seventh Omit Fifth:7|Major");
    ChordProgression result = from.spliceAtCollision(to, ImmutableSet.of(4, 8, 16), 1);
    assertEquals("10|Major:5|Major:10|Major:9|Major:3|Major:9|Major:10|Major:5|Major:2|Major:0|Major:11|Major:4|Major:4|Major:2|Major:8|Minor Seventh Omit Fifth:7|Major", result.toString());
  }

  @Test
  public void spliceAtCollision_v10() {
    ChordProgression from = new ChordProgression("0|Major:9|Minor Sixth:4|Minor:4|Minor Seventh:2|Minor Seventh:7|Major Seventh:3|Major Sixth:5|Major Sixth:5|Major:3|Major:9|Major Seventh:2|Minor Seventh:7|Minor Seventh:0|Major:0|Minor Seventh:5|Major Sixth");
    ChordProgression to = new ChordProgression("9|Minor:9|Minor Seventh:7|Minor Seventh:2|Minor Seventh:7|Major Seventh Add Ninth:0|Major:9|Major Seventh:2|Major:4|Minor Seventh:2|Major:9|Major:4|Major:6|Major:1|Major:9|Major:6|Major");
    ChordProgression result = from.spliceAtCollision(to, ImmutableSet.of(4, 8, 16), 1);
    assertEquals("0|Major:9|Minor Sixth:4|Minor:4|Minor Seventh:2|Minor Seventh:7|Major Seventh:3|Major Sixth:5|Major Sixth:5|Major:2|Major:9|Major:4|Major:6|Major:1|Major:9|Major:6|Major", result.toString());
  }

  @Test
  public void spliceAtCollision_v11() {
    ChordProgression from = new ChordProgression("9|Major:0|Major:0|Major:0|Minor Seventh:0|Major:9|Major Seventh:2|Minor:0|Major:0|Major:10|Major:7|Major:10|Major:5|Major:5|Major:7|Minor Seventh:10|Major Add Ninth");
    ChordProgression to = new ChordProgression("9|Major:0|Major:11|Major:5|Major:0|Major:9|Major Seventh:2|Minor:7|Major:7|Major:6|Major:0|Major:9|Major:2|Major:1|Major:6|Major:9|Major Ninth");
    ChordProgression result = from.spliceAtCollision(to, ImmutableSet.of(4, 8, 16), 1);
    assertEquals("9|Major:0|Major:0|Major:0|Minor Seventh:0|Major:9|Major Seventh:2|Minor:7|Major:7|Major:6|Major:0|Major:9|Major:2|Major:1|Major:6|Major:9|Major Ninth", result.toString());
  }

  @Test
  public void spliceAtCollision_v12() {
    ChordProgression from = new ChordProgression("11|Major:7|Major:3|Major:2|Major:11|Major:4|Major:2|Major:0|Major:11|Major:1|Major:4|Major:2|Major:8|Major:7|Minor Seventh:9|Minor Seventh:7|Minor Seventh");
    ChordProgression to = new ChordProgression("2|Major:4|Major:4|Major:4|Major:6|Major:1|Major:9|Major:9|Major:3|Major:4|Major:1|Minor:7|Major:1|Major:7|Major:1|Major:0|Major");
    ChordProgression result = from.spliceAtCollision(to, ImmutableSet.of(4, 8, 16), 1);
    assertEquals("11|Major:7|Major:3|Major:2|Major:11|Major:4|Major:2|Major:0|Major:11|Major:1|Major:4|Major:2|Major:8|Major:7|Major:1|Major:0|Major", result.toString());
  }

  @Test
  public void spliceAtCollision_v13() {
    ChordProgression from = new ChordProgression("2|Major:5|Major:0|Minor Seventh:0|Minor Seventh:5|Major Sixth:5|Major:2|Minor Sixth:9|Minor:9|Minor Seventh:4|Minor Seventh:2|Minor Seventh:7|Major Seventh:0|Major:9|Major Seventh:2|Major:0|Major");
    ChordProgression to = new ChordProgression("6|Major:0|Major:11|Major:0|Major:0|Major:11|Major:3|Major:4|Major:2|Major:1|Major:0|Major:7|Minor:5|Major:7|Minor Seventh:2|Minor Seventh:7|Major Seventh");
    ChordProgression result = from.spliceAtCollision(to, ImmutableSet.of(4, 8, 16), 1);
    assertEquals("2|Major:5|Major:0|Minor Seventh:0|Minor Seventh:5|Major:7|Minor Seventh:2|Minor Seventh:7|Major Seventh", result.toString());
  }

  @Test
  public void spliceAtCollision_v14() {
    ChordProgression from = new ChordProgression("11|Major:9|Major:5|Major:7|Major:3|Diminished Major:7|Major:7|Major:2|Major:11|Major:1|Major:0|Major:7|Major:7|Major:0|Major:0|Major:4|Major");
    ChordProgression to = new ChordProgression("0|Major:1|Major:4|Major:1|Minor:7|Major:3|Diminished Major:7|Major:3|Major:2|Major:10|Major:10|Major:6|Major:0|Major:0|Minor Seventh:5|Major Sixth:0|Minor Seventh");
    ChordProgression result = from.spliceAtCollision(to, ImmutableSet.of(4, 8, 16), 1);
    assertEquals("11|Major:9|Major:5|Major:7|Major:3|Diminished Major:7|Major:7|Major:3|Major:2|Major:10|Major:10|Major:6|Major:0|Major:0|Minor Seventh:5|Major Sixth:0|Minor Seventh", result.toString());
  }

  @Test
  public void spliceAtCollision_v15() {
    ChordProgression from = new ChordProgression("9|Major:11|Minor Seventh:4|Major:0|Major:7|Minor Seventh:9|Minor Seventh:7|Minor Seventh:10|Major Add Ninth:8|Major Ninth:2|Minor Seventh:7|Major Seventh Add Ninth:0|Minor Seventh:5|Major Sixth:5|Major Seventh:0|Minor Seventh:0|Minor Seventh");
    ChordProgression to = new ChordProgression("6|Major:9|Major Ninth:0|Major:0|Major:11|Major:4|Major:11|Major:5|Major:2|Major:4|Minor Seventh:2|Major:7|Major:9|Minor:0|Major:2|Minor:0|Major");
    ChordProgression result = from.spliceAtCollision(to, ImmutableSet.of(4, 8, 16), 1);
    assertEquals("9|Major:11|Minor Seventh:4|Major:0|Major:11|Major:4|Major:11|Major:5|Major:2|Major:4|Minor Seventh:2|Major:7|Major:9|Minor:0|Major:2|Minor:0|Major", result.toString());
  }


}
