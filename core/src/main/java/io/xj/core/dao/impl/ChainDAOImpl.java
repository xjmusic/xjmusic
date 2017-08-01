// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.dao.impl;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.UpdateSetFirstStep;
import org.jooq.types.ULong;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

import io.xj.core.app.access.impl.Access;
import io.xj.core.app.config.Config;
import io.xj.core.app.exception.BusinessException;
import io.xj.core.app.exception.CancelException;
import io.xj.core.app.exception.ConfigException;
import io.xj.core.app.exception.DatabaseException;
import io.xj.core.dao.ChainDAO;
import io.xj.core.database.sql.SQLDatabaseProvider;
import io.xj.core.database.sql.impl.SQLConnection;
import io.xj.core.model.chain.Chain;
import io.xj.core.model.chain.ChainState;
import io.xj.core.model.chain.ChainType;
import io.xj.core.model.link.Link;
import io.xj.core.model.link.LinkState;
import io.xj.core.model.role.Role;
import io.xj.core.tables.records.ChainRecord;
import io.xj.core.tables.records.LinkRecord;
import io.xj.core.transport.CSV;
import io.xj.core.work.WorkManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static io.xj.core.Tables.ACCOUNT;
import static io.xj.core.Tables.CHAIN;
import static io.xj.core.Tables.CHAIN_CONFIG;
import static io.xj.core.Tables.CHAIN_IDEA;
import static io.xj.core.Tables.CHAIN_INSTRUMENT;
import static io.xj.core.Tables.CHAIN_LIBRARY;
import static io.xj.core.Tables.LINK;

/**
 Chain D.A.O. Implementation
 <p>
 Core directive here is to keep business logic CENTRAL ("oneness")
 <p>
 All variants on an update resolve (after entity transformation,
 or additional validation) to one singular central update(fieldValues)
 <p>
 Also note buildNextLinkOrComplete(...) is one singular central implementation
 of the logic around adding links to chains and updating chain state to complete.
 */
public class ChainDAOImpl extends DAOImpl implements ChainDAO {
  private static final Logger log = LoggerFactory.getLogger(ChainDAOImpl.class);
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

  @Override
  public ChainRecord create(Access access, Chain entity) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(create(tx.getContext(), access, entity));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public ChainRecord readOne(Access access, ULong chainId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOneRecord(tx.getContext(), access, chainId));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public Result<ChainRecord> readAll(Access access, ULong accountId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAll(tx.getContext(), access, accountId));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Result<ChainRecord> readAllInState(Access access, ChainState state, Integer limit) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAllInState(tx.getContext(), access, state, limit));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public Result<ChainRecord> readAllInStateFabricating(Access access, Timestamp atOrBefore) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAllRecordsInStateFabricating(tx.getContext(), access, atOrBefore));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void update(Access access, ULong id, Chain entity) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      update(tx.getContext(), access, id, entity);
      tx.success();
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void updateState(Access access, ULong id, ChainState state) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      updateState(tx.getContext(), access, id, state);
      tx.success();
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Link buildNextLinkOrComplete(Access access, ChainRecord chain, Timestamp linkBeginBefore, Timestamp chainStopCompleteBefore) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(buildNextLinkOrComplete(tx.getContext(), access, chain, linkBeginBefore, chainStopCompleteBefore));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void delete(Access access, ULong chainId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      delete(tx.getContext(), access, chainId);
      tx.success();
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void erase(Access access, ULong chainId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      updateState(tx.getContext(), access, chainId, ChainState.Erase);
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
  private ChainRecord create(DSLContext db, Access access, Chain entity) throws BusinessException {
    entity.validate();

    Map<Field, Object> fieldValues = entity.updatableFieldValueMap();

    // [#126] Chains are always readMany in DRAFT state
    fieldValues.put(CHAIN.STATE, ChainState.Draft);

    if (access.isTopLevel())
      requireExists("Account", db.select(ACCOUNT.ID).from(ACCOUNT)
        .where(ACCOUNT.ID.eq(entity.getAccountId()))
        .fetchOne());
    else
      requireExists("Account", db.select(ACCOUNT.ID).from(ACCOUNT)
        .where(ACCOUNT.ID.in(access.getAccounts()))
        .and(ACCOUNT.ID.eq(entity.getAccountId()))
        .fetchOne());


    // [#190] Artist "Preview" Chain
    if (fieldValues.get(CHAIN.TYPE).equals(ChainType.Preview)) {
      requireRole("Artist to create preview Chain", access, Role.ARTIST);
      fieldValues.put(CHAIN.START_AT,
        Timestamp.from(Instant.now().minusSeconds(previewLengthMax)));
      fieldValues.put(CHAIN.STOP_AT,
        Timestamp.from(Instant.now()));

      // [#190] Engineer "Production" Chain
    } else if (fieldValues.get(CHAIN.TYPE).equals(ChainType.Production)) {
      requireRole("Engineer to create production Chain", access, Role.ENGINEER);

    } else {
      throw new BusinessException("Invalid Chain type '" + fieldValues.get(CHAIN.TYPE) + "'");
    }

    return executeCreate(db, CHAIN, fieldValues);
  }

  /**
   Read one record

   @param db     context
   @param access control
   @param id     of record
   @return record
   */
  private static ChainRecord readOneRecord(DSLContext db, Access access, ULong id) {
    if (access.isTopLevel())
      return db.selectFrom(CHAIN)
        .where(CHAIN.ID.eq(id))
        .fetchOne();
    else
      return db.selectFrom(CHAIN)
        .where(CHAIN.ID.eq(id))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne();
  }

  /**
   Read all records in parent by id

   @param db        context
   @param access    control
   @param accountId of parent
   @return array of records
   */
  private Result<ChainRecord> readAll(DSLContext db, Access access, ULong accountId) {
    if (access.isTopLevel())
      return resultInto(CHAIN, db.select(CHAIN.fields())
        .from(CHAIN)
        .where(CHAIN.ACCOUNT_ID.eq(accountId))
        .and(CHAIN.STATE.notEqual(ChainState.Erase.toString()))
        .fetch());
    else
      return resultInto(CHAIN, db.select(CHAIN.fields())
        .from(CHAIN)
        .where(CHAIN.ACCOUNT_ID.eq(accountId))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccounts()))
        .and(CHAIN.STATE.notEqual(ChainState.Erase.toString()))
        .fetch());
  }

  /**
   Read all records in a given state

   @param db     context
   @param access control
   @param state  to read chains in
   @param limit  records max
   @return array of records
   */
  private Result<ChainRecord> readAllInState(DSLContext db, Access access, ChainState state, Integer limit) throws Exception {
    requireTopLevel(access);
    return resultInto(CHAIN, db.select(CHAIN.fields())
      .from(CHAIN)
      .where(CHAIN.STATE.eq(state.toString()))
      .or(CHAIN.STATE.eq(state.toString().toLowerCase()))
      .limit(limit)
      .fetch());
  }

  /**
   Read all records now in fabricating-state

   @param db         context
   @param access     control
   @param atOrBefore time to check for chains in fabricating state
   @return array of records
   */
  private Result<ChainRecord> readAllRecordsInStateFabricating(DSLContext db, Access access, Timestamp atOrBefore) throws BusinessException {
    requireTopLevel(access);

    return db.selectFrom(CHAIN)
      .where(CHAIN.STATE.eq(ChainState.Fabricating.toString()))
      .and(CHAIN.START_AT.lessOrEqual(atOrBefore))
      .fetch();
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
  private void update(DSLContext db, Access access, ULong id, Chain entity) throws Exception {
    entity.validate();

    Map<Field, Object> fieldValues = entity.updatableFieldValueMap();

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
  private void update(DSLContext db, Access access, ULong id, Map<Field, Object> fieldValues) throws Exception {
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
        requireRole("engineer role", access, Role.ENGINEER);
        break;

      case Preview:
        requireRole("artist or engineer role", access, Role.ENGINEER, Role.ARTIST);
        break;
    }

    // logic based on existing Chain State
    switch (fromState) {

      case Draft:
        onlyAllowTransitions(toState, ChainState.Draft, ChainState.Ready, ChainState.Erase);
        if (Objects.equals(ChainState.Ready, toState)) {
          requireExistsAnyOf("Chain must be bound to at least one Library, Idea, or Instrument",
            db.selectFrom(CHAIN_LIBRARY).where(CHAIN_LIBRARY.CHAIN_ID.eq(id)).fetchOne(),
            db.selectFrom(CHAIN_IDEA).where(CHAIN_IDEA.CHAIN_ID.eq(id)).fetchOne(),
            db.selectFrom(CHAIN_INSTRUMENT).where(CHAIN_INSTRUMENT.CHAIN_ID.eq(id)).fetchOne()
          );
        }
        break;

      case Ready:
        onlyAllowTransitions(toState, ChainState.Draft, ChainState.Ready, ChainState.Fabricating);
        break;

      case Fabricating:
        onlyAllowTransitions(toState, ChainState.Fabricating, ChainState.Failed, ChainState.Complete);
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

    // [#116] cannot change chain startAt time after has links
    Object updateStartAt = fieldValues.get(CHAIN.START_AT);
    if (Objects.nonNull(updateStartAt)
      && !chain.getStartAt().equals(Timestamp.valueOf(updateStartAt.toString())))
      requireNotExists(
        "cannot change chain startAt time after it has links",
        db.select(LINK.ID).from(LINK)
          .where(LINK.CHAIN_ID.eq(chain.getId()))
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
    if (!Objects.equals(fromState,toState)) switch (toState) {
      case Draft:
      case Ready:
        // no op
        break;

      case Fabricating:
        workManager.startChainFabrication(id);
        break;

      case Complete:
        workManager.stopChainFabrication(id);
        break;

      case Failed:
        workManager.stopChainFabrication(id);
        break;

      case Erase:
        workManager.startChainDeletion(id);
        break;
    }
  }

  /**
   Read all records in parent by id

   @param db                      context
   @param chain                   to readMany pilot template link for
   @param linkBeginBefore         time upper threshold
   @param chainStopCompleteBefore time upper threshold
   @return Link template
   */
  @Nullable
  private Link buildNextLinkOrComplete(DSLContext db, Access access, ChainRecord chain, Timestamp linkBeginBefore, Timestamp chainStopCompleteBefore) throws Exception {
    requireTopLevel(access);

    Record lastRecordWithNoEndAtTime = db.select(LINK.CHAIN_ID)
      .from(LINK)
      .where(LINK.END_AT.isNull())
      .and(LINK.CHAIN_ID.eq(chain.getId()))
      .groupBy(LINK.CHAIN_ID, LINK.OFFSET, LINK.END_AT)
      .orderBy(LINK.OFFSET.desc())
      .limit(1)
      .fetchOne();

    // If there's already a no-endAt-time-having Link
    // at the end of this Chain, get outta here
    if (Objects.nonNull(lastRecordWithNoEndAtTime))
      return null;

    // Get the last link in the chain
    LinkRecord lastLinkInChain = db.selectFrom(LINK)
      .where(LINK.CHAIN_ID.eq(chain.getId()))
      .and(LINK.BEGIN_AT.isNotNull())
      .and(LINK.END_AT.isNotNull())
      .groupBy(LINK.CHAIN_ID, LINK.OFFSET, LINK.END_AT)
      .orderBy(LINK.OFFSET.desc())
      .limit(1)
      .fetchOne();

    // If the chain had no last link, it must be empty; return a template for its first link
    if (Objects.isNull(lastLinkInChain)) {
      Link pilotTemplate = new Link();
      pilotTemplate.setChainId(chain.getId().toBigInteger());
      pilotTemplate.setBeginAtTimestamp(chain.getStartAt());
      pilotTemplate.setOffset(BigInteger.ZERO);
      pilotTemplate.setState(LinkState.Planned.toString());
      return pilotTemplate;
    }

    // If the last link begins after our boundary, we're here early; get outta here.
    if (lastLinkInChain.getBeginAt().after(linkBeginBefore)) {
      return null;
    }

    /*
     [#204] Craftworker updates Chain to COMPLETE state when the final link is in dubbed state.
     */
    if (Objects.nonNull(lastLinkInChain.getEndAt())
      && Objects.nonNull(chain.getStopAt())
      && lastLinkInChain.getEndAt().after(chain.getStopAt())) {
      // this is where we check to see if the chain is ready to be COMPLETE.
      if (chain.getStopAt().before(chainStopCompleteBefore)
        // and [#122] require the last link in the chain to be in state DUBBED.
        && Objects.equals(lastLinkInChain.getState(), LinkState.Dubbed.toString())) {
        updateState(db, access, chain.getId(), ChainState.Complete);
      }
      return null;
    }

    // Build the template of the link that follows the last known one
    Link pilotTemplate = new Link();
    ULong pilotOffset = ULong.valueOf(lastLinkInChain.getOffset().toBigInteger().add(BigInteger.valueOf(1)));
    pilotTemplate.setChainId(chain.getId().toBigInteger());
    pilotTemplate.setBeginAtTimestamp(lastLinkInChain.getEndAt());
    pilotTemplate.setOffset(pilotOffset.toBigInteger());
    pilotTemplate.setState(LinkState.Planned.toString());
    return pilotTemplate;
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
  private void delete(DSLContext db, Access access, ULong id) throws Exception {
    if (access.isTopLevel())
      requireExists("Chain", db.selectFrom(CHAIN)
        .where(CHAIN.ID.eq(id))
        .fetchOne());
    else
      requireExists("Chain", db.select(CHAIN.fields())
        .from(CHAIN)
        .where(CHAIN.ID.eq(id))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne());

    requireNotExists("Link in Chain", db.select(LINK.ID)
      .from(LINK)
      .where(LINK.CHAIN_ID.eq(id))
      .fetch());

    // Chain-Idea before Chain
    db.deleteFrom(CHAIN_IDEA)
      .where(CHAIN_IDEA.CHAIN_ID.eq(id))
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

}
