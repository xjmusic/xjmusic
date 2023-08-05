// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.lib.music;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class IntervalTest {

  @Test
  public void TestInterval() {
    assertEquals(16, Interval.values().length);
  }

  @Test
  public void TestForAllIn() {
    Interval.forAll((Assert::assertNotNull));
  }

}
