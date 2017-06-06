// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.transport;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class CSVTest {
  @Test
  public void split() throws Exception {
    List<String> expected = Arrays.asList("one", "two", "three");
    List<String> actual = CSV.split("one,two,three");
    assert expected.equals(actual);
  }

  @Test
  public void join() throws Exception {
    String expected = "one,two,three";
    String actual = CSV.join(Arrays.asList("one", "two", "three"));
    assert expected.equals(actual);
  }

}
