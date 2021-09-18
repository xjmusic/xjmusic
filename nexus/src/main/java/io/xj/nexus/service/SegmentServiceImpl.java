// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.service;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.typesafe.config.Config;
import io.xj.api.Segment;
import io.xj.api.SegmentChoice;
import io.xj.api.SegmentChoiceArrangement;
import io.xj.api.SegmentChoiceArrangementPick;
import io.xj.api.SegmentChord;
import io.xj.api.SegmentChordVoicing;
import io.xj.api.SegmentMeme;
import io.xj.api.SegmentMessage;
import io.xj.api.SegmentState;
import io.xj.api.SegmentType;
import io.xj.lib.entity.EntityException;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.entity.common.ChordEntity;
import io.xj.lib.entity.common.MessageEntity;
import io.xj.lib.util.CSV;
import io.xj.lib.util.Text;
import io.xj.lib.util.Value;
import io.xj.lib.util.ValueException;
import io.xj.nexus.NexusException;
import io.xj.nexus.Segments;
import io.xj.nexus.hub_client.client.HubClientAccess;
import io.xj.nexus.persistence.NexusEntityStore;
import io.xj.nexus.service.exception.ServiceExistenceException;
import io.xj.nexus.service.exception.ServiceFatalException;
import io.xj.nexus.service.exception.ServicePrivilegeException;
import io.xj.nexus.service.exception.ServiceValidationException;

import java.time.Instant;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

/**
 Nexus Services are Singletons unless some other requirement changes that-- 'cuz here be cyclic dependencies...
 */
@Singleton
public class SegmentServiceImpl extends ServiceImpl<Segment> implements SegmentService {
  public static final Double LENGTH_MINIMUM = 0.01; //
  public static final Double AMPLITUDE_MINIMUM = 0.0; //
  private final ChainService chainService;
  private final int workBufferAheadSeconds;
  private final int workBufferBeforeSeconds;

  @Inject
  public SegmentServiceImpl(
    EntityFactory entityFactory,
    NexusEntityStore nexusEntityStore,
    ChainService chainService,
    Config config
  ) {
    super(entityFactory, nexusEntityStore);
    this.chainService = chainService;

    workBufferAheadSeconds = config.getInt("work.bufferAheadSeconds");
    workBufferBeforeSeconds = config.getInt("work.bufferBeforeSeconds");
  }

  /**
   Require state is in an array of states

   @param toState       to check
   @param allowedStates required to be in
   @throws ValueException if not in required states
   */
  public static void onlyAllowSegmentStateTransitions(SegmentState toState, SegmentState... allowedStates) throws ValueException {
    List<String> allowedStateNames = Lists.newArrayList();
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
   Segment state transitions are protected, dependent on the state this segment is being transitioned of, and the intended state it is being transitioned to.

   @param fromState to protect transition of
   @param toState   to test transition to
   @throws ValueException on prohibited transition
   */
  public static void protectSegmentStateTransition(SegmentState fromState, SegmentState toState) throws ValueException {
    switch (fromState) {
      case PLANNED -> onlyAllowSegmentStateTransitions(toState, SegmentState.PLANNED, SegmentState.CRAFTING);
      case CRAFTING -> onlyAllowSegmentStateTransitions(toState, SegmentState.CRAFTING, SegmentState.CRAFTED, SegmentState.DUBBING, SegmentState.FAILED, SegmentState.PLANNED);
      case CRAFTED -> onlyAllowSegmentStateTransitions(toState, SegmentState.CRAFTED, SegmentState.DUBBING);
      case DUBBING -> onlyAllowSegmentStateTransitions(toState, SegmentState.DUBBING, SegmentState.DUBBED, SegmentState.FAILED);
      case DUBBED -> onlyAllowSegmentStateTransitions(toState, SegmentState.DUBBED);
      case FAILED -> onlyAllowSegmentStateTransitions(toState, SegmentState.FAILED);
      default -> onlyAllowSegmentStateTransitions(toState, SegmentState.PLANNED);
    }
  }

  @Override
  public Segment create(Segment entity) throws ServicePrivilegeException, ServiceFatalException, ServiceValidationException {
    try {
      entity.setId(UUID.randomUUID());
      validate(entity);

      // [#126] Segments are always readMany in PLANNED state
      entity.setState(SegmentState.PLANNED);

      // create segment with Chain ID and offset are read-only, set at creation
      requireNotSameOffsetInChain(
        () -> readOneAtChainOffset(entity.getChainId(), entity.getOffset()));

      return store.put(entity);

    } catch (NexusException e) {
      throw new ServiceFatalException(e);
    }
  }

  @Override
  public SegmentMessage create(HubClientAccess access, SegmentMessage entity) throws ServicePrivilegeException, ServiceValidationException, ServiceFatalException {
    try {
      validate(entity);
      return store.put(entity);

    } catch (NexusException e) {
      throw new ServiceFatalException(e);
    }
  }

  @Override
  public Segment readOne(UUID id) throws ServiceExistenceException, ServiceFatalException {
    try {
      Segment segment = store.getSegment(id)
        .orElseThrow(() -> new ServiceExistenceException(Segment.class, id.toString()));
      return segment;

    } catch (NexusException e) {
      throw new ServiceFatalException(e);
    }
  }


  @Override
  public Segment readOneAtChainOffset(UUID chainId, Long offset) throws ServiceExistenceException, ServiceFatalException {
    try {
      return store.getAllSegments(chainId)
        .stream()
        .filter(s -> offset.equals(s.getOffset()))
        .findFirst()
        .orElseThrow(() -> new ServiceExistenceException(String.format("Found no Segment@%d in Chain[%s]!", offset, chainId)));

    } catch (NexusException e) {
      throw new ServiceFatalException(e);
    }
  }

  public Segment readOneInState(HubClientAccess access, UUID chainId, SegmentState segmentState, Instant segmentBeginBefore) throws ServiceFatalException, ServiceExistenceException {
    try {
      return store.getAllSegments(chainId)
        .stream()
        .sorted(Comparator.comparing(Segment::getOffset))
        .filter(s -> segmentState.equals(s.getState()) &&
          (segmentBeginBefore.equals(Instant.parse(s.getBeginAt())) ||
            segmentBeginBefore.isAfter(Instant.parse(s.getBeginAt()))))
        .findFirst()
        .orElseThrow(() -> new ServiceExistenceException(String.format("Found no Segment[state=%s] in Chain[%s]!", segmentState, chainId)));

    } catch (NexusException e) {
      throw new ServiceFatalException(e);
    }
  }

  @Override
  public Collection<Segment> readManyByShipKey(HubClientAccess access, String chainShipKey) throws ServicePrivilegeException, ServiceFatalException, ServiceExistenceException {
    try {
      var chainId = chainService.readOneByShipKey(chainShipKey).getId();
      return store.getAllSegments(chainId)
        .stream()
        .sorted(Comparator.comparing(Segment::getOffset).reversed())
        .collect(Collectors.toList());

    } catch (NexusException e) {
      throw new ServiceFatalException(e);
    }
  }

  @Override
  public <N> Collection<N> readManySubEntities(Collection<UUID> segmentIds, Boolean includePicks) throws ServiceFatalException {
    try {
      Collection<Object> entities = Lists.newArrayList();
      for (UUID sId : segmentIds) {
        entities.addAll(store.getAll(sId, SegmentMeme.class, Segment.class, segmentIds));
        entities.addAll(store.getAll(sId, SegmentChord.class, Segment.class, segmentIds));
        entities.addAll(store.getAll(sId, SegmentChordVoicing.class, Segment.class, segmentIds));
        entities.addAll(store.getAll(sId, SegmentChoice.class, Segment.class, segmentIds));
        entities.addAll(store.getAll(sId, SegmentMessage.class, Segment.class, segmentIds));
        entities.addAll(store.getAll(sId, SegmentChoiceArrangement.class, Segment.class, segmentIds));
        if (includePicks)
          entities.addAll(store.getAll(sId, SegmentChoiceArrangementPick.class, Segment.class, segmentIds));
      }
      //noinspection unchecked
      return (Collection<N>) entities;

    } catch (NexusException e) {
      throw new ServiceFatalException(e);
    }
  }

  @Override
  public <N> void createAllSubEntities(Collection<N> entities) throws ServiceFatalException {
    try {
      store.putAll(entities);

    } catch (NexusException e) {
      throw new ServiceFatalException(e);
    }
  }

  @Override
  public Collection<Segment> readMany(Collection<UUID> chainIds) throws ServiceFatalException {
    try {
      Collection<Segment> segments = Lists.newArrayList();
      for (UUID chainId : chainIds)
        store.getAllSegments(chainId)
          .stream()
          .sorted(Comparator.comparing(Segment::getOffset))
          .forEach(segments::add);
      return segments;

    } catch (NexusException e) {
      throw new ServiceFatalException(e);
    }
  }

  @Override
  public Collection<Segment> readManyFromToOffset(UUID chainId, Long fromOffset, Long toOffset) throws ServiceFatalException {
    try {
      return 0 > toOffset ?
        Lists.newArrayList() :
        store.getAllSegments(chainId)
          .stream()
          .filter(s -> s.getOffset() >= fromOffset && s.getOffset() <= toOffset)
          .sorted(Comparator.comparing(Segment::getOffset))
          .collect(Collectors.toList());

    } catch (NexusException e) {
      throw new ServiceFatalException(e);
    }
  }

  @Override
  public Collection<Segment> readManyFromSecondsUTC(HubClientAccess access, UUID chainId, Long fromSecondsUTC) throws ServiceFatalException {
    try {
      Instant from = Instant.ofEpochSecond(fromSecondsUTC);
      Instant maxBeginAt = from.plusSeconds(workBufferAheadSeconds);
      Instant minEndAt = from.minusSeconds(workBufferBeforeSeconds);
      return store.getAllSegments(chainId)
        .stream()
        .filter(s -> Value.isSet(s.getEndAt()) &&
          maxBeginAt.isAfter(Instant.parse(s.getBeginAt())) &&
          !Strings.isNullOrEmpty(s.getEndAt()) &&
          minEndAt.isBefore(Instant.parse(s.getEndAt())))
        .sorted(Comparator.comparing(Segment::getOffset))
        .collect(Collectors.toList());

    } catch (NexusException e) {
      throw new ServiceFatalException(e);
    }
  }

  @Override
  public Collection<Segment> readManyFromSecondsUTCbyShipKey(HubClientAccess access, String chainShipKey, Long fromSecondsUTC) throws ServicePrivilegeException, ServiceFatalException, ServiceExistenceException {
    return readManyFromSecondsUTC(access,
      chainService.readOneByShipKey(chainShipKey).getId(),
      fromSecondsUTC);
  }

  @Override
  public Segment update(UUID id, Segment entity) throws ServicePrivilegeException, ServiceFatalException, ServiceExistenceException, ServiceValidationException {
    try {
      // validate and cache to-state
      validate(entity);
      SegmentState toState = entity.getState();

      // fetch existing segment; further logic is based on its current state
      Segment existing = store.getSegment(id)
        .orElseThrow(() -> new ServiceExistenceException(Segment.class, id.toString()));
      requireExists("Segment #" + id, existing);

      // logic based on existing Segment State
      protectSegmentStateTransition(existing.getState(), toState);

      // fail if attempt to [#128] change chainId of a segment
      Object updateChainId = entity.getChainId();
      if (Value.isSet(updateChainId) && !Objects.equals(updateChainId, existing.getChainId()))
        throw new ServiceValidationException("cannot change chainId create a segment");

      // Never change id
      entity.setId(id);

      // save segment
      store.put(entity);
      return entity;

    } catch (NexusException e) {
      throw new ServiceFatalException(e);

    } catch (ValueException e) {
      throw new ServiceValidationException(e);
    }
  }

  @Override
  public void revert(HubClientAccess access, UUID id) throws ServicePrivilegeException, ServiceFatalException, ServiceExistenceException, ServiceValidationException {
    try {
      Segment segment = readOne(id);

      // Destroy child entities of segment-- but not the messages
      store.deleteAll(id, SegmentChoiceArrangementPick.class);
      store.deleteAll(id, SegmentChoiceArrangement.class);
      store.deleteAll(id, SegmentChoice.class);
      store.deleteAll(id, SegmentMeme.class);
      store.deleteAll(id, SegmentChord.class);
      store.deleteAll(id, SegmentMessage.class);

      update(id, segment);

    } catch (NexusException e) {
      throw new ServiceFatalException(e);
    }
  }

  @Override
  public Optional<Segment> readLastSegment(UUID chainId) throws ServiceFatalException {
    try {
      return store.getAllSegments(chainId)
        .stream()
        .max(Comparator.comparing(Segment::getOffset));

    } catch (NexusException e) {
      throw new ServiceFatalException(e);
    }
  }

  @Override
  public Optional<Segment> readLastDubbedSegment(HubClientAccess access, UUID chainId) throws ServiceFatalException {
    try {
      return Segments.getLastDubbed(store.getAllSegments(chainId));

    } catch (NexusException e) {
      throw new ServiceFatalException(e);
    }
  }

  @Override
  public void destroy(UUID id) throws ServiceFatalException {
    try {
      store.deleteSegment(id);

    } catch (NexusException e) {
      throw new ServiceFatalException(e);
    }
  }


  @Override
  public Segment newInstance() {
    try {
      return entityFactory.getInstance(Segment.class);
    } catch (EntityException ignored) {
      return new Segment();
    }
  }

  /**
   Validate a segment or child entity

   @param entity to validate
   @throws ServiceValidationException if invalid
   */
  public void validate(Object entity) throws ServiceValidationException {
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

    } catch (ValueException e) {
      throw new ServiceValidationException(e);
    }
  }

  private void validateSegmentMessage(SegmentMessage record) throws ValueException {
    Value.require(record.getSegmentId(), "Segment ID");
    Value.require(record.getType(), "Type");
    MessageEntity.validate(record);
  }

  private void validateSegmentMeme(SegmentMeme record) throws ValueException {
    Value.require(record.getSegmentId(), "Segment ID");
    Value.require(record.getName(), "Meme name");
    record.setName(Text.toMeme(record.getName()));
  }

  private void validateSegmentChord(SegmentChord record) throws ValueException {
    Value.require(record.getSegmentId(), "Segment ID");
    ChordEntity.validate(record);
  }

  private void validateSegmentChoiceArrangementPick(SegmentChoiceArrangementPick record) throws ValueException {
    Value.require(record.getSegmentId(), "Segment ID");
    Value.require(record.getSegmentChoiceArrangementId(), "Arrangement ID");
    Value.require(record.getProgramSequencePatternEventId(), "Pattern Event ID");
    Value.require(record.getInstrumentAudioId(), "Audio ID");
    Value.require(record.getStart(), "Start");
    Value.require(record.getLength(), "Length");
    Value.requireMinimum(LENGTH_MINIMUM, record.getLength(), "Length");
    Value.require(record.getAmplitude(), "Amplitude");
    Value.requireMinimum(AMPLITUDE_MINIMUM, record.getAmplitude(), "Amplitude");
    Value.require(record.getNote(), "Note");
  }

  private void validateSegmentChoiceArrangement(SegmentChoiceArrangement record) throws ValueException {
    Value.require(record.getSegmentId(), "Segment ID");
    Value.require(record.getSegmentChoiceId(), "Choice ID");
    Value.require(record.getProgramSequencePatternId(), "Program Sequence Pattern ID");
  }

  private void validateSegmentChoice(SegmentChoice record) throws ValueException {
    Value.require(record.getSegmentId(), "Segment ID");
    Value.require(record.getProgramId(), "Program ID");
    Value.require(record.getProgramType(), "Program Type");
    Value.require(record.getInstrumentId(), "Instrument ID");
    if (Value.isUnset(record.getDeltaIn())) record.setDeltaIn(Segments.DELTA_UNLIMITED);
    if (Value.isUnset(record.getDeltaOut())) record.setDeltaOut(Segments.DELTA_UNLIMITED);
  }

  private void validateSegment(Segment record) throws ValueException {
    Value.require(record.getChainId(), "Chain ID");
    Value.require(record.getOffset(), "Offset");
    if (Value.isEmpty(record.getWaveformPreroll())) record.setWaveformPreroll(0.0);
    if (Value.isEmpty(record.getDelta())) record.setDelta(0);
    Value.require(record.getType(), "Type");
    Value.require(record.getState(), "State");
    if (!SegmentType.PENDING.equals(record.getType()))
      Value.require(record.getBeginAt(), "Begin-at");
  }


  /**
   Require the given runnable throws an exception.@param mustThrowException when run, this must throw an exception
   */
  protected void requireNotSameOffsetInChain(Callable<?> mustThrowException) throws ServiceValidationException {
    try {
      mustThrowException.call();
    } catch (Exception ignored) {
      return;
    }
    throw new ServiceValidationException("Found Segment at same offset in Chain!");
  }

}
