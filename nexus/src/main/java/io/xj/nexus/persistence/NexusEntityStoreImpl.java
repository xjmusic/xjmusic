// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.nexus.persistence;

import io.xj.hub.util.ValueUtils;
import io.xj.lib.entity.EntityException;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.entity.EntityStoreImpl;
import io.xj.lib.entity.EntityUtils;
import io.xj.nexus.NexusException;
import io.xj.nexus.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 NexusEntityStore segments and child entities partitioned by segment id for rapid addressing https://www.pivotaltracker.com/story/show/175880468
 <p>
 XJ Lab Distributed Architecture https://www.pivotaltracker.com/story/show/171553408
 Chains, ChainBindings, TemplateConfigs, Segments and all Segment content sub-entities persisted in JSON:API record stored keyed by chain or segment id in memory
 */
public class NexusEntityStoreImpl implements NexusEntityStore {
  static final Logger LOG = LoggerFactory.getLogger(EntityStoreImpl.class);
  static final String SEGMENT_ID_ATTRIBUTE = EntityUtils.toIdAttribute(EntityUtils.toBelongsTo(Segment.class));
  final List<Segment> segmentArray = new ArrayList<>();
  final List<Map<Class<?>/*Type*/, Map<UUID/*ID*/, Object>>> store = new ArrayList<>();
  final EntityFactory entityFactory;

  Chain chain;

  public NexusEntityStoreImpl(EntityFactory entityFactory) {
    this.entityFactory = entityFactory;
  }

  @Override
  public <N> void delete(int segmentId, Class<N> type, UUID id) {
    if (store.size() > segmentId && store.get(segmentId).containsKey(type))
      store.get(segmentId).get(type).remove(id);
  }

  @Override
  public <N> void deleteAll(Integer segmentId, Class<N> type) throws NexusException {
    for (N entity : getAll(segmentId, type)) {
      try {
        delete(segmentId, type, EntityUtils.getId(entity));
      } catch (EntityException e) {
        throw new NexusException(e);
      }
    }
  }

  @Override
  public void deleteAll() {
    store.clear();
    segmentArray.clear();
    chain = null;
    LOG.debug("Did delete all records in store");
  }

  @Override
  public Optional<Chain> getChain() {
    return Optional.ofNullable(chain);
  }

  @Override
  public Optional<Segment> getSegment(int id) throws NexusException {
    return segmentArray.size() > id ? Optional.of(segmentArray.get(id)) : Optional.empty();
  }

  @Override
  public <N> Optional<N> get(int segmentId, Class<N> type, UUID id) throws NexusException {
    try {
      if (store.size() <= segmentId || !store.get(segmentId).containsKey(type))
        if (!store.get(segmentId).get(type).containsKey(id)) return Optional.empty();
      //noinspection unchecked
      return (Optional<N>) Optional.ofNullable(store.get(segmentId).get(type).get(id));

    } catch (Exception e) {
      throw new NexusException(e);
    }
  }

  @Override
  public <N> Collection<N> getAll(int segmentId, Class<N> type) throws NexusException {
    try {
      if (store.size() <= segmentId || !store.get(segmentId).containsKey(type))
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
  public <N, B> Collection<N> getAll(int segmentId, Class<N> type, Class<B> belongsToType, Collection<UUID> belongsToIds) throws NexusException {
    try {
      if (store.size() <= segmentId || !store.get(segmentId).containsKey(type))
        return List.of();
      //noinspection unchecked
      return (Collection<N>) store.get(segmentId).get(type).values().stream()
        .filter(entity -> EntityUtils.isChild(entity, belongsToType, belongsToIds))
        .collect(Collectors.toList());

    } catch (Exception e) {
      throw new NexusException(e);
    }
  }

  @Override
  public List<Segment> getAllSegments() {
    return segmentArray;
  }

  @Override
  public <N> N put(N entity) throws NexusException {
    if (entity instanceof Chain) {
      chain = (Chain) entity;
      return entity;
    } else if (entity instanceof Segment) {
      while (segmentArray.size() <= ((Segment) entity).getId()) {
        segmentArray.add(new Segment()
          .id(segmentArray.size())
          .chainId(chain.getId())
          .type(SegmentType.PENDING)
          .state(SegmentState.PLANNED));
      }
      segmentArray.set(((Segment) entity).getId(), (Segment) entity);
      return entity;
    }

    // fail to store entity without id
    UUID id;
    try {
      id = EntityUtils.getId(entity);
    } catch (EntityException e) {
      throw new NexusException(String.format("Can't get id of %s-type entity",
        entity.getClass().getSimpleName()));
    }

    // fail to store entity with unset id
    if (!ValueUtils.isSet(id))
      throw new NexusException(String.format("Can't store %s with null id",
        entity.getClass().getSimpleName()));

    else if (entity instanceof SegmentMeme ||
      entity instanceof SegmentChord ||
      entity instanceof SegmentChordVoicing ||
      entity instanceof SegmentChoice ||
      entity instanceof SegmentMessage ||
      entity instanceof SegmentMeta ||
      entity instanceof SegmentChoiceArrangement ||
      entity instanceof SegmentChoiceArrangementPick)
      try {
        var segmentIdValue = EntityUtils.get(entity, SEGMENT_ID_ATTRIBUTE)
          .orElseThrow(() -> new NexusException(String.format("Can't store %s without Segment ID!",
            entity.getClass().getSimpleName())));
        int segmentId = Integer.parseInt(String.valueOf(segmentIdValue));
        while (store.size() <= segmentId) {
          store.add(new ConcurrentHashMap<>());
        }
        store.get(segmentId).putIfAbsent(entity.getClass(), new ConcurrentHashMap<>());
        store.get(segmentId).get(entity.getClass()).put(id, entity);
      } catch (EntityException e) {
        throw new NexusException(e);
      }
    else return entity;

    return entity;
  }

  @Override
  public <N> void putAll(Collection<N> entities) throws NexusException {
    for (N entity : entities) put(entity);
  }

  @Override
  public List<SegmentChoiceArrangementPick> getPicks(List<Segment> segments) throws NexusException {
    List<SegmentChoiceArrangementPick> picks = new ArrayList<>();
    for (Segment segment : segments) {
      picks.addAll(getAll(segment.getId(), SegmentChoiceArrangementPick.class));
    }
    return picks;
  }

  @Override
  public Integer getSegmentCount() {
    return segmentArray.size();
  }

  @Override
  public Boolean isSegmentsEmpty() {
    return segmentArray.isEmpty();
  }

}
