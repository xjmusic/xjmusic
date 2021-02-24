// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.service.nexus.persistence;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.inject.Singleton;
import com.google.protobuf.MessageLite;
import io.xj.Chain;
import io.xj.ChainBinding;
import io.xj.Segment;
import io.xj.SegmentChoice;
import io.xj.SegmentChoiceArrangement;
import io.xj.SegmentChoiceArrangementPick;
import io.xj.SegmentChord;
import io.xj.SegmentChordVoicing;
import io.xj.SegmentMeme;
import io.xj.SegmentMessage;
import io.xj.lib.entity.Entities;
import io.xj.lib.entity.EntityException;
import io.xj.lib.entity.EntityStoreImpl;
import io.xj.lib.util.Value;
import io.xj.service.nexus.NexusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 [#175880468] NexusEntityStore segments and child entities partitioned by segment id for rapid addressing
 <p>
 [#171553408] XJ Mk3 Distributed Architecture
 Chains, ChainBindings, ChainConfigs, Segments and all Segment content sub-entities persisted in JSON:API record stored keyed by chain or segment id in memory
 */
@Singleton
public class NexusEntityStoreImpl implements NexusEntityStore {
  private static final Logger log = LoggerFactory.getLogger(EntityStoreImpl.class);
  private static final String SEGMENT_ID_ATTRIBUTE = Entities.toIdAttribute(Entities.toBelongsTo(Segment.class));
  private final Map<String/*ID*/, Chain> chainMap = Maps.newConcurrentMap();
  private final Map<String/*ID*/, ChainBinding> chainBindingMap = Maps.newConcurrentMap();
  private final Map<String/*ID*/, Segment> segmentMap = Maps.newConcurrentMap();
  private final Map<String/*SegID*/, Map<Class<?>/*Type*/, Map<String/*ID*/, Object>>> store = Maps.newConcurrentMap();

  @Override
  public <N> N put(N entity) throws NexusException {
    // fail to store builder
    if (entity instanceof MessageLite.Builder)
      throw new NexusException(String.format("Can't store builder %s!",
        entity.getClass().getSimpleName()));

    // fail to store entity without id
    String id;
    try {
      id = Entities.getId(entity);
    } catch (EntityException e) {
      throw new NexusException(String.format("Can't get id of %s-type entity",
        entity.getClass().getSimpleName()));
    }

    // fail to store entity with unset id
    if (!Value.isSet(id))
      throw new NexusException(String.format("Can't store %s with null id",
        entity.getClass().getSimpleName()));

    if (entity instanceof Chain)
      chainMap.put(id, (Chain) entity);

    else if (entity instanceof ChainBinding)
      chainBindingMap.put(id, (ChainBinding) entity);

    else if (entity instanceof Segment)
      segmentMap.put(id, (Segment) entity);

    else if (entity instanceof SegmentMeme ||
      entity instanceof SegmentChord ||
      entity instanceof SegmentChordVoicing ||
      entity instanceof SegmentChoice ||
      entity instanceof SegmentMessage ||
      entity instanceof SegmentChoiceArrangement ||
      entity instanceof SegmentChoiceArrangementPick)
      try {
        String segmentId = String.valueOf(Entities.get(entity, SEGMENT_ID_ATTRIBUTE)
          .orElseThrow(() -> new NexusException(String.format("Can't store %s without Segment ID!",
            entity.getClass().getSimpleName()))));
        store.putIfAbsent(segmentId, Maps.newConcurrentMap());
        store.get(segmentId).putIfAbsent(entity.getClass(), Maps.newConcurrentMap());
        store.get(segmentId).get(entity.getClass()).put(id, entity);
      } catch (EntityException e) {
        throw new NexusException(e);
      }

    else throw new NexusException(String.format("Can't store %s!", entity.getClass().getSimpleName()));
    return entity;
  }

  @Override
  public <N> Collection<N> putAll(Collection<N> entities) throws NexusException {
    for (N entity : entities) put(entity);
    return entities;
  }

  @Override
  public void deleteChain(String id) {
    chainMap.remove(id);
  }

  @Override
  public void deleteChainBinding(String id) {
    chainBindingMap.remove(id);
  }

  @Override
  public void deleteSegment(String id) {
    store.remove(id);
    segmentMap.remove(id);
  }

  @Override
  public <N> void delete(String segmentId, Class<N> type, String id) {
    if (store.containsKey(segmentId) && store.get(segmentId).containsKey(type))
      store.get(segmentId).get(type).remove(id);
  }

  @Override
  public <N> void deleteAll(String segmentId, Class<N> type) throws NexusException {
    for (N entity : getAll(segmentId, type)) {
      try {
        delete(segmentId, type, Entities.getId(entity));
      } catch (EntityException e) {
        throw new NexusException(e);
      }
    }
  }

  @Override
  public void deleteAll() {
    store.clear();
    segmentMap.clear();
    chainBindingMap.clear();
    chainMap.clear();
    log.debug("Did delete all records in store");
  }

  @Override
  public Optional<Chain> getChain(String id) {
    if (!chainMap.containsKey(id)) return Optional.empty();
    return Optional.of(chainMap.get(id));
  }

  @Override
  public Optional<ChainBinding> getChainBinding(String id) {
    if (!chainBindingMap.containsKey(id)) return Optional.empty();
    return Optional.of(chainBindingMap.get(id));
  }

  @Override
  public Optional<Segment> getSegment(String id) {
    if (!segmentMap.containsKey(id)) return Optional.empty();
    return Optional.of(segmentMap.get(id));
  }

  @Override
  public <N> Optional<N> get(String segmentId, Class<N> type, String id) throws NexusException {
    try {
      if (!store.containsKey(segmentId) || !store.get(segmentId).containsKey(type))
        if (!store.get(segmentId).get(type).containsKey(id)) return Optional.empty();
      //noinspection unchecked
      return (Optional<N>) Optional.of(store.get(segmentId).get(type).get(id));

    } catch (Exception e) {
      throw new NexusException(e);
    }
  }

  @Override
  public <N> Collection<N> getAll(String segmentId, Class<N> type) throws NexusException {
    try {
      if (!store.containsKey(segmentId) || !store.get(segmentId).containsKey(type))
        return ImmutableList.of();
      //noinspection unchecked
      return (Collection<N>) store.get(segmentId).get(type).values().stream()
        .filter(entity -> type.equals(entity.getClass()))
        .collect(Collectors.toList());

    } catch (Exception e) {
      throw new NexusException(e);
    }
  }

  @Override
  public <N, B> Collection<N> getAll(String segmentId, Class<N> type, Class<B> belongsToType, Collection<String> belongsToIds) throws NexusException {
    try {
      if (!store.containsKey(segmentId) || !store.get(segmentId).containsKey(type))
        return ImmutableList.of();
      //noinspection unchecked
      return (Collection<N>) store.get(segmentId).get(type).values().stream()
        .filter(entity -> Entities.isChild(entity, belongsToType, belongsToIds))
        .collect(Collectors.toList());

    } catch (Exception e) {
      throw new NexusException(e);
    }
  }

  @Override
  public Collection<Chain> getAllChains() {
    return chainMap.values();
  }

  @Override
  public Collection<ChainBinding> getAllChainBindings(String chainId) {
    return chainBindingMap.values().stream()
      .filter(segment -> chainId.equals(segment.getChainId()))
      .collect(Collectors.toList());
  }

  @Override
  public Collection<Segment> getAllSegments(String chainId) {
    return segmentMap.values().stream()
      .filter(segment -> chainId.equals(segment.getChainId()))
      .collect(Collectors.toList());
  }

  @Override
  public int size() {
    return 0;
  }
}
