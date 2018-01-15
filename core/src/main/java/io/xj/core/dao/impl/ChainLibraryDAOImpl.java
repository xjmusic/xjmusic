// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao.impl;

import io.xj.core.access.impl.Access;
import io.xj.core.dao.ChainLibraryDAO;
import io.xj.core.exception.BusinessException;
import io.xj.core.exception.ConfigException;
import io.xj.core.model.chain_library.ChainLibrary;
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
import static io.xj.core.tables.ChainLibrary.CHAIN_LIBRARY;
import static io.xj.core.tables.Library.LIBRARY;

public class ChainLibraryDAOImpl extends DAOImpl implements ChainLibraryDAO {

  @Inject
  public ChainLibraryDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
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
  private static ChainLibrary create(DSLContext db, Access access, ChainLibrary entity) throws Exception {
    entity.validate();

    Map<Field, Object> fieldValues = fieldValueMap(entity);

    if (access.isTopLevel()) {
      requireExists("Chain", db.selectCount().from(CHAIN)
        .where(CHAIN.ID.eq(ULong.valueOf(entity.getChainId())))
        .fetchOne(0, int.class));
      requireExists("Library", db.selectCount().from(LIBRARY)
        .where(LIBRARY.ID.eq(ULong.valueOf(entity.getLibraryId())))
        .fetchOne(0, int.class));
    } else {
      requireExists("Chain", db.selectCount().from(CHAIN)
        .where(CHAIN.ACCOUNT_ID.in(access.getAccountIds()))
        .and(CHAIN.ID.eq(ULong.valueOf(entity.getChainId())))
        .fetchOne(0, int.class));
      requireExists("Library", db.selectCount().from(LIBRARY)
        .where(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .and(LIBRARY.ID.eq(ULong.valueOf(entity.getLibraryId())))
        .fetchOne(0, int.class));
    }

    if (null != db.selectFrom(CHAIN_LIBRARY)
      .where(CHAIN_LIBRARY.CHAIN_ID.eq(ULong.valueOf(entity.getChainId())))
      .and(CHAIN_LIBRARY.LIBRARY_ID.eq(ULong.valueOf(entity.getLibraryId())))
      .fetchOne())
      throw new BusinessException("Library already added to Chain!");

    return modelFrom(executeCreate(db, CHAIN_LIBRARY, fieldValues), ChainLibrary.class);
  }

  /**
   Read one record

   @param db     context
   @param access control
   @param id     of record
   @return record
   */
  private static ChainLibrary readOne(DSLContext db, Access access, ULong id) throws BusinessException {
    if (access.isTopLevel())
      return modelFrom(db.selectFrom(CHAIN_LIBRARY)
        .where(CHAIN_LIBRARY.ID.eq(id))
        .fetchOne(), ChainLibrary.class);
    else
      return modelFrom(db.select(CHAIN_LIBRARY.fields()).from(CHAIN_LIBRARY)
        .join(LIBRARY).on(LIBRARY.ID.eq(CHAIN_LIBRARY.LIBRARY_ID))
        .where(CHAIN_LIBRARY.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne(), ChainLibrary.class);
  }

  /**
   Read all records in parent record

   @param db       context
   @param access   control
   @param chainIds of parent
   @return array of child records
   */
  private static Collection<ChainLibrary> readAll(DSLContext db, Access access, Collection<ULong> chainIds) throws BusinessException {
    if (access.isTopLevel())
      return modelsFrom(db.selectFrom(CHAIN_LIBRARY)
        .where(CHAIN_LIBRARY.CHAIN_ID.in(chainIds))
        .fetch(), ChainLibrary.class);
    else
      return modelsFrom(db.select(CHAIN_LIBRARY.fields()).from(CHAIN_LIBRARY)
        .join(LIBRARY).on(LIBRARY.ID.eq(CHAIN_LIBRARY.LIBRARY_ID))
        .where(CHAIN_LIBRARY.CHAIN_ID.in(chainIds))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .fetch(), ChainLibrary.class);
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
      requireExists("Chain Library", db.selectCount().from(CHAIN_LIBRARY)
        .where(CHAIN_LIBRARY.ID.eq(id))
        .fetchOne(0, int.class));
    else
      requireExists("Chain Library", db.selectCount().from(CHAIN_LIBRARY)
        .join(LIBRARY).on(LIBRARY.ID.eq(CHAIN_LIBRARY.LIBRARY_ID))
        .where(CHAIN_LIBRARY.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne(0, int.class));

    db.deleteFrom(CHAIN_LIBRARY)
      .where(CHAIN_LIBRARY.ID.eq(id))
      .execute();
  }

  /**
   Only certain (writable) fields are mapped back to jOOQ records--
   Read-only fields are excluded from here.

   @param entity to source values from
   @return values mapped to record fields
   */
  private static Map<Field, Object> fieldValueMap(ChainLibrary entity) {
    Map<Field, Object> fieldValues = Maps.newHashMap();
    fieldValues.put(CHAIN_LIBRARY.CHAIN_ID, ULong.valueOf(entity.getChainId()));
    fieldValues.put(CHAIN_LIBRARY.LIBRARY_ID, ULong.valueOf(entity.getLibraryId()));
    return fieldValues;
  }

  @Override
  public ChainLibrary create(Access access, ChainLibrary entity) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(create(tx.getContext(), access, entity));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public ChainLibrary readOne(Access access, BigInteger id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOne(tx.getContext(), access, ULong.valueOf(id)));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Collection<ChainLibrary> readAll(Access access, Collection<BigInteger> parentIds) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAll(tx.getContext(), access, uLongValuesOf(parentIds)));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void update(Access access, BigInteger id, ChainLibrary entity) throws Exception {
    throw new BusinessException("Not allowed to update ChainLibrary record.");
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
