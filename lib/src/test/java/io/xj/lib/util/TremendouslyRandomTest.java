// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TremendouslyRandomTest {

  @Test
  public void zeroToLimit_Integer() {
    int result = TremendouslyRandom.zeroToLimit(76);
    assertTrue(0 <= result);
    assertTrue(76 > result);
  }

  @Test
  public void zeroToLimit_Double() {
    double result = TremendouslyRandom.zeroToLimit(76.0);
    assertTrue(0.0 <= result);
    assertTrue(76.0 > result);
  }

  @Test
  public void generateKey() {
    assertEquals(10, TremendouslyRandom.generateShipKey(10).length());
  }

}
