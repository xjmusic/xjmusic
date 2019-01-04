// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.util;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public interface Value {

  /**
   Increment a BigInteger by an integer

   @param base  to begin with
   @param delta to increment base
   @return incremented base
   */
  static BigInteger inc(BigInteger base, int delta) {
    return base.add(BigInteger.valueOf(delta));
  }

  /**
   Return the first value if it's non-null, else the second

   @param d1 to check if non-null and return
   @param d2 to default to, if s1 is null
   @return s1 if non-null, else s2
   */
  static Double eitherOr(Double d1, Double d2) {
    if (Objects.nonNull(d1) && !d1.isNaN() && !Objects.equals(d1, 0.0d))
      return d1;
    else
      return d2;
  }

  /**
   Return the first value if it's non-null, else the second

   @param s1 to check if non-null and return
   @param s2 to default to, if s1 is null
   @return s1 if non-null, else s2
   */
  static String eitherOr(String s1, String s2) {
    if (Objects.nonNull(s1) && !s1.isEmpty())
      return s1;
    else
      return s2;
  }

  /**
   Divide a set of integers by a double and return the divided set

   @param divisor   to divide by
   @param originals to divide
   @return divided originals
   */
  static Set<Integer> dividedBy(Double divisor, Set<Integer> originals) {
    return originals.stream().map(original -> (int) Math.floor(original / divisor)).collect(Collectors.toSet());
  }

  /**
   Return the maximum from a set of values

   @param values to return maximum of
   @return maximum value
   */
  static BigInteger max(List<BigInteger> values) {
    BigInteger max = BigInteger.ZERO;
    for (BigInteger value : values)
      if (Objects.equals(1, value.compareTo(max)))
        max = value;
    return max;
  }

  /**
   Calculate ratio (from 0 to 1) within a zero-to-N limit

   @param value to calculate radio of
   @param limit N where ratio will be calculated based on zero-to-N
   @return ratio between 0 and 1
   */
  static double ratio(double value, double limit) {
    return Math.max(Math.min(1, value / limit), 0);
  }

  /**
   Is value greater than or equal to zero?
   @param value to check
   @return true if greater than or equal to zero
   */
  static boolean isGreaterThanOrEqualToZero(BigInteger value) {
    return 0 >= BigInteger.ZERO.compareTo(value);
  }
}
