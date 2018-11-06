// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.util;// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.assertEquals;

public class ValueTest {
  @Test
  public void eitherOr_Double() {
    assertEquals(Double.valueOf(5.0), Value.eitherOr(Double.valueOf(5.0), null));
    assertEquals(Double.valueOf(5.0), Value.eitherOr(null, Double.valueOf(5.0)));
    assertEquals(Double.valueOf(5.0), Value.eitherOr(Double.valueOf(5.0), Double.valueOf(7.0)));
  }

  @Test
  public void eitherOr_String() {
    assertEquals("bing", Value.eitherOr("bing", null));
    assertEquals("bing", Value.eitherOr(null, "bing"));
    assertEquals("bing", Value.eitherOr("bing", "schwang"));
  }

  @Test
  public void inc() {
    assertEquals(BigInteger.valueOf(23L), Value.inc(BigInteger.valueOf(24L), -1));
    assertEquals(BigInteger.valueOf(9543L), Value.inc(BigInteger.valueOf(9000L), 543));
    assertEquals(BigInteger.valueOf(742L), Value.inc(BigInteger.valueOf(1000L), -258));
  }

  @Test
  public void dividedBy() {
    assertEquals(ImmutableSet.of(2, 8, 23, 31, 40), Value.dividedBy(2.0, ImmutableSet.of(4, 16, 62, 80, 46)));
    assertEquals(ImmutableSet.of(1, 6, 18, 24, 32), Value.dividedBy(2.5, ImmutableSet.of(4, 16, 62, 80, 46)));
  }

  @Test
  public void max() {
    assertEquals(BigInteger.valueOf(73), Value.max(ImmutableList.of(BigInteger.valueOf(21), BigInteger.valueOf(45), BigInteger.valueOf(73), BigInteger.valueOf(18))));
  }

  @Test
  public void ratio() {
    assertEquals(0.0, Value.ratio(0.0,  5.0), 0.01);
    assertEquals(0.6, Value.ratio(3.0,  5.0),0.01);
  }

}
