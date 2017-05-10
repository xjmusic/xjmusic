// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.core.util;

import org.jooq.types.ULong;

import java.math.BigInteger;

public class Value {

  /**
   Increment a ULong by an integer

   @param base  to begin with
   @param delta to increment base
   @return incremented base
   */
  public static ULong inc(ULong base, int delta) {
    return ULong.valueOf(base.toBigInteger().add(BigInteger.valueOf(delta)));
  }

}
