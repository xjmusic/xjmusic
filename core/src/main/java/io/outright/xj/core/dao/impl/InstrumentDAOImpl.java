// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.dao.impl;

import io.outright.xj.core.app.access.AccessControl;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.app.exception.ConfigException;
import io.outright.xj.core.app.exception.DatabaseException;
import io.outright.xj.core.dao.InstrumentDAO;
import io.outright.xj.core.db.sql.SQLConnection;
import io.outright.xj.core.db.sql.SQLDatabaseProvider;
import io.outright.xj.core.model.instrument.InstrumentWrapper;
import io.outright.xj.core.tables.records.InstrumentRecord;
import io.outright.xj.core.transport.JSON;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.types.ULong;

import com.google.inject.Inject;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.annotation.Nullable;
import java.sql.SQLException;

import static io.outright.xj.core.Tables.AUDIO;
import static io.outright.xj.core.Tables.INSTRUMENT;
import static io.outright.xj.core.tables.InstrumentMeme.INSTRUMENT_MEME;
import static io.outright.xj.core.tables.Library.LIBRARY;

public class InstrumentDAOImpl extends DAOImpl implements InstrumentDAO {
  //  private static Logger log = LoggerFactory.getLogger(InstrumentDAOImpl.class);

  @Inject
  public InstrumentDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public JSONObject create(AccessControl access, InstrumentWrapper data) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(create(tx.getContext(), access, data));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public JSONObject readOne(AccessControl access, ULong id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOne(tx.getContext(), access, id));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public JSONArray readAllIn(AccessControl access, ULong libraryId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAllIn(tx.getContext(), access, libraryId));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void update(AccessControl access, ULong id, InstrumentWrapper data) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      update(tx.getContext(), access, id, data);
      tx.success();
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
   * Create a record
   *
   * @param db     context
   * @param access control
   * @param data   for new record
   * @return newly created record
   * @throws BusinessException on failure
   */
  private JSONObject create(DSLContext db, AccessControl access, InstrumentWrapper data) throws BusinessException {
    InstrumentRecord record;
    record = db.newRecord(INSTRUMENT);
    data.validate();
    data.getInstrument().intoFieldValueMap().forEach(record::setValue);
    if (access.isTopLevel()) {
      // Admin can create instrument in any existing library, with any user.
      requireRecordExists("Library",
        db.select(LIBRARY.ID).from(LIBRARY)
          .where(LIBRARY.ID.eq(data.getInstrument().getLibraryId()))
          .fetchOne());
    } else {
      // Not admin, must have account access, created by self user.
      requireRecordExists("Library",
        db.select(LIBRARY.ID).from(LIBRARY)
          .where(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
          .and(LIBRARY.ID.eq(data.getInstrument().getLibraryId()))
          .fetchOne());
      record.setUserId(access.getUserId());
    }

    record.store();

    return JSON.objectFromRecord(record);
  }

  /**
   * Read one record
   *
   * @param db     context
   * @param access control
   * @param id     of record
   * @return record
   */
  @Nullable
  private JSONObject readOne(DSLContext db, AccessControl access, ULong id) {
    if (access.isTopLevel()) {
      return JSON.objectFromRecord(db.selectFrom(INSTRUMENT)
        .where(INSTRUMENT.ID.eq(id))
        .fetchOne());
    } else {
      return JSON.objectFromRecord(db.select(INSTRUMENT.fields())
        .from(INSTRUMENT)
        .join(LIBRARY).on(LIBRARY.ID.eq(INSTRUMENT.LIBRARY_ID))
        .where(INSTRUMENT.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne());
    }
  }

  /**
   * Read all records in parent record by id
   *
   * @param db        context
   * @param access    control
   * @param libraryId of parent
   * @return array of records
   */
  private JSONArray readAllIn(DSLContext db, AccessControl access, ULong libraryId) throws SQLException {
    if (access.isTopLevel()) {
      return JSON.arrayFromResultSet(db.select(INSTRUMENT.fields())
        .from(INSTRUMENT)
        .where(INSTRUMENT.LIBRARY_ID.eq(libraryId))
        .fetchResultSet());
    } else {
      return JSON.arrayFromResultSet(db.select(INSTRUMENT.fields())
        .from(INSTRUMENT)
        .join(LIBRARY).on(LIBRARY.ID.eq(INSTRUMENT.LIBRARY_ID))
        .where(INSTRUMENT.LIBRARY_ID.eq(libraryId))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetchResultSet());
    }
  }

  /**
   * Update a record
   *
   * @param db     context
   * @param access control
   * @param id     of record
   * @param data   to update with
   * @throws BusinessException if a Business Rule is violated
   * @throws Exception         on database failure
   */
  private void update(DSLContext db, AccessControl access, ULong id, InstrumentWrapper data) throws Exception {
    data.validate();

    InstrumentRecord record;
    record = db.newRecord(INSTRUMENT);
    record.setId(id);
    data.getInstrument().intoFieldValueMap().forEach(record::setValue);

    if (access.isTopLevel()) {
      // Admin can create instrument in any existing library, with any user.
      requireRecordExists("Library",
        db.select(LIBRARY.ID).from(LIBRARY)
          .where(LIBRARY.ID.eq(data.getInstrument().getLibraryId()))
          .fetchOne());
    } else {
      // Not admin, must have account access, created by self user.
      requireRecordExists("Library",
        db.select(LIBRARY.ID).from(LIBRARY)
          .where(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
          .and(LIBRARY.ID.eq(data.getInstrument().getLibraryId()))
          .fetchOne());
      record.setUserId(access.getUserId());
    }

    if (db.executeUpdate(record) == 0) {
      throw new DatabaseException("No records updated.");
    }
  }

  /**
   * Delete an Instrument
   *
   * @param db context
   * @param id to delete
   * @throws Exception         if database failure
   * @throws ConfigException   if not configured properly
   * @throws BusinessException if fails business rule
   */
  private void delete(DSLContext db, AccessControl access, ULong id) throws Exception {
    if (!access.isTopLevel()) {
      Record record = db.select(INSTRUMENT.fields()).from(INSTRUMENT)
        .join(LIBRARY).on(INSTRUMENT.LIBRARY_ID.eq(LIBRARY.ID))
        .where(INSTRUMENT.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .and(INSTRUMENT.USER_ID.eq(access.getUserId()))
        .fetchOne();
      requireRecordExists("Instrument belonging to you", record);
    }

    requireEmptyResultSet(db.select(INSTRUMENT_MEME.ID)
      .from(INSTRUMENT_MEME)
      .where(INSTRUMENT_MEME.INSTRUMENT_ID.eq(id))
      .fetchResultSet());

    requireEmptyResultSet(db.select(AUDIO.ID)
      .from(AUDIO)
      .where(AUDIO.INSTRUMENT_ID.eq(id))
      .fetchResultSet());

    requireEmptyResultSet(db.select(INSTRUMENT_MEME.ID)
      .from(INSTRUMENT_MEME)
      .where(INSTRUMENT_MEME.INSTRUMENT_ID.eq(id))
      .fetchResultSet());

    db.deleteFrom(INSTRUMENT)
      .where(INSTRUMENT.ID.eq(id))
      .andNotExists(
        db.select(INSTRUMENT_MEME.ID)
          .from(INSTRUMENT_MEME)
          .where(INSTRUMENT_MEME.INSTRUMENT_ID.eq(id))
      )
      .andNotExists(
        db.select(AUDIO.ID)
          .from(AUDIO)
          .where(AUDIO.INSTRUMENT_ID.eq(id))
      )
      .andNotExists(
        db.select(INSTRUMENT_MEME.ID)
          .from(INSTRUMENT_MEME)
          .where(INSTRUMENT_MEME.INSTRUMENT_ID.eq(id))
      )
      .execute();
  }

}
