// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.entity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.xj.lib.util.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of Object Store
 * <p>
 * XJ Lab Distributed Architecture https://www.pivotaltracker.com/story/show/171553408
 * Chains, ChainBindings, TemplateConfigs, Segments and all Segment content sub-entities persisted in JSON:API record stored keyed by chain or segment id in memory
 */
@Service
public class EntityStoreImpl implements EntityStore {
  static final Logger LOG = LoggerFactory.getLogger(EntityStoreImpl.class);
  final Map<Class<?>/*Type*/, Map<UUID/*ID*/, Object>> store = Maps.newConcurrentMap();

  @Override
  public <N> N put(N entity) throws EntityStoreException {
    // fail to store entity without id
    UUID id;
    try {
      id = Entities.getId(entity);
    } catch (EntityException e) {
      throw new EntityStoreException(String.format("Can't get id of %s-type entity",
        entity.getClass().getSimpleName()));
    }

    // fail to store entity with unset id
    if (!Values.isSet(id))
      throw new EntityStoreException(String.format("Can't store %s with null id",
        entity.getClass().getSimpleName()));

    store.putIfAbsent(entity.getClass(), Maps.newConcurrentMap());
    store.get(entity.getClass()).put(id, entity);
    return entity;
  }

  @Override
  public <N> Collection<N> putAll(Collection<N> entities) throws EntityStoreException {
    Collection<N> results = Lists.newArrayList();
    for (N entity : entities) results.add(put(entity));
    return results;
  }

  @Override
  public <N> Optional<N> get(Class<N> type, UUID id) {
    try {
      if (!store.containsKey(type)) return Optional.empty();
      if (!store.get(type).containsKey(id)) return Optional.empty();
      //noinspection unchecked
      return (Optional<N>) Optional.of(store.get(type).get(id));

    } catch (Exception e) {
      LOG.error("Failed to get {}[{}}", type.getSimpleName(), id);
      return Optional.empty();
    }
  }

  @Override
  public <N> Collection<N> getAll(Class<N> type) {
    if (!store.containsKey(type)) return ImmutableList.of();
    //noinspection unchecked
    return (Collection<N>) store.get(type).values();
  }

  @Override
  public <N, B> Collection<N> getAll(Class<N> type, Class<B> belongsToType, Collection<UUID> belongsToIds) throws EntityStoreException {
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

  @Override
  public <N> void delete(Class<N> type, UUID id) {
    if (store.containsKey(type))
      store.get(type).remove(id);
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
