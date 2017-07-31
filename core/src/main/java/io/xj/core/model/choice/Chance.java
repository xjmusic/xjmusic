// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.model.choice;

import java.util.concurrent.ThreadLocalRandom;

public class Chance {

  // expect the result of nextGaussian() to always be between +/- this value
  private static final double GAUSSIAN_LIMIT = 6.5;

  /**
   Value normally distributed at random, normallyAround a base value

   @param base            to get values distributed normallyAround
   @param maxDistribution from center
   @return double
   */
  public static double normallyAround(double base, double maxDistribution) {
    return base + normalDistributionLimited(maxDistribution);
  }

  /**
   result distributed around 0, +/- max distribution

   @param maxDistribution from 0 to distribute, +/-
   @return random number, normally distributed around 0
   */
  private static double normalDistributionLimited(double maxDistribution) {
    return
      Math.max(-maxDistribution,
        Math.min(maxDistribution,
          ThreadLocalRandom.current().nextGaussian() *
            maxDistribution / GAUSSIAN_LIMIT));
  }

}
