// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.nexus.persistence;

import io.xj.hub.util.ValueUtils;
import io.xj.nexus.NexusException;
import io.xj.nexus.entity.EntityException;
import io.xj.nexus.entity.EntityFactory;
import io.xj.nexus.entity.EntityStoreImpl;
import io.xj.nexus.entity.EntityUtils;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 NexusEntityStore segments and child entities partitioned by segment id for rapid addressing
 https://www.pivotaltracker.com/story/show/175880468
 <p>
 XJ Lab Distributed Architecture
 https://www.pivotaltracker.com/story/show/171553408
 Chains, ChainBindings, TemplateConfigs, Segments and all Segment content sub-entities persisted in JSON:API record stored keyed by chain or segment id in memory
 */
public class NexusEntityStoreImpl implements NexusEntityStore {
  static final Logger LOG = LoggerFactory.getLogger(EntityStoreImpl.class);
  static final String SEGMENT_ID_ATTRIBUTE = EntityUtils.toIdAttribute(EntityUtils.toBelongsTo(Segment.class));
  final Map<Integer, Segment> segments = new ConcurrentHashMap<>();
  final Map<Integer, Map<Class<?>/*Type*/, Map<UUID/*ID*/, Object>>> entities = new ConcurrentHashMap<>();
  final EntityFactory entityFactory;

  Chain chain;

  public NexusEntityStoreImpl(EntityFactory entityFactory) {
    this.entityFactory = entityFactory;
  }

  @Override
  public <N> void delete(int segmentId, Class<N> type, UUID id) {
    if (entities.containsKey(segmentId) && entities.get(segmentId).containsKey(type))
      entities.get(segmentId).get(type).remove(id);
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
    entities.clear();
    segments.clear();
    chain = null;
    LOG.debug("Did delete all records in store");
  }

  @Override
  public Optional<Chain> getChain() {
    return Optional.ofNullable(chain);
  }

  @Override
  public Optional<Segment> getSegment(int id) throws NexusException {
    return segments.containsKey(id) ? Optional.of(segments.get(id)) : Optional.empty();
  }

  @Override
  public <N> Optional<N> get(int segmentId, Class<N> type, UUID id) throws NexusException {
    try {
      if (!entities.containsKey(segmentId)
        || !entities.get(segmentId).containsKey(type)
        || !entities.get(segmentId).get(type).containsKey(id)) return Optional.empty();
      //noinspection unchecked
      return (Optional<N>) Optional.ofNullable(entities.get(segmentId).get(type).get(id));

    } catch (Exception e) {
      throw new NexusException(e);
    }
  }

  @Override
  public <N> Collection<N> getAll(int segmentId, Class<N> type) throws NexusException {
    try {
      if (!entities.containsKey(segmentId)
        || !entities.get(segmentId).containsKey(type))
        return List.of();
      //noinspection unchecked
      return (Collection<N>) entities.get(segmentId).get(type).values().stream()
        .filter(entity -> type.equals(entity.getClass()))
        .collect(Collectors.toList());

    } catch (Exception e) {
      throw new NexusException(e);
    }
  }

  @Override
  public <N, B> Collection<N> getAll(int segmentId, Class<N> type, Class<B> belongsToType, Collection<UUID> belongsToIds) throws NexusException {
    try {
      if (!entities.containsKey(segmentId)
        || !entities.get(segmentId).containsKey(type))
        return List.of();
      //noinspection unchecked
      return (Collection<N>) entities.get(segmentId).get(type).values().stream()
        .filter(entity -> EntityUtils.isChild(entity, belongsToType, belongsToIds))
        .collect(Collectors.toList());

    } catch (Exception e) {
      throw new NexusException(e);
    }
  }

  @Override
  public List<Segment> getAllSegments() {
    return segments.values().stream()
      .sorted(Comparator.comparingInt(Segment::getId))
      .collect(Collectors.toList());
  }

  @Override
  public <N> N put(N entity) throws NexusException {
    if (entity instanceof Chain) {
      chain = (Chain) entity;
      return entity;
    } else if (entity instanceof Segment) {
      segments.put(((Segment) entity).getId(), (Segment) entity);
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
        if (!entities.containsKey(segmentId))
          entities.put(segmentId, new ConcurrentHashMap<>());
        entities.get(segmentId).putIfAbsent(entity.getClass(), new ConcurrentHashMap<>());
        entities.get(segmentId).get(entity.getClass()).put(id, entity);
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
    return segments.size();
  }

  @Override
  public Boolean isSegmentsEmpty() {
    return segments.isEmpty();
  }

  @Override
  public void deleteSegmentsBefore(int lastSegmentId) {
    for (var segmentId : segments.keySet().stream()
      .filter(segmentId -> segmentId < lastSegmentId)
      .toList()) {
      segments.remove(segmentId);
      entities.remove(segmentId);
    }
  }

  @Override
  public void deleteSegmentsAfter(int lastSegmentId) {
    for (var segmentId : segments.keySet().stream()
      .filter(segmentId -> segmentId > lastSegmentId)
      .toList()) {
      segments.remove(segmentId);
      entities.remove(segmentId);
    }
  }

  @Override
  public int lastSegmentId() {
    return segments.keySet().stream()
      .max(Integer::compareTo)
      .orElse(0);
  }
}
