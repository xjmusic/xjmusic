// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.dao.impl;

import io.xj.core.app.access.impl.Access;
import io.xj.core.app.exception.BusinessException;
import io.xj.core.app.exception.ConfigException;
import io.xj.core.dao.ChainLibraryDAO;
import io.xj.core.db.sql.SQLConnection;
import io.xj.core.db.sql.SQLDatabaseProvider;
import io.xj.core.model.chain_library.ChainLibrary;
import io.xj.core.tables.records.ChainLibraryRecord;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Result;
import org.jooq.types.ULong;

import com.google.inject.Inject;

import java.sql.SQLException;
import java.util.Map;

import static io.xj.core.tables.Chain.CHAIN;
import static io.xj.core.tables.ChainLibrary.CHAIN_LIBRARY;
import static io.xj.core.tables.Library.LIBRARY;

public class ChainLibraryDAOImpl extends DAOImpl implements ChainLibraryDAO {

  @Inject
  public ChainLibraryDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public ChainLibraryRecord create(Access access, ChainLibrary entity) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(createRecord(tx.getContext(), access, entity));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public ChainLibraryRecord readOne(Access access, ULong id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOneRecord(tx.getContext(), access, id));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Result<ChainLibraryRecord> readAll(Access access, ULong chainId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAll(tx.getContext(), access, chainId));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void delete(Access access, ULong id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      delete(tx.getContext(), access, id);
      tx.success();
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  /**
   Create a new Chain Library record

   @param db     context
   @param entity for new ChainLibrary
   @return new record
   @throws Exception         if database failure
   @throws ConfigException   if not configured properly
   @throws BusinessException if fails business rule
   */
  private ChainLibraryRecord createRecord(DSLContext db, Access access, ChainLibrary entity) throws Exception {
    entity.validate();

    Map<Field, Object> fieldValues = entity.updatableFieldValueMap();

    if (access.isTopLevel()) {
      requireExists("Chain", db.select(CHAIN.ID).from(CHAIN)
        .where(CHAIN.ID.eq(entity.getChainId()))
        .fetchOne());
      requireExists("Library", db.select(LIBRARY.ID).from(LIBRARY)
        .where(LIBRARY.ID.eq(entity.getLibraryId()))
        .fetchOne());
    } else {
      requireExists("Chain", db.select(CHAIN.ID).from(CHAIN)
        .where(CHAIN.ACCOUNT_ID.in(access.getAccounts()))
        .and(CHAIN.ID.eq(entity.getChainId()))
        .fetchOne());
      requireExists("Library", db.select(LIBRARY.ID).from(LIBRARY)
        .where(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .and(LIBRARY.ID.eq(entity.getLibraryId()))
        .fetchOne());
    }

    if (db.selectFrom(CHAIN_LIBRARY)
      .where(CHAIN_LIBRARY.CHAIN_ID.eq(entity.getChainId()))
      .and(CHAIN_LIBRARY.LIBRARY_ID.eq(entity.getLibraryId()))
      .fetchOne() != null)
      throw new BusinessException("Library already added to Chain!");

    return executeCreate(db, CHAIN_LIBRARY, fieldValues);
  }

  /**
   Read one record

   @param db     context
   @param access control
   @param id     of record
   @return record
   */
  private ChainLibraryRecord readOneRecord(DSLContext db, Access access, ULong id) {
    if (access.isTopLevel())
      return db.selectFrom(CHAIN_LIBRARY)
        .where(CHAIN_LIBRARY.ID.eq(id))
        .fetchOne();
    else
      return recordInto(CHAIN_LIBRARY, db.select(CHAIN_LIBRARY.fields()).from(CHAIN_LIBRARY)
        .join(LIBRARY).on(LIBRARY.ID.eq(CHAIN_LIBRARY.LIBRARY_ID))
        .where(CHAIN_LIBRARY.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne());
  }

  /**
   Read all records in parent record

   @param db      context
   @param access  control
   @param chainId of parent
   @return array of child records
   @throws SQLException on failure
   */
  private Result<ChainLibraryRecord> readAll(DSLContext db, Access access, ULong chainId) throws SQLException {
    if (access.isTopLevel())
      return db.selectFrom(CHAIN_LIBRARY)
        .where(CHAIN_LIBRARY.CHAIN_ID.eq(chainId))
        .fetch();
    else
      return resultInto(CHAIN_LIBRARY, db.select(CHAIN_LIBRARY.fields()).from(CHAIN_LIBRARY)
        .join(LIBRARY).on(LIBRARY.ID.eq(CHAIN_LIBRARY.LIBRARY_ID))
        .where(CHAIN_LIBRARY.CHAIN_ID.eq(chainId))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetch());
  }

  /**
   Delete a record

   @param db     context
   @param access control
   @param id     of record
   @throws BusinessException on failure
   */
  private void delete(DSLContext db, Access access, ULong id) throws BusinessException {
    // TODO: fail if no chainLibrary is deleted
    if (access.isTopLevel())
      requireExists("Chain Library", db.selectFrom(CHAIN_LIBRARY)
        .where(CHAIN_LIBRARY.ID.eq(id))
        .fetchOne());
    else
      requireExists("Chain Library", db.select(CHAIN_LIBRARY.fields()).from(CHAIN_LIBRARY)
        .join(LIBRARY).on(LIBRARY.ID.eq(CHAIN_LIBRARY.LIBRARY_ID))
        .where(CHAIN_LIBRARY.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne());

    db.deleteFrom(CHAIN_LIBRARY)
      .where(CHAIN_LIBRARY.ID.eq(id))
      .execute();
  }

}
