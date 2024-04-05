// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.hub.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

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

}
