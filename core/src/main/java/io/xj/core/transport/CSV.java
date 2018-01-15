// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.transport;

import io.xj.core.util.Text;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public interface CSV {
  static Collection<String> split(String csv) {
    return Arrays.asList(csv.split(","));
  }

  static Collection<String> splitProperSlug(String csv) {
    List<String> items = Arrays.asList(csv.split(","));
    ImmutableList.Builder<String> slugs = new ImmutableList.Builder<>();
    items.forEach((item) -> {
      if (Objects.nonNull(item) && !item.isEmpty())
        slugs.add(Text.toProperSlug(item));
    });
    return slugs.build();
  }

  static String join(Collection<String> parts) {
    return String.join(",", parts);
  }

  static String join(String[] parts) {
    return String.join(",", parts);
  }

  static <E extends Enum<E>> String joinEnum(E[] objects) {
    List<String> objectStrings = Lists.newArrayList();
    for (Object obj : objects)
      if (Objects.nonNull(obj))
        objectStrings.add(obj.toString());
    return join(objectStrings);
  }
}
