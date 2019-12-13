// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.typesafe.config.Config;
import io.xj.core.access.Access;
import io.xj.core.entity.MessageType;
import io.xj.core.exception.CoreException;
import io.xj.core.model.Chain;
import io.xj.core.model.ChainState;
import io.xj.core.model.ChainType;
import io.xj.core.model.PlatformMessage;
import io.xj.core.model.Segment;
import io.xj.core.model.SegmentState;
import io.xj.core.model.UserRoleType;
import io.xj.core.persistence.SQLDatabaseProvider;
import io.xj.core.tables.records.ChainRecord;
import io.xj.core.tables.records.SegmentRecord;
import io.xj.core.util.CSV;
import io.xj.core.work.WorkManager;
import org.jooq.DSLContext;
import org.jooq.Record;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static io.xj.core.Tables.ACCOUNT;
import static io.xj.core.Tables.CHAIN;
import static io.xj.core.Tables.CHAIN_BINDING;
import static io.xj.core.Tables.CHAIN_CONFIG;
import static io.xj.core.Tables.SEGMENT;

/**
 Chain D.A.O. Implementation
 <p>
 Core directive here is to keep business logic CENTRAL ("oneness")
 <p>
 All variants on an update resolve (after recordUpdateFirstStep transformation,
 or additional validation) to one singular central update(fieldValues)
 <p>
 Also note buildNextSegmentOrComplete(...) is one singular central implementation
 of the logic around adding segments to chains and updating chain state to complete.
 */
public class ChainDAOImpl extends DAOImpl<Chain> implements ChainDAO {
  //  private static final Logger log = LoggerFactory.getLogger(ChainDAOImpl.class);
  private final int previewLengthMax;
  private final int chainReviveThresholdStartSeconds;
  private final int chainReviveThresholdHeadSeconds;
  private final PlatformMessageDAO platformMessageDAO;
  private final WorkManager workManager;

  @Inject
  public ChainDAOImpl(
    PlatformMessageDAO platformMessageDAO,
    SQLDatabaseProvider dbProvider,
    WorkManager workManager,
    Config config
  ) {
    this.dbProvider = dbProvider;
    this.platformMessageDAO = platformMessageDAO;
    this.workManager = workManager;

    previewLengthMax = config.getInt("chain.previewLengthMax");
    chainReviveThresholdHeadSeconds = config.getInt("chain.reviveThresholdHeadSeconds");
    chainReviveThresholdStartSeconds = config.getInt("chain.reviveThresholdStartSeconds");
  }

  /**
   Require state is in an array of states

   @param toState       to check
   @param allowedStates required to be in
   @throws CoreException if not in required states
   */
  private void onlyAllowTransitions(ChainState toState, ChainState... allowedStates) throws CoreException {
    List<String> allowedStateNames = Lists.newArrayList();
    for (ChainState search : allowedStates) {
      allowedStateNames.add(search.toString());
      if (search == toState) {
        return;
      }
    }
    throw new CoreException(String.format("transition to %s not in allowed (%s)",
      toState, CSV.join(allowedStateNames)));
  }


  @Override
  public Chain create(Access access, Chain entity) throws CoreException {
    DSLContext db = dbProvider.getDSL();

    entity.validate();

    // [#126] Chains are always createdin DRAFT state
    entity.setStateEnum(ChainState.Draft);

    if (access.isTopLevel())
      requireExists("Account", db.selectCount().from(ACCOUNT)
        .where(ACCOUNT.ID.eq(entity.getAccountId()))
        .fetchOne(0, int.class));
    else
      requireExists("Account", db.selectCount().from(ACCOUNT)
        .where(ACCOUNT.ID.in(access.getAccountIds()))
        .and(ACCOUNT.ID.eq(entity.getAccountId()))
        .fetchOne(0, int.class));


    // logic based on Chain Type
    switch (entity.getType()) {

      case Production:
        requireRole("Engineer to create production Chain", access, UserRoleType.Engineer);

        // [#403] Chain must have unique `embed_key`
        if (Objects.nonNull(entity.getEmbedKey()))
          requireNotExists("Existing Chain with this embed_key", db.select(CHAIN.ID).from(CHAIN)
            .where(CHAIN.EMBED_KEY.eq(entity.getEmbedKey()))
            .fetch());
        break;

      case Preview:
        requireRole("Artist to create preview Chain", access, UserRoleType.Artist);
        entity.setStartAtInstant(Instant.now().minusSeconds(previewLengthMax));
        entity.setStopAtInstant(Instant.now());

        // [#402] Preview Chain cannot be public
        entity.setEmbedKey(null);
        break;
    }

    return DAO.modelFrom(Chain.class, executeCreate(dbProvider.getDSL(), CHAIN, entity));
  }

  @Override
  public Chain readOne(Access access, UUID id) throws CoreException {
    if (access.isTopLevel())
      return DAO.modelFrom(Chain.class, dbProvider.getDSL().selectFrom(CHAIN)
        .where(CHAIN.ID.eq(id))
        .fetchOne());
    else
      return DAO.modelFrom(Chain.class, dbProvider.getDSL().selectFrom(CHAIN)
        .where(CHAIN.ID.eq(id))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne());
  }

  @Override
  public Chain readOne(Access access, String embedKey) throws CoreException {
    return DAO.modelFrom(Chain.class, dbProvider.getDSL().selectFrom(CHAIN)
      .where(CHAIN.EMBED_KEY.eq(embedKey))
      .fetchOne());
  }

  @Override
  public Collection<Chain> readMany(Access access, Collection<UUID> parentIds) throws CoreException {
    if (access.isTopLevel())
      return DAO.modelsFrom(Chain.class, dbProvider.getDSL().select(CHAIN.fields())
        .from(CHAIN)
        .where(CHAIN.ACCOUNT_ID.in(parentIds))
        .and(CHAIN.STATE.notEqual(ChainState.Erase.toString()))
        .fetch());
    else
      return DAO.modelsFrom(Chain.class, dbProvider.getDSL().select(CHAIN.fields())
        .from(CHAIN)
        .where(CHAIN.ACCOUNT_ID.in(parentIds))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccountIds()))
        .and(CHAIN.STATE.notEqual(ChainState.Erase.toString()))
        .fetch());
  }

  @Override
  public Collection<Chain> readAllInState(Access access, ChainState state) throws CoreException {
    requireRole("platform access", access, UserRoleType.Admin, UserRoleType.Engineer);

    return DAO.modelsFrom(Chain.class, dbProvider.getDSL().select(CHAIN.fields())
      .from(CHAIN)
      .where(CHAIN.STATE.eq(state.toString()))
      .or(CHAIN.STATE.eq(state.toString().toLowerCase(Locale.ENGLISH)))
      .fetch());
  }

  @Override
  public void update(Access access, UUID id, Chain entity) throws CoreException {
    DSLContext db = dbProvider.getDSL();

    // fetch existing chain; further logic is based on its current type and state
    Chain chain = readOne(access, id);

    // cannot change chain type
    entity.setTypeEnum(chain.getType());

    // validate and cache to-state
    ChainState fromState = chain.getState();
    ChainState toState = entity.getState();

    // validate the updated chain entity
    entity.validate();

    // If not top level access, validate access to account
    if (!access.isTopLevel())
      try {
        requireAccount(access, chain.getAccountId());
      } catch (Exception e) {
        throw new CoreException("must have either top-level or account access", e);
      }

    // logic based on Chain Type
    switch (chain.getType()) {

      case Production:
        requireRole("Engineer role", access, UserRoleType.Engineer);
        // [#403] Chain must have unique `embed_key`
        if (Objects.nonNull(entity.getEmbedKey()))
          requireNotExists("Existing Chain with this embed_key",
            db.selectCount().from(CHAIN)
              .where(CHAIN.ID.notEqual(id))
              .and(CHAIN.EMBED_KEY.eq(entity.getEmbedKey()))
              .fetchOne(0, int.class));
        break;

      case Preview:
        requireRole("Artist or Engineer role", access, UserRoleType.Engineer, UserRoleType.Artist);

        // [#402] Preview Chain cannot be public
        entity.setEmbedKey(null);
        break;
    }

    // logic based on existing Chain State
    switch (fromState) {

      case Draft:
        onlyAllowTransitions(toState, ChainState.Draft, ChainState.Ready, ChainState.Erase);
        if (toState.equals(ChainState.Ready) && 0 >= db.selectCount().from(CHAIN_BINDING)
          .where(CHAIN_BINDING.CHAIN_ID.eq(id))
          .fetchOne(0, int.class))
          throw new CoreException("Chain must be bound to at least one Library, Sequence, or Instrument");
        break;

      case Ready:
        onlyAllowTransitions(toState, ChainState.Draft, ChainState.Ready, ChainState.Fabricate);
        break;

      case Fabricate:
        onlyAllowTransitions(toState, ChainState.Fabricate, ChainState.Failed, ChainState.Complete);
        break;

      case Complete:
        onlyAllowTransitions(toState, ChainState.Complete, ChainState.Erase);
        break;

      case Failed:
        onlyAllowTransitions(toState, ChainState.Failed, ChainState.Erase);
        break;

      case Erase:
        onlyAllowTransitions(toState, ChainState.Erase);
        break;
    }


    // If state is changing, there may be field updates based on the new state
    if (fromState != toState) switch (toState) {
      case Draft:
      case Ready:
      case Fabricate:
      case Complete:
      case Failed:
        // no op
        break;

      case Erase:
        entity.setEmbedKey(null);
        break;
    }

    // [#116] cannot change chain startAt time after has segments
    Instant updateStartAt = entity.getStartAt();
    if (Objects.nonNull(updateStartAt)
      && !chain.getStartAt().equals(updateStartAt))
      requireNotExists(
        "cannot change chain startAt time after it has segments",
        db.selectCount().from(SEGMENT)
          .where(SEGMENT.CHAIN_ID.eq(chain.getId()))
          .fetchOne(0, int.class)
      );

    // by only updating the chain from the expected state,
    // this prevents the state from being updated multiple times,
    // for example in the case of duplicate work
    ChainRecord updatedRecord = db.newRecord(CHAIN);
    DAO.setAll(updatedRecord, entity);
    int rowsAffected = db.update(CHAIN)
      .set(updatedRecord)
      .where(CHAIN.ID.eq(id))
      .and(CHAIN.STATE.eq(fromState.toString()))
      .execute();


    // If no records updated, failure
    if (0 == rowsAffected)
      throw new CoreException("No records updated.");

    // If state is changing, then trigger work logic based on the new Chain State
    if (fromState != toState) switch (toState) {
      case Draft:
      case Ready:
        // no op
        break;

      case Fabricate:
        workManager.startChainFabrication(id);
        break;

      case Complete:
      case Failed:
        workManager.stopChainFabrication(id);
        break;

      case Erase:
        workManager.startChainErase(id);
        break;
    }
  }

  @Override
  public void updateState(Access access, UUID id, ChainState state) throws CoreException {
    Chain chain = DAO.modelFrom(Chain.class, dbProvider.getDSL().selectFrom(CHAIN)
      .where(CHAIN.ID.eq(id))
      .fetchOne());
    chain.setStateEnum(state);
    update(access, id, chain);
  }

  @Override
  public Optional<Segment> buildNextSegmentOrComplete(Access access, Chain chain, Instant segmentBeginBefore, Instant chainStopCompleteBefore) throws CoreException {
    DSLContext db = dbProvider.getDSL();
    requireTopLevel(access);

    Record lastRecordWithNoEndAtTime = db.select(SEGMENT.CHAIN_ID)
      .from(SEGMENT)
      .where(SEGMENT.END_AT.isNull())
      .and(SEGMENT.CHAIN_ID.eq(chain.getId()))
      .groupBy(SEGMENT.CHAIN_ID, SEGMENT.OFFSET, SEGMENT.END_AT)
      .orderBy(SEGMENT.OFFSET.desc())
      .limit(1)
      .fetchOne();

    // If there's already a no-endAt-time-having Segment
    // at the end of this Chain, get outta here
    if (Objects.nonNull(lastRecordWithNoEndAtTime))
      return Optional.empty();

    // Get the last segment in the chain
    SegmentRecord lastSegmentInChain = db.selectFrom(SEGMENT)
      .where(SEGMENT.CHAIN_ID.eq(chain.getId()))
      .and(SEGMENT.BEGIN_AT.isNotNull())
      .and(SEGMENT.END_AT.isNotNull())
      .groupBy(SEGMENT.CHAIN_ID, SEGMENT.OFFSET, SEGMENT.END_AT, SEGMENT.ID)
      .orderBy(SEGMENT.OFFSET.desc())
      .limit(1)
      .fetchOne();

    // If the chain had no last segment, it must be empty; return a template for its first segment
    if (Objects.isNull(lastSegmentInChain)) {
      Segment pilotTemplate = new Segment();
      pilotTemplate.setChainId(chain.getId());
      pilotTemplate.setBeginAtInstant(chain.getStartAt());
      pilotTemplate.setOffset(0L);
      pilotTemplate.setState(SegmentState.Planned.toString());
      return Optional.of(pilotTemplate);
    }

    // If the last segment begins after our boundary, we're here early; get outta here.
    if (lastSegmentInChain.getBeginAt().toInstant().isAfter(segmentBeginBefore)) {
      return Optional.empty();
    }

    /*
     [#204] Craft worker updates Chain to COMPLETE state when the final segment is in dubbed state.
     */
    if (Objects.nonNull(lastSegmentInChain.getEndAt())
      && Objects.nonNull(chain.getStopAt())
      && lastSegmentInChain.getEndAt().toInstant().isAfter(chain.getStopAt())) {
      // this is where we check to see if the chain is ready to be COMPLETE.
      if (chain.getStopAt().isBefore(chainStopCompleteBefore)
        // and [#122] require the last segment in the chain to be in state DUBBED.
        && Objects.equals(lastSegmentInChain.getState(), SegmentState.Dubbed.toString())) {
        updateState(access, chain.getId(), ChainState.Complete);
      }
      return Optional.empty();
    }

    // Build the template of the segment that follows the last known one
    Segment pilotTemplate = new Segment();
    Long pilotOffset = lastSegmentInChain.getOffset() + 1;
    pilotTemplate.setChainId(chain.getId());
    pilotTemplate.setBeginAtInstant(lastSegmentInChain.getEndAt().toInstant());
    pilotTemplate.setOffset(pilotOffset);
    pilotTemplate.setState(SegmentState.Planned.toString());
    return Optional.of(pilotTemplate);
  }

  @Override
  public void destroy(Access access, UUID id) throws CoreException {
    DSLContext db = dbProvider.getDSL();
    if (access.isTopLevel())
      requireExists("Chain", db.selectCount().from(CHAIN)
        .where(CHAIN.ID.eq(id))
        .fetchOne(0, int.class));
    else
      requireExists("Chain", db.selectCount().from(CHAIN)
        .where(CHAIN.ID.eq(id))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne(0, int.class));

    requireNotExists("Segment in Chain", db.select(SEGMENT.ID)
      .from(SEGMENT)
      .where(SEGMENT.CHAIN_ID.eq(id))
      .fetch());

    db.deleteFrom(CHAIN_CONFIG)
      .where(CHAIN_CONFIG.CHAIN_ID.eq(id))
      .execute();

    db.deleteFrom(CHAIN_BINDING)
      .where(CHAIN_BINDING.CHAIN_ID.eq(id))
      .execute();

    db.deleteFrom(CHAIN)
      .where(CHAIN.ID.eq(id))
      .execute();
  }

  @Override
  public void erase(Access access, UUID chainId) throws CoreException {
    updateState(access, chainId, ChainState.Erase);
  }

  @Override
  public Chain newInstance() {
    return new Chain();
  }

  @Override
  public Chain revive(Access access, UUID priorChainId) throws CoreException {
    Chain priorChain = readOne(access, priorChainId);
    requireExists("prior Chain", priorChain);

    if (ChainState.Fabricate != priorChain.getState())
      throw new CoreException("Only a Fabricate-state Chain can be revived.");

    if (ChainType.Production != priorChain.getType())
      throw new CoreException("Only a Production-type Chain can be revived.");

    // save the embed key to re-use on new chain
    String embedKey = priorChain.getEmbedKey();

    // update the prior chain to failed state and null embed key
    priorChain.setStateEnum(ChainState.Failed);
    priorChain.setEmbedKey(null);
    update(access, priorChainId, priorChain);

    // of new chain with original properties (implicitly createdin draft state)
    priorChain.setId(UUID.randomUUID()); // new id
    priorChain.setEmbedKey(embedKey);
    priorChain.setStartAtInstant(Instant.now()); // [#170273871] Revived chain should always start now
    Chain createdChain = create(access, priorChain);

    // clone all chain configs and bindings from prior chain to createdchain
    Cloner cloner = new Cloner();
    cloner.clone(dbProvider.getDSL(), CHAIN_CONFIG, CHAIN_CONFIG.ID, ImmutableList.of(), CHAIN_CONFIG.CHAIN_ID, priorChainId, createdChain.getId());
    cloner.clone(dbProvider.getDSL(), CHAIN_BINDING, CHAIN_BINDING.ID, ImmutableList.of(), CHAIN_BINDING.CHAIN_ID, priorChainId, createdChain.getId());

    // update new chain into ready
    updateState(access, createdChain.getId(), ChainState.Ready);
    createdChain.setStateEnum(ChainState.Ready);

    // update new chain into fabricate, starting the work
    updateState(access, createdChain.getId(), ChainState.Fabricate);
    createdChain.setStateEnum(ChainState.Fabricate);

    // of a platform message reporting the event
    platformMessageDAO.create(Access.internal(), new PlatformMessage()
      .setType(MessageType.Warning.toString())
      .setBody(String.format("Revived Chain #%s create prior Chain #%s", createdChain.getId(), priorChain.getId())));

    // return newly created chain
    return createdChain;
  }

  @Override
  public Collection<Chain> checkAndReviveAll(Access access) throws CoreException {
    DSLContext db = dbProvider.getDSL();
    requireTopLevel(access);

    Collection<Chain> revivedChains = Lists.newArrayList();
    Collection<UUID> stalledChainIds = Lists.newArrayList();
    Timestamp thresholdChainStartAt = Timestamp.from(Instant.now().minusSeconds(chainReviveThresholdStartSeconds));
    Timestamp thresholdChainHeadAt = Timestamp.from(Instant.now().minusSeconds(chainReviveThresholdHeadSeconds));

    // recursive queries for stalled chains
    for (ChainRecord record : db.selectFrom(CHAIN)
      .where(CHAIN.TYPE.equal(ChainType.Production.toString()))
      .and(CHAIN.STATE.equal(ChainState.Fabricate.toString()))
      .and(CHAIN.START_AT.lessOrEqual(thresholdChainStartAt))
      .fetch()) {
      if (0 == db.selectCount().from(SEGMENT)
        .where(SEGMENT.CHAIN_ID.eq(record.getId()))
        .and(SEGMENT.STATE.equal(SegmentState.Dubbed.toString()))
        .and(SEGMENT.END_AT.greaterOrEqual(thresholdChainHeadAt))
        .fetchOne(0, int.class)) {
        stalledChainIds.add(record.getId());
      }
    }

    // revive all stalled chains
    for (UUID chainId : stalledChainIds) {
      revivedChains.add(revive(access, chainId));
    }

    return revivedChains;
  }

}
