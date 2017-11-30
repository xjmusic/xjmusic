// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.dao.impl;

import io.xj.core.access.impl.Access;
import io.xj.core.exception.BusinessException;
import io.xj.core.exception.ConfigException;
import io.xj.core.exception.DatabaseException;
import io.xj.core.dao.ChainConfigDAO;
import io.xj.core.persistence.sql.impl.SQLConnection;
import io.xj.core.persistence.sql.SQLDatabaseProvider;
import io.xj.core.model.chain_config.ChainConfig;
import io.xj.core.tables.records.ChainConfigRecord;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Result;
import org.jooq.types.ULong;

import com.google.inject.Inject;

import java.sql.SQLException;
import java.util.Map;

import static io.xj.core.tables.Chain.CHAIN;
import static io.xj.core.tables.ChainConfig.CHAIN_CONFIG;

public class ChainConfigDAOImpl extends DAOImpl implements ChainConfigDAO {

  @Inject
  public ChainConfigDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public ChainConfigRecord create(Access access, ChainConfig entity) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(createRecord(tx.getContext(), access, entity));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public ChainConfigRecord readOne(Access access, ULong id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOne(tx.getContext(), access, id));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Result<ChainConfigRecord> readAll(Access access, ULong chainId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAll(tx.getContext(), access, chainId));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void update(Access access, ULong id, ChainConfig entity) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      update(tx.getContext(), access, id, entity);
      tx.success();
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
   Create a new Chain config record

   @param db     context
   @param entity for new ChainConfig
   @return new record
   @throws Exception         if database failure
   @throws ConfigException   if not configured properly
   @throws BusinessException if fails business rule
   */
  private ChainConfigRecord createRecord(DSLContext db, Access access, ChainConfig entity) throws Exception {
    entity.validate();

    Map<Field, Object> fieldValues = entity.updatableFieldValueMap();

    if (access.isTopLevel())
      requireExists("Chain", db.selectCount().from(CHAIN)
        .where(CHAIN.ID.eq(entity.getChainId()))
        .fetchOne(0, int.class));
    else
      requireExists("Chain", db.selectCount().from(CHAIN)
        .where(CHAIN.ACCOUNT_ID.in(access.getAccounts()))
        .and(CHAIN.ID.eq(entity.getChainId()))
        .fetchOne(0, int.class));

    if (exists(db.selectFrom(CHAIN_CONFIG)
      .where(CHAIN_CONFIG.CHAIN_ID.eq(entity.getChainId()))
      .and(CHAIN_CONFIG.TYPE.eq(entity.getType().toString()))
      .fetchOne()))
      throw new BusinessException(entity.getType() + " config already exists for this Chain!");

    return executeCreate(db, CHAIN_CONFIG, fieldValues);
  }

  /**
   Read one record

   @param db     context
   @param access control
   @param id     of record
   @return record
   */
  private ChainConfigRecord readOne(DSLContext db, Access access, ULong id) {
    if (access.isTopLevel())
      return db.selectFrom(CHAIN_CONFIG)
        .where(CHAIN_CONFIG.ID.eq(id))
        .fetchOne();
    else
      return recordInto(CHAIN_CONFIG, db.select(CHAIN_CONFIG.fields()).from(CHAIN_CONFIG)
        .join(CHAIN).on(CHAIN.ID.eq(CHAIN_CONFIG.CHAIN_ID))
        .where(CHAIN_CONFIG.ID.eq(id))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccounts()))
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
  private Result<ChainConfigRecord> readAll(DSLContext db, Access access, ULong chainId) throws SQLException {
    if (access.isTopLevel())
      return db.selectFrom(CHAIN_CONFIG)
        .where(CHAIN_CONFIG.CHAIN_ID.eq(chainId))
        .fetch();
    else
      return resultInto(CHAIN_CONFIG, db.select(CHAIN_CONFIG.fields()).from(CHAIN_CONFIG)
        .join(CHAIN).on(CHAIN.ID.eq(CHAIN_CONFIG.CHAIN_ID))
        .where(CHAIN.ID.eq(chainId))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccounts()))
        .fetch());
  }


  /**
   Update a record

   @param db     context
   @param access control
   @param id     of record
   @param entity to update with
   @throws BusinessException if a Business Rule is violated
   */
  private void update(DSLContext db, Access access, ULong id, ChainConfig entity) throws BusinessException, DatabaseException {
    entity.validate();

    Map<Field, Object> fieldValues = entity.updatableFieldValueMap();
    fieldValues.put(CHAIN_CONFIG.ID, id);

    if (access.isTopLevel())
      requireExists("Chain config", db.selectCount().from(CHAIN_CONFIG)
        .where(CHAIN_CONFIG.ID.eq(id))
        .fetchOne(0, int.class));
    else
      requireExists("Chain config", db.selectCount().from(CHAIN_CONFIG)
        .join(CHAIN).on(CHAIN.ID.eq(CHAIN_CONFIG.CHAIN_ID))
        .where(CHAIN_CONFIG.ID.eq(id))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne(0, int.class));

    // [#128] cannot change chainId of a chainConfig
    Object updateChainId = fieldValues.get(CHAIN_CONFIG.CHAIN_ID);
    if (exists(updateChainId) && !updateChainId.equals(entity.getChainId()))
      throw new BusinessException("cannot change chainId of a chainConfig");

    if (0 == executeUpdate(db, CHAIN_CONFIG, fieldValues))
      throw new BusinessException("No records updated.");
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
      requireExists("Chain config", db.selectCount().from(CHAIN_CONFIG)
        .where(CHAIN_CONFIG.ID.eq(id))
        .fetchOne(0, int.class));
    else
      requireExists("Chain config", db.selectCount().from(CHAIN_CONFIG)
        .join(CHAIN).on(CHAIN.ID.eq(CHAIN_CONFIG.CHAIN_ID))
        .where(CHAIN_CONFIG.ID.eq(id))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne(0, int.class));

    db.deleteFrom(CHAIN_CONFIG)
      .where(CHAIN_CONFIG.ID.eq(id))
      .execute();
  }

}
