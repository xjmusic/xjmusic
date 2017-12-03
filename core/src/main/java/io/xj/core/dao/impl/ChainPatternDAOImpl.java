// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.dao.impl;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Result;
import org.jooq.types.ULong;

import com.google.inject.Inject;

import io.xj.core.access.impl.Access;
import io.xj.core.dao.ChainPatternDAO;
import io.xj.core.exception.BusinessException;
import io.xj.core.exception.ConfigException;
import io.xj.core.model.chain_pattern.ChainPattern;
import io.xj.core.persistence.sql.SQLDatabaseProvider;
import io.xj.core.persistence.sql.impl.SQLConnection;
import io.xj.core.tables.records.ChainPatternRecord;

import java.sql.SQLException;
import java.util.Map;

import static io.xj.core.tables.Chain.CHAIN;
import static io.xj.core.tables.ChainPattern.CHAIN_PATTERN;
import static io.xj.core.tables.Library.LIBRARY;
import static io.xj.core.tables.Pattern.PATTERN;

public class ChainPatternDAOImpl extends DAOImpl implements ChainPatternDAO {

  @Inject
  public ChainPatternDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public ChainPatternRecord create(Access access, ChainPattern entity) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(createRecord(tx.getContext(), access, entity));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public ChainPatternRecord readOne(Access access, ULong id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOneRecord(tx.getContext(), access, id));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Result<ChainPatternRecord> readAll(Access access, ULong chainId) throws Exception {
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
   Create a new Chain Pattern record

   @param db     context
   @param entity for new ChainPattern
   @return new record
   @throws Exception         if database failure
   @throws ConfigException   if not configured properly
   @throws BusinessException if fails business rule
   */
  private ChainPatternRecord createRecord(DSLContext db, Access access, ChainPattern entity) throws Exception {
    entity.validate();

    Map<Field, Object> fieldValues = entity.updatableFieldValueMap();

    if (access.isTopLevel()) {
      requireExists("Chain", db.selectCount().from(CHAIN)
        .where(CHAIN.ID.eq(entity.getChainId()))
        .fetchOne(0, int.class));
      requireExists("Pattern", db.selectCount().from(PATTERN)
        .where(PATTERN.ID.eq(entity.getPatternId()))
        .fetchOne(0, int.class));
    } else {
      requireExists("Chain", db.selectCount().from(CHAIN)
        .where(CHAIN.ACCOUNT_ID.in(access.getAccounts()))
        .and(CHAIN.ID.eq(entity.getChainId()))
        .fetchOne(0, int.class));
      requireExists("Pattern", db.selectCount().from(PATTERN)
        .join(LIBRARY).on(LIBRARY.ID.eq(PATTERN.LIBRARY_ID))
        .where(PATTERN.ID.eq(entity.getPatternId()))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne(0, int.class));
    }

    if (null != db.selectFrom(CHAIN_PATTERN)
      .where(CHAIN_PATTERN.CHAIN_ID.eq(entity.getChainId()))
      .and(CHAIN_PATTERN.PATTERN_ID.eq(entity.getPatternId()))
      .fetchOne())
      throw new BusinessException("Pattern already added to Chain!");

    return executeCreate(db, CHAIN_PATTERN, fieldValues);
  }

  /**
   Read one record

   @param db     context
   @param access control
   @param id     of record
   @return record
   */
  private ChainPatternRecord readOneRecord(DSLContext db, Access access, ULong id) {
    if (access.isTopLevel())
      return db.selectFrom(CHAIN_PATTERN)
        .where(CHAIN_PATTERN.ID.eq(id))
        .fetchOne();
    else
      return recordInto(CHAIN_PATTERN, db.select(CHAIN_PATTERN.fields()).from(CHAIN_PATTERN)
        .join(PATTERN).on(PATTERN.ID.eq(CHAIN_PATTERN.PATTERN_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(PATTERN.LIBRARY_ID))
        .where(CHAIN_PATTERN.ID.eq(id))
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
  private Result<ChainPatternRecord> readAll(DSLContext db, Access access, ULong chainId) throws SQLException {
    if (access.isTopLevel())
      return db.selectFrom(CHAIN_PATTERN)
        .where(CHAIN_PATTERN.CHAIN_ID.eq(chainId))
        .fetch();
    else
      return resultInto(CHAIN_PATTERN, db.select(CHAIN_PATTERN.fields()).from(CHAIN_PATTERN)
        .join(PATTERN).on(PATTERN.ID.eq(CHAIN_PATTERN.PATTERN_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(PATTERN.LIBRARY_ID))
        .where(CHAIN_PATTERN.CHAIN_ID.eq(chainId))
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

    if (access.isTopLevel())
      requireExists("Chain Pattern", db.selectCount().from(CHAIN_PATTERN)
        .where(CHAIN_PATTERN.ID.eq(id))
        .fetchOne(0, int.class));
    else
      requireExists("Chain Pattern", db.selectCount().from(CHAIN_PATTERN)
        .join(PATTERN).on(PATTERN.ID.eq(CHAIN_PATTERN.PATTERN_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(PATTERN.LIBRARY_ID))
        .where(CHAIN_PATTERN.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne(0, int.class));

    db.deleteFrom(CHAIN_PATTERN)
      .where(CHAIN_PATTERN.ID.eq(id))
      .execute();
  }

}
