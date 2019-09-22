//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core.model.entity;

import com.google.common.collect.ConcurrentHashMultiset;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import io.xj.core.transport.CSV;
import io.xj.core.util.Text;

import java.math.BigInteger;
import java.time.Instant;
import java.util.Collection;
import java.util.List;

/**
 Standard Entity
 */
public interface Entity extends ResourceEntity {

  /**
   Format a comma-separated list of entity counts from a collection of entities

   @param entities for format a comma-separated list of the # occurrences of each class
   @return comma-separated list in text
   */
  static <N extends Entity> String histogramString(Collection<N> entities) {
    Multiset<String> entityHistogram = ConcurrentHashMultiset.create();
    entities.forEach((N entity) -> entityHistogram.add(Text.getSimpleName(entity)));
    List<String> descriptors = Lists.newArrayList();
    entityHistogram.elementSet().forEach((String name) -> descriptors.add(String.format("%d %s", entityHistogram.count(name), name)));
    return String.join(", ", descriptors);
  }

  /**
   Get a string representation of an entity, comprising a key-value map of its properties

   @param name       of entity
   @param properties to map
   @return string representation
   */
  static String keyValueString(String name, ImmutableMap<String, String> properties) {
    return String.format("%s{%s}", name, CSV.from(properties));
  }

  /**
   Get created-at instant

   @return created-at instant
   */
  Instant getCreatedAt();

  /**
   Get entity id

   @return entity id
   */
  BigInteger getId();

  /**
   Get parent id

   @return parent id
   */
  BigInteger getParentId();

  /**
   Get updated-at time

   @return updated-at time
   */
  Instant getUpdatedAt();

  /**
   Set created at time

   @param createdAt time
   @return entity
   */
  Entity setCreatedAt(String createdAt);

  /**
   Set created at time

   @param createdAt time
   @return entity
   */
  Entity setCreatedAtInstant(Instant createdAt);

  /**
   Set entity id

   @param id to set
   @return entity
   */
  Entity setId(BigInteger id);

  /**
   Set updated-at time

   @param updatedAt time
   @return entity
   */
  Entity setUpdatedAt(String updatedAt);

  /**
   Set updated-at time

   @param updatedAt time
   @return entity
   */
  Entity setUpdatedAtInstant(Instant updatedAt);

}
