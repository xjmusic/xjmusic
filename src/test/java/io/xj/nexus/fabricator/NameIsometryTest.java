// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.fabricator;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class NameIsometryTest {

  @Test
  public void of_List() {
    NameIsometry result = NameIsometry.ofEvents(ImmutableList.of(
      "Kick",
      "Snare"
    ));

    assertArrayEquals(new String[]{"KK", "SNR"}, result.getSources().toArray());
  }

  @Test
  public void add() {
    NameIsometry result = NameIsometry.ofEvents(ImmutableList.of(
      "Kick"
    ));
    result.add("Snare");

    assertArrayEquals(new String[]{"KK", "SNR"}, result.getSources().toArray());
  }

  @Test
  public void getSourceStems() {
    Set<String> result = NameIsometry.ofEvents(ImmutableList.of(
      "TomHigh",
      "TomLow",
      "Tom"
    )).getSources();

    assertArrayEquals(new String[]{"TM", "TMH", "TML"}, result.stream().sorted().toArray());
  }

}
