// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.core.util;// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class TremendouslyRandomTest {

  @Test
  public void zeroToLimit_Integer() throws Exception {
    Integer result = TremendouslyRandom.zeroToLimit(76);
    assertTrue(0 <= result);
    assertTrue(76 > result);
  }

  @Test
  public void zeroToLimit_Double() throws Exception {
    Double result = TremendouslyRandom.zeroToLimit(76.0);
    assertTrue(0.0 <= result);
    assertTrue(76.0 > result);
  }

}
