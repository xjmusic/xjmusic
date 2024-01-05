// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.nexus.persistence;

import io.xj.hub.enums.ProgramType;
import io.xj.hub.util.CsvUtils;
import io.xj.hub.util.StringUtils;
import io.xj.hub.util.ValueException;
import io.xj.hub.util.ValueUtils;
import io.xj.nexus.NexusException;
import io.xj.nexus.entity.EntityException;
import io.xj.nexus.entity.EntityFactory;
import io.xj.nexus.entity.EntityUtils;
import io.xj.nexus.entity.common.ChordEntity;
import io.xj.nexus.entity.common.MessageEntity;
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
import io.xj.nexus.model.SegmentState;
import io.xj.nexus.model.SegmentType;
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

import static io.xj.hub.util.ValueUtils.MICROS_PER_SECOND;

/**
 NexusEntityStore segments and child entities partitioned by segment id for rapid addressing
 https://www.pivotaltracker.com/story/show/175880468
 <p>
 XJ Lab Distributed Architecture
 https://www.pivotaltracker.com/story/show/171553408
 Chains, ChainBindings, TemplateConfigs, Segments and all Segment content sub-entities persisted in JSON:API record stored keyed by chain or segment id in memory
 */
public class NexusEntityStoreImpl implements NexusEntityStore {
  static final Logger LOG = LoggerFactory.getLogger(NexusEntityStoreImpl.class);
  static final String SEGMENT_ID_ATTRIBUTE = EntityUtils.toIdAttribute(EntityUtils.toBelongsTo(Segment.class));
  public static final Long LENGTH_MINIMUM_MICROS = MICROS_PER_SECOND;
  public static final float AMPLITUDE_MINIMUM = 0.0f;
  final Map<Integer, Segment> segments = new ConcurrentHashMap<>();
  final Map<Integer, Map<Class<?>/*Type*/, Map<UUID/*ID*/, Object>>> entities = new ConcurrentHashMap<>();
  final EntityFactory entityFactory;

  Chain chain;

  public NexusEntityStoreImpl(EntityFactory entityFactory) {
    this.entityFactory = entityFactory;
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
  public <N> void createAll(Collection<N> entities) throws NexusException {
    for (N entity : entities) put(entity);
  }

  @Override
  public Segment createSegment(Segment segment) throws ManagerFatalException, ManagerValidationException {
    try {
      validate(segment);

      // [#126] Segments are always readMany in PLANNED state
      segment.setState(SegmentState.PLANNED);

      // Updated at is always now
      segment.setUpdatedNow();

      // create segment with Chain ID and offset are read-only, set at creation
      if (readSegment(segment.getId()).isPresent()) {
        throw new ManagerValidationException("Found Segment at same offset in Chain!");
      }

      return put(segment);

    } catch (NexusException e) {
      throw new ManagerFatalException(e);
    }
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
  public <N> Optional<N> read(int segmentId, Class<N> type, UUID id) throws NexusException {
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
      .filter(s -> s.getId() >= fromOffset && s.getId() <= toOffset)
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
      picks.addAll(readAll(segment.getId(), SegmentChoiceArrangementPick.class));
    }
    return picks;
  }

  @Override
  public <N> Collection<N> readManySubEntities(Collection<Integer> segmentIds, Boolean includePicks) throws ManagerFatalException {
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
  public Optional<SegmentChoice> readChoice(int segmentId, ProgramType programType) {
    return readAll(segmentId, SegmentChoice.class)
      .stream()
      .filter(sc -> programType.equals(sc.getProgramType()))
      .findAny();
  }

  @Override
  public String readChoiceHash(Segment segment) {
    try {
      return
        readManySubEntities(Set.of(segment.getId()), false)
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

    } catch (ManagerFatalException e) {
      LOG.warn("Failed to get choice hash for Segment #" + segment.getId(), e);
      return String.format("%s_%d", segment.getChainId(), segment.getId());
    }
  }

  @Override
  public void updateSegment(int segmentId, Segment segment) throws ManagerFatalException, ManagerExistenceException, ManagerValidationException {
    try {
      // validate and cache to-state
      validate(segment);
      SegmentState toState = segment.getState();

      // fetch existing segment; further logic is based on its current state
      Segment existing = readSegment(segmentId)
        .orElseThrow(() -> new ManagerExistenceException(Segment.class, Integer.toString(segmentId)));
      requireExists("Segment #" + segmentId, existing);

      // logic based on existing Segment State
      protectSegmentStateTransition(existing.getState(), toState);

      // fail if attempt to [#128] change chainId of a segment
      Object updateChainId = segment.getChainId();
      if (ValueUtils.isSet(updateChainId) && !Objects.equals(updateChainId, existing.getChainId()))
        throw new ManagerValidationException("cannot change chainId create a segment");

      // Never change id
      segment.setId(segmentId);

      // Updated at is always now
      segment.setUpdatedNow();

      // save segment
      put(segment);

    } catch (NexusException e) {
      throw new ManagerFatalException(e);

    } catch (ValueException e) {
      throw new ManagerValidationException(e);
    }
  }

  @Override
  public <N> void delete(int segmentId, Class<N> type, UUID id) {
    if (entities.containsKey(segmentId) && entities.get(segmentId).containsKey(type))
      entities.get(segmentId).get(type).remove(id);
  }

  @Override
  public <N> void clear(Integer segmentId, Class<N> type) throws NexusException {
    for (N entity : readAll(segmentId, type)) {
      try {
        delete(segmentId, type, EntityUtils.getId(entity));
      } catch (EntityException e) {
        throw new NexusException(e);
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
   @throws ValueException on prohibited transition
   */
  public static void protectSegmentStateTransition(SegmentState fromState, SegmentState toState) throws ValueException {
    switch (fromState) {
      case PLANNED -> onlyAllowSegmentStateTransitions(toState, SegmentState.PLANNED, SegmentState.CRAFTING);
      case CRAFTING ->
        onlyAllowSegmentStateTransitions(toState, SegmentState.CRAFTING, SegmentState.CRAFTED, SegmentState.CRAFTING, SegmentState.FAILED, SegmentState.PLANNED);
      case CRAFTED -> onlyAllowSegmentStateTransitions(toState, SegmentState.CRAFTED, SegmentState.CRAFTING);
      case FAILED -> onlyAllowSegmentStateTransitions(toState, SegmentState.FAILED);
      default -> onlyAllowSegmentStateTransitions(toState, SegmentState.PLANNED);
    }
  }

  /**
   Require state is in an array of states

   @param toState       to check
   @param allowedStates required to be in
   @throws ValueException if not in required states
   */
  public static void onlyAllowSegmentStateTransitions(SegmentState toState, SegmentState... allowedStates) throws ValueException {
    List<String> allowedStateNames = new ArrayList<>();
    for (SegmentState search : allowedStates) {
      allowedStateNames.add(search.toString());
      if (Objects.equals(search, toState)) {
        return;
      }
    }
    throw new ValueException(String.format("transition to %s not in allowed (%s)",
      toState, CsvUtils.join(allowedStateNames)));
  }

  /**
   Require that an entity is non-null

   @param name   name of entity (for error message)
   @param entity to require existence of
   @throws ManagerExistenceException if not isNonNull
   */
  protected <E> void requireExists(String name, E entity) throws ManagerExistenceException {
    if (!ValueUtils.isNonNull(entity)) throw new ManagerExistenceException(String.format("%s does not exist!", name));
  }

  /**
   Validate a segment or child entity

   @param entity to validate
   @throws ManagerValidationException if invalid
   */
  private void validate(Object entity) throws ManagerValidationException {
    try {
      if (entity instanceof Segment)
        validateSegment((Segment) entity);
      else if (entity instanceof SegmentChoice)
        validateSegmentChoice((SegmentChoice) entity);
      else if (entity instanceof SegmentChoiceArrangement)
        validateSegmentChoiceArrangement((SegmentChoiceArrangement) entity);
      else if (entity instanceof SegmentChoiceArrangementPick)
        validateSegmentChoiceArrangementPick((SegmentChoiceArrangementPick) entity);
      else if (entity instanceof SegmentChord)
        validateSegmentChord((SegmentChord) entity);
      else if (entity instanceof SegmentMeme)
        validateSegmentMeme((SegmentMeme) entity);
      else if (entity instanceof SegmentMessage)
        validateSegmentMessage((SegmentMessage) entity);
      else if (entity instanceof SegmentMeta)
        validateSegmentMeta((SegmentMeta) entity);

    } catch (ValueException e) {
      throw new ManagerValidationException(e);
    }
  }

  private void validateSegmentMessage(SegmentMessage record) throws ValueException {
    ValueUtils.require(record.getSegmentId(), "Segment ID");
    ValueUtils.require(record.getType(), "Type");
    MessageEntity.validate(record);
  }

  private void validateSegmentMeta(SegmentMeta record) throws ValueException {
    ValueUtils.require(record.getSegmentId(), "Segment ID");
    ValueUtils.require(record.getKey(), "Key");
    ValueUtils.require(record.getValue(), "Value");
  }

  private void validateSegmentMeme(SegmentMeme record) throws ValueException {
    ValueUtils.require(record.getSegmentId(), "Segment ID");
    ValueUtils.require(record.getName(), "Meme name");
    record.setName(StringUtils.toMeme(record.getName()));
  }

  private void validateSegmentChord(SegmentChord record) throws ValueException {
    ValueUtils.require(record.getSegmentId(), "Segment ID");
    ChordEntity.validate(record);
  }

  private void validateSegmentChoiceArrangementPick(SegmentChoiceArrangementPick record) throws ValueException {
    ValueUtils.require(record.getSegmentId(), "Segment ID");
    ValueUtils.require(record.getSegmentChoiceArrangementId(), "Arrangement ID");
    ValueUtils.require(record.getProgramSequencePatternEventId(), "Pattern Event ID");
    ValueUtils.require(record.getInstrumentAudioId(), "Audio ID");
    ValueUtils.require(record.getStartAtSegmentMicros(), "Start");
    if (Objects.nonNull(record.getLengthMicros()))
      ValueUtils.requireMinimum(LENGTH_MINIMUM_MICROS, record.getLengthMicros(), "Length");
    ValueUtils.require(record.getAmplitude(), "Amplitude");
    ValueUtils.requireMinimum((double) AMPLITUDE_MINIMUM, (double) record.getAmplitude(), "Amplitude");
    ValueUtils.require(record.getTones(), "Note");
  }

  private void validateSegmentChoiceArrangement(SegmentChoiceArrangement record) throws ValueException {
    ValueUtils.require(record.getSegmentId(), "Segment ID");
    ValueUtils.require(record.getSegmentChoiceId(), "Choice ID");
    ValueUtils.require(record.getProgramSequencePatternId(), "Program Sequence Pattern ID");
  }

  private void validateSegmentChoice(SegmentChoice record) throws ValueException {
    ValueUtils.require(record.getSegmentId(), "Segment ID");
    ValueUtils.require(record.getProgramId(), "Program ID");
    ValueUtils.require(record.getProgramType(), "Program Type");
    ValueUtils.require(record.getInstrumentId(), "Instrument ID");
    if (ValueUtils.isUnset(record.getDeltaIn())) record.setDeltaIn(Segment.DELTA_UNLIMITED);
    if (ValueUtils.isUnset(record.getDeltaOut())) record.setDeltaOut(Segment.DELTA_UNLIMITED);
  }

  private void validateSegment(Segment record) throws ValueException {
    ValueUtils.require(record.getChainId(), "Chain ID");
    ValueUtils.require(record.getId(), "Offset");
    if (ValueUtils.isEmpty(record.getWaveformPreroll())) record.setWaveformPreroll(0.0);
    if (ValueUtils.isEmpty(record.getWaveformPostroll())) record.setWaveformPostroll(0.0);
    if (ValueUtils.isEmpty(record.getDelta())) record.setDelta(0);
    ValueUtils.require(record.getType(), "Type");
    ValueUtils.require(record.getState(), "State");
    if (!SegmentType.PENDING.equals(record.getType()))
      ValueUtils.require(record.getBeginAtChainMicros(), "Begin-at");
  }
}
