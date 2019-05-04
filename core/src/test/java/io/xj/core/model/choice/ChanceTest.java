// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.choice;

import io.xj.core.util.Chance;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ChanceTest {

  @Test
  public void around_NeverExceedsLimit() throws Exception {
    int totalRuns = 1000000;

    int[] floorCounts = new int[11];

    for (int i = 0; i < totalRuns; i++)
      floorCounts[
        (int) Math.floor(Math.abs(
          Chance.normallyAround((double) 0, 10.0) // values up to +/- 10
        ))
        ]++;

    for (int c = 0; c < 11; c++)
      System.out.println(
        "# of values between " + String.valueOf(c) +
          " and " + String.valueOf(c + 1) +
          ": " + floorCounts[c]);

    // Assert that no value has exceeded 10
    assertEquals(0L, floorCounts[10]);
  }

}
