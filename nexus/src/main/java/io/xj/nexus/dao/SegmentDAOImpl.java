// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.dao;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.typesafe.config.Config;
import io.xj.api.Chain;
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
import io.xj.nexus.dao.exception.DAOExistenceException;
import io.xj.nexus.dao.exception.DAOFatalException;
import io.xj.nexus.dao.exception.DAOPrivilegeException;
import io.xj.nexus.dao.exception.DAOValidationException;
import io.xj.nexus.hub_client.client.HubClientAccess;
import io.xj.nexus.persistence.NexusEntityStore;

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
 Nexus DAOs are Singletons unless some other requirement changes that-- 'cuz here be cyclic dependencies...
 */
@Singleton
public class SegmentDAOImpl extends DAOImpl<Segment> implements SegmentDAO {
  public static final Double LENGTH_MINIMUM = 0.01; //
  public static final Double AMPLITUDE_MINIMUM = 0.0; //
  private final ChainDAO chainDAO;
  private final int workBufferAheadSeconds;
  private final int workBufferBeforeSeconds;

  @Inject
  public SegmentDAOImpl(
    EntityFactory entityFactory,
    NexusEntityStore nexusEntityStore,
    ChainDAO chainDAO,
    Config config
  ) {
    super(entityFactory, nexusEntityStore);
    this.chainDAO = chainDAO;

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
  public Segment create(HubClientAccess access, Segment entity) throws DAOPrivilegeException, DAOFatalException, DAOValidationException {
    try {
      entity.setId(UUID.randomUUID());
      requireTopLevel(access);
      validate(entity);

      // [#126] Segments are always readMany in PLANNED state
      entity.setState(SegmentState.PLANNED);

      // create segment with Chain ID and offset are read-only, set at creation
      requireNotSameOffsetInChain(
        () -> readOneAtChainOffset(access, entity.getChainId(), entity.getOffset()));

      return store.put(entity);

    } catch (NexusException e) {
      throw new DAOFatalException(e);
    }
  }

  @Override
  public SegmentMessage create(HubClientAccess access, SegmentMessage entity) throws DAOPrivilegeException, DAOValidationException, DAOFatalException {
    try {
      requireTopLevel(access);
      validate(entity);
      return store.put(entity);

    } catch (NexusException e) {
      throw new DAOFatalException(e);
    }
  }

  @Override
  public Segment readOne(HubClientAccess access, UUID id) throws DAOPrivilegeException, DAOExistenceException, DAOFatalException {
    try {
      Segment segment = store.getSegment(id)
        .orElseThrow(() -> new DAOExistenceException(Segment.class, id.toString()));
      requireChainAccount(access, segment.getChainId());
      return segment;

    } catch (NexusException e) {
      throw new DAOFatalException(e);
    }
  }


  @Override
  public Segment readOneAtChainOffset(HubClientAccess access, UUID chainId, Long offset) throws DAOPrivilegeException, DAOExistenceException, DAOFatalException {
    try {
      requireTopLevel(access);
      return store.getAllSegments(chainId)
        .stream()
        .filter(s -> offset.equals(s.getOffset()))
        .findFirst()
        .orElseThrow(() -> new DAOExistenceException(String.format("Found no Segment@%d in Chain[%s]!", offset, chainId)));

    } catch (NexusException e) {
      throw new DAOFatalException(e);
    }
  }

  public Segment readOneInState(HubClientAccess access, UUID chainId, SegmentState segmentState, Instant segmentBeginBefore) throws DAOPrivilegeException, DAOFatalException, DAOExistenceException {
    try {
      requireTopLevel(access);
      return store.getAllSegments(chainId)
        .stream()
        .sorted(Comparator.comparing(Segment::getOffset))
        .filter(s -> segmentState.equals(s.getState()) &&
          (segmentBeginBefore.equals(Instant.parse(s.getBeginAt())) ||
            segmentBeginBefore.isAfter(Instant.parse(s.getBeginAt()))))
        .findFirst()
        .orElseThrow(() -> new DAOExistenceException(String.format("Found no Segment[state=%s] in Chain[%s]!", segmentState, chainId)));

    } catch (NexusException e) {
      throw new DAOFatalException(e);
    }
  }

  @Override
  public Collection<Segment> readManyByEmbedKey(HubClientAccess access, String chainEmbedKey) throws DAOPrivilegeException, DAOFatalException, DAOExistenceException {
    try {
      var chainId = chainDAO.readOneByEmbedKey(access, chainEmbedKey).getId();
      return store.getAllSegments(chainId)
        .stream()
        .sorted(Comparator.comparing(Segment::getOffset).reversed())
        .collect(Collectors.toList());

    } catch (NexusException e) {
      throw new DAOFatalException(e);
    }
  }

  @Override
  public <N> Collection<N> readManySubEntities(HubClientAccess access, Collection<UUID> segmentIds, Boolean includePicks) throws DAOPrivilegeException, DAOFatalException {
    try {
      requireTopLevel(access);
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
      throw new DAOFatalException(e);
    }
  }

  @Override
  public <N> void createAllSubEntities(HubClientAccess access, Collection<N> entities) throws DAOPrivilegeException, DAOFatalException {
    try {
      requireTopLevel(access);
      store.putAll(entities);

    } catch (NexusException e) {
      throw new DAOFatalException(e);
    }
  }

  @Override
  public Collection<Segment> readMany(HubClientAccess access, Collection<UUID> chainIds) throws DAOPrivilegeException, DAOFatalException, DAOExistenceException {
    try {
      Collection<Segment> segments = Lists.newArrayList();
      for (UUID chainId : chainIds)
        store.getAllSegments(requireChainAccount(access, chainId))
          .stream()
          .sorted(Comparator.comparing(Segment::getOffset))
          .forEach(segments::add);
      return segments;

    } catch (NexusException e) {
      throw new DAOFatalException(e);
    }
  }

  @Override
  public Collection<Segment> readManyFromToOffset(HubClientAccess access, UUID chainId, Long fromOffset, Long toOffset) throws DAOFatalException, DAOPrivilegeException, DAOExistenceException {
    try {
      requireChainAccount(access, chainId);
      return 0 > toOffset ?
        Lists.newArrayList() :
        store.getAllSegments(chainId)
          .stream()
          .filter(s -> s.getOffset() >= fromOffset && s.getOffset() <= toOffset)
          .sorted(Comparator.comparing(Segment::getOffset))
          .collect(Collectors.toList());

    } catch (NexusException e) {
      throw new DAOFatalException(e);
    }
  }

  @Override
  public Collection<Segment> readManyFromSecondsUTC(HubClientAccess access, UUID chainId, Long fromSecondsUTC) throws DAOPrivilegeException, DAOFatalException, DAOExistenceException {
    try {
      Instant from = Instant.ofEpochSecond(fromSecondsUTC);
      Instant maxBeginAt = from.plusSeconds(workBufferAheadSeconds);
      Instant minEndAt = from.minusSeconds(workBufferBeforeSeconds);
      requireChainAccount(access, chainId);
      return store.getAllSegments(chainId)
        .stream()
        .filter(s -> Value.isSet(s.getEndAt()) &&
          maxBeginAt.isAfter(Instant.parse(s.getBeginAt())) &&
          !Strings.isNullOrEmpty(s.getEndAt()) &&
          minEndAt.isBefore(Instant.parse(s.getEndAt())))
        .sorted(Comparator.comparing(Segment::getOffset))
        .collect(Collectors.toList());

    } catch (NexusException e) {
      throw new DAOFatalException(e);
    }
  }

  @Override
  public Collection<Segment> readManyFromSecondsUTCbyEmbedKey(HubClientAccess access, String chainEmbedKey, Long fromSecondsUTC) throws DAOPrivilegeException, DAOFatalException, DAOExistenceException {
    return readManyFromSecondsUTC(access,
      chainDAO.readOneByEmbedKey(access, chainEmbedKey).getId(),
      fromSecondsUTC);
  }

  @Override
  public Segment update(HubClientAccess access, UUID id, Segment entity) throws DAOPrivilegeException, DAOFatalException, DAOExistenceException, DAOValidationException {
    try {
      requireTopLevel(access);

      // validate and cache to-state
      validate(entity);
      SegmentState toState = entity.getState();

      // fetch existing segment; further logic is based on its current state
      Segment existing = store.getSegment(id)
        .orElseThrow(() -> new DAOExistenceException(Segment.class, id.toString()));
      requireExists("Segment #" + id, existing);

      // logic based on existing Segment State
      protectSegmentStateTransition(existing.getState(), toState);

      // fail if attempt to [#128] change chainId of a segment
      Object updateChainId = entity.getChainId();
      if (Value.isSet(updateChainId) && !Objects.equals(updateChainId, existing.getChainId()))
        throw new DAOValidationException("cannot change chainId create a segment");

      // Never change id
      entity.setId(id);

      // save segment
      store.put(entity);
      return entity;

    } catch (NexusException e) {
      throw new DAOFatalException(e);

    } catch (ValueException e) {
      throw new DAOValidationException(e);
    }
  }

  @Override
  public void revert(HubClientAccess access, UUID id) throws DAOPrivilegeException, DAOFatalException, DAOExistenceException, DAOValidationException {
    try {
      requireTopLevel(access);

      Segment segment = readOne(access, id);

      // Destroy child entities of segment-- but not the messages
      store.deleteAll(id, SegmentChoiceArrangementPick.class);
      store.deleteAll(id, SegmentChoiceArrangement.class);
      store.deleteAll(id, SegmentChoice.class);
      store.deleteAll(id, SegmentMeme.class);
      store.deleteAll(id, SegmentChord.class);
      store.deleteAll(id, SegmentMessage.class);

      update(access, id, segment);

    } catch (NexusException e) {
      throw new DAOFatalException(e);
    }
  }

  @Override
  public Optional<Segment> readLastSegment(HubClientAccess access, UUID chainId) throws DAOPrivilegeException, DAOFatalException, DAOExistenceException {
    try {
      requireChainAccount(access, chainId);
      return store.getAllSegments(chainId)
        .stream()
        .max(Comparator.comparing(Segment::getOffset));

    } catch (NexusException e) {
      throw new DAOFatalException(e);
    }
  }

  @Override
  public Optional<Segment> readLastDubbedSegment(HubClientAccess access, UUID chainId) throws DAOPrivilegeException, DAOFatalException {
    try {
      requireTopLevel(access);
      return Segments.getLastDubbed(store.getAllSegments(chainId));

    } catch (NexusException e) {
      throw new DAOFatalException(e);
    }
  }

  @Override
  public void destroy(HubClientAccess access, UUID id) throws DAOPrivilegeException, DAOFatalException {
    try {
      requireTopLevel(access);

      // Delete Segment
      store.deleteSegment(id);

    } catch (NexusException e) {
      throw new DAOFatalException(e);
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
   Require access to the specified Segment via its Chain's require account and role(s)

   @param access  control
   @param chainId to check for access to
   @return chainId (for chaining methods)
   @throws DAOExistenceException if something doesn't exist
   @throws DAOPrivilegeException if we don't have access
   @throws DAOFatalException     on critical internal failure
   */
  private UUID requireChainAccount(HubClientAccess access, UUID chainId) throws DAOExistenceException, DAOPrivilegeException, DAOFatalException {
    try {
      chainDAO.requireAccount(access,
        store.getChain(chainId)
          .orElseThrow(() -> new DAOExistenceException(Chain.class, chainId.toString())));
      return chainId;

    } catch (NexusException e) {
      throw new DAOFatalException(e);
    }
  }


  /**
   Validate a segment or child entity

   @param entity to validate
   @throws DAOValidationException if invalid
   */
  public void validate(Object entity) throws DAOValidationException {
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
      throw new DAOValidationException(e);
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
  protected void requireNotSameOffsetInChain(Callable<?> mustThrowException) throws DAOValidationException {
    try {
      mustThrowException.call();
    } catch (Exception ignored) {
      return;
    }
    throw new DAOValidationException("Found Segment at same offset in Chain!");
  }

}
