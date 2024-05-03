// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.music;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
