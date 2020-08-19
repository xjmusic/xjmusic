// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.entity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 Implementation of Entity Store
 <p>
 [#171553408] XJ Mk3 Distributed Architecture
 Chains, ChainBindings, ChainConfigs, Segments and all Segment content sub-entities persisted in JSON:API record stored keyed by chain or segment id in memory
 */
public class EntityStoreImpl implements EntityStore {
  private static final Logger log = LoggerFactory.getLogger(EntityStoreImpl.class);
  private final Map<Class<? extends Entity>/*Type*/, Map<UUID/*ID*/, Entity>> store = Maps.newConcurrentMap();
  private final EntityFactory entityFactory;

  /**
   Constructor requires that an Entity Factory is provided

   @param entityFactory to use for store
   */
  @Inject
  public EntityStoreImpl(
    EntityFactory entityFactory
  ) {
    this.entityFactory = entityFactory;
  }

  public <N extends Entity> N put(N entity) throws EntityStoreException {
    try {
      store.putIfAbsent(entity.getClass(), Maps.newConcurrentMap());
      N actual = entityFactory.clone(entity);
      store.get(entity.getClass()).put(entity.getId(), actual);
      return actual;

    } catch (Exception e) {
      throw new EntityStoreException(e);
    }
  }

  public <N extends Entity> Collection<N> putAll(Collection<N> entities) throws EntityStoreException {
    Collection<N> results = Lists.newArrayList();
    for (N entity : entities) results.add(put(entity));
    return results;
  }

  public <N extends Entity> Optional<N> get(Class<N> type, UUID id) throws EntityStoreException {
    try {
      if (!store.containsKey(type)) return Optional.empty();
      if (!store.get(type).containsKey(id)) return Optional.empty();
      //noinspection unchecked
      return Optional.of((N) entityFactory.clone(store.get(type).get(id)));

    } catch (Exception e) {
      throw new EntityStoreException(e);
    }
  }

  public <N extends Entity> Collection<N> getAll(Class<N> type) throws EntityStoreException {
    try {
      if (!store.containsKey(type)) return ImmutableList.of();
      //noinspection unchecked
      return (Collection<N>) entityFactory.cloneAll(store.get(type).values());

    } catch (Exception e) {
      throw new EntityStoreException(e);
    }
  }

  public <N extends Entity, B extends Entity> Collection<N> getAll(Class<N> type, Class<B> belongsToType, Collection<UUID> belongsToIds) throws EntityStoreException {
    try {
      if (!store.containsKey(type)) return ImmutableList.of();
      //noinspection unchecked
      return (Collection<N>) entityFactory.cloneAll(store.get(type).values().stream()
        .filter(entity -> entity.isChild(belongsToType, belongsToIds))
        .collect(Collectors.toList()));

    } catch (Exception e) {
      throw new EntityStoreException(e);
    }
  }

  public <N extends Entity> void delete(Class<N> type, UUID id) {
    if (store.containsKey(type))
      store.get(type).remove(id);
  }

  public void deleteAll() {
    store.clear();
    log.info("Did delete all records in store");
  }

  public <N extends Entity, B extends Entity> void deleteAll(Class<N> type, Class<B> belongsToType, UUID belongsToId) throws EntityStoreException {
    for (N entity : getAll(type, belongsToType, ImmutableList.of(belongsToId)))
      delete(type, entity.getId());
  }

  public <N extends Entity> void deleteAll(Class<N> type) {
    if (store.containsKey(type))
      store.get(type).clear();
  }

  @Override
  public Collection<Entity> getAll() {
    return store.values().stream()
      .flatMap(map -> map.values().stream()).collect(Collectors.toList());
  }

}
