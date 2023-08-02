// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.persistence;

import io.xj.hub.client.HubClientAccess;
import io.xj.hub.enums.ProgramType;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.entity.common.ChordEntity;
import io.xj.lib.entity.common.MessageEntity;
import io.xj.lib.util.CSV;
import io.xj.lib.util.StringUtils;
import io.xj.lib.util.ValueException;
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
import io.xj.nexus.model.SegmentState;
import io.xj.nexus.model.SegmentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static io.xj.lib.util.ValueUtils.MICROS_PER_SECOND;

/**
 * Nexus Managers are Singletons unless some other requirement changes that-- 'cuz here be cyclic dependencies...
 */
@Service
public class SegmentManagerImpl extends ManagerImpl<Segment> implements SegmentManager {
  public static final Long LENGTH_MINIMUM_MICROS = MICROS_PER_SECOND;
  public static final Double AMPLITUDE_MINIMUM = 0.0;

  @Autowired
  public SegmentManagerImpl(
    EntityFactory entityFactory,
    NexusEntityStore nexusEntityStore
  ) {
    super(entityFactory, nexusEntityStore);
  }

  /**
   * Require state is in an array of states
   *
   * @param toState       to check
   * @param allowedStates required to be in
   * @throws ValueException if not in required states
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
      toState, CSV.join(allowedStateNames)));
  }

  /**
   * Segment state transitions are protected, dependent on the state this segment is being transitioned of, and the intended state it is being transitioned to.
   *
   * @param fromState to protect transition of
   * @param toState   to test transition to
   * @throws ValueException on prohibited transition
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
  public Segment create(Segment entity) throws ManagerPrivilegeException, ManagerFatalException, ManagerValidationException {
    try {
      entity.setId(UUID.randomUUID());
      validate(entity);

      // [#126] Segments are always readMany in PLANNED state
      entity.setState(SegmentState.PLANNED);

      // create segment with Chain ID and offset are read-only, set at creation
      if (readOneAtChainOffset(entity.getChainId(), entity.getOffset()).isPresent()) {
        throw new ManagerValidationException("Found Segment at same offset in Chain!");
      }

      return store.put(entity);

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
  public List<Segment> readAllSpanning(UUID chainId, Long fromChainMicros, Long toChainMicros) {
    try {
      return store.getAllSegments(chainId)
        .stream()
        .filter(s -> Segments.isSpanning(s, fromChainMicros, toChainMicros))
        .toList();

    } catch (NexusException e) {
      return List.of();
    }
  }

  @Override
  public Segment readOne(UUID id) throws ManagerExistenceException, ManagerFatalException {
    try {
      return store.getSegment(id)
        .orElseThrow(() -> new ManagerExistenceException(Segment.class, id.toString()));

    } catch (NexusException e) {
      throw new ManagerFatalException(e);
    }
  }


  @Override
  public Optional<Segment> readOneAtChainMicros(UUID chainId, long chainMicros) {
    try {
      var segments = store.getAllSegments(chainId)
        .stream()
        .filter(s -> Segments.isSpanning(s, chainMicros, chainMicros))
        .sorted(Comparator.comparing(Segment::getOffset))
        .toList();
      return segments.isEmpty() ? Optional.empty() : Optional.of(segments.get(segments.size() - 1));
    } catch (NexusException e) {
      return Optional.empty();
    }
  }

  @Override
  public Optional<Segment> readOneAtChainOffset(UUID chainId, Long offset) {
    try {
      return store.getAllSegments(chainId)
        .stream()
        .filter(s -> offset.equals(s.getOffset()))
        .findFirst();

    } catch (NexusException e) {
      return Optional.empty();
    }
  }

  public Segment readOneInState(HubClientAccess access, UUID chainId, SegmentState segmentState, Long segmentBeginBeforeChainMicros) throws ManagerFatalException, ManagerExistenceException {
    try {
      return store.getAllSegments(chainId)
        .stream()
        .sorted(Comparator.comparing(Segment::getOffset))
        .filter(s -> segmentState.equals(s.getState()) &&
          segmentBeginBeforeChainMicros >= s.getBeginAtChainMicros())
        .findFirst()
        .orElseThrow(() -> new ManagerExistenceException(String.format("Found no Segment[state=%s] in Chain[%s]!", segmentState, chainId)));

    } catch (NexusException e) {
      throw new ManagerFatalException(e);
    }
  }

  @Override
  public <N> Collection<N> readManySubEntities(Collection<UUID> segmentIds, Boolean includePicks) throws ManagerFatalException {
    try {
      Collection<Object> entities = new ArrayList<>();
      for (UUID sId : segmentIds) {
        entities.addAll(store.getAll(sId, SegmentChoice.class, Segment.class, segmentIds));
        entities.addAll(store.getAll(sId, SegmentChoiceArrangement.class, Segment.class, segmentIds));
        entities.addAll(store.getAll(sId, SegmentChord.class, Segment.class, segmentIds));
        entities.addAll(store.getAll(sId, SegmentChordVoicing.class, Segment.class, segmentIds));
        entities.addAll(store.getAll(sId, SegmentMeme.class, Segment.class, segmentIds));
        entities.addAll(store.getAll(sId, SegmentMessage.class, Segment.class, segmentIds));
        entities.addAll(store.getAll(sId, SegmentMeta.class, Segment.class, segmentIds));
        if (includePicks)
          entities.addAll(store.getAll(sId, SegmentChoiceArrangementPick.class, Segment.class, segmentIds));
      }
      //noinspection unchecked
      return (Collection<N>) entities;

    } catch (NexusException e) {
      throw new ManagerFatalException(e);
    }
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
  public Collection<Segment> readMany(Collection<UUID> chainIds) throws ManagerFatalException {
    try {
      Collection<Segment> segments = new ArrayList<>();
      for (UUID chainId : chainIds)
        store.getAllSegments(chainId)
          .stream()
          .sorted(Comparator.comparing(Segment::getOffset))
          .forEach(segments::add);
      return segments;

    } catch (NexusException e) {
      throw new ManagerFatalException(e);
    }
  }

  @Override
  public Collection<Segment> readManyFromToOffset(UUID chainId, Long fromOffset, Long toOffset) throws ManagerFatalException {
    try {
      return 0 > toOffset ?
        new ArrayList<>() :
        store.getAllSegments(chainId)
          .stream()
          .filter(s -> s.getOffset() >= fromOffset && s.getOffset() <= toOffset)
          .sorted(Comparator.comparing(Segment::getOffset))
          .collect(Collectors.toList());

    } catch (NexusException e) {
      throw new ManagerFatalException(e);
    }
  }

  @Override
  public Segment update(UUID id, Segment entity) throws ManagerPrivilegeException, ManagerFatalException, ManagerExistenceException, ManagerValidationException {
    try {
      // validate and cache to-state
      validate(entity);
      SegmentState toState = entity.getState();

      // fetch existing segment; further logic is based on its current state
      Segment existing = store.getSegment(id)
        .orElseThrow(() -> new ManagerExistenceException(Segment.class, id.toString()));
      requireExists("Segment #" + id, existing);

      // logic based on existing Segment State
      protectSegmentStateTransition(existing.getState(), toState);

      // fail if attempt to [#128] change chainId of a segment
      Object updateChainId = entity.getChainId();
      if (ValueUtils.isSet(updateChainId) && !Objects.equals(updateChainId, existing.getChainId()))
        throw new ManagerValidationException("cannot change chainId create a segment");

      // Never change id
      entity.setId(id);

      // save segment
      store.put(entity);
      return entity;

    } catch (NexusException e) {
      throw new ManagerFatalException(e);

    } catch (ValueException e) {
      throw new ManagerValidationException(e);
    }
  }

  @Override
  public Optional<Segment> readLastSegment(UUID chainId) throws ManagerFatalException {
    try {
      return store.getAllSegments(chainId)
        .stream()
        .max(Comparator.comparing(Segment::getOffset));

    } catch (NexusException e) {
      throw new ManagerFatalException(e);
    }
  }

  @Override
  public Optional<Segment> readLastCraftedSegment(HubClientAccess access, UUID chainId) throws ManagerFatalException {
    try {
      return Segments.getLastCrafted(store.getAllSegments(chainId));

    } catch (NexusException e) {
      throw new ManagerFatalException(e);
    }
  }

  @Override
  public Optional<SegmentChoice> readChoice(UUID segmentId, ProgramType programType) throws ManagerFatalException {
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
  public boolean exists(UUID id) {
    return store.segmentExists(id);
  }

  @Override
  public Chain getChain(Segment segment) throws NexusException, ManagerFatalException {
    return store.getChain(segment.getChainId())
      .orElseThrow(() -> new ManagerFatalException("Segment #" + segment.getId() + " has no chain"));
  }

  @Override
  public void destroy(UUID id) throws ManagerFatalException {
    try {
      store.deleteSegment(id);

    } catch (NexusException e) {
      throw new ManagerFatalException(e);
    }
  }

  /**
   * Validate a segment or child entity
   *
   * @param entity to validate
   * @throws ManagerValidationException if invalid
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
    if (ValueUtils.isUnset(record.getDeltaIn())) record.setDeltaIn(Segments.DELTA_UNLIMITED);
    if (ValueUtils.isUnset(record.getDeltaOut())) record.setDeltaOut(Segments.DELTA_UNLIMITED);
  }

  void validateSegment(Segment record) throws ValueException {
    ValueUtils.require(record.getChainId(), "Chain ID");
    ValueUtils.require(record.getOffset(), "Offset");
    if (ValueUtils.isEmpty(record.getWaveformPreroll())) record.setWaveformPreroll(0.0);
    if (ValueUtils.isEmpty(record.getWaveformPostroll())) record.setWaveformPostroll(0.0);
    if (ValueUtils.isEmpty(record.getDelta())) record.setDelta(0);
    ValueUtils.require(record.getType(), "Type");
    ValueUtils.require(record.getState(), "State");
    if (!SegmentType.PENDING.equals(record.getType()))
      ValueUtils.require(record.getBeginAtChainMicros(), "Begin-at");
  }

}
