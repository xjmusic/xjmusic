// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.core.util;// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.

import org.jooq.types.ULong;

import org.junit.Test;

import static org.junit.Assert.*;

public class ValueTest {

  @Test
  public void inc() throws Exception {
    assertEquals(ULong.valueOf(23),Value.inc(ULong.valueOf(24),-1));
    assertEquals(ULong.valueOf(9543),Value.inc(ULong.valueOf(9000),543));
    assertEquals(ULong.valueOf(742),Value.inc(ULong.valueOf(1000),-258));
  }

}
