// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.core.cache;

import com.google.common.collect.Lists;
import io.xj.lib.core.access.Access;
import io.xj.lib.core.exception.CoreException;
import io.xj.lib.core.entity.Entity;
import io.xj.lib.core.util.Text;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 Get a cache key for a variety of things.
 Related to cache invalidation.
 */
public interface CacheKey {
  String SIGNATURE_DELIMITER_ALPHA = "-";
  String SIGNATURE_DELIMITER_BRAVO = "|";
  String SIGNATURE_DELIMITER_CHARLIE = "[]";

  /**
   Get a unique signature for any unique access of a collection of entities

   @param access   to get signature of
   @param entities to get signature of
   @return signature
   */
  static <N extends Entity> String of(Access access, Collection<N> entities) {
    return String.format("%s%s%s", of(access), SIGNATURE_DELIMITER_CHARLIE, of(entities));
  }

  /**
   Get a unique signature for any unique access

   @return signature
   */
  static String of(Access access) {
    List<String> pieces = Lists.newArrayList();
    try {
      pieces.add(String.format("User%s%s",
        SIGNATURE_DELIMITER_ALPHA, access.getUserId()));
    } catch (CoreException ignored) {
    }
    if (Objects.nonNull(access.getUserAuthId()))
      pieces.add(String.format("UserAuth%s%s",
        SIGNATURE_DELIMITER_ALPHA, access.getUserAuthId()));
    access.getAccountIds().forEach(id -> pieces.add(String.format("Account%s%s",
      SIGNATURE_DELIMITER_ALPHA, id)));
    access.getRoleTypes().forEach(type -> pieces.add(String.format("Role%s%s",
      SIGNATURE_DELIMITER_ALPHA, type.toString())));
    pieces.sort(Comparator.naturalOrder());
    return String.join(SIGNATURE_DELIMITER_BRAVO, pieces);
  }

  /**
   Get a unique signature for any unique collection of entities

   @param entities to get unique signature of
   @return signature
   */
  static <N extends Entity> String of(Collection<N> entities) {
    List<String> pieces = entities.stream().map(entity -> String.format("%s%s%s",
      Text.getSimpleName(entity), SIGNATURE_DELIMITER_ALPHA, entity.getResourceId())).sorted(Comparator.naturalOrder()).collect(Collectors.toList());
    return String.join(SIGNATURE_DELIMITER_BRAVO, pieces);
  }


}
