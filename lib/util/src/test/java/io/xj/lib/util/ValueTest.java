// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.util;// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.

import com.google.common.collect.ImmutableSet;
import io.xj.lib.util.Value;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ValueTest {
  @Test
  public void eitherOr_Double() {
    assertEquals(Double.valueOf(5.0), Value.eitherOr(5.0, null));
    assertEquals(Double.valueOf(5.0), Value.eitherOr(null, 5.0));
    assertEquals(Double.valueOf(5.0), Value.eitherOr(5.0, 7.0));
  }

  @Test
  public void eitherOr_String() {
    assertEquals("bing", Value.eitherOr("bing", null));
    assertEquals("bing", Value.eitherOr(null, "bing"));
    assertEquals("bing", Value.eitherOr("bing", "schwang"));
  }

  @Test
  public void dividedBy() {
    assertEquals(ImmutableSet.of(2, 8, 23, 31, 40), Value.dividedBy(2.0, ImmutableSet.of(4, 16, 62, 80, 46)));
    assertEquals(ImmutableSet.of(1, 6, 18, 24, 32), Value.dividedBy(2.5, ImmutableSet.of(4, 16, 62, 80, 46)));
  }

  @Test
  public void ratio() {
    assertEquals(0.0, Value.ratio(0.0, 5.0), 0.01);
    assertEquals(0.6, Value.ratio(3.0, 5.0), 0.01);
  }

  @Test
  public void isInteger() {
    assertEquals(false, Value.isInteger("a"));
    assertEquals(false, Value.isInteger("125a"));
    assertEquals(true, Value.isInteger("377"));
    assertEquals(false, Value.isInteger("237.1"));
    assertEquals(true, Value.isInteger("100000045"));
    assertEquals(false, Value.isInteger(" 97"));
    assertEquals(false, Value.isInteger(" 27773"));
    assertEquals(true, Value.isInteger("32"));
  }
}
