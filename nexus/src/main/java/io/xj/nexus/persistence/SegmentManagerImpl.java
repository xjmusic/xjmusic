// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.persistence;

import io.xj.hub.enums.ProgramType;
import io.xj.hub.util.CsvUtils;
import io.xj.hub.util.StringUtils;
import io.xj.hub.util.ValueException;
import io.xj.hub.util.ValueUtils;
import io.xj.lib.entity.common.ChordEntity;
import io.xj.lib.entity.common.MessageEntity;
import io.xj.nexus.NexusException;
import io.xj.nexus.hub_client.HubClientAccess;
import io.xj.nexus.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.xj.hub.util.ValueUtils.MICROS_PER_SECOND;

/**
 Nexus Managers are Singletons unless some other requirement changes that-- 'cuz here be cyclic dependencies...
 */
@Service
public class SegmentManagerImpl implements SegmentManager {
  static final Logger LOG = LoggerFactory.getLogger(SegmentManagerImpl.class);
  public static final Long LENGTH_MINIMUM_MICROS = MICROS_PER_SECOND;
  public static final Double AMPLITUDE_MINIMUM = 0.0;
  private final NexusEntityStore store;

  @Autowired
  public SegmentManagerImpl(
    NexusEntityStore store
  ) {
    this.store = store;
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

  @Override
  public Segment create(Segment segment) throws ManagerPrivilegeException, ManagerFatalException, ManagerValidationException {
    try {
      validate(segment);

      // [#126] Segments are always readMany in PLANNED state
      segment.setState(SegmentState.PLANNED);

      // create segment with Chain ID and offset are read-only, set at creation
      if (readOneAtChainOffset(segment.getId()).isPresent()) {
        throw new ManagerValidationException("Found Segment at same offset in Chain!");
      }

      return store.put(segment);

    } catch (NexusException e) {
      throw new ManagerFatalException(e);
    }
  }

  @Override
  public SegmentMessage create(HubClientAccess access, SegmentMessage entity) throws ManagerPrivilegeException, ManagerValidationException, ManagerFatalException {
    try {
      validate(entity);
      return store.put(entity);

    } catch (NexusException e) {
      throw new ManagerFatalException(e);
    }
  }

  @Override
  public SegmentMeta create(HubClientAccess access, SegmentMeta entity) throws ManagerPrivilegeException, ManagerValidationException, ManagerFatalException {
    try {
      validate(entity);
      return store.put(entity);

    } catch (NexusException e) {
      throw new ManagerFatalException(e);
    }
  }

  @Override
  public List<Segment> readAll() {
    try {
      return store.getAllSegments()
        .stream()
        .sorted(Comparator.comparing(Segment::getId))
        .toList();

    } catch (NexusException e) {
      return List.of();
    }
  }

  @Override
  public void reset() {
    try {
      store.deleteAll();
    } catch (NexusException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public List<Segment> readAllSpanning(Long fromChainMicros, Long toChainMicros) {
    try {
      return store.getAllSegments()
        .stream()
        .filter(s -> SegmentUtils.isSpanning(s, fromChainMicros, toChainMicros))
        .sorted(Comparator.comparing(Segment::getId))
        .toList();

    } catch (NexusException e) {
      return List.of();
    }
  }

  @Override
  public Segment readOne(int segmentId) throws ManagerExistenceException, ManagerFatalException {
    try {
      return store.getSegment(segmentId)
        .orElseThrow(() -> new ManagerExistenceException(Segment.class, Integer.toString(segmentId)));

    } catch (NexusException e) {
      throw new ManagerFatalException(e);
    }
  }


  @Override
  public Optional<Segment> readOneAtChainMicros(long chainMicros) {
    try {
      var segments = store.getAllSegments()
        .stream()
        .filter(s -> SegmentUtils.isSpanning(s, chainMicros, chainMicros))
        .sorted(Comparator.comparing(Segment::getId))
        .toList();
      return segments.isEmpty() ? Optional.empty() : Optional.of(segments.get(segments.size() - 1));
    } catch (NexusException e) {
      return Optional.empty();
    }
  }

  @Override
  public Optional<Segment> readOneAtChainOffset(int offset) {
    try {
      if (store.getAllSegments().size() <= offset) return Optional.empty();
      return Optional.of(store.getAllSegments().get(offset));
    } catch (Exception e) {
      LOG.error("Failed to read Segment at offset " + offset, e);
      return Optional.empty();
    }
  }

  public Segment readFirstInState(HubClientAccess access, SegmentState segmentState, Long segmentBeginBeforeChainMicros) throws ManagerFatalException, ManagerExistenceException {
    try {
      return store.getAllSegments()
        .stream()
        .sorted(Comparator.comparing(Segment::getId))
        .filter(s -> segmentState.equals(s.getState()) &&
          segmentBeginBeforeChainMicros >= s.getBeginAtChainMicros())
        .findFirst()
        .orElseThrow(() -> new ManagerExistenceException(String.format("Found no Segment[state=%s]!", segmentState)));

    } catch (NexusException e) {
      throw new ManagerFatalException(e);
    }
  }

  @Override
  public <N> Collection<N> readManySubEntities(Collection<Integer> segmentIds, Boolean includePicks) throws ManagerFatalException {
    try {
      Collection<Object> entities = new ArrayList<>();
      for (Integer sId : segmentIds) {
        entities.addAll(store.getAll(sId, SegmentChoice.class));
        entities.addAll(store.getAll(sId, SegmentChoiceArrangement.class));
        entities.addAll(store.getAll(sId, SegmentChord.class));
        entities.addAll(store.getAll(sId, SegmentChordVoicing.class));
        entities.addAll(store.getAll(sId, SegmentMeme.class));
        entities.addAll(store.getAll(sId, SegmentMessage.class));
        entities.addAll(store.getAll(sId, SegmentMeta.class));
        if (includePicks)
          entities.addAll(store.getAll(sId, SegmentChoiceArrangementPick.class));
      }
      //noinspection unchecked
      return (Collection<N>) entities;

    } catch (NexusException e) {
      throw new ManagerFatalException(e);
    }
  }

  @Override
  public <N> Collection<N> readManySubEntitiesOfType(int segmentId, Class<N> type) throws ManagerFatalException {
    try {
      return store.getAll(segmentId, type);
    } catch (NexusException e) {
      throw new ManagerFatalException(e);
    }
  }

  @Override
  public <N> Collection<N> readManySubEntitiesOfType(Collection<Integer> segmentIds, Class<N> type) {
    return segmentIds.stream().flatMap(segmentId -> {
      try {
        return readManySubEntitiesOfType(segmentId, type).stream();
      } catch (ManagerFatalException e) {
        return Stream.empty();
      }
    }).toList();
  }

  @Override
  public <N> void createAllSubEntities(Collection<N> entities) throws ManagerFatalException {
    try {
      store.putAll(entities);

    } catch (NexusException e) {
      throw new ManagerFatalException(e);
    }
  }

  @Override
  public Collection<Segment> readManyFromToOffset(Long fromOffset, Long toOffset) throws ManagerFatalException {
    try {
      return 0 > toOffset ?
        new ArrayList<>() :
        store.getAllSegments()
          .stream()
          .filter(s -> s.getId() >= fromOffset && s.getId() <= toOffset)
          .sorted(Comparator.comparing(Segment::getId))
          .collect(Collectors.toList());

    } catch (NexusException e) {
      throw new ManagerFatalException(e);
    }
  }

  @Override
  public Segment update(int segmentId, Segment segment) throws ManagerPrivilegeException, ManagerFatalException, ManagerExistenceException, ManagerValidationException {
    try {
      // validate and cache to-state
      validate(segment);
      SegmentState toState = segment.getState();

      // fetch existing segment; further logic is based on its current state
      Segment existing = store.getSegment(segmentId)
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

      // save segment
      store.put(segment);
      return segment;

    } catch (NexusException e) {
      throw new ManagerFatalException(e);

    } catch (ValueException e) {
      throw new ManagerValidationException(e);
    }
  }

  @Override
  public Optional<Segment> readLastSegment() throws ManagerFatalException {
    try {
      return store.getAllSegments()
        .stream()
        .max(Comparator.comparing(Segment::getId));

    } catch (NexusException e) {
      throw new ManagerFatalException(e);
    }
  }

  @Override
  public Optional<Segment> readLastCraftedSegment(HubClientAccess access) throws ManagerFatalException {
    try {
      return SegmentUtils.getLastCrafted(store.getAllSegments());

    } catch (NexusException e) {
      throw new ManagerFatalException(e);
    }
  }

  @Override
  public Optional<SegmentChoice> readChoice(int segmentId, ProgramType programType) throws ManagerFatalException {
    try {
      return store.getAll(segmentId, SegmentChoice.class)
        .stream()
        .filter(sc -> programType.equals(sc.getProgramType()))
        .findAny();

    } catch (NexusException e) {
      throw new ManagerFatalException(e);
    }


  }

  @Override
  public Chain getChain(Segment segment) throws NexusException, ManagerFatalException {
    return store.getChain()
      .orElseThrow(() -> new ManagerFatalException("Segment #" + segment.getId() + " has no chain"));
  }

  /**
   Validate a segment or child entity

   @param entity to validate
   @throws ManagerValidationException if invalid
   */
  public void validate(Object entity) throws ManagerValidationException {
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

  void validateSegmentMessage(SegmentMessage record) throws ValueException {
    ValueUtils.require(record.getSegmentId(), "Segment ID");
    ValueUtils.require(record.getType(), "Type");
    MessageEntity.validate(record);
  }

  void validateSegmentMeta(SegmentMeta record) throws ValueException {
    ValueUtils.require(record.getSegmentId(), "Segment ID");
    ValueUtils.require(record.getKey(), "Key");
    ValueUtils.require(record.getValue(), "Value");
  }

  void validateSegmentMeme(SegmentMeme record) throws ValueException {
    ValueUtils.require(record.getSegmentId(), "Segment ID");
    ValueUtils.require(record.getName(), "Meme name");
    record.setName(StringUtils.toMeme(record.getName()));
  }

  void validateSegmentChord(SegmentChord record) throws ValueException {
    ValueUtils.require(record.getSegmentId(), "Segment ID");
    ChordEntity.validate(record);
  }

  void validateSegmentChoiceArrangementPick(SegmentChoiceArrangementPick record) throws ValueException {
    ValueUtils.require(record.getSegmentId(), "Segment ID");
    ValueUtils.require(record.getSegmentChoiceArrangementId(), "Arrangement ID");
    ValueUtils.require(record.getProgramSequencePatternEventId(), "Pattern Event ID");
    ValueUtils.require(record.getInstrumentAudioId(), "Audio ID");
    ValueUtils.require(record.getStartAtSegmentMicros(), "Start");
    if (Objects.nonNull(record.getLengthMicros()))
      ValueUtils.requireMinimum(LENGTH_MINIMUM_MICROS, record.getLengthMicros(), "Length");
    ValueUtils.require(record.getAmplitude(), "Amplitude");
    ValueUtils.requireMinimum(AMPLITUDE_MINIMUM, record.getAmplitude(), "Amplitude");
    ValueUtils.require(record.getTones(), "Note");
  }

  void validateSegmentChoiceArrangement(SegmentChoiceArrangement record) throws ValueException {
    ValueUtils.require(record.getSegmentId(), "Segment ID");
    ValueUtils.require(record.getSegmentChoiceId(), "Choice ID");
    ValueUtils.require(record.getProgramSequencePatternId(), "Program Sequence Pattern ID");
  }

  void validateSegmentChoice(SegmentChoice record) throws ValueException {
    ValueUtils.require(record.getSegmentId(), "Segment ID");
    ValueUtils.require(record.getProgramId(), "Program ID");
    ValueUtils.require(record.getProgramType(), "Program Type");
    ValueUtils.require(record.getInstrumentId(), "Instrument ID");
    if (ValueUtils.isUnset(record.getDeltaIn())) record.setDeltaIn(Segment.DELTA_UNLIMITED);
    if (ValueUtils.isUnset(record.getDeltaOut())) record.setDeltaOut(Segment.DELTA_UNLIMITED);
  }

  void validateSegment(Segment record) throws ValueException {
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

  /**
   Require that an entity is non-null

   @param name   name of entity (for error message)
   @param entity to require existence of
   @throws ManagerExistenceException if not isNonNull
   */
  protected <E> void requireExists(String name, E entity) throws ManagerExistenceException {
    if (!ValueUtils.isNonNull(entity)) throw new ManagerExistenceException(String.format("%s does not exist!", name));
  }
}
