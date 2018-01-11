// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.music;

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
