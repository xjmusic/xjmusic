// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.cache;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import io.xj.core.entity.Entity;
import io.xj.core.exception.CoreException;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

/**
 Wraps a DAO to cache its results

 @param <E> type of Entity */
public class EntityCache<E extends Entity> {
  private Map<UUID, E> cache = Maps.newConcurrentMap();
  private Map<UUID, E> added = Maps.newConcurrentMap();
  private Class type = Entity.class;

  /**
   Construct a new Entity Cache
   */
  public EntityCache() {
  }

  /**
   Add an entity
   */
  public E add(E entity) {
    type = entity.getClass();
    cache.put(entity.getId(), entity);
    added.put(entity.getId(), entity);
    return entity;
  }

  /**
   Add many entities
   */
  public void addAll(Collection<E> entity) {
    entity.forEach(this::add);
  }

  /**
   Return the added entities once, then clear the added entities queue
   such as to do the writing of those entities to the database only once
   */
  public Collection<E> flushInserts() {
    Collection<E> flushed = ImmutableList.copyOf(added.values());
    added.clear();
    return flushed;
  }

  /**
   @return all caches entities
   */
  public Collection<E> getAll() {
    return cache.values();
  }

  /**
   Get one cached entity

   @param id to get
   @return cached entity
   @throws CoreException if no such entity is cached
   */
  public E get(UUID id) throws CoreException {
    if (!cache.containsKey(id))
      throw new CoreException(String.format("No such %s[id=%s]", type.getSimpleName(), id));
    return cache.get(id);
  }
}
