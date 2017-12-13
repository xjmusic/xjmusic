// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.util;// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.

import org.jooq.types.ULong;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

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
    assertEquals(ULong.valueOf(23), Value.inc(ULong.valueOf(24), -1));
    assertEquals(ULong.valueOf(9543), Value.inc(ULong.valueOf(9000), 543));
    assertEquals(ULong.valueOf(742), Value.inc(ULong.valueOf(1000), -258));
  }

}
