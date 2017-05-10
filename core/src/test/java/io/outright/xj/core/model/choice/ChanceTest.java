// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.core.model.choice;

import io.outright.xj.core.model.choice.Chance;

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
          Chance.normallyAround(0, 10) // values up to +/- 10
        ))
        ]++;

    for (int c = 0; c < 11; c++)
      System.out.println(
        "# of values between " + String.valueOf(c) +
          " and " + String.valueOf(c + 1) +
          ": " + floorCounts[c]);

    // Assert that no value has exceeded 10
    assertEquals(0, floorCounts[10]);
  }

}
