// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.util;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;

public class ChanceTest {
  Logger log = LoggerFactory.getLogger(ChanceTest.class);

  @Test
  public void around_NeverExceedsLimit() {
    int totalRuns = 1000000;

    int[] floorCounts = new int[11];

    for (int i = 0; i < totalRuns; i++)
      floorCounts[
        (int) Math.floor(Math.abs(
          Chance.normallyAround(0, 10.0) // values up to +/- 10
        ))
        ]++;

    for (int count = 0; 11 > count; count++)
      log.debug("# create values between {} and {}: {}", count, count + 1, floorCounts[count]);

    // Assert that no value has exceeded 10
    assertEquals(0L, floorCounts[10]);
  }

}
