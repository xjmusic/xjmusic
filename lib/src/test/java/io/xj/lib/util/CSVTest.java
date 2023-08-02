// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.lib.util;

import java.util.Map;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CSVTest {
  @Test
  public void split() {
    assertEquals(
      Arrays.asList("one", "two", "three"),
      CSV.split("one,two,three"));
  }

  @Test
  public void splitUUIDs() {
    assertEquals(
      Arrays.asList(
        UUID.fromString("e33e4c97-49f5-4fa8-85bf-3f0618eca351"),
        UUID.fromString("e0e6c13c-9f84-4641-b4e5-796af7100864"),
        UUID.fromString("cc7a6a7a-113e-44bb-a892-29a7a5610945")
      ),
      CSV.splitUUIDs("e33e4c97-49f5-4fa8-85bf-3f0618eca351,e0e6c13c-9f84-4641-b4e5-796af7100864,cc7a6a7a-113e-44bb-a892-29a7a5610945"));
  }

  @Test
  public void split_null() {
    assertEquals(
      List.of(),
      CSV.split(null));
  }

  @Test
  public void split_empty() {
    assertEquals(
      List.of(),
      CSV.split(""));
  }

  @Test
  public void split_alsoTrims() {
    assertEquals(
      Arrays.asList("one", "two", "three"),
      CSV.split("one, two, three"));
  }

  @Test
  public void splitProperSlug() {
    assertEquals(
      Arrays.asList("One", "Two", "Three"),
      CSV.splitProperSlug("one,two,three"));
  }

  @Test
  public void join() {
    assertEquals(
      "one, two, three",
      CSV.join(Arrays.asList("one", "two", "three")));
  }

  @Test
  public void from_keyValuePairs() {
    var result = CSV.from(Map.of("one", "1", "two", "2", "three", "3"));

    // can't use this because map has no guaranteed order: assertEquals("one=1, two=2, three=3");
    assertEquals(21, result.length());
    assertTrue(result.contains("one=1"));
    assertTrue(result.contains("two=2"));
    assertTrue(result.contains("three=3"));
  }

  @Test
  public void prettyFrom() {
    assertEquals(
      "One, Two, or Three",
      CSV.prettyFrom(Arrays.asList("One", "Two", "Three"), "or"));
  }
}
