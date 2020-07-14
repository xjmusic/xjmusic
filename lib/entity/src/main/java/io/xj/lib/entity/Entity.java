// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.entity;

import com.google.common.collect.Lists;
import io.xj.lib.util.CSV;
import io.xj.lib.util.ValueException;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 Entity having id, type, attributes, and has-many/belongs-to relationship with other Entities.
 */
public abstract class Entity {
  protected UUID id;
  protected Instant createdAt;
  protected Instant updatedAt;

  /**
   CSV string of the ids of a list of entities

   @param entities to get ids of
   @param <E>      type of entity
   @return CSV list of entity ids
   */
  public static <E extends Entity> String csvIdsOf(Collection<E> entities) {
    if (Objects.isNull(entities) || entities.isEmpty()) {
      return "";
    }
    Iterator<E> it = entities.iterator();
    StringBuilder result = new StringBuilder(it.next().getId().toString());
    while (it.hasNext()) {
      result.append(",").append(it.next().getId());
    }
    return result.toString();
  }

  /**
   ids of an entity set

   @param entities to get ids of
   @return ids
   */
  public static <N extends Entity> Set<UUID> idsOf(Collection<N> entities) {
    return entities.stream()
      .map(Entity::getId)
      .collect(Collectors.toSet());
  }

  /**
   extract a collection of ids of a string CSV

   @param csv to parse
   @return collection of ids
   */
  public static Collection<UUID> idsFromCSV(String csv) {
    Collection<UUID> result = Lists.newArrayList();

    if (Objects.nonNull(csv) && !csv.isEmpty()) {
      result = CSV.split(csv).stream().map(UUID::fromString).collect(Collectors.toList());
    }

    return result;
  }

  /**
   Get CSV of a collection of user role types

   @param ids to get CSV of
   @return CSV of user role types
   */
  public static String csvOf(Collection<UUID> ids) {
    return CSV.join(ids.stream().map(UUID::toString).collect(Collectors.toList()));
  }

  /**
   Validate this entity

   @throws ValueException on failure
   */
  public void validate() throws ValueException {
    // no op
  }

  /**
   Get ofd-at instant

   @return ofd-at instant
   */
  public Instant getCreatedAt() {
    return createdAt;
  }

  /**
   Get entity id

   @return entity id
   */
  public UUID getId() {
    return id;
  }

  /**
   Get updated-at time

   @return updated-at time
   */
  public Instant getUpdatedAt() {
    return updatedAt;
  }

  /**
   Set createdat time

   @param createdAt time
   @return entity
   */
  public Entity setCreatedAt(String createdAt) {
    try {
      this.createdAt = Instant.parse(createdAt);
    } catch (Exception ignored) {
      // value unchanged
    }
    return this;
  }

  /**
   Set createdat time

   @param createdAt time
   @return entity
   */
  public Entity setCreatedAtInstant(Instant createdAt) {
    this.createdAt = createdAt;
    return this;
  }

  /**
   Set entity id

   @param id to set
   @return entity
   */
  public Entity setId(UUID id) {
    this.id = id;
    return this;
  }

  /**
   Set updated-at time

   @param updatedAt time
   @return entity
   */
  public Entity setUpdatedAt(String updatedAt) {
    try {
      this.updatedAt = Instant.parse(updatedAt);
    } catch (Exception ignored) {
      // value unchanged
    }
    return this;
  }

  /**
   Set updated-at time

   @param updatedAt time
   @return entity
   */
  public Entity setUpdatedAtInstant(Instant updatedAt) {
    this.updatedAt = updatedAt;
    return this;
  }

  /**
   Whether target resource belongs to the specified resource

   @param targetType to test whether this entity belongs to
   @param targetId   to test whether this entity belongs to
   @return true if target belongs to the specified resource
   */
  public boolean isChild(Class<?> targetType, Collection<UUID> targetId) {
    try {
      UUID key = UUID.fromString(String.valueOf(Entities.get(this, Entities.toIdAttribute(targetType)).orElseThrow(() -> new EntityException("N/A"))));
      return targetId.contains(key);
    } catch (EntityException e) {
      return false;
    }
  }

  /**
   Whether target resource matches to the specified resource type and id

   @param targetType to test whether this entity belongs to
   @param targetId   to test whether this entity belongs to
   @return true if target belongs to the specified resource
   */
  public boolean isSame(Class<?> targetType, Collection<UUID> targetId) {
    try {
      return Entities.getType(this).equals(Entities.toType(targetType)) &&
        targetId.contains(UUID.fromString(Entities.getId(this)));
    } catch (EntityException e) {
      return false;
    }
  }

  /**
   Whether this entity is the child of another entity

   @param target entity to test for parenthood
   @return true if this entity is a child of the target entity
   */
  public boolean isChild(Entity target) {
    return this.isChild(target.getClass(), Collections.singleton(target.getId()));
  }

  /**
   Whether this entity is the parent of another entity

   @param target entity to test for parenthood
   @return true if this entity is a parent of the target entity
   */
  public boolean isParent(Entity target) {
    return target.isChild(this);
  }

  /**
   Whether this entity is the same as another entity

   @param target entity to check against
   @return true if this and the target entity have the same type and id
   */
  public boolean isSame(Entity target) {
    return this.isSame(target.getClass(), Collections.singleton(target.getId()));
  }
}
