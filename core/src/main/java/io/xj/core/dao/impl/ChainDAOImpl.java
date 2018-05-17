// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao.impl;

import io.xj.core.access.impl.Access;
import io.xj.core.config.Config;
import io.xj.core.dao.ChainDAO;
import io.xj.core.exception.BusinessException;
import io.xj.core.exception.CancelException;
import io.xj.core.exception.ConfigException;
import io.xj.core.exception.DatabaseException;
import io.xj.core.model.chain.Chain;
import io.xj.core.model.chain.ChainState;
import io.xj.core.model.chain.ChainType;
import io.xj.core.model.segment.Segment;
import io.xj.core.model.segment.SegmentState;
import io.xj.core.model.user_role.UserRoleType;
import io.xj.core.persistence.sql.SQLDatabaseProvider;
import io.xj.core.persistence.sql.impl.SQLConnection;
import io.xj.core.tables.records.ChainRecord;
import io.xj.core.tables.records.SegmentRecord;
import io.xj.core.transport.CSV;
import io.xj.core.work.WorkManager;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.UpdateSetFirstStep;
import org.jooq.impl.DSL;
import org.jooq.types.ULong;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static io.xj.core.Tables.ACCOUNT;
import static io.xj.core.Tables.CHAIN;
import static io.xj.core.Tables.CHAIN_CONFIG;
import static io.xj.core.Tables.CHAIN_INSTRUMENT;
import static io.xj.core.Tables.CHAIN_LIBRARY;
import static io.xj.core.Tables.CHAIN_SEQUENCE;
import static io.xj.core.Tables.SEGMENT;

/**
 Chain D.A.O. Implementation
 <p>
 Core directive here is to keep business logic CENTRAL ("oneness")
 <p>
 All variants on an update resolve (after entity transformation,
 or additional validation) to one singular central update(fieldValues)
 <p>
 Also note buildNextSegmentOrComplete(...) is one singular central implementation
 of the logic around adding segments to chains and updating chain state to complete.
 */
public class ChainDAOImpl extends DAOImpl implements ChainDAO {
  //  private static final Logger log = LoggerFactory.getLogger(ChainDAOImpl.class);
  private final int previewLengthMax;
  private final WorkManager workManager;

  @Inject
  public ChainDAOImpl(
    SQLDatabaseProvider dbProvider,
    WorkManager workManager
  ) {
    this.workManager = workManager;
    this.dbProvider = dbProvider;
    previewLengthMax = Config.chainPreviewLengthMax();
  }

  /**
   Read one record by id

   @param db     context
   @param access control
   @param id     of record
   @return record
   */
  private static Chain readOne(DSLContext db, Access access, ULong id) throws BusinessException {
    if (access.isTopLevel())
      return modelFrom(db.selectFrom(CHAIN)
        .where(CHAIN.ID.eq(id))
        .fetchOne(), Chain.class);
    else
      return modelFrom(db.selectFrom(CHAIN)
        .where(CHAIN.ID.eq(id))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne(), Chain.class);
  }

  /**
   [#150279540] Unauthenticated or specifically-authenticated public Client wants to access a Chain by embed key (as alias for chain id) in order to provide data for playback.

   @param db     context
   @param embedKey of record
   @return record
   */
  private static Chain readOne(DSLContext db, String embedKey) throws BusinessException {
    return modelFrom(db.selectFrom(CHAIN)
      .where(CHAIN.EMBED_KEY.eq(embedKey))
      .fetchOne(), Chain.class);
  }

  /**
   Read all records in parent by id

   @param db         context
   @param access     control
   @param accountIds of parent
   @return array of records
   */
  private static Collection<Chain> readAll(DSLContext db, Access access, Collection<ULong> accountIds) throws BusinessException {
    if (access.isTopLevel())
      return modelsFrom(db.select(CHAIN.fields())
        .from(CHAIN)
        .where(CHAIN.ACCOUNT_ID.in(accountIds))
        .and(CHAIN.STATE.notEqual(ChainState.Erase.toString()))
        .fetch(), Chain.class);
    else
      return modelsFrom(db.select(CHAIN.fields())
        .from(CHAIN)
        .where(CHAIN.ACCOUNT_ID.in(accountIds))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccountIds()))
        .and(CHAIN.STATE.notEqual(ChainState.Erase.toString()))
        .fetch(), Chain.class);
  }

  /**
   Read all records in a given state

   @param db     context
   @param access control
   @param state  to read chains in
   @return array of records
   */
  private static Collection<Chain> readAllInState(DSLContext db, Access access, ChainState state) throws Exception {
    requireRole("platform access", access, UserRoleType.Admin, UserRoleType.Engineer);

    return modelsFrom(db.select(CHAIN.fields())
      .from(CHAIN)
      .where(CHAIN.STATE.eq(state.toString()))
      .or(CHAIN.STATE.eq(state.toString().toLowerCase()))
      .fetch(), Chain.class);
  }

  /**
   Delete a Chain

   @param db     context
   @param access control
   @param id     to delete
   @throws Exception         if database failure
   @throws ConfigException   if not configured properly
   @throws BusinessException if fails business rule
   */
  private static void delete(DSLContext db, Access access, ULong id) throws Exception {
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

    // Chain-Sequence before Chain
    db.deleteFrom(CHAIN_SEQUENCE)
      .where(CHAIN_SEQUENCE.CHAIN_ID.eq(id))
      .execute();

    // Chain-Instrument before Chain
    db.deleteFrom(CHAIN_INSTRUMENT)
      .where(CHAIN_INSTRUMENT.CHAIN_ID.eq(id))
      .execute();

    // Chain-Library before Chain
    db.deleteFrom(CHAIN_LIBRARY)
      .where(CHAIN_LIBRARY.CHAIN_ID.eq(id))
      .execute();

    // Chain-Config before Chain
    db.deleteFrom(CHAIN_CONFIG)
      .where(CHAIN_CONFIG.CHAIN_ID.eq(id))
      .execute();

    db.deleteFrom(CHAIN)
      .where(CHAIN.ID.eq(id))
      .execute();
  }

  /**
   Require state is in an array of states

   @param toState       to check
   @param allowedStates required to be in
   @throws CancelException if not in required states
   */
  private static void onlyAllowTransitions(ChainState toState, ChainState... allowedStates) throws CancelException {
    List<String> allowedStateNames = Lists.newArrayList();
    for (ChainState search : allowedStates) {
      allowedStateNames.add(search.toString());
      if (Objects.equals(search, toState)) {
        return;
      }
    }
    throw new CancelException(String.format("transition to %s not in allowed (%s)",
      toState, CSV.join(allowedStateNames)));
  }

  /**
   Only certain (writable) fields are mapped back to jOOQ records--
   Read-only fields are excluded from here.

   @param entity to source values from
   @return values mapped to record fields
   */
  private static Map<Field, Object> fieldValueMap(Chain entity) {
    Map<Field, Object> fieldValues = com.google.api.client.util.Maps.newHashMap();
    fieldValues.put(CHAIN.ACCOUNT_ID, ULong.valueOf(entity.getAccountId()));
    fieldValues.put(CHAIN.NAME, entity.getName());
    fieldValues.put(CHAIN.TYPE, entity.getType());
    fieldValues.put(CHAIN.STATE, entity.getState());
    fieldValues.put(CHAIN.START_AT, entity.getStartAt());
    fieldValues.put(CHAIN.STOP_AT, entity.getStopAt());
    fieldValues.put(CHAIN.EMBED_KEY, entity.getEmbedKey());
    return fieldValues;
  }

  @Override
  public Chain create(Access access, Chain entity) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(create(tx.getContext(), access, entity));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public Chain readOne(Access access, BigInteger id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOne(tx.getContext(), access, ULong.valueOf(id)));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Nullable
  @Override
  public Chain readOne(Access access, String embedKey) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOne(tx.getContext(), embedKey));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public Collection<Chain> readAll(Access access, Collection<BigInteger> parentIds) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAll(tx.getContext(), access, uLongValuesOf(parentIds)));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Collection<Chain> readAllInState(Access access, ChainState state) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAllInState(tx.getContext(), access, state));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void update(Access access, BigInteger id, Chain entity) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      updateAllFields(tx.getContext(), access, ULong.valueOf(id), entity);
      tx.success();
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void updateState(Access access, BigInteger id, ChainState state) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      updateState(tx.getContext(), access, ULong.valueOf(id), state);
      tx.success();
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Segment buildNextSegmentOrComplete(Access access, Chain chain, Timestamp segmentBeginBefore, Timestamp chainStopCompleteBefore) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(buildNextSegmentOrComplete(tx.getContext(), access, chain, segmentBeginBefore, chainStopCompleteBefore));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void destroy(Access access, BigInteger id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      delete(tx.getContext(), access, ULong.valueOf(id));
      tx.success();
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void erase(Access access, BigInteger chainId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      updateState(tx.getContext(), access, ULong.valueOf(chainId), ChainState.Erase);
      tx.success();
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  /**
   Create a new record

   @param db     context
   @param access control
   @param entity for new record
   @return newly readMany record
   @throws BusinessException if a Business Rule is violated
   */
  private Chain create(DSLContext db, Access access, Chain entity) throws Exception {
    entity.validate();

    Map<Field, Object> fieldValues = fieldValueMap(entity);

    // [#126] Chains are always readMany in DRAFT state
    fieldValues.put(CHAIN.STATE, ChainState.Draft);

    if (access.isTopLevel())
      requireExists("Account", db.selectCount().from(ACCOUNT)
        .where(ACCOUNT.ID.eq(ULong.valueOf(entity.getAccountId())))
        .fetchOne(0, int.class));
    else
      requireExists("Account", db.selectCount().from(ACCOUNT)
        .where(ACCOUNT.ID.in(access.getAccountIds()))
        .and(ACCOUNT.ID.eq(ULong.valueOf(entity.getAccountId())))
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
        fieldValues.put(CHAIN.START_AT,
          Timestamp.from(Instant.now().minusSeconds((long) previewLengthMax)));
        fieldValues.put(CHAIN.STOP_AT,
          Timestamp.from(Instant.now()));

        // [#402] Preview Chain cannot be public
        fieldValues.put(CHAIN.EMBED_KEY, DSL.val((String) null));
        break;
    }

    return modelFrom(executeCreate(db, CHAIN, fieldValues), Chain.class);
  }

  /**
   Update a record using a model wrapper

   @param db     context
   @param access control
   @param id     of chain to update
   @param entity wrapper
   @throws BusinessException on failure
   @throws DatabaseException on failure
   */
  private void updateAllFields(DSLContext db, Access access, ULong id, Chain entity) throws Exception {
    entity.validate();

    Map<Field, Object> fieldValues = fieldValueMap(entity);

    // Cannot update TYPE of chain
    if (fieldValues.containsKey(CHAIN.TYPE))
      fieldValues.remove(CHAIN.TYPE);

    // Cannot update ACCOUNT_ID of chain
    if (fieldValues.containsKey(CHAIN.ACCOUNT_ID))
      fieldValues.remove(CHAIN.ACCOUNT_ID);

    fieldValues.put(CHAIN.ID, id);
    update(db, access, id, fieldValues);
  }

  /**
   Update the state of a record

   @param db     context
   @param access control
   @param id     of record
   @throws BusinessException if a Business Rule is violated
   */
  private void updateState(DSLContext db, Access access, ULong id, ChainState state) throws Exception {
    Map<Field, Object> fieldValues = ImmutableMap.of(
      CHAIN.ID, id,
      CHAIN.STATE, state.toString()
    );

    update(db, access, id, fieldValues);

    if (0 == executeUpdate(db, CHAIN, fieldValues))
      throw new BusinessException("No records updated.");
  }

  /**
   Update a record

   @param db     context
   @param access control
   @param id     of record
   @throws BusinessException if a Business Rule is violated
   */
  private void update(DSLContext db, Access access, ULong id, Map<Field, Object> toUpdate) throws Exception {
    Map<Field, Object> fieldValues = Maps.newHashMap(toUpdate);

    // validate and cache to-state
    ChainState toState = ChainState.validate(fieldValues.get(CHAIN.STATE).toString());

    // fetch existing chain; further logic is based on its current type and state
    ChainRecord chain = db.selectFrom(CHAIN).where(CHAIN.ID.eq(id)).fetchOne();
    requireExists("Chain #" + id, chain);
    ChainState fromState = ChainState.validate(chain.getState());

    // If not top level access, validate access to account
    if (!access.isTopLevel())
      try {
        requireAccount(access, chain.getAccountId());
      } catch (Exception e) {
        throw new BusinessException("must have either top-level or account access", e);
      }

    // logic based on Chain Type
    switch (ChainType.validate(chain.getType())) {

      case Production:
        requireRole("Engineer role", access, UserRoleType.Engineer);
        // [#403] Chain must have unique `embed_key`
        if (Objects.nonNull(fieldValues.get(CHAIN.EMBED_KEY)))
          requireNotExists("Existing Chain with this embed_key", db.select(CHAIN.ID).from(CHAIN)
            .where(CHAIN.ID.notEqual(id))
            .and(CHAIN.EMBED_KEY.eq(fieldValues.get(CHAIN.EMBED_KEY).toString()))
            .fetch());
        break;

      case Preview:
        requireRole("Artist or Engineer role", access, UserRoleType.Engineer, UserRoleType.Artist);

        // [#402] Preview Chain cannot be public
        fieldValues.put(CHAIN.EMBED_KEY, DSL.val((String) null));
        break;
    }

    // logic based on existing Chain State
    switch (fromState) {

      case Draft:
        onlyAllowTransitions(toState, ChainState.Draft, ChainState.Ready, ChainState.Erase);
        if (Objects.equals(ChainState.Ready, toState)) {
          requireExistsAnyOf(String.format("Chain must be bound to %s", "at least one Library, Sequence, or Instrument"),
            db.selectCount().from(CHAIN_LIBRARY).where(CHAIN_LIBRARY.CHAIN_ID.eq(id)).fetchOne(0, int.class),
            db.selectCount().from(CHAIN_SEQUENCE).where(CHAIN_SEQUENCE.CHAIN_ID.eq(id)).fetchOne(0, int.class),
            db.selectCount().from(CHAIN_INSTRUMENT).where(CHAIN_INSTRUMENT.CHAIN_ID.eq(id)).fetchOne(0, int.class)
          );
        }
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

      default:
        onlyAllowTransitions(toState, ChainState.Draft);
        break;
    }


    // If state is changing, there may be field updates based on the new state
    if (!Objects.equals(fromState, toState)) switch (toState) {
      case Draft:
      case Ready:
      case Fabricate:
      case Complete:
      case Failed:
        // no op
        break;

      case Erase:
        fieldValues.put(CHAIN.EMBED_KEY, DSL.val((String) null));
        break;
    }

    // [#116] cannot change chain startAt time after has segments
    Object updateStartAt = fieldValues.get(CHAIN.START_AT);
    if (Objects.nonNull(updateStartAt)
      && !Objects.equals(chain.getStartAt(), Timestamp.valueOf(updateStartAt.toString())))
      requireNotExists(
        "cannot change chain startAt time after it has segments",
        db.select(SEGMENT.ID).from(SEGMENT)
          .where(SEGMENT.CHAIN_ID.eq(chain.getId()))
          .fetch()
      );

    // This "change from state to state" complexity
    // is required in order to prevent duplicate
    // state-changes of the same chain
    UpdateSetFirstStep<ChainRecord> update = db.update(CHAIN);
    fieldValues.forEach(update::set);
    int rowsAffected = update.set(CHAIN.STATE, toState.toString())
      .where(CHAIN.ID.eq(id))
      .and(CHAIN.STATE.eq(chain.getState()))
      .execute();

    // If no records updated, failure
    if (0 == rowsAffected)
      throw new BusinessException("No records updated.");

    // If state is changing, then trigger work logic based on the new Chain State
    if (!Objects.equals(fromState, toState)) switch (toState) {
      case Draft:
      case Ready:
        // no op
        break;

      case Fabricate:
        workManager.startChainFabrication(id.toBigInteger());
        break;

      case Complete:
        workManager.stopChainFabrication(id.toBigInteger());
        break;

      case Failed:
        workManager.stopChainFabrication(id.toBigInteger());
        break;

      case Erase:
        workManager.startChainErase(id.toBigInteger());
        break;
    }
  }

  /**
   Read all records in parent by id

   @param db                      context
   @param chain                   to readMany pilot template segment for
   @param segmentBeginBefore      time upper threshold
   @param chainStopCompleteBefore time upper threshold
   @return Segment template
   */
  @Nullable
  private Segment buildNextSegmentOrComplete(DSLContext db, Access access, Chain chain, Timestamp segmentBeginBefore, Timestamp chainStopCompleteBefore) throws Exception {
    requireTopLevel(access);

    Record lastRecordWithNoEndAtTime = db.select(SEGMENT.CHAIN_ID)
      .from(SEGMENT)
      .where(SEGMENT.END_AT.isNull())
      .and(SEGMENT.CHAIN_ID.eq(ULong.valueOf(chain.getId())))
      .groupBy(SEGMENT.CHAIN_ID, SEGMENT.OFFSET, SEGMENT.END_AT)
      .orderBy(SEGMENT.OFFSET.desc())
      .limit(1)
      .fetchOne();

    // If there's already a no-endAt-time-having Segment
    // at the end of this Chain, get outta here
    if (Objects.nonNull(lastRecordWithNoEndAtTime))
      return null;

    // Get the last segment in the chain
    SegmentRecord lastSegmentInChain = db.selectFrom(SEGMENT)
      .where(SEGMENT.CHAIN_ID.eq(ULong.valueOf(chain.getId())))
      .and(SEGMENT.BEGIN_AT.isNotNull())
      .and(SEGMENT.END_AT.isNotNull())
      .groupBy(SEGMENT.CHAIN_ID, SEGMENT.OFFSET, SEGMENT.END_AT)
      .orderBy(SEGMENT.OFFSET.desc())
      .limit(1)
      .fetchOne();

    // If the chain had no last segment, it must be empty; return a template for its first segment
    if (Objects.isNull(lastSegmentInChain)) {
      Segment pilotTemplate = new Segment();
      pilotTemplate.setChainId(chain.getId());
      pilotTemplate.setBeginAtTimestamp(chain.getStartAt());
      pilotTemplate.setOffset(BigInteger.ZERO);
      pilotTemplate.setState(SegmentState.Planned.toString());
      return pilotTemplate;
    }

    // If the last segment begins after our boundary, we're here early; get outta here.
    if (lastSegmentInChain.getBeginAt().after(segmentBeginBefore)) {
      return null;
    }

    /*
     [#204] Craftworker updates Chain to COMPLETE state when the final segment is in dubbed state.
     */
    if (Objects.nonNull(lastSegmentInChain.getEndAt())
      && Objects.nonNull(chain.getStopAt())
      && lastSegmentInChain.getEndAt().after(chain.getStopAt())) {
      // this is where we check to see if the chain is ready to be COMPLETE.
      if (chain.getStopAt().before(chainStopCompleteBefore)
        // and [#122] require the last segment in the chain to be in state DUBBED.
        && Objects.equals(lastSegmentInChain.getState(), SegmentState.Dubbed.toString())) {
        updateState(db, access, ULong.valueOf(chain.getId()), ChainState.Complete);
      }
      return null;
    }

    // Build the template of the segment that follows the last known one
    Segment pilotTemplate = new Segment();
    ULong pilotOffset = ULong.valueOf(lastSegmentInChain.getOffset().toBigInteger().add(BigInteger.valueOf(1L)));
    pilotTemplate.setChainId(chain.getId());
    pilotTemplate.setBeginAtTimestamp(lastSegmentInChain.getEndAt());
    pilotTemplate.setOffset(pilotOffset.toBigInteger());
    pilotTemplate.setState(SegmentState.Planned.toString());
    return pilotTemplate;
  }


}
