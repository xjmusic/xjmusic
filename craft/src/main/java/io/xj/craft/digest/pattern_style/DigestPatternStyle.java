// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.craft.digest.pattern_style;

import com.google.common.math.StatsAccumulator;

import io.xj.craft.digest.Digest;
import io.xj.craft.digest.meme.impl.DigestMemesItem;

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

}
