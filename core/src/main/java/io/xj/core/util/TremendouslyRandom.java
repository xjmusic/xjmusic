// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.util;

import java.util.concurrent.ThreadLocalRandom;

/**
 TremendouslyRandom class provides extremely high quality selection from small sets of possibilities.
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
    return ThreadLocalRandom.current().nextDouble((double) 0, limit);
  }

}
