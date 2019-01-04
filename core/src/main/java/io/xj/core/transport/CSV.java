// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.transport;

import com.google.common.collect.ImmutableList;
import io.xj.core.model.entity.Entity;
import io.xj.core.util.Text;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public interface CSV {
  static Collection<String> split(String csv) {
    return Arrays.asList(csv.split(","));
  }

  static Collection<String> splitProperSlug(String csv) {
    List<String> items = Arrays.asList(csv.split(","));
    ImmutableList.Builder<String> slugs = new ImmutableList.Builder<>();
    items.stream().filter(item -> Objects.nonNull(item) && !item.isEmpty()).map(Text::toProperSlug).forEach(slugs::add);
    return slugs.build();
  }

  static String join(Collection<String> parts) {
    return String.join(",", parts);
  }

  @SafeVarargs
  static <E extends Enum<E>> String joinEnum(E... objects) {
    List<String> objectStrings = Arrays.stream(objects).filter(Objects::nonNull).map(Enum::toString).collect(Collectors.toList());
    return join(objectStrings);
  }

  /**
   write a collection of ids to a CSV string

   @param ids to write
   @return CSV of ids
   */
  static <T> String fromStringsOf(Collection<T> ids) {
    if (Objects.isNull(ids) || ids.isEmpty()) {
      return "";
    }
    Iterator<T> it = ids.iterator();
    StringBuilder result = new StringBuilder(it.next().toString());
    while (it.hasNext()) {
      result.append(",").append(it.next());
    }
    return result.toString();
  }

  /**
   CSV string of the ids of a list of entities

   @param entities to get ids of
   @param <T>      type of entity
   @return CSV list of entity ids
   */
  static <T extends Entity> String fromIdsOf(Collection<T> entities) {
    if (Objects.isNull(entities) || entities.isEmpty()) {
      return "";
    }
    Iterator<T> it = entities.iterator();
    StringBuilder result = new StringBuilder(it.next().getId().toString());
    while (it.hasNext()) {
      result.append(",").append(it.next().getId());
    }
    return result.toString();
  }
}
