// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.service.nexus.persistence;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.xj.lib.entity.Entity;
import io.xj.lib.entity.EntityFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 Implementation of Nexus service record store
 <p>
 [#171553408] XJ Mk3 Distributed Architecture
 Chains, ChainBindings, ChainConfigs, Segments and all Segment content sub-entities persisted in JSON:API record stored keyed by chain or segment id in memory
 */
@Singleton
public class NexusEntityStoreImpl implements NexusEntityStore {
  private static final Logger log = LoggerFactory.getLogger(NexusEntityStoreImpl.class);
  private final Map<Class<? extends Entity>/*Type*/, Map<UUID/*ID*/, Entity>> store = Maps.newConcurrentMap();
  private final EntityFactory entityFactory;

  @Inject
  public NexusEntityStoreImpl(
    EntityFactory entityFactory
  ) {
    this.entityFactory = entityFactory;
  }

  @Override
  public <N extends Entity> N put(N entity) throws NexusEntityStoreException {
    try {
      store.putIfAbsent(entity.getClass(), Maps.newConcurrentMap());
      N actual = entityFactory.clone(entity);
      store.get(entity.getClass()).put(entity.getId(), actual);
      return actual;

    } catch (Exception e) {
      throw new NexusEntityStoreException(e);
    }
  }

  @Override
  public <N extends Entity> Collection<N> putAll(Collection<N> entities) throws NexusEntityStoreException {
    Collection<N> results = Lists.newArrayList();
    for (N entity : entities) results.add(put(entity));
    return results;
  }

  @Override
  public <N extends Entity> Optional<N> get(Class<N> type, UUID id) throws NexusEntityStoreException {
    try {
      if (!store.containsKey(type)) return Optional.empty();
      if (!store.get(type).containsKey(id)) return Optional.empty();
      //noinspection unchecked
      return Optional.of((N) entityFactory.clone(store.get(type).get(id)));

    } catch (Exception e) {
      throw new NexusEntityStoreException(e);
    }
  }

  @Override
  public <N extends Entity> Collection<N> getAll(Class<N> type) throws NexusEntityStoreException {
    try {
      if (!store.containsKey(type)) return ImmutableList.of();
      //noinspection unchecked
      return (Collection<N>) entityFactory.cloneAll(store.get(type).values());

    } catch (Exception e) {
      throw new NexusEntityStoreException(e);
    }
  }

  @Override
  public <N extends Entity, B extends Entity> Collection<N> getAll(Class<N> type, Class<B> belongsToType, Collection<UUID> belongsToIds) throws NexusEntityStoreException {
    try {
      if (!store.containsKey(type)) return ImmutableList.of();
      //noinspection unchecked
      return (Collection<N>) entityFactory.cloneAll(store.get(type).values().stream()
        .filter(entity -> entity.isChild(belongsToType, belongsToIds))
        .collect(Collectors.toList()));

    } catch (Exception e) {
      throw new NexusEntityStoreException(e);
    }
  }

  @Override
  public <N extends Entity> void delete(Class<N> type, UUID id) {
    if (store.containsKey(type))
      store.get(type).remove(id);
  }

  @Override
  public void deleteAll() {
    store.clear();
    log.info("Did delete all records in store");
  }

  @Override
  public <N extends Entity, B extends Entity> void deleteAll(Class<N> type, Class<B> belongsToType, UUID belongsToId) throws NexusEntityStoreException {
    for (N entity : getAll(type, belongsToType, ImmutableList.of(belongsToId)))
      delete(type, entity.getId());
  }

  @Override
  public <N extends Entity> void deleteAll(Class<N> type) {
    if (store.containsKey(type))
      store.get(type).clear();
  }

}
