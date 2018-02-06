// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.craft.digest.pattern_style;

import com.google.common.collect.Multiset;
import com.google.common.math.StatsAccumulator;

import io.xj.craft.digest.Digest;

public interface DigestPatternStyle extends Digest {

  /**
   @return main-type pattern count-of-phases-per-pattern statistics
   */
  StatsAccumulator getMainPhasesPerPatternStats();

  /**
   @return main-type pattern histogram of count-of-phases-per-pattern
   */
  Multiset<Integer> getMainPhasesPerPatternHistogram();

  /**
   @return main-type pattern total-per-phase statistics
   */
  StatsAccumulator getMainPhaseTotalStats();

  /**
   @return histogram of main-type pattern phase totals
   */
  Multiset<Integer> getMainPhaseTotalHistogram();

  /**
   @return histogram of main-type pattern chord spacing
   */
  Multiset<Double> getMainChordSpacingHistogram();
}
