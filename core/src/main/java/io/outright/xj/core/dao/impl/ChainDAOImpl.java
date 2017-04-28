// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.dao.impl;

import io.outright.xj.core.app.access.impl.AccessControl;
import io.outright.xj.core.app.config.Config;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.app.exception.ConfigException;
import io.outright.xj.core.app.exception.DatabaseException;
import io.outright.xj.core.dao.ChainDAO;
import io.outright.xj.core.db.sql.SQLConnection;
import io.outright.xj.core.db.sql.SQLDatabaseProvider;
import io.outright.xj.core.model.chain.Chain;
import io.outright.xj.core.model.chain.ChainWrapper;
import io.outright.xj.core.model.link.Link;
import io.outright.xj.core.model.role.Role;
import io.outright.xj.core.tables.records.ChainRecord;
import io.outright.xj.core.tables.records.LinkRecord;
import io.outright.xj.core.transport.JSON;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.UpdateSetFirstStep;
import org.jooq.types.ULong;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;

import static io.outright.xj.core.Tables.CHAIN;
import static io.outright.xj.core.tables.Account.ACCOUNT;
import static io.outright.xj.core.tables.ChainConfig.CHAIN_CONFIG;
import static io.outright.xj.core.tables.Link.LINK;

/**
 * Chain D.A.O. Implementation
 * <p>
 * Core directive here is to keep business logic CENTRAL ("oneness")
 * <p>
 * All variants on an update resolve (after data transformation,
 * or additional validation) to one singular central update(fieldValues)
 * <p>
 * Also note buildNextLinkOrComplete(...) is one singular central implementation
 * of the logic around adding links to chains and updating chain state to complete.
 */
public class ChainDAOImpl extends DAOImpl implements ChainDAO {
  private final int previewLengthMax;

  @Inject
  public ChainDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
    this.previewLengthMax = Config.chainPreviewLengthMax();
  }

  @Override
  public JSONObject create(AccessControl access, ChainWrapper data) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(create(tx.getContext(), access, data));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public JSONObject readOne(AccessControl access, ULong id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOne(tx.getContext(), access, id));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public JSONArray readAllIn(AccessControl access, ULong accountId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAllIn(tx.getContext(), access, accountId));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public Result<ChainRecord> readAllRecordsInStateFabricating(AccessControl access, Timestamp atOrBefore) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAllRecordsInStateFabricating(tx.getContext(), access, atOrBefore));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void update(AccessControl access, ULong id, ChainWrapper data) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      updateWrapper(tx.getContext(), access, id, data);
      tx.success();
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void updateState(AccessControl access, ULong id, String state) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      updateState(tx.getContext(), access, id, state);
      tx.success();
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public JSONObject buildNextLinkOrComplete(AccessControl access, ChainRecord chain, Timestamp linkBeginBefore, Timestamp chainStopCompleteBefore) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(buildNextLinkOrComplete(tx.getContext(), access, chain, linkBeginBefore, chainStopCompleteBefore));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }


  @Override
  public void delete(AccessControl access, ULong chainId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      delete(tx.getContext(), access, chainId);
      tx.success();
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  /**
   * Create a new record
   *
   * @param db     context
   * @param access control
   * @param data   for new record
   * @return newly created record
   * @throws BusinessException if a Business Rule is violated
   */
  private JSONObject create(DSLContext db, AccessControl access, ChainWrapper data) throws BusinessException {
    Chain model = data.validate();
    Map<Field, Object> fieldValues = model.intoFieldValueMap();

    // [#126] Chains are always created in DRAFT state
    fieldValues.put(CHAIN.STATE, Chain.DRAFT);

    if (access.isTopLevel()) {
      requireRecordExists("Account", db.select(ACCOUNT.ID).from(ACCOUNT)
        .where(ACCOUNT.ID.eq(model.getAccountId()))
        .fetchOne());
    } else {
      requireRecordExists("Account", db.select(ACCOUNT.ID).from(ACCOUNT)
        .where(ACCOUNT.ID.in(access.getAccounts()))
        .and(ACCOUNT.ID.eq(model.getAccountId()))
        .fetchOne());
    }

    // [#190] Artist "Preview" Chain
    if (fieldValues.get(CHAIN.TYPE).equals(Chain.PREVIEW)) {
      requireRole("Artist to create preview Chain", access, Role.ARTIST);
      fieldValues.put(CHAIN.START_AT,
        Timestamp.from(Instant.now().minusSeconds(previewLengthMax)));
      fieldValues.put(CHAIN.STOP_AT,
        Timestamp.from(Instant.now()));

      // [#190] Engineer "Production" Chain
    } else if (fieldValues.get(CHAIN.TYPE).equals(Chain.PRODUCTION)) {
      requireRole("Engineer to create production Chain", access, Role.ENGINEER);

    } else {
      throw new BusinessException("Invalid Chain type '" + fieldValues.get(CHAIN.TYPE) + "'");
    }

    return JSON.objectFromRecord(executeCreate(db, CHAIN, fieldValues));
  }

  /**
   * Read one record
   *
   * @param db     context
   * @param access control
   * @param id     of record
   * @return record
   */
  private JSONObject readOne(DSLContext db, AccessControl access, ULong id) {
    if (access.isTopLevel()) {
      return JSON.objectFromRecord(db.selectFrom(CHAIN)
        .where(CHAIN.ID.eq(id))
        .fetchOne());
    } else {
      return JSON.objectFromRecord(db.select(CHAIN.fields())
        .from(CHAIN)
        .where(CHAIN.ID.eq(id))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne());
    }
  }

  /**
   * Read all records in parent by id
   *
   * @param db        context
   * @param access    control
   * @param accountId of parent
   * @return array of records
   */
  private JSONArray readAllIn(DSLContext db, AccessControl access, ULong accountId) throws SQLException {
    if (access.isTopLevel()) {
      return JSON.arrayFromResultSet(db.select(CHAIN.fields())
        .from(CHAIN)
        .where(CHAIN.ACCOUNT_ID.eq(accountId))
        .fetchResultSet());
    } else {
      return JSON.arrayFromResultSet(db.select(CHAIN.fields())
        .from(CHAIN)
        .where(CHAIN.ACCOUNT_ID.eq(accountId))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccounts()))
        .fetchResultSet());
    }
  }

  /**
   * Read all records now in fabricating-state
   *
   * @param db         context
   * @param access     control
   * @param atOrBefore time to check for chains in fabricating state
   * @return array of records
   */
  private Result<ChainRecord> readAllRecordsInStateFabricating(DSLContext db, AccessControl access, Timestamp atOrBefore) throws SQLException, BusinessException {
    requireTopLevel(access);

    return db.selectFrom(CHAIN)
      .where(CHAIN.STATE.eq(Chain.FABRICATING))
      .and(CHAIN.START_AT.lessOrEqual(atOrBefore))
      .fetch();
  }

  /**
   * Update a record using a model wrapper
   *
   * @param db     context
   * @param access control
   * @param id     of chain to update
   * @param data   wrapper
   * @throws BusinessException on failure
   * @throws DatabaseException on failure
   */
  private void updateWrapper(DSLContext db, AccessControl access, ULong id, ChainWrapper data) throws Exception {
    Chain model = data.validate();
    Map<Field, Object> fieldValues = model.intoFieldValueMap();

    // Cannot update TYPE of chain
    if (fieldValues.containsKey(CHAIN.TYPE)) {
      fieldValues.remove(CHAIN.TYPE);
    }

    fieldValues.put(CHAIN.ID, id);
    update(db, access, id, fieldValues);
  }

  /**
   * Update the state of a record
   *
   * @param db     context
   * @param access control
   * @param id     of record
   * @throws BusinessException if a Business Rule is violated
   */
  private void updateState(DSLContext db, AccessControl access, ULong id, String state) throws Exception {
    Map<Field, Object> fieldValues = ImmutableMap.of(
      CHAIN.ID, id,
      CHAIN.STATE, state
    );

    update(db, access, id, fieldValues);

    if (executeUpdate(db, CHAIN, fieldValues) == 0) {
      throw new BusinessException("No records updated.");
    }
  }

  /**
   * Update a record
   *
   * @param db     context
   * @param access control
   * @param id     of record
   * @throws BusinessException if a Business Rule is violated
   */
  private void update(DSLContext db, AccessControl access, ULong id, Map<Field, Object> fieldValues) throws Exception {
    // If not top level access, validate access to account
    if (!access.isTopLevel()) {
      try {
        requireAccount(access, ULong.valueOf(fieldValues.get(CHAIN.ACCOUNT_ID).toString()));
      } catch (Exception e) {
        throw new BusinessException("if not top level access, must provide account id");
      }
    }

    // validate and cache to-state
    String updateState = fieldValues.get(CHAIN.STATE).toString();
    Chain.validateState(updateState);

    // fetch existing chain; further logic is based on its current state
    ChainRecord chain = db.selectFrom(CHAIN).where(CHAIN.ID.eq(id)).fetchOne();
    requireRecordExists("Chain #" + id, chain);
    switch (chain.getState()) {

      case Chain.DRAFT:
        onlyAllowTransitions(updateState, Chain.DRAFT, Chain.READY);
        break;

      case Chain.READY:
        onlyAllowTransitions(updateState, Chain.DRAFT, Chain.READY, Chain.FABRICATING);
        break;

      case Chain.FABRICATING:
        onlyAllowTransitions(updateState, Chain.READY, Chain.FABRICATING, Chain.COMPLETE);
        break;

      case Chain.COMPLETE:
        onlyAllowTransitions(updateState, Chain.COMPLETE, Chain.DRAFT, Chain.READY);
        break;

      default:
        onlyAllowTransitions(updateState, Chain.DRAFT);
        break;
    }

    // [#116] cannot change chain startAt time after has links
    Object updateStartAt = fieldValues.get(CHAIN.START_AT);
    if (updateStartAt != null
      && !chain.getStartAt().equals(Timestamp.valueOf(updateStartAt.toString()))) {
      requireEmptyResultSet(
        "cannot change chain startAt time after it has links",
        db.select(LINK.ID).from(LINK)
          .where(LINK.CHAIN_ID.eq(chain.getId()))
          .fetchResultSet()
      );
    }

    // This "change from state to state" complexity
    // is required in order to prevent duplicate
    // state-changes of the same chain
    UpdateSetFirstStep<ChainRecord> update = db.update(CHAIN);
    fieldValues.forEach(update::set);
    int rowsAffected = update.set(CHAIN.STATE, updateState)
      .where(CHAIN.ID.eq(id))
      .and(CHAIN.STATE.eq(chain.getState()))
      .execute();
    if (rowsAffected == 0) {
      throw new BusinessException("No records updated.");
    }
  }

  /**
   * Read all records in parent by id
   *
   * @param db                      context
   * @param chain                   to read pilot template link for
   * @param linkBeginBefore         time upper threshold
   * @param chainStopCompleteBefore time upper threshold
   * @return array of records
   */
  @Nullable
  private JSONObject buildNextLinkOrComplete(DSLContext db, AccessControl access, ChainRecord chain, Timestamp linkBeginBefore, Timestamp chainStopCompleteBefore) throws Exception {
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
    if (lastRecordWithNoEndAtTime != null) {
      return null;
    }

    // Get the last link in the chain
    LinkRecord lastLinkInChain = db.selectFrom(LINK)
      .where(LINK.CHAIN_ID.eq(chain.getId()))
      .and(LINK.BEGIN_AT.isNotNull())
      .and(LINK.END_AT.isNotNull())
      .groupBy(LINK.CHAIN_ID, LINK.OFFSET, LINK.END_AT)
      .orderBy(LINK.OFFSET.desc())
      .limit(1)
      .fetchOne();

    // If the chain had no last link, it must be empty; return its first link
    if (lastLinkInChain == null) {
      JSONObject pilotTemplate = new JSONObject();
      pilotTemplate.put(Link.KEY_CHAIN_ID, chain.getId());
      pilotTemplate.put(Link.KEY_BEGIN_AT, chain.getStartAt());
      pilotTemplate.put(Link.KEY_OFFSET, 0);
      return pilotTemplate;
    }

    // If the last link begins after our boundary, we're here early; get outta here.
    if (lastLinkInChain.getBeginAt().after(linkBeginBefore)) {
      return null;
    }

    // If the last link ends after the chain stops, our work is done;
    if (Objects.nonNull(lastLinkInChain.getEndAt())
      && Objects.nonNull(chain.getStopAt())
      && lastLinkInChain.getEndAt().after(chain.getStopAt())) {
      // this is where we check to see if the chain is ready to be COMPLETE.
      if (chain.getStopAt().before(chainStopCompleteBefore)
        // and [#122] require the last link in the chain to be in state DUBBED.
        && lastLinkInChain.getState().equals(Link.DUBBED)) {
        updateState(db, access, chain.getId(), Chain.COMPLETE);
      }
      return null;
    }

    // Build the template of the link that follows the last known one
    JSONObject pilotTemplate = new JSONObject();
    ULong pilotOffset = ULong.valueOf(lastLinkInChain.getOffset().toBigInteger().add(BigInteger.valueOf(1)));
    pilotTemplate.put(Link.KEY_CHAIN_ID, chain.getId());
    pilotTemplate.put(Link.KEY_BEGIN_AT, lastLinkInChain.getEndAt());
    pilotTemplate.put(Link.KEY_OFFSET, pilotOffset);
    return pilotTemplate;
  }

  /**
   * Delete a Chain
   *
   * @param db     context
   * @param access control
   * @param id     to delete
   * @throws Exception         if database failure
   * @throws ConfigException   if not configured properly
   * @throws BusinessException if fails business rule
   */
  private void delete(DSLContext db, AccessControl access, ULong id) throws Exception {
    if (access.isTopLevel()) {
      requireRecordExists("Chain", db.selectFrom(CHAIN)
        .where(CHAIN.ID.eq(id))
        .fetchOne());
    } else {
      requireRecordExists("Chain", db.select(CHAIN.fields())
        .from(CHAIN)
        .where(CHAIN.ID.eq(id))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne());
    }

    requireEmptyResultSet(db.select(LINK.ID)
      .from(LINK)
      .where(LINK.CHAIN_ID.eq(id))
      .fetchResultSet());

    requireEmptyResultSet(db.select(CHAIN_CONFIG.ID)
      .from(CHAIN_CONFIG)
      .where(CHAIN_CONFIG.CHAIN_ID.eq(id))
      .fetchResultSet());

    db.deleteFrom(CHAIN)
      .where(CHAIN.ID.eq(id))
      .andNotExists(
        db.select(LINK.ID)
          .from(LINK)
          .where(LINK.CHAIN_ID.eq(id))
      )
      .andNotExists(
        db.select(CHAIN_CONFIG.ID)
          .from(CHAIN_CONFIG)
          .where(CHAIN_CONFIG.CHAIN_ID.eq(id))
      )
      .execute();
  }

}
