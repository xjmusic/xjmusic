// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.entity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.protobuf.MessageLite ;
import io.xj.lib.util.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 Implementation of Object Store
 <p>
 [#171553408] XJ Mk3 Distributed Architecture
 Chains, ChainBindings, ChainConfigs, Segments and all Segment content sub-entities persisted in JSON:API record stored keyed by chain or segment id in memory
 */
public class EntityStoreImpl implements EntityStore {
  private static final Logger log = LoggerFactory.getLogger(EntityStoreImpl.class);
  private final Map<Class<?>/*Type*/, Map<String/*ID*/, Object>> store = Maps.newConcurrentMap();

  public <N> N put(N entity) throws EntityStoreException {
    try {

      // explicitly fail on attempt to store builder
      if (entity instanceof MessageLite.Builder)
        throw new EntityStoreException(String.format("Can't store builder %s!",
          entity.getClass().getSimpleName()));

      // explicitly fail on attempt to store entity without id
      String id = Entities.getId(entity);
      if (!Value.isSet(id))
        throw new EntityStoreException(String.format("Can't put entity %s with no id",
          entity.getClass().getSimpleName()));

      store.putIfAbsent(entity.getClass(), Maps.newConcurrentMap());
      store.get(entity.getClass()).put(id, entity);
      return entity;

    } catch (EntityException e) {
      throw new EntityStoreException(e);
    }
  }

  public <N> Collection<N> putAll(Collection<N> entities) throws EntityStoreException {
    Collection<N> results = Lists.newArrayList();
    for (N entity : entities) results.add(put(entity));
    return results;
  }

  public <N> Optional<N> get(Class<N> type, String id) throws EntityStoreException {
    try {
      if (!store.containsKey(type)) return Optional.empty();
      if (!store.get(type).containsKey(id)) return Optional.empty();
      //noinspection unchecked
      return (Optional<N>) Optional.of(store.get(type).get(id));

    } catch (Exception e) {
      throw new EntityStoreException(e);
    }
  }

  public <N> Collection<N> getAll(Class<N> type) throws EntityStoreException {
    try {
      if (!store.containsKey(type)) return ImmutableList.of();
      //noinspection unchecked
      return (Collection<N>) store.get(type).values();

    } catch (Exception e) {
      throw new EntityStoreException(e);
    }
  }

  public <N, B> Collection<N> getAll(Class<N> type, Class<B> belongsToType, Collection<String> belongsToIds) throws EntityStoreException {
    try {
      if (!store.containsKey(type)) return ImmutableList.of();
      //noinspection unchecked
      return (Collection<N>) store.get(type).values().stream()
        .filter(entity -> Entities.isChild(entity, belongsToType, belongsToIds))
        .collect(Collectors.toList());

    } catch (Exception e) {
      throw new EntityStoreException(e);
    }
  }

  public <N> void delete(Class<N> type, String id) {
    if (store.containsKey(type))
      store.get(type).remove(id);
  }

  public void deleteAll() {
    store.clear();
    log.info("Did delete all records in store");
  }

  public <N, B> void deleteAll(Class<N> type, Class<B> belongsToType, String belongsToId) throws EntityStoreException {
    for (N entity : getAll(type, belongsToType, ImmutableList.of(belongsToId))) {
      try {
        delete(type, Entities.getId(entity));
      } catch (EntityException e) {
        throw new EntityStoreException(e);
      }
    }
  }

  public <N> void deleteAll(Class<N> type) {
    if (store.containsKey(type))
      store.get(type).clear();
  }

  @Override
  public Collection<Object> getAll() {
    return store.values().stream()
      .flatMap(map -> map.values().stream()).collect(Collectors.toList());
  }

  @Override
  public int size() {
    return store.size();
  }

}
