// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.dao.impl;

import io.xj.core.app.access.impl.Access;
import io.xj.core.app.exception.BusinessException;
import io.xj.core.app.exception.ConfigException;
import io.xj.core.dao.ChainInstrumentDAO;
import io.xj.core.db.sql.impl.SQLConnection;
import io.xj.core.db.sql.SQLDatabaseProvider;
import io.xj.core.model.chain_instrument.ChainInstrument;
import io.xj.core.tables.records.ChainInstrumentRecord;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Result;
import org.jooq.types.ULong;

import com.google.inject.Inject;

import java.sql.SQLException;
import java.util.Map;

import static io.xj.core.tables.Chain.CHAIN;
import static io.xj.core.tables.ChainInstrument.CHAIN_INSTRUMENT;
import static io.xj.core.tables.Instrument.INSTRUMENT;
import static io.xj.core.tables.Library.LIBRARY;

public class ChainInstrumentDAOImpl extends DAOImpl implements ChainInstrumentDAO {

  @Inject
  public ChainInstrumentDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public ChainInstrumentRecord create(Access access, ChainInstrument entity) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(createRecord(tx.getContext(), access, entity));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public ChainInstrumentRecord readOne(Access access, ULong id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOneRecord(tx.getContext(), access, id));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Result<ChainInstrumentRecord> readAll(Access access, ULong chainId) throws Exception {
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
   Create a new Chain Instrument record

   @param db     context
   @param entity for new ChainInstrument
   @return new record
   @throws Exception         if database failure
   @throws ConfigException   if not configured properly
   @throws BusinessException if fails business rule
   */
  private ChainInstrumentRecord createRecord(DSLContext db, Access access, ChainInstrument entity) throws Exception {
    entity.validate();

    Map<Field, Object> fieldValues = entity.updatableFieldValueMap();

    if (access.isTopLevel()) {
      requireExists("Chain", db.select(CHAIN.ID).from(CHAIN)
        .where(CHAIN.ID.eq(entity.getChainId()))
        .fetchOne());
      requireExists("Instrument", db.select(INSTRUMENT.ID).from(INSTRUMENT)
        .where(INSTRUMENT.ID.eq(entity.getInstrumentId()))
        .fetchOne());
    } else {
      requireExists("Chain", db.select(CHAIN.ID).from(CHAIN)
        .where(CHAIN.ACCOUNT_ID.in(access.getAccounts()))
        .and(CHAIN.ID.eq(entity.getChainId()))
        .fetchOne());
      requireExists("Instrument", db.select(INSTRUMENT.ID).from(INSTRUMENT)
        .join(LIBRARY).on(LIBRARY.ID.eq(INSTRUMENT.LIBRARY_ID))
        .where(INSTRUMENT.ID.eq(entity.getInstrumentId()))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne());
    }

    if (db.selectFrom(CHAIN_INSTRUMENT)
      .where(CHAIN_INSTRUMENT.CHAIN_ID.eq(entity.getChainId()))
      .and(CHAIN_INSTRUMENT.INSTRUMENT_ID.eq(entity.getInstrumentId()))
      .fetchOne() != null)
      throw new BusinessException("Instrument already added to Chain!");

    return executeCreate(db, CHAIN_INSTRUMENT, fieldValues);
  }

  /**
   Read one record

   @param db     context
   @param access control
   @param id     of record
   @return record
   */
  private ChainInstrumentRecord readOneRecord(DSLContext db, Access access, ULong id) {
    if (access.isTopLevel())
      return db.selectFrom(CHAIN_INSTRUMENT)
        .where(CHAIN_INSTRUMENT.ID.eq(id))
        .fetchOne();
    else
      return recordInto(CHAIN_INSTRUMENT, db.select(CHAIN_INSTRUMENT.fields()).from(CHAIN_INSTRUMENT)
        .join(INSTRUMENT).on(INSTRUMENT.ID.eq(CHAIN_INSTRUMENT.INSTRUMENT_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(INSTRUMENT.LIBRARY_ID))
        .where(CHAIN_INSTRUMENT.ID.eq(id))
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
  private Result<ChainInstrumentRecord> readAll(DSLContext db, Access access, ULong chainId) throws SQLException {
    if (access.isTopLevel())
      return db.selectFrom(CHAIN_INSTRUMENT)
        .where(CHAIN_INSTRUMENT.CHAIN_ID.eq(chainId))
        .fetch();
    else
      return resultInto(CHAIN_INSTRUMENT, db.select(CHAIN_INSTRUMENT.fields()).from(CHAIN_INSTRUMENT)
        .join(INSTRUMENT).on(INSTRUMENT.ID.eq(CHAIN_INSTRUMENT.INSTRUMENT_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(INSTRUMENT.LIBRARY_ID))
        .where(CHAIN_INSTRUMENT.CHAIN_ID.eq(chainId))
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
      requireExists("Chain Instrument", db.selectFrom(CHAIN_INSTRUMENT)
        .where(CHAIN_INSTRUMENT.ID.eq(id))
        .fetchOne());
    else
      requireExists("Chain Instrument", db.select(CHAIN_INSTRUMENT.fields()).from(CHAIN_INSTRUMENT)
        .join(INSTRUMENT).on(INSTRUMENT.ID.eq(CHAIN_INSTRUMENT.INSTRUMENT_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(INSTRUMENT.LIBRARY_ID))
        .where(CHAIN_INSTRUMENT.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne());


    db.deleteFrom(CHAIN_INSTRUMENT)
      .where(CHAIN_INSTRUMENT.ID.eq(id))
      .execute();
  }

}
