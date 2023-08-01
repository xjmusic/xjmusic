// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.util;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public interface CSV {
  String COMMA = ", ";
  char SPACE = ' ';

  static List<String> split(@Nullable String csv) {
    if (Strings.isNullOrEmpty(csv)) return List.of();
    return Arrays.stream(csv.split(","))
      .map(String::trim)
      .collect(Collectors.toList());
  }

  static List<String> splitProperSlug(String csv) {
    Collection<String> items = split(csv);
    List<String> slugs = Lists.newArrayList();
    items.stream().filter(item -> Objects.nonNull(item) && !item.isEmpty()).map(Text::toProperSlug).forEach(slugs::add);
    return slugs;
  }

  static String join(List<?> parts) {
    return parts.stream().map(Object::toString).collect(Collectors.joining(COMMA));
  }

  @SafeVarargs
  static <E extends Enum<E>> String joinEnum(E... objects) {
    List<String> objectStrings = Arrays.stream(objects).filter(Objects::nonNull).map(Enum::toString).collect(Collectors.toList());
    return join(objectStrings);
  }

  /**
   Join a set of items' toString() values properly, e.g. "One, Two, Three, and Four"

   @param ids             to write
   @param beforeFinalItem text after last comma
   @return CSV of ids
   */
  static <T> String prettyFrom(Collection<T> ids, String beforeFinalItem) {
    if (Objects.isNull(ids) || ids.isEmpty()) {
      return "";
    }
    Iterator<T> it = ids.iterator();
    StringBuilder result = new StringBuilder(it.next().toString());
    while (it.hasNext()) {
      result.append(COMMA);
      String item = it.next().toString();
      if (!it.hasNext())
        result.append(beforeFinalItem).append(SPACE);
      result.append(item);
    }
    return result.toString();
  }

  /**
   Get a CSV string of key=value properties

   @param properties key=value
   @return CSV string
   */
  static String from(Map<String, String> properties) {
    List<String> pieces = Lists.newArrayList();
    properties.forEach((key, value) -> pieces.add(String.format("%s=%s", key, value)));
    return join(pieces);
  }

  /**
   Get a CSV string of key=value properties

   @param properties key=value
   @return CSV string
   */
  static String from(Collection<?> properties) {
    return join(properties.stream().map(Objects::toString).collect(Collectors.toList()));
  }

  /**
   Split a string into a list of UUIDs

   @param uuids string to split
   @return list of UUIDs
   */
  static List<UUID> splitUUIDs(String uuids) {
    return split(uuids).stream().map(UUID::fromString).collect(Collectors.toList());
  }
}
