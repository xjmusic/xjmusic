// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao.impl;

import io.xj.core.access.impl.Access;
import io.xj.core.exception.BusinessException;
import io.xj.core.exception.ConfigException;
import io.xj.core.dao.InstrumentMemeDAO;
import io.xj.core.persistence.sql.impl.SQLConnection;
import io.xj.core.persistence.sql.SQLDatabaseProvider;
import io.xj.core.model.instrument_meme.InstrumentMeme;
import io.xj.core.tables.records.InstrumentMemeRecord;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Result;
import org.jooq.types.ULong;

import com.google.inject.Inject;

import java.sql.SQLException;
import java.util.Map;

import static io.xj.core.tables.Instrument.INSTRUMENT;
import static io.xj.core.tables.InstrumentMeme.INSTRUMENT_MEME;
import static io.xj.core.tables.Library.LIBRARY;

/**
 InstrumentMeme DAO
 <p>
 future: more specific permissions of user (artist) access by per-entity ownership
 */
public class InstrumentMemeDAOImpl extends DAOImpl implements InstrumentMemeDAO {

  @Inject
  public InstrumentMemeDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public InstrumentMemeRecord create(Access access, InstrumentMeme entity) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(createRecord(tx.getContext(), access, entity));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public InstrumentMemeRecord readOne(Access access, ULong id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOneRecord(tx.getContext(), access, id));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Result<InstrumentMemeRecord> readAll(Access access, ULong instrumentId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAll(tx.getContext(), access, instrumentId));
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
   Create a new Instrument Meme record

   @param db     context
   @param access control
   @param entity for new InstrumentMeme
   @return new record
   @throws Exception         if database failure
   @throws ConfigException   if not configured properly
   @throws BusinessException if fails business rule
   */
  private InstrumentMemeRecord createRecord(DSLContext db, Access access, InstrumentMeme entity) throws Exception {
    entity.validate();

    Map<Field, Object> fieldValues = entity.updatableFieldValueMap();

    if (access.isTopLevel())
      requireExists("Instrument", db.selectCount().from(INSTRUMENT)
        .where(INSTRUMENT.ID.eq(entity.getInstrumentId()))
        .fetchOne(0, int.class));
    else
      requireExists("Instrument", db.selectCount().from(INSTRUMENT)
        .join(LIBRARY).on(INSTRUMENT.LIBRARY_ID.eq(LIBRARY.ID))
        .where(INSTRUMENT.ID.eq(entity.getInstrumentId()))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne(0, int.class));

    if (db.selectFrom(INSTRUMENT_MEME)
      .where(INSTRUMENT_MEME.INSTRUMENT_ID.eq(entity.getInstrumentId()))
      .and(INSTRUMENT_MEME.NAME.eq(entity.getName()))
      .fetchOne() != null)
      throw new BusinessException("Instrument Meme already exists!");

    return executeCreate(db, INSTRUMENT_MEME, fieldValues);
  }

  /**
   Read one Instrument Meme where able

   @param db     context
   @param access control
   @param id     of record
   @return record
   */
  private InstrumentMemeRecord readOneRecord(DSLContext db, Access access, ULong id) throws SQLException {
    if (access.isTopLevel())
      return db.selectFrom(INSTRUMENT_MEME)
        .where(INSTRUMENT_MEME.ID.eq(id))
        .fetchOne();
    else
      return recordInto(INSTRUMENT_MEME, db.select(INSTRUMENT_MEME.fields()).from(INSTRUMENT_MEME)
        .join(INSTRUMENT).on(INSTRUMENT.ID.eq(INSTRUMENT_MEME.INSTRUMENT_ID))
        .join(LIBRARY).on(INSTRUMENT.LIBRARY_ID.eq(LIBRARY.ID))
        .where(INSTRUMENT_MEME.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne());
  }

  /**
   Read all Memes of an Instrument where able

   @param db           context
   @param access       control
   @param instrumentId to readMany memes for
   @return array of instrument memes
   @throws SQLException if failure
   */
  private Result<InstrumentMemeRecord> readAll(DSLContext db, Access access, ULong instrumentId) throws SQLException {
    if (access.isTopLevel())
      return db.selectFrom(INSTRUMENT_MEME)
        .where(INSTRUMENT_MEME.INSTRUMENT_ID.eq(instrumentId))
        .fetch();
    else
      return resultInto(INSTRUMENT_MEME, db.select(INSTRUMENT_MEME.fields()).from(INSTRUMENT_MEME)
        .join(INSTRUMENT).on(INSTRUMENT.ID.eq(INSTRUMENT_MEME.INSTRUMENT_ID))
        .join(LIBRARY).on(INSTRUMENT.LIBRARY_ID.eq(LIBRARY.ID))
        .where(INSTRUMENT.ID.eq(instrumentId))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetch());
  }

  /**
   Delete an InstrumentMeme record

   @param db     context
   @param access control
   @param id     to delete
   @throws BusinessException if failure
   */
  private void delete(DSLContext db, Access access, ULong id) throws BusinessException {
    if (!access.isTopLevel())
      requireExists("Instrument Meme", db.selectCount().from(INSTRUMENT_MEME)
        .join(INSTRUMENT).on(INSTRUMENT.ID.eq(INSTRUMENT_MEME.INSTRUMENT_ID))
        .join(LIBRARY).on(INSTRUMENT.LIBRARY_ID.eq(LIBRARY.ID))
        .where(INSTRUMENT_MEME.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne(0, int.class));

    db.deleteFrom(INSTRUMENT_MEME)
      .where(INSTRUMENT_MEME.ID.eq(id))
      .execute();
  }

}
