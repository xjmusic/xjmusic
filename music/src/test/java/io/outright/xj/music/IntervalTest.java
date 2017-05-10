// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.music;

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
