// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.digest.sequence_style;

import com.google.common.collect.Multiset;
import com.google.common.math.StatsAccumulator;

import io.xj.craft.digest.Digest;

public interface DigestSequenceStyle extends Digest {

  /**
   @return main-type sequence count-of-patterns-per-sequence statistics
   */
  StatsAccumulator getMainPatternsPerSequenceStats();

  /**
   @return main-type sequence histogram of count-of-patterns-per-sequence
   */
  Multiset<Integer> getMainPatternsPerSequenceHistogram();

  /**
   @return main-type sequence total-per-pattern statistics
   */
  StatsAccumulator getMainPatternTotalStats();

  /**
   @return histogram of main-type sequence pattern totals
   */
  Multiset<Integer> getMainPatternTotalHistogram();

  /**
   @return histogram of main-type sequence chord spacing
   */
  Multiset<Double> getMainChordSpacingHistogram();
}
