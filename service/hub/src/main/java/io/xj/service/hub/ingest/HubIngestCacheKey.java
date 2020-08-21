// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.ingest;

import com.google.common.collect.Lists;
import io.xj.lib.entity.Entity;
import io.xj.lib.util.Text;
import io.xj.service.hub.access.HubAccess;
import io.xj.service.hub.entity.Instrument;
import io.xj.service.hub.entity.Library;
import io.xj.service.hub.entity.Program;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 Get a cache key for a variety of things.
 Related to cache invalidation.
 */
public interface HubIngestCacheKey {
  String SIGNATURE_DELIMITER_ALPHA = "-";
  String SIGNATURE_DELIMITER_BRAVO = "|";
  String SIGNATURE_DELIMITER_CHARLIE = "[]";

  /**
   Get a unique signature for any unique hubAccess of a collection of entities

   @param hubAccess to get signature of
   @param entities  to get signature of
   @return signature
   */
  static <N extends Entity> String of(HubAccess hubAccess, Collection<N> entities) {
    return String.format("%s%s%s", of(hubAccess), SIGNATURE_DELIMITER_CHARLIE, of(entities));
  }

  /**
   Get a unique signature for any unique hubAccess

   @return signature
   */
  static String of(HubAccess hubAccess) {
    List<String> pieces = Lists.newArrayList();
    pieces.add(String.format("User%s%s",
      SIGNATURE_DELIMITER_ALPHA, hubAccess.getUserId()));
    if (Objects.nonNull(hubAccess.getUserAuthId()))
      pieces.add(String.format("UserAuth%s%s",
        SIGNATURE_DELIMITER_ALPHA, hubAccess.getUserAuthId()));
    hubAccess.getAccountIds().forEach(id -> pieces.add(String.format("Account%s%s",
      SIGNATURE_DELIMITER_ALPHA, id)));
    hubAccess.getRoleTypes().forEach(type -> pieces.add(String.format("Role%s%s",
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
    List<String> pieces = entities.stream().map(entity -> getKey(entity.getClass(), entity.getId())).sorted(Comparator.naturalOrder()).collect(Collectors.toList());
    return String.join(SIGNATURE_DELIMITER_BRAVO, pieces);
  }

  /**
   Get the key for one entity type and id

   @param type of entity
   @param id   of entity
   @param <N>  type of entity
   @return key for entity type and id
   */
  static <N> String getKey(Class<N> type, UUID id) {
    return String.format("%s%s%s",
      Text.getSimpleName(type), SIGNATURE_DELIMITER_ALPHA, id);
  }

  /**
   Get a unique signature for any unique collection of library, program, and instrument UUIDs

   @param libraryIds    to get signature for
   @param programIds    to get signature for
   @param instrumentIds to get signature for
   @return signature
   */
  static <N extends Entity> String of(Set<UUID> libraryIds, Set<UUID> programIds, Set<UUID> instrumentIds) {
    List<String> pieces = Lists.newArrayList();
    libraryIds.forEach(id -> pieces.add(getKey(Library.class, id)));
    programIds.forEach(id -> pieces.add(getKey(Program.class, id)));
    instrumentIds.forEach(id -> pieces.add(getKey(Instrument.class, id)));
    pieces.sort(Comparator.naturalOrder());
    return String.join(SIGNATURE_DELIMITER_BRAVO, pieces);
  }

  /**
   Get a unique signature for an HubAccess object plus any unique collection of library, program, and instrument UUIDs

   @param hubAccess     to get signature of
   @param libraryIds    to get signature for
   @param programIds    to get signature for
   @param instrumentIds to get signature for
   @return signature
   */
  static <N extends Entity> String of(HubAccess hubAccess, Set<UUID> libraryIds, Set<UUID> programIds, Set<UUID> instrumentIds) {
    return String.format("%s%s%s", of(hubAccess), SIGNATURE_DELIMITER_CHARLIE, of(libraryIds, programIds, instrumentIds));
  }


}
