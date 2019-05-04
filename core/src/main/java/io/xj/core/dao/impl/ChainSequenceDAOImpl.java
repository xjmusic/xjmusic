// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao.impl;

import io.xj.core.Tables;
import io.xj.core.access.impl.Access;
import io.xj.core.dao.ChainSequenceDAO;
import io.xj.core.exception.CoreException;
import io.xj.core.exception.CoreException;
import io.xj.core.model.chain_sequence.ChainSequence;
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
import static io.xj.core.tables.ChainSequence.CHAIN_SEQUENCE;
import static io.xj.core.tables.Library.LIBRARY;
import static io.xj.core.tables.Sequence.SEQUENCE;

public class ChainSequenceDAOImpl extends DAOImpl implements ChainSequenceDAO {

  @Inject
  public ChainSequenceDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  /**
   Create a new Chain Sequence record

   @param db     context
   @param entity for new ChainSequence
   @return new record
   @throws CoreException         if database failure
   @throws CoreException   if not configured properly
   @throws CoreException if fails business rule
   */
  private static ChainSequence create(DSLContext db, Access access, ChainSequence entity) throws CoreException {
    entity.validate();

    Map<Field, Object> fieldValues = fieldValueMap(entity);

    if (access.isTopLevel()) {
      requireExists("Chain", db.selectCount().from(CHAIN)
        .where(CHAIN.ID.eq(ULong.valueOf(entity.getChainId())))
        .fetchOne(0, int.class));
      requireExists("Sequence", db.selectCount().from(SEQUENCE)
        .where(SEQUENCE.ID.eq(ULong.valueOf(entity.getSequenceId())))
        .fetchOne(0, int.class));
    } else {
      requireExists("Chain", db.selectCount().from(CHAIN)
        .where(CHAIN.ACCOUNT_ID.in(access.getAccountIds()))
        .and(CHAIN.ID.eq(ULong.valueOf(entity.getChainId())))
        .fetchOne(0, int.class));
      requireExists("Sequence", db.selectCount().from(SEQUENCE)
        .join(LIBRARY).on(LIBRARY.ID.eq(SEQUENCE.LIBRARY_ID))
        .where(SEQUENCE.ID.eq(ULong.valueOf(entity.getSequenceId())))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne(0, int.class));
    }

    if (null != db.selectFrom(CHAIN_SEQUENCE)
      .where(CHAIN_SEQUENCE.CHAIN_ID.eq(ULong.valueOf(entity.getChainId())))
      .and(CHAIN_SEQUENCE.SEQUENCE_ID.eq(ULong.valueOf(entity.getSequenceId())))
      .fetchOne())
      throw new CoreException("Sequence already added to Chain!");

    return modelFrom(executeCreate(db, CHAIN_SEQUENCE, fieldValues), ChainSequence.class);
  }

  /**
   Read one record

   @param db     context
   @param access control
   @param id     of record
   @return record
   */
  private static ChainSequence readOne(DSLContext db, Access access, ULong id) throws CoreException {
    if (access.isTopLevel())
      return modelFrom(db.selectFrom(CHAIN_SEQUENCE)
        .where(CHAIN_SEQUENCE.ID.eq(id))
        .fetchOne(), ChainSequence.class);
    else
      return modelFrom(db.select(CHAIN_SEQUENCE.fields()).from(CHAIN_SEQUENCE)
        .join(SEQUENCE).on(SEQUENCE.ID.eq(CHAIN_SEQUENCE.SEQUENCE_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(SEQUENCE.LIBRARY_ID))
        .where(CHAIN_SEQUENCE.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne(), ChainSequence.class);
  }

  /**
   Read all records in parent record

   @param db      context
   @param access  control
   @param chainId of parent
   @return array of child records
   */
  private static Collection<ChainSequence> readAll(DSLContext db, Access access, Collection<ULong> chainId) throws CoreException {
    if (access.isTopLevel())
      return modelsFrom(db.selectFrom(CHAIN_SEQUENCE)
        .where(CHAIN_SEQUENCE.CHAIN_ID.in(chainId))
        .fetch(), ChainSequence.class);
    else
      return modelsFrom(db.select(CHAIN_SEQUENCE.fields()).from(CHAIN_SEQUENCE)
        .join(SEQUENCE).on(SEQUENCE.ID.eq(CHAIN_SEQUENCE.SEQUENCE_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(SEQUENCE.LIBRARY_ID))
        .where(CHAIN_SEQUENCE.CHAIN_ID.in(chainId))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .fetch(), ChainSequence.class);
  }

  /**
   Delete a record

   @param db     context
   @param access control
   @param id     of record
   @throws CoreException on failure
   */
  private static void delete(DSLContext db, Access access, ULong id) throws CoreException {
    if (access.isTopLevel())
      requireExists("Chain Sequence", db.selectCount().from(CHAIN_SEQUENCE)
        .where(CHAIN_SEQUENCE.ID.eq(id))
        .fetchOne(0, int.class));
    else
      requireExists("Chain Sequence", db.selectCount().from(CHAIN_SEQUENCE)
        .join(SEQUENCE).on(SEQUENCE.ID.eq(CHAIN_SEQUENCE.SEQUENCE_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(SEQUENCE.LIBRARY_ID))
        .where(CHAIN_SEQUENCE.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne(0, int.class));

    db.deleteFrom(CHAIN_SEQUENCE)
      .where(CHAIN_SEQUENCE.ID.eq(id))
      .execute();
  }

  /**
   Only certain (writable) fields are mapped back to jOOQ records--
   Read-only fields are excluded from here.

   @param entity to source values from
   @return values mapped to record fields
   */
  private static Map<Field, Object> fieldValueMap(ChainSequence entity) {
    Map<Field, Object> fieldValues = Maps.newHashMap();
    fieldValues.put(Tables.CHAIN_SEQUENCE.CHAIN_ID, ULong.valueOf(entity.getChainId()));
    fieldValues.put(Tables.CHAIN_SEQUENCE.SEQUENCE_ID, ULong.valueOf(entity.getSequenceId()));
    return fieldValues;
  }

  @Override
  public ChainSequence create(Access access, ChainSequence entity) throws CoreException {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(create(tx.getContext(), access, entity));
    } catch (CoreException e) {
      throw tx.failure(e);
    }
  }

  @Override
  public ChainSequence readOne(Access access, BigInteger id) throws CoreException {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOne(tx.getContext(), access, ULong.valueOf(id)));
    } catch (CoreException e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Collection<ChainSequence> readAll(Access access, Collection<BigInteger> parentIds) throws CoreException {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAll(tx.getContext(), access, uLongValuesOf(parentIds)));
    } catch (CoreException e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void update(Access access, BigInteger id, ChainSequence entity) throws CoreException {
    throw new CoreException("Not allowed to update ChainSequence record.");
  }

  @Override
  public void destroy(Access access, BigInteger id) throws CoreException {
    SQLConnection tx = dbProvider.getConnection();
    try {
      delete(tx.getContext(), access, ULong.valueOf(id));
      tx.success();
    } catch (CoreException e) {
      throw tx.failure(e);
    }
  }

}
