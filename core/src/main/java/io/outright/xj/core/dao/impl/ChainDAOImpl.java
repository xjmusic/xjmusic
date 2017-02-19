// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.dao.impl;

import io.outright.xj.core.app.access.impl.AccessControl;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.app.exception.ConfigException;
import io.outright.xj.core.app.exception.DatabaseException;
import io.outright.xj.core.dao.ChainDAO;
import io.outright.xj.core.db.sql.SQLConnection;
import io.outright.xj.core.db.sql.SQLDatabaseProvider;
import io.outright.xj.core.model.chain.Chain;
import io.outright.xj.core.model.chain.ChainWrapper;
import io.outright.xj.core.transport.JSON;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.types.ULong;

import com.google.inject.Inject;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.annotation.Nullable;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Map;

import static io.outright.xj.core.Tables.CHAIN;
import static io.outright.xj.core.tables.Account.ACCOUNT;
import static io.outright.xj.core.tables.ChainLibrary.CHAIN_LIBRARY;
import static io.outright.xj.core.tables.Link.LINK;

public class ChainDAOImpl extends DAOImpl implements ChainDAO {

  @Inject
  public ChainDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
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
  public JSONArray readAllIdBoundsInProduction(AccessControl access, Timestamp at, int rangeSeconds) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAllIdBoundsInProduction(tx.getContext(), access, at, rangeSeconds));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void update(AccessControl access, ULong id, ChainWrapper data) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      update(tx.getContext(), access, id, data);
      tx.success();
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
   * Read all records in parent by id
   *
   * @param db     context
   * @param access control
   * @param at     time to check for chains in production
   * @param rangeSeconds plus or minus
   * @return array of records
   */
  private JSONArray readAllIdBoundsInProduction(DSLContext db, AccessControl access, Timestamp at, int rangeSeconds) throws SQLException, BusinessException {
    requireTopLevel(access);

    Timestamp upper = Timestamp.from(at.toInstant().plusSeconds(rangeSeconds));
    Timestamp lower = Timestamp.from(at.toInstant().minusSeconds(rangeSeconds));

    return JSON.arrayFromResultSet(db.select(CHAIN.ID,CHAIN.START_AT,CHAIN.STOP_AT)
      .from(CHAIN)
      .where(CHAIN.STATE.eq(Chain.PRODUCTION))
      .and(CHAIN.START_AT.lessOrEqual(upper).and(CHAIN.STOP_AT.isNull()))
      .or(CHAIN.START_AT.lessOrEqual(upper).and(CHAIN.STOP_AT.greaterOrEqual(lower)))
      .fetchResultSet());
  }

  /**
   * Update a record
   *
   * @param db     context
   * @param access control
   * @param id     of record
   * @param data   to update with
   * @throws BusinessException if a Business Rule is violated
   */
  private void update(DSLContext db, AccessControl access, ULong id, ChainWrapper data) throws BusinessException, DatabaseException {
    Chain model = data.validate();
    Map<Field, Object> fieldValues = model.intoFieldValueMap();
    fieldValues.put(CHAIN.ID, id);

    if (access.isTopLevel()) {
      requireRecordExists("Account",
        db.selectFrom(ACCOUNT)
          .where(ACCOUNT.ID.eq(model.getAccountId()))
          .fetchOne());
    } else {
      requireRecordExists("Account",
        db.select(ACCOUNT.ID).from(ACCOUNT)
          .where(ACCOUNT.ID.eq(model.getAccountId()))
          .and(ACCOUNT.ID.in(access.getAccounts()))
          .fetchOne());
    }

    if (executeUpdate(db, CHAIN, fieldValues) == 0) {
      throw new BusinessException("No records updated.");
    }
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

    requireEmptyResultSet(db.select(CHAIN_LIBRARY.ID)
      .from(CHAIN_LIBRARY)
      .where(CHAIN_LIBRARY.CHAIN_ID.eq(id))
      .fetchResultSet());

    db.deleteFrom(CHAIN)
      .where(CHAIN.ID.eq(id))
      .andNotExists(
        db.select(LINK.ID)
          .from(LINK)
          .where(LINK.CHAIN_ID.eq(id))
      )
      .andNotExists(
        db.select(CHAIN_LIBRARY.ID)
          .from(CHAIN_LIBRARY)
          .where(CHAIN_LIBRARY.CHAIN_ID.eq(id))
      )
      .execute();
  }

}
