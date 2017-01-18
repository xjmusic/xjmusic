package io.outright.xj.core.util.CSV;

import java.util.Arrays;
import java.util.List;

public abstract class CSV {
  public static List<String> split(String csv) {
    return Arrays.asList(csv.split(","));
  }

  public static String join(List<String> parts) {
    return String.join(",", parts);
  }

  public static String join(String[] parts) {
    return String.join(",", parts);
  }
}
