// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.util;// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.

import com.google.common.collect.ImmutableMap;

import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ValueTest {
  @Test
  public void eitherOr_Double() throws Exception {
    assertEquals(Double.valueOf(5), Value.eitherOr(Double.valueOf(5), null));
    assertEquals(Double.valueOf(5), Value.eitherOr(null, Double.valueOf(5)));
    assertEquals(Double.valueOf(5), Value.eitherOr(Double.valueOf(5), Double.valueOf(7)));
  }

  @Test
  public void eitherOr_String() throws Exception {
    assertEquals("bing", Value.eitherOr("bing", null));
    assertEquals("bing", Value.eitherOr(null, "bing"));
    assertEquals("bing", Value.eitherOr("bing", "schwang"));
  }

  @Test
  public void inc() throws Exception {
    assertEquals(BigInteger.valueOf(23), Value.inc(BigInteger.valueOf(24), -1));
    assertEquals(BigInteger.valueOf(9543), Value.inc(BigInteger.valueOf(9000), 543));
    assertEquals(BigInteger.valueOf(742), Value.inc(BigInteger.valueOf(1000), -258));
  }

}
