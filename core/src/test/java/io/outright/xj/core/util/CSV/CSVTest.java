package io.outright.xj.core.util.CSV;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

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
