// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.transport;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class CSVTest {
  @Test
  public void split() throws Exception {
    assertEquals(
      Arrays.asList("one", "two", "three"),
      CSV.split("one,two,three"));
  }

  @Test
  public void splitProperSlug() throws Exception {
    assertEquals(
      Arrays.asList("One", "Two", "Three"),
      CSV.splitProperSlug("one,two,three"));
  }

  @Test
  public void join() throws Exception {
    assertEquals(
      "one,two,three",
      CSV.join(Arrays.asList("one", "two", "three")));
  }

}
