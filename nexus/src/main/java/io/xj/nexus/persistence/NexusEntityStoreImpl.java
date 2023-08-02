// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.nexus.persistence;

import io.xj.lib.entity.Entities;
import io.xj.lib.entity.EntityException;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.entity.EntityStoreImpl;
import io.xj.lib.util.ValueUtils;
import io.xj.nexus.NexusException;
import io.xj.nexus.model.Chain;
import io.xj.nexus.model.Segment;
import io.xj.nexus.model.SegmentChoice;
import io.xj.nexus.model.SegmentChoiceArrangement;
import io.xj.nexus.model.SegmentChoiceArrangementPick;
import io.xj.nexus.model.SegmentChord;
import io.xj.nexus.model.SegmentChordVoicing;
import io.xj.nexus.model.SegmentMeme;
import io.xj.nexus.model.SegmentMessage;
import io.xj.nexus.model.SegmentMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * NexusEntityStore segments and child entities partitioned by segment id for rapid addressing https://www.pivotaltracker.com/story/show/175880468
 * <p>
 * XJ Lab Distributed Architecture https://www.pivotaltracker.com/story/show/171553408
 * Chains, ChainBindings, TemplateConfigs, Segments and all Segment content sub-entities persisted in JSON:API record stored keyed by chain or segment id in memory
 */
@Service
public class NexusEntityStoreImpl implements NexusEntityStore {
  static final Logger LOG = LoggerFactory.getLogger(EntityStoreImpl.class);
  static final String SEGMENT_ID_ATTRIBUTE = Entities.toIdAttribute(Entities.toBelongsTo(Segment.class));
  final Map<UUID/*ID*/, Chain> chainMap = new ConcurrentHashMap<>();
  final Map<UUID/*ID*/, Segment> segmentMap = new ConcurrentHashMap<>();
  final Map<UUID/*SegID*/, Map<Class<?>/*Type*/, Map<UUID/*ID*/, Object>>> store = new ConcurrentHashMap<>();
  final EntityFactory entityFactory;

  @Autowired
  public NexusEntityStoreImpl(EntityFactory entityFactory) {
    this.entityFactory = entityFactory;
  }

  @Override
  public void deleteChain(UUID id) {
    chainMap.remove(id);
  }

  @Override
  public void deleteSegment(UUID id) {
    store.remove(id);
    segmentMap.remove(id);
  }

  @Override
  public <N> void delete(UUID segmentId, Class<N> type, UUID id) {
    if (store.containsKey(segmentId) && store.get(segmentId).containsKey(type))
      store.get(segmentId).get(type).remove(id);
  }

  @Override
  public <N> void deleteAll(UUID segmentId, Class<N> type) throws NexusException {
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
    chainMap.clear();
    LOG.debug("Did delete all records in store");
  }

  @Override
  public Optional<Chain> getChain(UUID id) {
    if (!chainMap.containsKey(id)) return Optional.empty();
    return Optional.of(chainMap.get(id));
  }

  @Override
  public Optional<Segment> getSegment(UUID id) throws NexusException {
    if (!segmentMap.containsKey(id)) return Optional.empty();
    return Optional.of(segmentMap.get(id));
  }

  @Override
  public boolean segmentExists(UUID id) {
    return segmentMap.containsKey(id);
  }

  @Override
  public <N> Optional<N> get(UUID segmentId, Class<N> type, UUID id) throws NexusException {
    try {
      if (!store.containsKey(segmentId) || !store.get(segmentId).containsKey(type))
        if (!store.get(segmentId).get(type).containsKey(id)) return Optional.empty();
      //noinspection unchecked
      return (Optional<N>) Optional.ofNullable(store.get(segmentId).get(type).get(id));

    } catch (Exception e) {
      throw new NexusException(e);
    }
  }

  @Override
  public <N> Collection<N> getAll(UUID segmentId, Class<N> type) throws NexusException {
    try {
      if (!store.containsKey(segmentId) || !store.get(segmentId).containsKey(type))
        return List.of();
      //noinspection unchecked
      return (Collection<N>) store.get(segmentId).get(type).values().stream()
        .filter(entity -> type.equals(entity.getClass()))
        .collect(Collectors.toList());

    } catch (Exception e) {
      throw new NexusException(e);
    }
  }

  @Override
  public <N, B> Collection<N> getAll(UUID segmentId, Class<N> type, Class<B> belongsToType, Collection<UUID> belongsToIds) throws NexusException {
    try {
      if (!store.containsKey(segmentId) || !store.get(segmentId).containsKey(type))
        return List.of();
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
    return new ArrayList<>(chainMap.values());
  }

  @Override
  public Collection<Segment> getAllSegments(UUID chainId) {
    return segmentMap.values().stream()
      .filter(segment -> chainId.equals(segment.getChainId()))
      .collect(Collectors.toList());
  }

  @Override
  public <N> N put(N entity) throws NexusException {
    // fail to store entity without id
    UUID id;
    try {
      id = Entities.getId(entity);
    } catch (EntityException e) {
      throw new NexusException(String.format("Can't get id of %s-type entity",
        entity.getClass().getSimpleName()));
    }

    // fail to store entity with unset id
    if (!ValueUtils.isSet(id))
      throw new NexusException(String.format("Can't store %s with null id",
        entity.getClass().getSimpleName()));

    if (entity instanceof Chain)
      chainMap.put(id, (Chain) entity);

    else if (entity instanceof Segment)
      segmentMap.put(id, (Segment) entity);

    else if (entity instanceof SegmentMeme ||
      entity instanceof SegmentChord ||
      entity instanceof SegmentChordVoicing ||
      entity instanceof SegmentChoice ||
      entity instanceof SegmentMessage ||
      entity instanceof SegmentMeta ||
      entity instanceof SegmentChoiceArrangement ||
      entity instanceof SegmentChoiceArrangementPick)
      try {
        var segmentIdValue = Entities.get(entity, SEGMENT_ID_ATTRIBUTE)
          .orElseThrow(() -> new NexusException(String.format("Can't store %s without Segment ID!",
            entity.getClass().getSimpleName())));
        UUID segmentId = UUID.fromString(String.valueOf(segmentIdValue));
        store.putIfAbsent(segmentId, new ConcurrentHashMap<>());
        store.get(segmentId).putIfAbsent(entity.getClass(), new ConcurrentHashMap<>());
        store.get(segmentId).get(entity.getClass()).put(id, entity);
      } catch (EntityException e) {
        throw new NexusException(e);
      }
    else return entity;

    try {
      return entityFactory.clone(entity);
    } catch (EntityException e) {
      throw new NexusException(e);
    }
  }

  @Override
  public <N> void putAll(Collection<N> entities) throws NexusException {
    for (N entity : entities) put(entity);
  }

  @Override
  public int size() {
    return 0;
  }

  @Override
  public List<SegmentChoiceArrangementPick> getPicks(List<Segment> segments) throws NexusException {
    List<SegmentChoiceArrangementPick> picks = new ArrayList<>();
    for (Segment segment : segments) {
      picks.addAll(getAll(segment.getId(), SegmentChoiceArrangementPick.class));
    }
    return picks;
  }
}
