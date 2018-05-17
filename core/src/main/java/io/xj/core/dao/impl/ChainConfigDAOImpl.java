// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao.impl;

import io.xj.core.Tables;
import io.xj.core.access.impl.Access;
import io.xj.core.dao.ChainConfigDAO;
import io.xj.core.exception.BusinessException;
import io.xj.core.exception.ConfigException;
import io.xj.core.model.chain_config.ChainConfig;
import io.xj.core.persistence.sql.SQLDatabaseProvider;
import io.xj.core.persistence.sql.impl.SQLConnection;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.types.ULong;

import com.google.api.client.util.Maps;
import com.google.inject.Inject;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import static io.xj.core.tables.Chain.CHAIN;
import static io.xj.core.tables.ChainConfig.CHAIN_CONFIG;

public class ChainConfigDAOImpl extends DAOImpl implements ChainConfigDAO {

  @Inject
  public ChainConfigDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
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
  private static ChainConfig create(DSLContext db, Access access, ChainConfig entity) throws Exception {
    entity.validate();

    Map<Field, Object> fieldValues = fieldValueMap(entity);

    if (access.isTopLevel())
      requireExists("Chain", db.selectCount().from(CHAIN)
        .where(CHAIN.ID.eq(ULong.valueOf(entity.getChainId())))
        .fetchOne(0, int.class));
    else
      requireExists("Chain", db.selectCount().from(CHAIN)
        .where(CHAIN.ACCOUNT_ID.in(access.getAccountIds()))
        .and(CHAIN.ID.eq(ULong.valueOf(entity.getChainId())))
        .fetchOne(0, int.class));

    if (isNonNull(db.selectFrom(CHAIN_CONFIG)
      .where(CHAIN_CONFIG.CHAIN_ID.eq(ULong.valueOf(entity.getChainId())))
      .and(CHAIN_CONFIG.TYPE.eq(entity.getType().toString()))
      .fetchOne()))
      throw new BusinessException(entity.getType() + " config already exists for this Chain!");

    return modelFrom(executeCreate(db, CHAIN_CONFIG, fieldValues), ChainConfig.class);
  }

  /**
   Read one record

   @param db     context
   @param access control
   @param id     of record
   @return record
   */
  private static ChainConfig readOne(DSLContext db, Access access, ULong id) throws BusinessException {
    if (access.isTopLevel())
      return modelFrom(db.selectFrom(CHAIN_CONFIG)
        .where(CHAIN_CONFIG.ID.eq(id))
        .fetchOne(), ChainConfig.class);
    else
      return modelFrom(db.select(CHAIN_CONFIG.fields()).from(CHAIN_CONFIG)
        .join(CHAIN).on(CHAIN.ID.eq(CHAIN_CONFIG.CHAIN_ID))
        .where(CHAIN_CONFIG.ID.eq(id))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne(), ChainConfig.class);
  }

  /**
   Read all records in parent record

   @param db      context
   @param access  control
   @param chainId of parent
   @return array of child records
   */
  private static Collection<ChainConfig> readAll(DSLContext db, Access access, Collection<ULong> chainId) throws BusinessException {
    if (access.isTopLevel())
      return modelsFrom(db.selectFrom(CHAIN_CONFIG)
        .where(CHAIN_CONFIG.CHAIN_ID.in(chainId))
        .fetch(), ChainConfig.class);
    else
      return modelsFrom(db.select(CHAIN_CONFIG.fields()).from(CHAIN_CONFIG)
        .join(CHAIN).on(CHAIN.ID.eq(CHAIN_CONFIG.CHAIN_ID))
        .where(CHAIN.ID.in(chainId))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccountIds()))
        .fetch(), ChainConfig.class);
  }

  /**
   Update a record

   @param db     context
   @param access control
   @param id     of record
   @param entity to update with
   */
  private static void update(DSLContext db, Access access, ULong id, ChainConfig entity) throws BusinessException {
    entity.validate();

    Map<Field, Object> fieldValues = fieldValueMap(entity);
    fieldValues.put(CHAIN_CONFIG.ID, id);

    if (access.isTopLevel())
      requireExists("Chain config", db.selectCount().from(CHAIN_CONFIG)
        .where(CHAIN_CONFIG.ID.eq(id))
        .fetchOne(0, int.class));
    else
      requireExists("Chain config", db.selectCount().from(CHAIN_CONFIG)
        .join(CHAIN).on(CHAIN.ID.eq(CHAIN_CONFIG.CHAIN_ID))
        .where(CHAIN_CONFIG.ID.eq(id))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne(0, int.class));

    // [#128] cannot change chainId of a chainConfig
    Object updateChainId = fieldValues.get(CHAIN_CONFIG.CHAIN_ID);
    if (isNonNull(updateChainId) && !Objects.equals(updateChainId, entity.getChainId()))
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
  private static void delete(DSLContext db, Access access, ULong id) throws BusinessException {
    if (access.isTopLevel())
      requireExists("Chain config", db.selectCount().from(CHAIN_CONFIG)
        .where(CHAIN_CONFIG.ID.eq(id))
        .fetchOne(0, int.class));
    else
      requireExists("Chain config", db.selectCount().from(CHAIN_CONFIG)
        .join(CHAIN).on(CHAIN.ID.eq(CHAIN_CONFIG.CHAIN_ID))
        .where(CHAIN_CONFIG.ID.eq(id))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne(0, int.class));

    db.deleteFrom(CHAIN_CONFIG)
      .where(CHAIN_CONFIG.ID.eq(id))
      .execute();
  }

  /**
   Only certain (writable) fields are mapped back to jOOQ records--
   Read-only fields are excluded from here.

   @param entity to source values from
   @return values mapped to record fields
   */
  private static Map<Field, Object> fieldValueMap(ChainConfig entity) {
    Map<Field, Object> fieldValues = Maps.newHashMap();
    fieldValues.put(Tables.CHAIN_CONFIG.CHAIN_ID, ULong.valueOf(entity.getChainId()));
    fieldValues.put(Tables.CHAIN_CONFIG.TYPE, entity.getType());
    fieldValues.put(Tables.CHAIN_CONFIG.VALUE, entity.getValue());
    return fieldValues;
  }

  @Override
  public ChainConfig create(Access access, ChainConfig entity) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(create(tx.getContext(), access, entity));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public ChainConfig readOne(Access access, BigInteger id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOne(tx.getContext(), access, ULong.valueOf(id)));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Collection<ChainConfig> readAll(Access access, Collection<BigInteger> parentIds) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAll(tx.getContext(), access, uLongValuesOf(parentIds)));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void update(Access access, BigInteger id, ChainConfig entity) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      update(tx.getContext(), access, ULong.valueOf(id), entity);
      tx.success();
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

}
