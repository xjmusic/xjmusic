// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.craft.digest;

import com.google.common.collect.Multiset;
import com.google.common.math.StatsAccumulator;

public interface DigestProgramStyle extends Digest {

  /**
   @return main-type program count-of-sequences-per-sequence statistics
   */
  StatsAccumulator getMainSequencesPerProgramStats();

  /**
   @return main-type program histogram of count-of-sequences-per-sequence
   */
  Multiset<Integer> getMainSequencesPerProgramHistogram();

  /**
   @return main-type program total-per-sequence statistics
   */
  StatsAccumulator getMainSequenceTotalStats();

  /**
   @return histogram of main-type program sequence totals
   */
  Multiset<Integer> getMainSequenceTotalHistogram();

  /**
   @return histogram of main-type program chord spacing
   */
  Multiset<Double> getMainChordSpacingHistogram();
}
