// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao.impl;

import io.xj.core.Tables;
import io.xj.core.access.impl.Access;
import io.xj.core.dao.ChainPatternDAO;
import io.xj.core.exception.BusinessException;
import io.xj.core.exception.ConfigException;
import io.xj.core.model.chain_pattern.ChainPattern;
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
  public ChainPattern create(Access access, ChainPattern entity) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(create(tx.getContext(), access, entity));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public ChainPattern readOne(Access access, BigInteger id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOne(tx.getContext(), access, ULong.valueOf(id)));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Collection<ChainPattern> readAll(Access access, BigInteger chainId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAll(tx.getContext(), access, ULong.valueOf(chainId)));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void delete(Access access, BigInteger id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      delete(tx.getContext(), access, ULong.valueOf(id));
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
  private static ChainPattern create(DSLContext db, Access access, ChainPattern entity) throws Exception {
    entity.validate();

    Map<Field, Object> fieldValues = fieldValueMap(entity);

    if (access.isTopLevel()) {
      requireExists("Chain", db.selectCount().from(CHAIN)
        .where(CHAIN.ID.eq(ULong.valueOf(entity.getChainId())))
        .fetchOne(0, int.class));
      requireExists("Pattern", db.selectCount().from(PATTERN)
        .where(PATTERN.ID.eq(ULong.valueOf(entity.getPatternId())))
        .fetchOne(0, int.class));
    } else {
      requireExists("Chain", db.selectCount().from(CHAIN)
        .where(CHAIN.ACCOUNT_ID.in(access.getAccountIds()))
        .and(CHAIN.ID.eq(ULong.valueOf(entity.getChainId())))
        .fetchOne(0, int.class));
      requireExists("Pattern", db.selectCount().from(PATTERN)
        .join(LIBRARY).on(LIBRARY.ID.eq(PATTERN.LIBRARY_ID))
        .where(PATTERN.ID.eq(ULong.valueOf(entity.getPatternId())))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne(0, int.class));
    }

    if (null != db.selectFrom(CHAIN_PATTERN)
      .where(CHAIN_PATTERN.CHAIN_ID.eq(ULong.valueOf(entity.getChainId())))
      .and(CHAIN_PATTERN.PATTERN_ID.eq(ULong.valueOf(entity.getPatternId())))
      .fetchOne())
      throw new BusinessException("Pattern already added to Chain!");

    return modelFrom(executeCreate(db, CHAIN_PATTERN, fieldValues), ChainPattern.class);
  }

  /**
   Read one record

   @param db     context
   @param access control
   @param id     of record
   @return record
   */
  private static ChainPattern readOne(DSLContext db, Access access, ULong id) throws BusinessException {
    if (access.isTopLevel())
      return modelFrom(db.selectFrom(CHAIN_PATTERN)
        .where(CHAIN_PATTERN.ID.eq(id))
        .fetchOne(), ChainPattern.class);
    else
      return modelFrom(db.select(CHAIN_PATTERN.fields()).from(CHAIN_PATTERN)
        .join(PATTERN).on(PATTERN.ID.eq(CHAIN_PATTERN.PATTERN_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(PATTERN.LIBRARY_ID))
        .where(CHAIN_PATTERN.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne(), ChainPattern.class);
  }

  /**
   Read all records in parent record

   @param db      context
   @param access  control
   @param chainId of parent
   @return array of child records
   */
  private static Collection<ChainPattern> readAll(DSLContext db, Access access, ULong chainId) throws BusinessException {
    if (access.isTopLevel())
      return modelsFrom(db.selectFrom(CHAIN_PATTERN)
        .where(CHAIN_PATTERN.CHAIN_ID.eq(chainId))
        .fetch(), ChainPattern.class);
    else
      return modelsFrom(db.select(CHAIN_PATTERN.fields()).from(CHAIN_PATTERN)
        .join(PATTERN).on(PATTERN.ID.eq(CHAIN_PATTERN.PATTERN_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(PATTERN.LIBRARY_ID))
        .where(CHAIN_PATTERN.CHAIN_ID.eq(chainId))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .fetch(), ChainPattern.class);
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
      requireExists("Chain Pattern", db.selectCount().from(CHAIN_PATTERN)
        .where(CHAIN_PATTERN.ID.eq(id))
        .fetchOne(0, int.class));
    else
      requireExists("Chain Pattern", db.selectCount().from(CHAIN_PATTERN)
        .join(PATTERN).on(PATTERN.ID.eq(CHAIN_PATTERN.PATTERN_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(PATTERN.LIBRARY_ID))
        .where(CHAIN_PATTERN.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne(0, int.class));

    db.deleteFrom(CHAIN_PATTERN)
      .where(CHAIN_PATTERN.ID.eq(id))
      .execute();
  }

  /**
   Only certain (writable) fields are mapped back to jOOQ records--
   Read-only fields are excluded from here.

   @param entity to source values from
   @return values mapped to record fields
   */
  private static Map<Field, Object> fieldValueMap(ChainPattern entity) {
    Map<Field, Object> fieldValues = Maps.newHashMap();
    fieldValues.put(Tables.CHAIN_PATTERN.CHAIN_ID, ULong.valueOf(entity.getChainId()));
    fieldValues.put(Tables.CHAIN_PATTERN.PATTERN_ID, ULong.valueOf(entity.getPatternId()));
    return fieldValues;
  }

}
