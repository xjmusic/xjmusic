// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.dao.impl;

import io.outright.xj.core.app.access.AccessControl;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.app.exception.ConfigException;
import io.outright.xj.core.dao.ChainDAO;
import io.outright.xj.core.db.sql.SQLConnection;
import io.outright.xj.core.db.sql.SQLDatabaseProvider;
import io.outright.xj.core.model.chain.ChainWrapper;
import io.outright.xj.core.tables.records.ChainRecord;
import io.outright.xj.core.transport.JSON;

import org.jooq.DSLContext;
import org.jooq.types.ULong;

import com.google.inject.Inject;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.annotation.Nullable;
import java.sql.SQLException;

import static io.outright.xj.core.Tables.CHAIN;
import static io.outright.xj.core.tables.Account.ACCOUNT;
import static io.outright.xj.core.tables.ChainLibrary.CHAIN_LIBRARY;
import static io.outright.xj.core.tables.Link.LINK;

public class ChainDAOImpl extends DAOImpl implements ChainDAO {
  //  private static Logger log = LoggerFactory.getLogger(ChainDAOImpl.class);

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
    ChainRecord record = db.newRecord(CHAIN);
    data.validate();
    data.getChain().intoFieldValueMap().forEach(record::setValue);

    if (access.isTopLevel()) {
      requireRecordExists("Account", db.select(ACCOUNT.ID).from(ACCOUNT)
        .where(ACCOUNT.ID.eq(data.getChain().getAccountId()))
        .fetchOne());
    } else {
      requireRecordExists("Account", db.select(ACCOUNT.ID).from(ACCOUNT)
        .where(ACCOUNT.ID.in(access.getAccounts()))
        .and(ACCOUNT.ID.eq(data.getChain().getAccountId()))
        .fetchOne());
    }

    record.store();

    return JSON.objectFromRecord(record);
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
   * Update a record
   *
   * @param db     context
   * @param access control
   * @param id     of record
   * @param data   to update with
   * @throws BusinessException if a Business Rule is violated
   */
  private void update(DSLContext db, AccessControl access, ULong id, ChainWrapper data) throws BusinessException {
    data.validate();

    if (access.isTopLevel()) {
      requireRecordExists("Account",
        db.selectFrom(ACCOUNT)
          .where(ACCOUNT.ID.eq(data.getChain().getAccountId()))
          .fetchOne());
    } else {
      requireRecordExists("Account",
        db.select(ACCOUNT.ID).from(ACCOUNT)
        .where(ACCOUNT.ID.eq(data.getChain().getAccountId()))
        .and(ACCOUNT.ID.in(access.getAccounts()))
        .fetchOne());
    }

    db.update(CHAIN)
      .set(CHAIN.NAME, data.getChain().getName())
      .set(CHAIN.ACCOUNT_ID, data.getChain().getAccountId())
      .where(CHAIN.ID.eq(id))
      .execute();
  }

  /**
   * Delete a Chain
   *
   * @param db        context
   * @param access    control
   * @param id to delete
   * @throws Exception if database failure
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
