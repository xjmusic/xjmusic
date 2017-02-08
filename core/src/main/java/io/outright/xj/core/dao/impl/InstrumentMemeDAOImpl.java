// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.dao.impl;

import io.outright.xj.core.app.access.AccessControl;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.app.exception.ConfigException;
import io.outright.xj.core.dao.InstrumentMemeDAO;
import io.outright.xj.core.db.sql.SQLConnection;
import io.outright.xj.core.db.sql.SQLDatabaseProvider;
import io.outright.xj.core.model.instrument_meme.InstrumentMemeWrapper;
import io.outright.xj.core.tables.records.InstrumentMemeRecord;
import io.outright.xj.core.transport.JSON;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.types.ULong;

import com.google.inject.Inject;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

import static io.outright.xj.core.tables.Instrument.INSTRUMENT;
import static io.outright.xj.core.tables.InstrumentMeme.INSTRUMENT_MEME;
import static io.outright.xj.core.tables.Library.LIBRARY;

/**
 * InstrumentMeme DAO
 * <p>
 * TODO [core] more specific permissions of user (artist) access by per-entity ownership
 */
public class InstrumentMemeDAOImpl extends DAOImpl implements InstrumentMemeDAO {
  private static Logger log = LoggerFactory.getLogger(InstrumentMemeDAOImpl.class);

  @Inject
  public InstrumentMemeDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public JSONObject create(AccessControl access, InstrumentMemeWrapper data) throws Exception {
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
      return tx.success(readOneAble(tx.getContext(), access, id));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public JSONArray readAllIn(AccessControl access, ULong instrumentId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAllIn(tx.getContext(), access, instrumentId));
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
   * Create a new Instrument Meme record
   *
   * @param db     context
   * @param access control
   * @param data   for new InstrumentMeme
   * @return new record
   * @throws Exception         if database failure
   * @throws ConfigException   if not configured properly
   * @throws BusinessException if fails business rule
   */
  private JSONObject create(DSLContext db, AccessControl access, InstrumentMemeWrapper data) throws Exception {
    data.validate();

    ULong instrumentId = ULong.valueOf(data.getInstrumentMeme().getInstrumentId());
    String name = data.getInstrumentMeme().getName();

    if (access.isTopLevel()) {
      requireRecordExists("Instrument", db.select(INSTRUMENT.ID).from(INSTRUMENT)
        .where(INSTRUMENT.ID.eq(instrumentId))
        .fetchOne());
    } else {
      requireRecordExists("Instrument", db.select(INSTRUMENT.ID).from(INSTRUMENT)
        .join(LIBRARY).on(INSTRUMENT.LIBRARY_ID.eq(LIBRARY.ID))
        .where(INSTRUMENT.ID.eq(instrumentId))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne());
    }

    if (db.selectFrom(INSTRUMENT_MEME)
      .where(INSTRUMENT_MEME.INSTRUMENT_ID.eq(instrumentId))
      .and(INSTRUMENT_MEME.NAME.eq(name))
      .fetchOne() != null) {
      throw new BusinessException("Instrument Meme already exists!");
    }

    InstrumentMemeRecord record;
    record = db.newRecord(INSTRUMENT_MEME);
    data.getInstrumentMeme().intoFieldValueMap().forEach(record::setValue);

    try {
      record.store();
    } catch (Exception e) {
      log.warn("Cannot create InstrumentMeme", e.getMessage());
      throw new BusinessException("Cannot create Instrument Meme. Please ensure name+instrumentId are valid and unique.");
    }

    return JSON.objectFromRecord(record);
  }

  /**
   * Read one Instrument Meme where able
   *
   * @param db     context
   * @param access control
   * @param id     of record
   * @return record
   */
  private JSONObject readOneAble(DSLContext db, AccessControl access, ULong id) throws SQLException {
    if (access.isTopLevel()) {
      return JSON.objectFromRecord(db.selectFrom(INSTRUMENT_MEME)
        .where(INSTRUMENT_MEME.ID.eq(id))
        .fetchOne());
    } else {
      return JSON.objectFromRecord(db.select(INSTRUMENT_MEME.fields()).from(INSTRUMENT_MEME)
        .join(INSTRUMENT).on(INSTRUMENT.ID.eq(INSTRUMENT_MEME.INSTRUMENT_ID))
        .join(LIBRARY).on(INSTRUMENT.LIBRARY_ID.eq(LIBRARY.ID))
        .where(INSTRUMENT_MEME.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne());
    }
  }

  /**
   * Read all Memes of an Instrument where able
   *
   * @param db     context
   * @param access control
   * @param instrumentId to read memes for
   * @return array of instrument memes
   * @throws SQLException if failure
   */
  private JSONArray readAllIn(DSLContext db, AccessControl access, ULong instrumentId) throws SQLException {
    if (access.isTopLevel()) {
      return JSON.arrayFromResultSet(db.selectFrom(INSTRUMENT_MEME)
        .where(INSTRUMENT_MEME.INSTRUMENT_ID.eq(instrumentId))
        .fetchResultSet());
    } else {
      return JSON.arrayFromResultSet(db.select(INSTRUMENT_MEME.fields()).from(INSTRUMENT_MEME)
        .join(INSTRUMENT).on(INSTRUMENT.ID.eq(INSTRUMENT_MEME.INSTRUMENT_ID))
        .join(LIBRARY).on(INSTRUMENT.LIBRARY_ID.eq(LIBRARY.ID))
        .where(INSTRUMENT.ID.eq(instrumentId))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetchResultSet());
    }
  }

  /**
   * Delete an InstrumentMeme record
   *
   * @param db     context
   * @param access control
   * @param id     to delete
   * @throws BusinessException if failure
   */
  // TODO: fail if no instrumentMeme is deleted
  private void delete(DSLContext db, AccessControl access, ULong id) throws BusinessException {
    if (!access.isTopLevel()) {
      Record record = db.select(INSTRUMENT_MEME.ID).from(INSTRUMENT_MEME)
        .join(INSTRUMENT).on(INSTRUMENT.ID.eq(INSTRUMENT_MEME.INSTRUMENT_ID))
        .join(LIBRARY).on(INSTRUMENT.LIBRARY_ID.eq(LIBRARY.ID))
        .where(INSTRUMENT_MEME.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne();
      requireRecordExists("Instrument Meme", record);
    }

    db.deleteFrom(INSTRUMENT_MEME)
      .where(INSTRUMENT_MEME.ID.eq(id))
      .execute();
  }

}
