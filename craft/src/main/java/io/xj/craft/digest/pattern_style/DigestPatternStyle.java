// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.craft.digest.pattern_style;

import com.google.common.math.StatsAccumulator;

import io.xj.craft.digest.Digest;

import java.util.Map;

public interface DigestPatternStyle extends Digest {

  /**
   @return count-of-phases-per-pattern statistics
   */
  StatsAccumulator getMainPhasesPerPattern();

  /**
   @return total-per-phase statistics
   */
  StatsAccumulator getMainPhaseTotal();

  /**
   @return count of each phase total (value # of times a phase appeared with a total of N key)
   */
  Map<Integer, Integer> getMainPhaseTotalCount();

}
