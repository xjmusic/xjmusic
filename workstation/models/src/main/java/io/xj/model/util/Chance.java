// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.model.util;

import java.util.concurrent.ThreadLocalRandom;

public interface Chance {

  // expect the result of nextGaussian() to always be between +/- this value
  double GAUSSIAN_LIMIT = 6.5;

  /**
   Value normally distributed at random, normallyAround a base value

   @param base            to get values distributed normallyAround
   @param maxDistribution of center
   @return double
   */
  static double normallyAround(double base, double maxDistribution) {
    return base + normalDistributionLimited(maxDistribution);
  }

  /**
   result distributed around 0, +/- max distribution

   @param maxDistribution of 0 to distribute, +/-
   @return random number, normally distributed around 0
   */
  static double normalDistributionLimited(double maxDistribution) {
    return
      Math.max(-maxDistribution,
        Math.min(maxDistribution,
          ThreadLocalRandom.current().nextGaussian() *
            maxDistribution / GAUSSIAN_LIMIT));
  }

}
