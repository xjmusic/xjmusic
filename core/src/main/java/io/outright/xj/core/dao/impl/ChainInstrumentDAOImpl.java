// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.dao.impl;

import io.outright.xj.core.app.access.impl.AccessControl;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.app.exception.ConfigException;
import io.outright.xj.core.dao.ChainInstrumentDAO;
import io.outright.xj.core.db.sql.SQLConnection;
import io.outright.xj.core.db.sql.SQLDatabaseProvider;
import io.outright.xj.core.model.chain_instrument.ChainInstrument;
import io.outright.xj.core.model.chain_instrument.ChainInstrumentWrapper;
import io.outright.xj.core.transport.JSON;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.types.ULong;

import com.google.inject.Inject;

import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.Map;

import static io.outright.xj.core.tables.Chain.CHAIN;
import static io.outright.xj.core.tables.ChainInstrument.CHAIN_INSTRUMENT;
import static io.outright.xj.core.tables.Instrument.INSTRUMENT;
import static io.outright.xj.core.tables.Library.LIBRARY;

public class ChainInstrumentDAOImpl extends DAOImpl implements ChainInstrumentDAO {

  @Inject
  public ChainInstrumentDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public JSONObject create(AccessControl access, ChainInstrumentWrapper data) throws Exception {
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
   * Create a new Chain Instrument record
   *
   * @param db   context
   * @param data for new ChainInstrument
   * @return new record
   * @throws Exception         if database failure
   * @throws ConfigException   if not configured properly
   * @throws BusinessException if fails business rule
   */
  private JSONObject create(DSLContext db, AccessControl access, ChainInstrumentWrapper data) throws Exception {
    ChainInstrument model = data.validate();
    Map<Field, Object> fieldValues = model.intoFieldValueMap();

    if (access.isTopLevel()) {
      requireRecordExists("Chain", db.select(CHAIN.ID).from(CHAIN)
        .where(CHAIN.ID.eq(model.getChainId()))
        .fetchOne());
      requireRecordExists("Instrument", db.select(INSTRUMENT.ID).from(INSTRUMENT)
        .where(INSTRUMENT.ID.eq(model.getInstrumentId()))
        .fetchOne());
    } else {
      requireRecordExists("Chain", db.select(CHAIN.ID).from(CHAIN)
        .where(CHAIN.ACCOUNT_ID.in(access.getAccounts()))
        .and(CHAIN.ID.eq(model.getChainId()))
        .fetchOne());
      requireRecordExists("Instrument", db.select(INSTRUMENT.ID).from(INSTRUMENT)
        .join(LIBRARY).on(LIBRARY.ID.eq(INSTRUMENT.LIBRARY_ID))
        .where(INSTRUMENT.ID.eq(model.getInstrumentId()))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne());
    }

    if (db.selectFrom(CHAIN_INSTRUMENT)
      .where(CHAIN_INSTRUMENT.CHAIN_ID.eq(model.getChainId()))
      .and(CHAIN_INSTRUMENT.INSTRUMENT_ID.eq(model.getInstrumentId()))
      .fetchOne() != null) {
      throw new BusinessException("Instrument already added to Chain!");
    }

    return JSON.objectFromRecord(executeCreate(db, CHAIN_INSTRUMENT, fieldValues));
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
      return JSON.objectFromRecord(db.selectFrom(CHAIN_INSTRUMENT)
        .where(CHAIN_INSTRUMENT.ID.eq(id))
        .fetchOne());
    } else {
      return JSON.objectFromRecord(db.select(CHAIN_INSTRUMENT.fields()).from(CHAIN_INSTRUMENT)
        .join(INSTRUMENT).on(INSTRUMENT.ID.eq(CHAIN_INSTRUMENT.INSTRUMENT_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(INSTRUMENT.LIBRARY_ID))
        .where(CHAIN_INSTRUMENT.ID.eq(id))
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
      return JSON.arrayFromResultSet(db.selectFrom(CHAIN_INSTRUMENT)
        .where(CHAIN_INSTRUMENT.CHAIN_ID.eq(chainId))
        .fetchResultSet());
    } else {
      return JSON.arrayFromResultSet(db.select(CHAIN_INSTRUMENT.fields()).from(CHAIN_INSTRUMENT)
        .join(INSTRUMENT).on(INSTRUMENT.ID.eq(CHAIN_INSTRUMENT.INSTRUMENT_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(INSTRUMENT.LIBRARY_ID))
        .where(CHAIN_INSTRUMENT.CHAIN_ID.eq(chainId))
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
    // TODO: fail if no chainInstrument is deleted
    if (access.isTopLevel()) {
      requireRecordExists("Chain Instrument", db.selectFrom(CHAIN_INSTRUMENT)
        .where(CHAIN_INSTRUMENT.ID.eq(id))
        .fetchOne());
    } else {
      requireRecordExists("Chain Instrument", db.select(CHAIN_INSTRUMENT.fields()).from(CHAIN_INSTRUMENT)
        .join(INSTRUMENT).on(INSTRUMENT.ID.eq(CHAIN_INSTRUMENT.INSTRUMENT_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(INSTRUMENT.LIBRARY_ID))
        .where(CHAIN_INSTRUMENT.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne());
    }

    db.deleteFrom(CHAIN_INSTRUMENT)
      .where(CHAIN_INSTRUMENT.ID.eq(id))
      .execute();
  }

}
