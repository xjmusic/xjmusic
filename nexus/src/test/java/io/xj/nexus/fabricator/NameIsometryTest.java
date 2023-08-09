// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.fabricator;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class NameIsometryTest {

  @Test
  public void of_List() {
    NameIsometry result = NameIsometry.ofEvents(List.of(
      "Kick",
      "Snare"
    ));

    assertArrayEquals(new String[]{"KK", "SNR"}, result.getSources().toArray());
  }

  @Test
  public void add() {
    NameIsometry result = NameIsometry.ofEvents(List.of(
      "Kick"
    ));
    result.add("Snare");

    assertArrayEquals(new String[]{"KK", "SNR"}, result.getSources().toArray());
  }

  @Test
  public void getSourceStems() {
    Set<String> result = NameIsometry.ofEvents(List.of(
      "TomHigh",
      "TomLow",
      "Tom"
    )).getSources();

    assertArrayEquals(new String[]{"TM", "TMH", "TML"}, result.stream().sorted().toArray());
  }

  @Test
  public void similarity() {
    assertEquals(300.0, NameIsometry.similarity("KICK", "KICK"), 0.1);
    assertEquals(300.0, NameIsometry.similarity("KICK", "KCK"), 0.1);
    assertEquals(300.0, NameIsometry.similarity("HIHATCLOSED", "HIHATCL"), 0.1);
    assertEquals(0.0, NameIsometry.similarity("ONE", "TWO"), 0.1);
  }

}
