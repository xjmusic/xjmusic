// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.model.util;

import java.util.concurrent.ThreadLocalRandom;

/**
 TremendouslyRandom class provides extremely high quality selection of small sets of possibilities.
 */
public interface TremendouslyRandom {
  int GENERATE_SHIP_KEY_LIMIT_LOWER = 48; // numeral '0'
  int GENERATE_SHIP_KEY_LIMIT_UPPER = 122; // letter 'z'

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
   Make a tremendously random boolean selection based on probability

   @param probability 0 <= n < limit
   @return random integer n, where 0 <= n < limit
   */
  static Boolean booleanChanceOf(Double probability) {
    return ThreadLocalRandom.current().nextDouble(0, 1) <= probability;
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
   Generate an ship key of N length

   @param length of key to generate
   @return generated key
   */
  static String generateShipKey(int length) {
    return ThreadLocalRandom.current().ints(GENERATE_SHIP_KEY_LIMIT_LOWER, GENERATE_SHIP_KEY_LIMIT_UPPER + 1)
      .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
      .limit(length)
      .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
      .toString();
  }
}
