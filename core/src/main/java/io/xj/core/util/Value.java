// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.util;

import java.math.BigInteger;
import java.util.Objects;

public class Value {

  /**
   Increment a BigInteger by an integer

   @return incremented base
    @param base  to begin with
   @param delta to increment base
   */
  public static BigInteger inc(BigInteger base, int delta) {
    return base.add(BigInteger.valueOf(delta));
  }

  /**
   Return the first value if it's non-null, else the second

   @param d1 to check if non-null and return
   @param d2 to default to, if s1 is null
   @return s1 if non-null, else s2
   */
  public static Double eitherOr(Double d1, Double d2) {
    if (Objects.nonNull(d1) && !d1.isNaN() && !d1.equals(0d))
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
  public static String eitherOr(String s1, String s2) {
    if (Objects.nonNull(s1) && s1.length() > 0)
      return s1;
    else
      return s2;
  }

}
