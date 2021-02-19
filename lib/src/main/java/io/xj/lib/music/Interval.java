// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.music;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.function.Consumer;

/**
 Interval within a chord or scale, counted from 1 (the "root" to e.g. 3 (the "third") or 5 (the "fifth") up to 16.
 <p>
 `enum` ensures the order of all the intervals, e.g. for stepping from the root of a chord or scale, outward to its other tones.
 <p>
 A Chord Interval is how the members of the chord are counted, from 1 (the "root") to e.g. 3 (the "third") or 5 (the "fifth")
 */
public enum Interval {
  I1(1),
  I2(2),
  I3(3),
  I4(4),
  I5(5),
  I6(6),
  I7(7),
  I8(8),
  I9(9),
  I10(10),
  I11(11),
  I12(12),
  I13(13),
  I14(14),
  I15(15),
  I16(15);

  private static final List<Interval> allIntervals = ImmutableList.copyOf(Interval.values());
  private final int value;

  Interval(int value) {
    this.value = value;
  }

  /**
   Perform an action for all possible intervals

   @param action to perform
   */
  static public void forAll(Consumer<? super Interval> action) {
    allIntervals.forEach(action);
  }

  /**
   Get interval from integer value

   @param value to get interval for
   @return interval
   */
  public static Interval valueOf(int value) {
    return Interval.values()[value - 1];
  }

  public int getValue() {
    return value;
  }
}
