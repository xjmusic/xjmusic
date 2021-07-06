// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.dao;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.protobuf.MessageLite;
import com.typesafe.config.Config;
import io.xj.Chain;
import io.xj.Segment;
import io.xj.SegmentChoice;
import io.xj.SegmentChoiceArrangement;
import io.xj.SegmentChoiceArrangementPick;
import io.xj.SegmentChord;
import io.xj.SegmentChordVoicing;
import io.xj.SegmentMeme;
import io.xj.SegmentMessage;
import io.xj.lib.entity.EntityException;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.entity.common.ChordEntity;
import io.xj.lib.entity.common.MessageEntity;
import io.xj.lib.util.CSV;
import io.xj.lib.util.Value;
import io.xj.lib.util.ValueException;
import io.xj.nexus.NexusException;
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
import java.util.stream.Collectors;

/**
 Nexus DAOs are Singletons unless some other requirement changes that-- 'cuz here be cyclic dependencies...
 */
@Singleton
public class SegmentDAOImpl extends DAOImpl<Segment> implements SegmentDAO {
  private static final long MILLIS_PER_SECOND = 1000;
  private final ChainDAO chainDAO;
  private final int playerBufferAheadSeconds;
  private final int playerBufferDelaySeconds;
  private final int limitSegmentReadSize;
  public static final Double LENGTH_MINIMUM = 0.01; //
  public static final Double AMPLITUDE_MINIMUM = 0.0; //

  @Inject
  public SegmentDAOImpl(
    EntityFactory entityFactory,
    NexusEntityStore nexusEntityStore,
    ChainDAO chainDAO,
    Config config
  ) {
    super(entityFactory, nexusEntityStore);
    this.chainDAO = chainDAO;

    playerBufferAheadSeconds = config.getInt("player.bufferAheadSeconds");
    playerBufferDelaySeconds = config.getInt("player.bufferDelaySeconds");
    limitSegmentReadSize = config.getInt("segment.limitReadSize");
  }

  /**
   Require state is in an array of states

   @param toState       to check
   @param allowedStates required to be in
   @throws ValueException if not in required states
   */
  public static void onlyAllowSegmentStateTransitions(Segment.State toState, Segment.State... allowedStates) throws ValueException {
    List<String> allowedStateNames = Lists.newArrayList();
    for (Segment.State search : allowedStates) {
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
  public static void protectSegmentStateTransition(Segment.State fromState, Segment.State toState) throws ValueException {
    switch (fromState) {
      case Planned -> onlyAllowSegmentStateTransitions(toState, Segment.State.Planned, Segment.State.Crafting);
      case Crafting -> onlyAllowSegmentStateTransitions(toState, Segment.State.Crafting, Segment.State.Crafted, Segment.State.Dubbing, Segment.State.Failed, Segment.State.Planned);
      case Crafted -> onlyAllowSegmentStateTransitions(toState, Segment.State.Crafted, Segment.State.Dubbing);
      case Dubbing -> onlyAllowSegmentStateTransitions(toState, Segment.State.Dubbing, Segment.State.Dubbed, Segment.State.Failed);
      case Dubbed -> onlyAllowSegmentStateTransitions(toState, Segment.State.Dubbed);
      case Failed -> onlyAllowSegmentStateTransitions(toState, Segment.State.Failed);
      default -> onlyAllowSegmentStateTransitions(toState, Segment.State.Planned);
    }
  }

  @Override
  public Segment create(HubClientAccess access, Segment entity) throws DAOPrivilegeException, DAOFatalException, DAOValidationException {
    try {
      Segment.Builder segment = entity.toBuilder();
      segment.setId(UUID.randomUUID().toString());
      requireTopLevel(access);
      validate(segment);

      // [#126] Segments are always readMany in PLANNED state
      segment.setState(Segment.State.Planned);

      // create segment with Chain ID and offset are read-only, set at creation
      requireNot("Found Segment at same offset in Chain!",
        () -> readOneAtChainOffset(access, segment.getChainId(), segment.getOffset()));

      return store.put(segment.build());

    } catch (NexusException e) {
      throw new DAOFatalException(e);
    }
  }

  @Override
  public SegmentMessage create(HubClientAccess access, SegmentMessage entity) throws DAOPrivilegeException, DAOValidationException, DAOFatalException {
    try {
      requireTopLevel(access);
      SegmentMessage.Builder builder = entity.toBuilder();
      validate(builder);
      return store.put(builder.build());

    } catch (NexusException e) {
      throw new DAOFatalException(e);
    }
  }

  @Override
  public Segment readOne(HubClientAccess access, String id) throws DAOPrivilegeException, DAOExistenceException, DAOFatalException {
    try {
      Segment segment = store.getSegment(id)
        .orElseThrow(() -> new DAOExistenceException(Segment.class, id));
      requireChainAccount(access, segment.getChainId());
      return segment;

    } catch (NexusException e) {
      throw new DAOFatalException(e);
    }
  }


  @Override
  public Segment readOneAtChainOffset(HubClientAccess access, String chainId, Long offset) throws DAOPrivilegeException, DAOExistenceException, DAOFatalException {
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

  @Override
  public Segment readOneInState(HubClientAccess access, String chainId, Segment.State segmentState, Instant segmentBeginBefore) throws DAOPrivilegeException, DAOFatalException, DAOExistenceException {
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
      String chainId = chainDAO.readOneByEmbedKey(access, chainEmbedKey).getId();
      return store.getAllSegments(chainId)
        .stream()
        .sorted(Comparator.comparing(Segment::getOffset).reversed())
        .limit(limitSegmentReadSize)
        .collect(Collectors.toList());

    } catch (NexusException e) {
      throw new DAOFatalException(e);
    }
  }

  @Override
  public <N> Collection<N> readManySubEntities(HubClientAccess access, Collection<String> segmentIds, Boolean includePicks) throws DAOPrivilegeException, DAOFatalException {
    try {
      requireTopLevel(access);
      Collection<Object> entities = Lists.newArrayList();
      for (String sId : segmentIds) {
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
  public Collection<Segment> readMany(HubClientAccess access, Collection<String> chainIds) throws DAOPrivilegeException, DAOFatalException, DAOExistenceException {
    return readAll(access, chainIds).stream().limit(limitSegmentReadSize).collect(Collectors.toList());
  }

  @Override
  public Collection<Segment> readAll(HubClientAccess access, Collection<String> chainIds) throws DAOPrivilegeException, DAOFatalException, DAOExistenceException {
    try {
      Collection<Segment> segments = Lists.newArrayList();
      for (String chainId : chainIds)
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
  public Collection<Segment> readManyFromOffset(HubClientAccess access, String chainId, Long fromOffset) throws DAOFatalException, DAOPrivilegeException, DAOExistenceException {
    return readManyFromToOffset(access, chainId, fromOffset, fromOffset + limitSegmentReadSize);
  }

  @Override
  public Collection<Segment> readManyFromToOffset(HubClientAccess access, String chainId, Long fromOffset, Long toOffset) throws DAOFatalException, DAOPrivilegeException, DAOExistenceException {
    try {
      requireChainAccount(access, chainId);
      return 0 > toOffset ?
        Lists.newArrayList() :
        store.getAllSegments(chainId)
          .stream()
          .filter(s -> s.getOffset() >= fromOffset && s.getOffset() <= toOffset)
          .sorted(Comparator.comparing(Segment::getOffset))
          .limit(limitSegmentReadSize)
          .collect(Collectors.toList());

    } catch (NexusException e) {
      throw new DAOFatalException(e);
    }
  }

  @Override
  public Collection<Segment> readManyInState(HubClientAccess access, String chainId, Segment.State state) throws DAOPrivilegeException, DAOFatalException {
    try {
      requireTopLevel(access);
      return
        store.getAllSegments(chainId)
          .stream()
          .filter(s -> state.equals(s.getState()))
          .sorted(Comparator.comparing(Segment::getOffset))
          .limit(limitSegmentReadSize)
          .collect(Collectors.toList());

    } catch (NexusException e) {
      throw new DAOFatalException(e);
    }
  }

  @Override
  public Collection<Segment> readManyFromOffsetByEmbedKey(HubClientAccess access, String chainEmbedKey, Long fromOffset) throws DAOPrivilegeException, DAOFatalException, DAOExistenceException {
    return readManyFromToOffset(access,
      chainDAO.readOneByEmbedKey(access, chainEmbedKey).getId(),
      fromOffset, fromOffset + limitSegmentReadSize);
  }

  @Override
  public Collection<Segment> readManyFromSecondsUTC(HubClientAccess access, String chainId, Long fromSecondsUTC) throws DAOPrivilegeException, DAOFatalException, DAOExistenceException {
    try {
      Instant from = Instant.ofEpochSecond(fromSecondsUTC);
      Instant maxBeginAt = from.plusSeconds(playerBufferAheadSeconds);
      Instant minEndAt = from.minusSeconds(playerBufferDelaySeconds);
      requireChainAccount(access, chainId);
      return store.getAllSegments(chainId)
        .stream()
        .filter(s -> Value.isSet(s.getEndAt()) &&
          maxBeginAt.isAfter(Instant.parse(s.getBeginAt())) &&
          !Strings.isNullOrEmpty(s.getEndAt()) &&
          minEndAt.isBefore(Instant.parse(s.getEndAt())))
        .sorted(Comparator.comparing(Segment::getOffset))
        .limit(limitSegmentReadSize)
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
  public Segment update(HubClientAccess access, String id, Segment entity) throws DAOPrivilegeException, DAOFatalException, DAOExistenceException, DAOValidationException {
    try {
      requireTopLevel(access);
      Segment.Builder builder = entity.toBuilder();

      // validate and cache to-state
      validate(builder);
      Segment.State toState = builder.getState();

      // fetch existing segment; further logic is based on its current state
      Segment existing = store.getSegment(id)
        .orElseThrow(() -> new DAOExistenceException(Segment.class, id));
      requireExists("Segment #" + id, existing);

      // logic based on existing Segment State
      protectSegmentStateTransition(existing.getState(), toState);

      // fail if attempt to [#128] change chainId of a segment
      Object updateChainId = builder.getChainId();
      if (Value.isSet(updateChainId) && !Objects.equals(updateChainId, existing.getChainId()))
        throw new DAOValidationException("cannot change chainId create a segment");

      // Never change id
      builder.setId(id);

      // save segment
      var record = builder.build();
      store.put(record);
      return record;

    } catch (NexusException e) {
      throw new DAOFatalException(e);

    } catch (ValueException e) {
      throw new DAOValidationException(e);
    }
  }

  @Override
  public void revert(HubClientAccess access, String id) throws DAOPrivilegeException, DAOFatalException, DAOExistenceException, DAOValidationException {
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
  public Optional<Segment> readLastSegment(HubClientAccess access, String chainId) throws DAOPrivilegeException, DAOFatalException, DAOExistenceException {
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
  public Optional<Segment> readLastDubbedSegment(HubClientAccess access, String chainId) throws DAOPrivilegeException, DAOFatalException {
    try {
      requireTopLevel(access);
      return getLastDubbed(store.getAllSegments(chainId));

    } catch (NexusException e) {
      throw new DAOFatalException(e);
    }
  }

  @Override
  public Optional<Segment> getLastDubbed(Collection<Segment> segments) {
    return segments
      .stream()
      .filter(segment -> Segment.State.Dubbed == segment.getState())
      .max(Comparator.comparing(Segment::getOffset));
  }

  @Override
  public float getLengthSeconds(Segment segment) {
    return (float) (Instant.parse(segment.getEndAt()).toEpochMilli() - Instant.parse(segment.getBeginAt()).toEpochMilli()) / MILLIS_PER_SECOND;
  }

  @Override
  public void destroy(HubClientAccess access, String id) throws DAOPrivilegeException, DAOFatalException {
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
      return Segment.getDefaultInstance();
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
  private String requireChainAccount(HubClientAccess access, String chainId) throws DAOExistenceException, DAOPrivilegeException, DAOFatalException {
    try {
      chainDAO.requireAccount(access,
        store.getChain(chainId)
          .orElseThrow(() -> new DAOExistenceException(Chain.class, chainId)));
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
  public void validate(MessageLite.Builder entity) throws DAOValidationException {
    try {
      if (entity instanceof Segment.Builder)
        validateSegment((Segment.Builder) entity);
      else if (entity instanceof SegmentChoice.Builder)
        validateSegmentChoice((SegmentChoice.Builder) entity);
      else if (entity instanceof SegmentChoiceArrangement.Builder)
        validateSegmentChoiceArrangement((SegmentChoiceArrangement.Builder) entity);
      else if (entity instanceof SegmentChoiceArrangementPick.Builder)
        validateSegmentChoiceArrangementPick((SegmentChoiceArrangementPick.Builder) entity);
      else if (entity instanceof SegmentChord.Builder)
        validateSegmentChord((SegmentChord.Builder) entity);
      else if (entity instanceof SegmentMeme.Builder)
        validateSegmentMeme((SegmentMeme.Builder) entity);
      else if (entity instanceof SegmentMessage.Builder)
        validateSegmentMessage((SegmentMessage.Builder) entity);

    } catch (ValueException e) {
      throw new DAOValidationException(e);
    }
  }

  private void validateSegmentMessage(SegmentMessage.Builder record) throws ValueException {
    Value.require(record.getSegmentId(), "Segment ID");
    Value.require(record.getType(), "Type");
    MessageEntity.validate(record);
  }

  private void validateSegmentMeme(SegmentMeme.Builder record) throws ValueException {
    Value.require(record.getSegmentId(), "Segment ID");
    Value.require(record.getName(), "Meme name");
  }

  private void validateSegmentChord(SegmentChord.Builder record) throws ValueException {
    Value.require(record.getSegmentId(), "Segment ID");
    ChordEntity.validate(record);
  }

  private void validateSegmentChoiceArrangementPick(SegmentChoiceArrangementPick.Builder record) throws ValueException {
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

  private void validateSegmentChoiceArrangement(SegmentChoiceArrangement.Builder record) throws ValueException {
    Value.require(record.getSegmentId(), "Segment ID");
    Value.require(record.getSegmentChoiceId(), "Choice ID");
    Value.require(record.getProgramSequencePatternId(), "Program Sequence Pattern ID");
  }

  private void validateSegmentChoice(SegmentChoice.Builder record) throws ValueException {
    Value.require(record.getSegmentId(), "Segment ID");
    Value.require(record.getProgramId(), "Program ID");
    Value.require(record.getProgramType(), "Program Type");
    Value.require(record.getInstrumentId(), "Instrument ID");
  }

  private void validateSegment(Segment.Builder record) throws ValueException {
    Value.require(record.getChainId(), "Chain ID");
    Value.require(record.getOffset(), "Offset");
    if (Value.isEmpty(record.getWaveformPreroll())) record.setWaveformPreroll(0.0);
    Value.require(record.getType(), "Type");
    Value.require(record.getState(), "State");
    Value.require(record.getBeginAt(), "Begin-at");
  }


}
