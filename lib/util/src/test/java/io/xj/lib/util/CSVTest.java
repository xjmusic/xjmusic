// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.util;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class CSVTest {
  @Test
  public void split() {
    assertEquals(
      Arrays.asList("one", "two", "three"),
      CSV.split("one,two,three"));
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
      "one,two,three",
      CSV.join(Arrays.asList("one", "two", "three")));
  }

  @Test
  public void from_keyValuePairs() {
    assertEquals("one=1,two=2,three=3",
      CSV.from(ImmutableMap.of("one", "1", "two", "2", "three", "3")));
  }

  @Test
  public void prettyFrom() {
    assertEquals(
      "One, Two, or Three",
      CSV.prettyFrom(Arrays.asList("One", "Two", "Three"), "or"));
  }
}
