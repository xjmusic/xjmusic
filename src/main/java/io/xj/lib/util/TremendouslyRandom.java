// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.util;

import java.util.concurrent.ThreadLocalRandom;

/**
 TremendouslyRandom class provides extremely high quality selection of small sets of possibilities.
 */
public interface TremendouslyRandom {

  /**
   Make a tremendously random selection of an integer n, where 0 <= n < limit

   @param limit 0 <= n < limit
   @return random integer n, where 0 <= n < limit
   */
  static Integer zeroToLimit(Integer limit) {
    if (0 >= limit) return 0;
    return ThreadLocalRandom.current().nextInt(0, limit);
  }

  /**
   Make a tremendously random selection of an double n, where 0 <= n < limit

   @param limit 0 <= n < limit
   @return random double n, where 0 <= n < limit
   */
  static Double zeroToLimit(Double limit) {
    if ((double) 0 >= limit) return 0.0;
    return ThreadLocalRandom.current().nextDouble(0, limit);
  }

  /**
   Put N marbles in the bag, with one marble set to true, and the others set to false

   @param odds against one, or the total number of marbles, of which one is true
   @return a marble from the bag
   */
  static boolean beatOddsAgainstOne(int odds) {
    if (0 >= odds) return false;
    return zeroToLimit(odds) == 0;
  }

  /**
   Return true if we hit the given % chance

   @param percent to test
   @return true if the odds hit
   */
  static boolean beatOddsPercent(int percent) {
    return percent >= zeroToLimit(100);
  }
}
