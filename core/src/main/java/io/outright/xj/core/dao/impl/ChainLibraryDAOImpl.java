// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.dao.impl;

import io.outright.xj.core.app.access.AccessControl;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.app.exception.ConfigException;
import io.outright.xj.core.dao.ChainLibraryDAO;
import io.outright.xj.core.db.sql.SQLConnection;
import io.outright.xj.core.db.sql.SQLDatabaseProvider;
import io.outright.xj.core.model.chain_library.ChainLibraryWrapper;
import io.outright.xj.core.tables.records.ChainLibraryRecord;
import io.outright.xj.core.transport.JSON;

import org.jooq.DSLContext;
import org.jooq.types.ULong;

import com.google.inject.Inject;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

import static io.outright.xj.core.tables.Chain.CHAIN;
import static io.outright.xj.core.tables.ChainLibrary.CHAIN_LIBRARY;
import static io.outright.xj.core.tables.Library.LIBRARY;

public class ChainLibraryDAOImpl extends DAOImpl implements ChainLibraryDAO {
  private static Logger log = LoggerFactory.getLogger(ChainLibraryDAOImpl.class);

  @Inject
  public ChainLibraryDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public JSONObject create(AccessControl access, ChainLibraryWrapper data) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(create(tx.getContext(), access, data));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public JSONObject readOne(AccessControl access, ULong id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOne(tx.getContext(), access, id));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public JSONArray readAllIn(AccessControl access, ULong chainId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAllIn(tx.getContext(), access, chainId));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void delete(AccessControl access, ULong id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      delete(tx.getContext(), access, id);
      tx.success();
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  /**
   * Create a new Chain Library record
   *
   * @param db   context
   * @param data for new ChainLibrary
   * @return new record
   * @throws Exception         if database failure
   * @throws ConfigException   if not configured properly
   * @throws BusinessException if fails business rule
   */
  private JSONObject create(DSLContext db, AccessControl access, ChainLibraryWrapper data) throws Exception {
    ChainLibraryRecord record = db.newRecord(CHAIN_LIBRARY);
    data.validate();
    data.getChainLibrary().intoFieldValueMap().forEach(record::setValue);
    ULong chainId = data.getChainLibrary().getChainId();
    ULong libraryId = data.getChainLibrary().getLibraryId();

    if (access.isTopLevel()) {
      requireRecordExists("Chain", db.select(CHAIN.ID).from(CHAIN)
        .where(CHAIN.ID.eq(chainId))
        .fetchOne());
      requireRecordExists("Library", db.select(LIBRARY.ID).from(LIBRARY)
        .where(LIBRARY.ID.eq(libraryId))
        .fetchOne());
    } else {
      requireRecordExists("Chain", db.select(CHAIN.ID).from(CHAIN)
        .where(CHAIN.ACCOUNT_ID.in(access.getAccounts()))
        .and(CHAIN.ID.eq(chainId))
        .fetchOne());
      requireRecordExists("Library", db.select(LIBRARY.ID).from(LIBRARY)
        .where(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .and(LIBRARY.ID.eq(libraryId))
        .fetchOne());
    }

    if (db.selectFrom(CHAIN_LIBRARY)
      .where(CHAIN_LIBRARY.CHAIN_ID.eq(chainId))
      .and(CHAIN_LIBRARY.LIBRARY_ID.eq(libraryId))
      .fetchOne() != null) {
      throw new BusinessException("Chain Library already exists!");
    }

    try {
      record.store();
    } catch (Exception e) {
      log.warn("Cannot create ChainLibrary", e.getMessage());
      throw new BusinessException("Cannot create Chain Library.");
    }

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
      return JSON.objectFromRecord(db.selectFrom(CHAIN_LIBRARY)
        .where(CHAIN_LIBRARY.ID.eq(id))
        .fetchOne());
    } else {
      return JSON.objectFromRecord(db.select(CHAIN_LIBRARY.fields()).from(CHAIN_LIBRARY)
        .join(LIBRARY).on(LIBRARY.ID.eq(CHAIN_LIBRARY.LIBRARY_ID))
        .where(CHAIN_LIBRARY.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne());
    }
  }

  /**
   * Read all records in parent record
   *
   * @param db      context
   * @param access  control
   * @param chainId of parent
   * @return array of child records
   * @throws SQLException on failure
   */
  private JSONArray readAllIn(DSLContext db, AccessControl access, ULong chainId) throws SQLException {
    if (access.isTopLevel()) {
      return JSON.arrayFromResultSet(db.selectFrom(CHAIN_LIBRARY)
        .where(CHAIN_LIBRARY.CHAIN_ID.eq(chainId))
        .fetchResultSet());
    } else {
      return JSON.arrayFromResultSet(db.select(CHAIN_LIBRARY.fields()).from(CHAIN_LIBRARY)
        .join(LIBRARY).on(LIBRARY.ID.eq(CHAIN_LIBRARY.LIBRARY_ID))
        .where(CHAIN_LIBRARY.CHAIN_ID.eq(chainId))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetchResultSet());
    }
  }

  /**
   * Delete a record
   *
   * @param db     context
   * @param access control
   * @param id     of record
   * @throws BusinessException on failure
   */
  private void delete(DSLContext db, AccessControl access, ULong id) throws BusinessException {
    // TODO: fail if no chainLibrary is deleted
    if (access.isTopLevel()) {
      requireRecordExists("Chain Library", db.selectFrom(CHAIN_LIBRARY)
        .where(CHAIN_LIBRARY.ID.eq(id))
        .fetchOne());
    } else {
      requireRecordExists("Chain Library", db.select(CHAIN_LIBRARY.fields()).from(CHAIN_LIBRARY)
        .join(LIBRARY).on(LIBRARY.ID.eq(CHAIN_LIBRARY.LIBRARY_ID))
        .where(CHAIN_LIBRARY.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne());
    }

    db.deleteFrom(CHAIN_LIBRARY)
      .where(CHAIN_LIBRARY.ID.eq(id))
      .execute();
  }

}
