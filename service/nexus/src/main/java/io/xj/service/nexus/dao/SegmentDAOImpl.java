// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.dao;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
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
import io.xj.SegmentMeme;
import io.xj.SegmentMessage;
import io.xj.lib.entity.EntityException;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.entity.EntityStoreException;
import io.xj.lib.entity.common.ChordEntity;
import io.xj.lib.entity.common.MessageEntity;
import io.xj.lib.util.CSV;
import io.xj.lib.util.Text;
import io.xj.lib.util.Value;
import io.xj.lib.util.ValueException;
import io.xj.service.hub.client.HubClientAccess;
import io.xj.service.nexus.dao.exception.DAOExistenceException;
import io.xj.service.nexus.dao.exception.DAOFatalException;
import io.xj.service.nexus.dao.exception.DAOPrivilegeException;
import io.xj.service.nexus.dao.exception.DAOValidationException;
import io.xj.service.nexus.persistence.NexusEntityStore;

import java.time.Instant;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 Nexus DAOs are Singletons unless some other requirement changes that-- 'cuz here be cyclic dependencies...
 */
@Singleton
public class SegmentDAOImpl extends DAOImpl<Segment> implements SegmentDAO {
  private final ChainDAO chainDAO;
  private final int playerBufferAheadSeconds;
  private final int playerBufferDelaySeconds;
  private final int limitSegmentReadSize;
  public static final Double LENGTH_MINIMUM = 0.01; //
  public static final Double AMPLITUDE_MINIMUM = 0.0; //
  public static final Double PITCH_MINIMUM = 1.0; //

  @Inject
  public SegmentDAOImpl(
    EntityFactory entityFactory,
    NexusEntityStore nexusEntityStore,
    ChainDAO chainDAO,
    Config config
  ) {
    super(entityFactory, nexusEntityStore);
    this.chainDAO = chainDAO;

    playerBufferAheadSeconds = config.getInt("chain.playerBufferAheadSeconds");
    playerBufferDelaySeconds = config.getInt("chain.playerBufferDelaySeconds");
    limitSegmentReadSize = config.getInt("segment.limitReadSize");
  }

  /**
   String Values

   @return ImmutableList of string values
   */
  public static List<String> segmentStateStringValues() {
    return Text.toStrings(Segment.State.values());
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

      case Planned:
        onlyAllowSegmentStateTransitions(toState, Segment.State.Planned, Segment.State.Crafting);
        break;

      case Crafting:
        onlyAllowSegmentStateTransitions(toState, Segment.State.Crafting, Segment.State.Crafted, Segment.State.Dubbing, Segment.State.Failed, Segment.State.Planned);
        break;

      case Crafted:
        onlyAllowSegmentStateTransitions(toState, Segment.State.Crafted, Segment.State.Dubbing);
        break;

      case Dubbing:
        onlyAllowSegmentStateTransitions(toState, Segment.State.Dubbing, Segment.State.Dubbed, Segment.State.Failed);
        break;

      case Dubbed:
        onlyAllowSegmentStateTransitions(toState, Segment.State.Dubbed);
        break;

      case Failed:
        onlyAllowSegmentStateTransitions(toState, Segment.State.Failed);
        break;

      default:
        onlyAllowSegmentStateTransitions(toState, Segment.State.Planned);
        break;
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

    } catch (EntityStoreException e) {
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

    } catch (EntityStoreException e) {
      throw new DAOFatalException(e);
    }
  }

  @Override
  public Segment readOne(HubClientAccess access, String id) throws DAOPrivilegeException, DAOExistenceException, DAOFatalException {
    try {
      Segment segment = store.get(Segment.class, id)
        .orElseThrow(() -> new DAOExistenceException(Segment.class, id));
      requireChainAccount(access, segment.getChainId());
      return segment;

    } catch (EntityStoreException e) {
      throw new DAOFatalException(e);
    }
  }


  @Override
  public Segment readOneAtChainOffset(HubClientAccess access, String chainId, Long offset) throws DAOPrivilegeException, DAOExistenceException, DAOFatalException {
    try {
      requireTopLevel(access);
      return store.getAll(Segment.class, Chain.class, ImmutableSet.of(chainId))
        .stream()
        .filter(s -> offset.equals(s.getOffset()))
        .findFirst()
        .orElseThrow(() -> new DAOExistenceException(String.format("Found no Segment@%d in Chain[%s]!", offset, chainId)));

    } catch (EntityStoreException e) {
      throw new DAOFatalException(e);
    }
  }

  @Override
  public Segment readOneInState(HubClientAccess access, String chainId, Segment.State segmentState, Instant segmentBeginBefore) throws DAOPrivilegeException, DAOFatalException, DAOExistenceException {
    try {
      requireTopLevel(access);
      return store.getAll(Segment.class, Chain.class, ImmutableSet.of(chainId))
        .stream()
        .sorted(Comparator.comparing(Segment::getOffset))
        .filter(s -> segmentState.equals(s.getState()) &&
          (segmentBeginBefore.equals(Instant.parse(s.getBeginAt())) ||
            segmentBeginBefore.isAfter(Instant.parse(s.getBeginAt()))))
        .findFirst()
        .orElseThrow(() -> new DAOExistenceException(String.format("Found no Segment[state=%s] in Chain[%s]!", segmentState, chainId)));

    } catch (EntityStoreException e) {
      throw new DAOFatalException(e);
    }
  }

  @Override
  public Collection<Segment> readManyByEmbedKey(HubClientAccess access, String chainEmbedKey) throws DAOPrivilegeException, DAOFatalException, DAOExistenceException {
    try {
      String chainId = chainDAO.readOneByEmbedKey(access, chainEmbedKey).getId();
      return store.getAll(Segment.class, Chain.class, ImmutableList.of(chainId))
        .stream()
        .sorted(Comparator.comparing(Segment::getOffset).reversed())
        .limit(limitSegmentReadSize)
        .collect(Collectors.toList());

    } catch (EntityStoreException e) {
      throw new DAOFatalException(e);
    }
  }

  @Override
  public <N> Collection<N> readManySubEntities(HubClientAccess access, Collection<String> segmentIds, Boolean includePicks) throws DAOPrivilegeException, DAOFatalException {
    try {
      requireTopLevel(access);
      Collection<Object> entities = Lists.newArrayList();
      entities.addAll(store.getAll(SegmentMeme.class, Segment.class, segmentIds));
      entities.addAll(store.getAll(SegmentChord.class, Segment.class, segmentIds));
      entities.addAll(store.getAll(SegmentChoice.class, Segment.class, segmentIds));
      entities.addAll(store.getAll(SegmentMessage.class, Segment.class, segmentIds));
      entities.addAll(store.getAll(SegmentChoiceArrangement.class, Segment.class, segmentIds));
      if (includePicks)
        entities.addAll(store.getAll(SegmentChoiceArrangementPick.class, Segment.class, segmentIds));
      //noinspection unchecked
      return (Collection<N>) entities;

    } catch (EntityStoreException e) {
      throw new DAOFatalException(e);
    }
  }

  @Override
  public <N> void createAllSubEntities(HubClientAccess access, Collection<N> entities) throws DAOPrivilegeException, DAOFatalException {
    try {
      requireTopLevel(access);
      store.putAll(entities);

    } catch (EntityStoreException e) {
      throw new DAOFatalException(e);
    }
  }

  @Override
  public Collection<Segment> readMany(HubClientAccess access, Collection<String> chainIds) throws DAOPrivilegeException, DAOFatalException, DAOExistenceException {
    try {
      for (String chainId : chainIds) requireChainAccount(access, chainId);
      return store.getAll(Segment.class, Chain.class, chainIds)
        .stream()
        .sorted(Comparator.comparing(Segment::getOffset))
        .limit(limitSegmentReadSize)
        .collect(Collectors.toList());

    } catch (EntityStoreException e) {
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
        store.getAll(Segment.class, Chain.class, ImmutableSet.of(chainId))
          .stream()
          .filter(s -> s.getOffset() >= fromOffset && s.getOffset() <= toOffset)
          .sorted(Comparator.comparing(Segment::getOffset))
          .limit(limitSegmentReadSize)
          .collect(Collectors.toList());

    } catch (EntityStoreException e) {
      throw new DAOFatalException(e);
    }
  }

  @Override
  public Collection<Segment> readManyInState(HubClientAccess access, String chainId, Segment.State state) throws DAOPrivilegeException, DAOFatalException {
    try {
      requireTopLevel(access);
      return
        store.getAll(Segment.class, Chain.class, ImmutableSet.of(chainId))
          .stream()
          .filter(s -> state.equals(s.getState()))
          .sorted(Comparator.comparing(Segment::getOffset))
          .limit(limitSegmentReadSize)
          .collect(Collectors.toList());

    } catch (EntityStoreException e) {
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
      return store.getAll(Segment.class, Chain.class, ImmutableSet.of(chainId))
        .stream()
        .filter(s -> Value.isSet(s.getEndAt()) &&
          maxBeginAt.isAfter(Instant.parse(s.getBeginAt())) &&
          !Strings.isNullOrEmpty(s.getEndAt()) &&
          minEndAt.isBefore(Instant.parse(s.getEndAt())))
        .sorted(Comparator.comparing(Segment::getOffset))
        .limit(limitSegmentReadSize)
        .collect(Collectors.toList());

    } catch (EntityStoreException e) {
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
  public void update(HubClientAccess access, String id, Segment entity) throws DAOPrivilegeException, DAOFatalException, DAOExistenceException, DAOValidationException {
    try {
      requireTopLevel(access);
      Segment.Builder builder = entity.toBuilder();

      // validate and cache to-state
      validate(builder);
      Segment.State toState = builder.getState();

      // fetch existing segment; further logic is based on its current state
      Segment existing = store.get(Segment.class, id)
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
      store.put(builder.build());

    } catch (EntityStoreException e) {
      throw new DAOFatalException(e);

    } catch (ValueException e) {
      throw new DAOValidationException(e);
    }
  }

  @Override
  public void revert(HubClientAccess access, String id) throws DAOPrivilegeException, DAOFatalException, DAOExistenceException, DAOValidationException {
    requireTopLevel(access);

    Segment segment = readOne(access, id);

    // Destroy child entities of segment-- but not the messages
    destroyChildEntities(access, id, false);

    update(access, id, segment);
  }

  @Override
  public Segment readLastSegment(HubClientAccess access, String chainId) throws DAOPrivilegeException, DAOFatalException, DAOExistenceException {
    try {
      requireChainAccount(access, chainId);
      return store.getAll(Segment.class, Chain.class, ImmutableSet.of(chainId))
        .stream()
        .max(Comparator.comparing(Segment::getOffset))
        .orElseThrow(() -> new DAOExistenceException(String.format("Found no last Segment in Chain[%s]!", chainId)));

    } catch (EntityStoreException e) {
      throw new DAOFatalException(e);
    }
  }

  @Override
  public Segment readLastDubbedSegment(HubClientAccess access, String chainId) throws DAOPrivilegeException, DAOFatalException, DAOExistenceException {
    try {
      requireTopLevel(access);
      return store.getAll(Segment.class, Chain.class, ImmutableSet.of(chainId))
        .stream()
        .filter(segment -> Segment.State.Dubbed == segment.getState())
        .max(Comparator.comparing(Segment::getOffset))
        .orElseThrow(() -> new DAOExistenceException(String.format("Found no last dubbed-state Segment in Chain[%s]!", chainId)));

    } catch (EntityStoreException e) {
      throw new DAOFatalException(e);
    }
  }

  @Override
  public void destroy(HubClientAccess access, String id) throws DAOPrivilegeException, DAOFatalException {
    requireTopLevel(access);

    // Destroy ALL child entities of segment
    destroyChildEntities(access, id, true);

    // Delete Segment
    store.delete(Segment.class, id);
  }

  /**
   Destroy all child entities of segment@param access

   @param id              segment to destroy child entities of
   @param destroyMessages true if we also want to include the segment messages (false to preserve the messages)
   */
  private void destroyChildEntities(HubClientAccess access, String id, Boolean destroyMessages) throws DAOPrivilegeException, DAOFatalException {
    try {
      requireTopLevel(access);
      store.deleteAll(SegmentChoiceArrangementPick.class, Segment.class, id);
      store.deleteAll(SegmentChoiceArrangement.class, Segment.class, id);
      store.deleteAll(SegmentChoice.class, Segment.class, id);
      store.deleteAll(SegmentMeme.class, Segment.class, id);
      store.deleteAll(SegmentChord.class, Segment.class, id);
      if (destroyMessages)
        store.deleteAll(SegmentMessage.class, Segment.class, id);

    } catch (EntityStoreException e) {
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
   @throws DAOExistenceException if something doesn't exist
   @throws DAOPrivilegeException if we don't have access
   @throws DAOFatalException     on critical internal failure
   */
  private void requireChainAccount(HubClientAccess access, String chainId) throws DAOExistenceException, DAOPrivilegeException, DAOFatalException {
    try {
      chainDAO.requireAccount(access,
        store.get(Chain.class, chainId)
          .orElseThrow(() -> new DAOExistenceException(Chain.class, chainId)));

    } catch (EntityStoreException e) {
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
    Value.require(record.getPitch(), "Pitch");
    Value.requireMinimum(PITCH_MINIMUM, record.getPitch(), "Pitch");
  }

  private void validateSegmentChoiceArrangement(SegmentChoiceArrangement.Builder record) throws ValueException {
    Value.require(record.getSegmentId(), "Segment ID");
    Value.require(record.getSegmentChoiceId(), "Choice ID");
    Value.require(record.getProgramVoiceId(), "Voice ID");
    Value.require(record.getInstrumentId(), "Instrument ID");
  }

  private void validateSegmentChoice(SegmentChoice.Builder record) throws ValueException {
    Value.require(record.getSegmentId(), "Segment ID");
    Value.require(record.getProgramId(), "Program ID");
    Value.require(record.getProgramType(), "Program Type");
    if (Value.isEmpty(record.getTranspose())) record.setTranspose(0);
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
