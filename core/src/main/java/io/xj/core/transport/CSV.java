// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.transport;

import com.google.common.collect.Lists;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public abstract class CSV {
  public static Collection<String> split(String csv) {
    return Arrays.asList(csv.split(","));
  }

  public static String join(List<String> parts) {
    return String.join(",", parts);
  }

  public static String join(String[] parts) {
    return String.join(",", parts);
  }

  public static <E extends Enum<E>> String joinEnum(E[] objects) {
    List<String> objectStrings = Lists.newArrayList();
    for (Object obj : objects)
      if (Objects.nonNull(obj))
        objectStrings.add(obj.toString());
    return join(objectStrings);
  }
}
