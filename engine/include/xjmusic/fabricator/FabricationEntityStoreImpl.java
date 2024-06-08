// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.engine.fabricator;

import io.xj.model.entity.EntityException;
import io.xj.model.entity.EntityFactory;
import io.xj.model.entity.EntityUtils;
import io.xj.model.enums.Program::Type;
import io.xj.model.util.CsvUtils;
import io.xj.model.util.StringUtils;
import io.xj.model.util.ValueException;
import io.xj.model.util.ValueUtils;
import io.xj.engine.FabricationException;
import io.xj.model.pojos.Chain;
import io.xj.model.pojos.Segment;
import io.xj.model.pojos.SegmentChoice;
import io.xj.model.pojos.SegmentChoiceArrangement;
import io.xj.model.pojos.SegmentChoiceArrangementPick;
import io.xj.model.pojos.SegmentChord;
import io.xj.model.pojos.SegmentChordVoicing;
import io.xj.model.pojos.SegmentMeme;
import io.xj.model.pojos.SegmentMessage;
import io.xj.model.pojos.SegmentMeta;
import io.xj.model.enums.Segment::State;
import io.xj.model.enums.Segment::Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 FabricationEntityStore segments and child entities partitioned by segment id for rapid addressing
 https://github.com/xjmusic/workstation/issues/276
 <p>
 XJ Lab Distributed Architecture
 https://github.com/xjmusic/workstation/issues/207
 Chains, ChainBindings, TemplateConfigs, Segments and all Segment content sub-entities persisted in JSON:API record stored keyed by chain or segment id in memory
 */
public class FabricationEntityStoreImpl implements FabricationEntityStore {
  static final Logger LOG = LoggerFactory.getLogger(FabricationEntityStoreImpl.class);
  static final String SEGMENT_ID_ATTRIBUTE = EntityUtils.toIdAttribute(EntityUtils.toBelongsTo(Segment.class));
  final Map<Integer, Segment> segments = new ConcurrentHashMap<>();
  final Map<Integer, Map<Class<?>/*Type*/, Map<UUID/*ID*/, Object>>> entities = new ConcurrentHashMap<>();
  final EntityFactory entityFactory;

  Chain chain;

  public FabricationEntityStoreImpl(EntityFactory entityFactory) {
    this.entityFactory = entityFactory;
  }

  @Override
  public <N> N put(N entity) throws FabricationException {
    validate(entity);

    if (entity instanceof Chain) {
      chain = (Chain) entity;
      return entity;
    }

    if (entity instanceof Segment) {
      segments.put(((Segment) entity).id, (Segment) entity);
      return entity;
    }

    // fail to store entity without id
    UUID id;
    try {
      id = EntityUtils.getId(entity);
    } catch (EntityException e) {
      throw new FabricationException(String.format("Can't get id of %s-type entity",
        entity.getClass().getSimpleName()));
    }

    // fail to store entity with unset id
    if (!ValueUtils.isSet(id))
      throw new FabricationException(String.format("Can't store %s with null id",
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
          .orElseThrow(() -> new FabricationException(String.format("Can't store %s without Segment ID!",
            entity.getClass().getSimpleName())));
        int segmentId = Integer.parseInt(String.valueOf(segmentIdValue));
        if (!entities.containsKey(segmentId))
          entities.put(segmentId, new ConcurrentHashMap<>());
        entities.get(segmentId).putIfAbsent(entity.getClass(), new ConcurrentHashMap<>());
        entities.get(segmentId).get(entity.getClass()).put(id, entity);
      } catch (EntityException e) {
        throw new FabricationException(e);
      }
    else return entity;

    return entity;
  }

  @Override
  public Optional<Chain> readChain() {
    return Optional.ofNullable(chain);
  }

  @Override
  public Optional<Segment> readSegment(int id) {
    return segments.containsKey(id) ? Optional.of(segments.get(id)) : Optional.empty();
  }

  @Override
  public Optional<Segment> readSegmentLast() {
    return readAllSegments()
      .stream()
      .max(Comparator.comparing(Segment::getId));
  }

  @Override
  public Optional<Segment> readSegmentAtChainMicros(long chainMicros) {
    var segments = readAllSegments()
      .stream()
      .filter(s -> SegmentUtils.isSpanning(s, chainMicros, chainMicros))
      .sorted(Comparator.comparing(Segment::getId))
      .toList();
    return segments.isEmpty() ? Optional.empty() : Optional.of(segments.get(segments.size() - 1));
  }

  @Override
  public <N> Optional<N> read(int segmentId, Class<N> type, UUID id) throws FabricationException {
    try {
      if (!entities.containsKey(segmentId)
        || !entities.get(segmentId).containsKey(type)
        || !entities.get(segmentId).get(type).containsKey(id)) return Optional.empty();
      //noinspection unchecked
      return (Optional<N>) Optional.ofNullable(entities.get(segmentId).get(type).get(id));

    } catch (Exception e) {
      throw new FabricationException(e);
    }
  }

  @Override
  public <N> Collection<N> readAll(int segmentId, Class<N> type) {
    if (!entities.containsKey(segmentId)
      || !entities.get(segmentId).containsKey(type))
      return List.of();
    //noinspection unchecked
    return (Collection<N>) entities.get(segmentId).get(type).values().stream()
      .filter(entity -> type.equals(entity.getClass()))
      .collect(Collectors.toList());
  }

  @Override
  public <N, B> Collection<N> readAll(int segmentId, Class<N> type, Class<B> belongsToType, Collection<UUID> belongsToIds) {
    if (!entities.containsKey(segmentId)
      || !entities.get(segmentId).containsKey(type))
      return List.of();
    //noinspection unchecked
    return (Collection<N>) entities.get(segmentId).get(type).values().stream()
      .filter(entity -> EntityUtils.isChild(entity, belongsToType, belongsToIds))
      .collect(Collectors.toList());
  }

  @Override
  public List<Segment> readAllSegments() {
    return segments.values().stream()
      .sorted(Comparator.comparingInt(Segment::getId))
      .collect(Collectors.toList());
  }

  @Override
  public List<Segment> readSegmentsFromToOffset(int fromOffset, int toOffset) {
    return readAllSegments()
      .stream()
      .filter(s -> s.id >= fromOffset && s.id <= toOffset)
      .toList();
  }

  @Override
  public List<Segment> readAllSegmentsSpanning(Long fromChainMicros, Long toChainMicros) {
    return readAllSegments()
      .stream()
      .filter(s -> SegmentUtils.isSpanning(s, fromChainMicros, toChainMicros))
      .toList();
  }

  @Override
  public int readLastSegmentId() {
    return segments.keySet().stream()
      .max(Integer::compareTo)
      .orElse(0);
  }

  @Override
  public List<SegmentChoiceArrangementPick> readPicks(List<Segment> segments) {
    List<SegmentChoiceArrangementPick> picks = new ArrayList<>();
    for (Segment segment : segments) {
      picks.addAll(readAll(segment.id, SegmentChoiceArrangementPick.class));
    }
    return picks;
  }

  @Override
  public <N> Collection<N> readManySubEntities(Collection<Integer> segmentIds, Boolean includePicks) {
    Collection<Object> entities = new ArrayList<>();
    for (Integer sId : segmentIds) {
      entities.addAll(readAll(sId, SegmentChoice.class));
      entities.addAll(readAll(sId, SegmentChoiceArrangement.class));
      entities.addAll(readAll(sId, SegmentChord.class));
      entities.addAll(readAll(sId, SegmentChordVoicing.class));
      entities.addAll(readAll(sId, SegmentMeme.class));
      entities.addAll(readAll(sId, SegmentMessage.class));
      entities.addAll(readAll(sId, SegmentMeta.class));
      if (includePicks)
        entities.addAll(readAll(sId, SegmentChoiceArrangementPick.class));
    }
    //noinspection unchecked
    return (Collection<N>) entities;
  }

  @Override
  public <N> Collection<N> readManySubEntitiesOfType(int segmentId, Class<N> type) {
    return readAll(segmentId, type);
  }

  @Override
  public <N> Collection<N> readManySubEntitiesOfType(Collection<Integer> segmentIds, Class<N> type) {
    return segmentIds.stream().flatMap(segmentId -> readManySubEntitiesOfType(segmentId, type).stream()).toList();
  }

  @Override
  public Optional<SegmentChoice> readChoice(int segmentId, Program::Type programType) {
    return readAll(segmentId, SegmentChoice.class)
      .stream()
      .filter(sc -> programType.equals(sc.programType))
      .findAny();
  }

  @Override
  public String readChoiceHash(Segment segment) {
    return
      readManySubEntities(Set.of(segment.id), false)
        .stream()
        .flatMap((entity) -> {
          try {
            return Stream.of(EntityUtils.getId(entity));
          } catch (EntityException e) {
            return Stream.empty();
          }
        })
        .map(UUID::toString)
        .sorted()
        .collect(Collectors.joining("_"));

  }

  @Override
  public void updateSegment(Segment segment) throws FabricationException {
    // validate and cache to-state
    validate(segment);
    Segment::State toState = segment.state;

    // fetch existing segment; further logic is based on its current state
    Segment existing = readSegment(segment.id)
      .orElseThrow(() -> new FabricationException("Segment #" + segment.id + " does not exist"));
    if (Objects.isNull(existing)) throw new FabricationException("Segment #" + segment.id + " does not exist");

    // logic based on existing Segment State
    protectSegmentStateTransition(existing.state, toState);

    // fail if attempt to [#128] change chainId of a segment
    Object updateChainId = segment.getChainId();
    if (ValueUtils.isSet(updateChainId) && !Objects.equals(updateChainId, existing.getChainId()))
      throw new FabricationException("cannot change chainId create a segment");

    // Never change id
    segment.setId(segment.id);

    // Updated at is always now
    segment.setUpdatedNow();

    // save segment
    put(segment);

  }

  @Override
  public <N> void delete(int segmentId, Class<N> type, UUID id) {
    if (entities.containsKey(segmentId) && entities.get(segmentId).containsKey(type))
      entities.get(segmentId).get(type).remove(id);
  }

  @Override
  public <N> void clear(Integer segmentId, Class<N> type) {
    for (N entity : readAll(segmentId, type)) {
      try {
        delete(segmentId, type, EntityUtils.getId(entity));
      } catch (EntityException e) {
        LOG.error("Failed to delete {} in Segment[{}]", type.name, segmentId, e);
      }
    }
  }

  @Override
  public void clear() {
    entities.clear();
    segments.clear();
    chain = null;
    LOG.debug("Did delete all records in store");
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
  public void deleteSegment(int segmentId) {
    segments.remove(segmentId);
    entities.remove(segmentId);
  }

  @Override
  public void deleteSegmentsAfter(int lastSegmentId) {
    for (var segmentId : segments.keySet().stream()
      .filter(segmentId -> segmentId > lastSegmentId)
      .toList())
      deleteSegment(segmentId);
  }

  @Override
  public Integer getSegmentCount() {
    return segments.size();
  }

  @Override
  public Boolean isEmpty() {
    return segments.isEmpty();
  }

  /**
   Segment state transitions are protected, dependent on the state this segment is being transitioned of, and the intended state it is being transitioned to.

   @param fromState to protect transition of
   @param toState   to test transition to
   @throws FabricationException on prohibited transition
   */
  public static void protectSegmentStateTransition(Segment::State fromState, Segment::State toState) throws FabricationException {
    switch (fromState) {
      case PLANNED -> onlyAllowSegmentStateTransitions(toState, Segment::State.PLANNED, Segment::State.CRAFTING);
      case CRAFTING ->
        onlyAllowSegmentStateTransitions(toState, Segment::State.CRAFTING, Segment::State.CRAFTED, Segment::State.CRAFTING, Segment::State.FAILED, Segment::State.PLANNED);
      case CRAFTED -> onlyAllowSegmentStateTransitions(toState, Segment::State.CRAFTED, Segment::State.CRAFTING);
      case FAILED -> onlyAllowSegmentStateTransitions(toState, Segment::State.FAILED);
      default -> onlyAllowSegmentStateTransitions(toState, Segment::State.PLANNED);
    }
  }

  /**
   Require state is in an array of states

   @param toState       to check
   @param allowedStates required to be in
   @throws FabricationException if not in required states
   */
  public static void onlyAllowSegmentStateTransitions(Segment::State toState, Segment::State... allowedStates) throws FabricationException {
    List<String> allowedStateNames = new ArrayList<>();
    for (Segment::State search : allowedStates) {
      allowedStateNames.add(search);
      if (Objects.equals(search, toState)) {
        return;
      }
    }
    throw new FabricationException(String.format("transition to %s not in allowed (%s)",
      toState, CsvUtils.join(allowedStateNames)));
  }

  /**
   Validate a segment or child entity

   @param entity to validate
   @throws FabricationException if invalid
   */
  private void validate(Object entity) throws FabricationException {
    try {
      if (entity instanceof Segment)
        validateSegment((Segment) entity);
      else if (entity instanceof SegmentChoice)
        validateSegmentChoice((SegmentChoice) entity);
      else if (entity instanceof SegmentMeme)
        validateSegmentMeme((SegmentMeme) entity);

    } catch (ValueException e) {
      throw new FabricationException(e);
    }
  }

  private void validateSegmentMeme(SegmentMeme entity) throws ValueException {
    entity.setName(StringUtils.toMeme(entity.name));
  }


  private void validateSegmentChoice(SegmentChoice entity) throws ValueException {
    if (ValueUtils.isUnset(entity.getDeltaIn())) entity.setDeltaIn(Segment::DELTA_UNLIMITED);
    if (ValueUtils.isUnset(entity.getDeltaOut())) entity.setDeltaOut(Segment::DELTA_UNLIMITED);
  }

  private void validateSegment(Segment entity) throws ValueException {
    if (ValueUtils.isEmpty(entity.getWaveformPreroll())) entity.setWaveformPreroll(0.0);
    if (ValueUtils.isEmpty(entity.getWaveformPostroll())) entity.setWaveformPostroll(0.0);
    if (ValueUtils.isEmpty(entity.getDelta())) entity.setDelta(0);

    // Segments not in pending state must have begin-at chain micros
    if (!Segment::Type.PENDING.equals(entity.type))
      ValueUtils.require(entity.beginAtChainMicros, "Begin-at");

    // Updated at is always now
    entity.setUpdatedNow();
  }
}
