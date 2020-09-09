// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.dao;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.typesafe.config.Config;
import io.xj.lib.entity.Entity;
import io.xj.lib.entity.EntityException;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.entity.EntityStoreException;
import io.xj.lib.util.ValueException;
import io.xj.service.hub.client.HubClientAccess;
import io.xj.service.nexus.dao.exception.DAOExistenceException;
import io.xj.service.nexus.dao.exception.DAOFatalException;
import io.xj.service.nexus.dao.exception.DAOPrivilegeException;
import io.xj.service.nexus.dao.exception.DAOValidationException;
import io.xj.service.nexus.entity.Chain;
import io.xj.service.nexus.entity.Segment;
import io.xj.service.nexus.entity.SegmentChoice;
import io.xj.service.nexus.entity.SegmentChoiceArrangement;
import io.xj.service.nexus.entity.SegmentChoiceArrangementPick;
import io.xj.service.nexus.entity.SegmentChord;
import io.xj.service.nexus.entity.SegmentMeme;
import io.xj.service.nexus.entity.SegmentMessage;
import io.xj.service.nexus.entity.SegmentState;
import io.xj.service.nexus.persistence.NexusEntityStore;

import java.time.Instant;
import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 Nexus DAOs are Singletons unless some other requirement changes that-- 'cuz here be cyclic dependencies...
 */
@Singleton
public class SegmentDAOImpl extends DAOImpl<Segment> implements SegmentDAO {
  private final ChainDAO chainDAO;
  private final int playBufferAheadSeconds;
  private final int playBufferDelaySeconds;
  private final int limitSegmentReadSize;

  @Inject
  public SegmentDAOImpl(
    EntityFactory entityFactory,
    NexusEntityStore nexusEntityStore,
    ChainDAO chainDAO,
    Config config
  ) {
    super(entityFactory, nexusEntityStore);
    this.chainDAO = chainDAO;

    playBufferAheadSeconds = config.getInt("play.bufferAheadSeconds");
    playBufferDelaySeconds = config.getInt("play.bufferDelaySeconds");
    limitSegmentReadSize = config.getInt("segment.limitReadSize");
  }

  @Override
  public Segment create(HubClientAccess access, Segment segment) throws DAOPrivilegeException, DAOFatalException, DAOValidationException {
    try {
      segment.setId(UUID.randomUUID());
      requireTopLevel(access);
      segment.validate();

      // [#126] Segments are always readMany in PLANNED state
      segment.setStateEnum(SegmentState.Planned);

      // create segment with Chain ID and offset are read-only, set at creation
      requireNot("Found Segment at same offset in Chain!",
        () -> readOneAtChainOffset(access, segment.getChainId(), segment.getOffset()));

      return store.put(segment);

    } catch (ValueException e) {
      throw new DAOValidationException(e);

    } catch (EntityStoreException e) {
      throw new DAOFatalException(e);
    }
  }

  @Override
  public SegmentMessage create(HubClientAccess access, SegmentMessage entity) throws DAOPrivilegeException, DAOValidationException, DAOFatalException {
    try {
      requireTopLevel(access);
      entity.validate();
      return store.put(entity);

    } catch (ValueException e) {
      throw new DAOValidationException(e);

    } catch (EntityStoreException e) {
      throw new DAOFatalException(e);
    }
  }

  @Override
  public Segment readOne(HubClientAccess access, UUID id) throws DAOPrivilegeException, DAOExistenceException, DAOFatalException {
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
  public Segment readOneAtChainOffset(HubClientAccess access, UUID chainId, Long offset) throws DAOPrivilegeException, DAOExistenceException, DAOFatalException {
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
  public Segment readOneInState(HubClientAccess access, UUID chainId, SegmentState segmentState, Instant segmentBeginBefore) throws DAOPrivilegeException, DAOFatalException, DAOExistenceException {
    try {
      requireTopLevel(access);
      return store.getAll(Segment.class, Chain.class, ImmutableSet.of(chainId))
        .stream()
        .sorted(Comparator.comparing(Segment::getOffset))
        .filter(s -> segmentState.equals(s.getState()) &&
          (segmentBeginBefore.equals(s.getBeginAt()) || segmentBeginBefore.isAfter(s.getBeginAt())))
        .findFirst()
        .orElseThrow(() -> new DAOExistenceException(String.format("Found no Segment[state=%s] in Chain[%s]!", segmentState, chainId)));

    } catch (EntityStoreException e) {
      throw new DAOFatalException(e);
    }
  }

  @Override
  public Collection<Segment> readMany(HubClientAccess access, String chainEmbedKey) throws DAOPrivilegeException, DAOFatalException, DAOExistenceException {
    try {
      UUID chainId = chainDAO.readOne(access, chainEmbedKey).getId();
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
  public <N extends Entity> Collection<N> readManySubEntities(HubClientAccess access, Collection<UUID> segmentIds, Boolean includePicks) throws DAOPrivilegeException, DAOFatalException {
    try {
      requireTopLevel(access);
      Collection<Entity> entities = Lists.newArrayList();
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
  public <N extends Entity> void createAllSubEntities(HubClientAccess access, Collection<N> entities) throws DAOPrivilegeException, DAOFatalException {
    try {
      requireTopLevel(access);
      store.putAll(entities);

    } catch (EntityStoreException e) {
      throw new DAOFatalException(e);
    }
  }

  @Override
  public Collection<Segment> readMany(HubClientAccess access, Collection<UUID> chainIds) throws DAOPrivilegeException, DAOFatalException, DAOExistenceException {
    try {
      for (UUID chainId : chainIds) requireChainAccount(access, chainId);
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
  public Collection<Segment> readManyFromOffset(HubClientAccess access, UUID chainId, Long fromOffset) throws DAOFatalException, DAOPrivilegeException, DAOExistenceException {
    return readManyFromToOffset(access, chainId, fromOffset, fromOffset + limitSegmentReadSize);
  }

  @Override
  public Collection<Segment> readManyFromToOffset(HubClientAccess access, UUID chainId, Long fromOffset, Long toOffset) throws DAOFatalException, DAOPrivilegeException, DAOExistenceException {
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
  public Collection<Segment> readManyInState(HubClientAccess access, UUID chainId, SegmentState state) throws DAOPrivilegeException, DAOFatalException {
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
  public Collection<Segment> readManyFromOffset(HubClientAccess access, String chainEmbedKey, Long fromOffset) throws DAOPrivilegeException, DAOFatalException, DAOExistenceException {
    return readManyFromToOffset(access,
      chainDAO.readOne(access, chainEmbedKey).getId(),
      fromOffset, fromOffset + limitSegmentReadSize);
  }

  @Override
  public Collection<Segment> readManyFromSecondsUTC(HubClientAccess access, UUID chainId, Long fromSecondsUTC) throws DAOPrivilegeException, DAOFatalException, DAOExistenceException {
    try {
      Instant from = Instant.ofEpochSecond(fromSecondsUTC);
      Instant maxBeginAt = from.plusSeconds(playBufferAheadSeconds);
      Instant minEndAt = from.minusSeconds(playBufferDelaySeconds);
      requireChainAccount(access, chainId);
      return store.getAll(Segment.class, Chain.class, ImmutableSet.of(chainId))
        .stream()
        .filter(s -> Objects.nonNull(s.getEndAt()) &&
          maxBeginAt.isAfter(s.getBeginAt()) &&
          minEndAt.isBefore(s.getEndAt()))
        .sorted(Comparator.comparing(Segment::getOffset))
        .limit(limitSegmentReadSize)
        .collect(Collectors.toList());

    } catch (EntityStoreException e) {
      throw new DAOFatalException(e);
    }
  }

  @Override
  public Collection<Segment> readManyFromSecondsUTC(HubClientAccess access, String chainEmbedKey, Long fromSecondsUTC) throws DAOPrivilegeException, DAOFatalException, DAOExistenceException {
    return readManyFromSecondsUTC(access,
      chainDAO.readOne(access, chainEmbedKey).getId(),
      fromSecondsUTC);
  }

  @Override
  public void update(HubClientAccess access, UUID id, Segment updateSegment) throws DAOPrivilegeException, DAOFatalException, DAOExistenceException, DAOValidationException {
    try {
      requireTopLevel(access);

      // validate and cache to-state
      updateSegment.validate();
      SegmentState toState = updateSegment.getState();

      // fetch existing segment; further logic is based on its current state
      Segment existing = store.get(Segment.class, id)
        .orElseThrow(() -> new DAOExistenceException(Segment.class, id));
      requireExists("Segment #" + id, existing);

      // logic based on existing Segment State
      SegmentState.protectTransition(existing.getState(), toState);

      // fail if attempt to [#128] change chainId of a segment
      Object updateChainId = updateSegment.getChainId();
      if (Objects.nonNull(updateChainId) && !Objects.equals(updateChainId, existing.getChainId()))
        throw new DAOValidationException("cannot change chainId create a segment");

      // Never change id
      updateSegment.setId(id);

      // save segment
      store.put(updateSegment);

    } catch (EntityStoreException e) {
      throw new DAOFatalException(e);

    } catch (ValueException e) {
      throw new DAOValidationException(e);
    }
  }

  @Override
  public void revert(HubClientAccess access, UUID id) throws DAOPrivilegeException, DAOFatalException, DAOExistenceException, DAOValidationException {
    requireTopLevel(access);

    Segment segment = readOne(access, id);

    // Destroy child entities of segment-- but not the messages
    destroyChildEntities(access, id, false);

    update(access, id, segment);
  }

  @Override
  public Segment readLastSegment(HubClientAccess access, UUID chainId) throws DAOPrivilegeException, DAOFatalException, DAOExistenceException {
    try {
      requireChainAccount(access, chainId);
      return store.getAll(Segment.class, Chain.class, ImmutableSet.of(chainId))
        .stream()
        .max(Comparator.comparing(Segment::getOffset))
        .orElseThrow(() -> new DAOExistenceException(String.format("Found no last Segment with no end-at in Chain[%s]!", chainId)));

    } catch (EntityStoreException e) {
      throw new DAOFatalException(e);
    }
  }

  @Override
  public boolean existsAnyDubbedEndingAfter(UUID chainId, Instant thresholdChainHeadAt) throws DAOFatalException {
    try {
      return store.getAll(Segment.class, Chain.class, ImmutableSet.of(chainId)).stream()
        .noneMatch(segment -> segment.isDubbedEndingAfter(thresholdChainHeadAt));

    } catch (EntityStoreException e) {
      throw new DAOFatalException(e);
    }
  }

  @Override
  public void destroy(HubClientAccess access, UUID id) throws DAOPrivilegeException, DAOFatalException, DAOExistenceException {
    requireTopLevel(access);

    // Require Segment exist in order to destroy it
    Segment segment = readOne(access, id);
    requireExists("Segment #" + id, segment);

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
  private void destroyChildEntities(HubClientAccess access, UUID id, Boolean destroyMessages) throws DAOPrivilegeException, DAOFatalException {
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

  /**
   Require access to the specified Segment via its Chain's require account and role(s)

   @param access  control
   @param chainId to check for access to
   @throws DAOExistenceException if something doesn't exist
   @throws DAOPrivilegeException if we don't have access
   @throws DAOFatalException     on critical internal failure
   */
  private void requireChainAccount(HubClientAccess access, UUID chainId) throws DAOExistenceException, DAOPrivilegeException, DAOFatalException {
    try {
      chainDAO.requireAccount(access,
        store.get(Chain.class, chainId)
          .orElseThrow(() -> new DAOExistenceException(Chain.class, chainId)));

    } catch (EntityStoreException e) {
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

}
