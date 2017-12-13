// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.transport;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class CSVTest {
  @Test
  public void split() throws Exception {
    List<String> expected = Arrays.asList("one", "two", "three");
    Collection<String> actual = CSV.split("one,two,three");
    assert expected.equals(actual);
  }

  @Test
  public void join() throws Exception {
    String expected = "one,two,three";
    String actual = CSV.join(Arrays.asList("one", "two", "three"));
    assert expected.equals(actual);
  }

}
