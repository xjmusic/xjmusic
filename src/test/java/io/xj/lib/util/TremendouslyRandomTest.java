// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.util;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
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
  public void beatOddsAgainstOne() {
    assertTrue(TremendouslyRandom.beatOddsAgainstOne(1));
    assertFalse(TremendouslyRandom.beatOddsAgainstOne(0));
    assertFalse(TremendouslyRandom.beatOddsAgainstOne(-1));
  }
}
