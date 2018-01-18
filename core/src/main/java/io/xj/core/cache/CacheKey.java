// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.cache;

import io.xj.core.access.impl.Access;
import io.xj.core.model.entity.Entity;

import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

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
  static String of(Access access, Collection<Entity> entities) {
    return String.format("%s%s%s", of(access), SIGNATURE_DELIMITER_CHARLIE, of(entities));
  }

  /**
   Get a unique signature for any unique access

   @return signature
   */
  static String of(Access access) {
    List<String> pieces = Lists.newArrayList();
    pieces.add(String.format("User%s%s",
      SIGNATURE_DELIMITER_ALPHA, access.getUserId()));
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
  static String of(Collection<Entity> entities) {
    List<String> pieces = Lists.newArrayList();
    entities.forEach(entity -> pieces.add(String.format("%s%s%s",
      entity.getClass().getSimpleName(), SIGNATURE_DELIMITER_ALPHA, entity.getId())));
    pieces.sort(Comparator.naturalOrder());
    return String.join(SIGNATURE_DELIMITER_BRAVO, pieces);
  }


}
