// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.dao.impl;

import io.xj.core.app.access.impl.Access;
import io.xj.core.app.exception.BusinessException;
import io.xj.core.app.exception.ConfigException;
import io.xj.core.dao.InstrumentDAO;
import io.xj.core.db.sql.impl.SQLConnection;
import io.xj.core.db.sql.SQLDatabaseProvider;
import io.xj.core.model.MemeEntity;
import io.xj.core.model.instrument.Instrument;
import io.xj.core.model.instrument.InstrumentType;
import io.xj.core.tables.records.InstrumentRecord;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SelectFromStep;
import org.jooq.types.ULong;

import com.google.inject.Inject;

import javax.annotation.Nullable;
import java.util.Map;

import static io.xj.core.Tables.AUDIO;
import static io.xj.core.Tables.CHAIN_INSTRUMENT;
import static io.xj.core.Tables.INSTRUMENT;
import static io.xj.core.Tables.INSTRUMENT_MEME;
import static io.xj.core.Tables.LIBRARY;
import static io.xj.core.tables.ChainLibrary.CHAIN_LIBRARY;
import static org.jooq.impl.DSL.groupConcat;

public class InstrumentDAOImpl extends DAOImpl implements InstrumentDAO {

  @Inject
  public InstrumentDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public InstrumentRecord create(Access access, Instrument entity) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(createRecord(tx.getContext(), access, entity));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public InstrumentRecord readOne(Access access, ULong id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOneRecord(tx.getContext(), access, id));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public Result<InstrumentRecord> readAllInAccount(Access access, ULong accountId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAllInAccount(tx.getContext(), access, accountId));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public Result<InstrumentRecord> readAllInLibrary(Access access, ULong libraryId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAllInLibrary(tx.getContext(), access, libraryId));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Result<? extends Record> readAllBoundToChain(Access access, ULong chainId, InstrumentType instrumentType) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAllBoundToChain(tx.getContext(), access, chainId, instrumentType));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Result<? extends Record> readAllBoundToChainLibrary(Access access, ULong chainId, InstrumentType instrumentType) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAllBoundToChainLibrary(tx.getContext(), access, chainId, instrumentType));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void update(Access access, ULong instrumentId, Instrument entity) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      update(tx.getContext(), access, instrumentId, entity);
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
   Create a record

   @param db     context
   @param access control
   @param entity for new record
   @return newly readMany record
   @throws BusinessException on failure
   */
  private InstrumentRecord createRecord(DSLContext db, Access access, Instrument entity) throws BusinessException {
    entity.validate();

    Map<Field, Object> fieldValues = entity.updatableFieldValueMap();

    if (access.isTopLevel())
      requireExists("Library",
        db.select(LIBRARY.ID).from(LIBRARY)
          .where(LIBRARY.ID.eq(entity.getLibraryId()))
          .fetchOne());
    else
      requireExists("Library",
        db.select(LIBRARY.ID).from(LIBRARY)
          .where(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
          .and(LIBRARY.ID.eq(entity.getLibraryId()))
          .fetchOne());
    fieldValues.put(INSTRUMENT.USER_ID, access.getUserId());

    return executeCreate(db, INSTRUMENT, fieldValues);
  }

  /**
   Read one record

   @param db     context
   @param access control
   @param id     of record
   @return record
   */
  @Nullable
  private InstrumentRecord readOneRecord(DSLContext db, Access access, ULong id) {
    if (access.isTopLevel())
      return db.selectFrom(INSTRUMENT)
        .where(INSTRUMENT.ID.eq(id))
        .fetchOne();
    else
      return recordInto(INSTRUMENT, db.select(INSTRUMENT.fields())
        .from(INSTRUMENT)
        .join(LIBRARY).on(LIBRARY.ID.eq(INSTRUMENT.LIBRARY_ID))
        .where(INSTRUMENT.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne());
  }

  /**
   Read all records in parent record by id

   @param db        context
   @param access    control
   @param accountId of parent
   @return array of records
   */
  private Result<InstrumentRecord> readAllInAccount(DSLContext db, Access access, ULong accountId) {
    if (access.isTopLevel())
      return resultInto(INSTRUMENT, db.select(INSTRUMENT.fields())
        .from(INSTRUMENT)
        .join(LIBRARY).on(INSTRUMENT.LIBRARY_ID.eq(LIBRARY.ID))
        .where(LIBRARY.ACCOUNT_ID.eq(accountId))
        .fetch());
    else
      return resultInto(INSTRUMENT, db.select(INSTRUMENT.fields())
        .from(INSTRUMENT)
        .join(LIBRARY).on(INSTRUMENT.LIBRARY_ID.eq(LIBRARY.ID))
        .where(LIBRARY.ACCOUNT_ID.in(accountId))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetch());
  }

  /**
   Read all records in parent record by id

   @param db        context
   @param access    control
   @param libraryId of parent
   @return array of records
   */
  private Result<InstrumentRecord> readAllInLibrary(DSLContext db, Access access, ULong libraryId) {
    if (access.isTopLevel())
      return resultInto(INSTRUMENT, db.select(INSTRUMENT.fields())
        .from(INSTRUMENT)
        .where(INSTRUMENT.LIBRARY_ID.eq(libraryId))
        .fetch());
    else
      return resultInto(INSTRUMENT, db.select(INSTRUMENT.fields())
        .from(INSTRUMENT)
        .join(LIBRARY).on(LIBRARY.ID.eq(INSTRUMENT.LIBRARY_ID))
        .where(INSTRUMENT.LIBRARY_ID.eq(libraryId))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetch());
  }

  /**
   Read all instrument records bound to a Chain via ChainInstrument records

   @return array of records
    @param db      context
   @param access  control
   @param chainId of parent
   @param instrumentType of which to read all bound to chain
   */
  private Result<? extends Record> readAllBoundToChain(DSLContext db, Access access, ULong chainId, InstrumentType instrumentType) throws Exception {
    requireTopLevel(access);
    return selectInstrumentAndMemes(db)
      .from(INSTRUMENT_MEME)
      .join(CHAIN_INSTRUMENT).on(CHAIN_INSTRUMENT.INSTRUMENT_ID.eq(INSTRUMENT_MEME.INSTRUMENT_ID))
      .join(INSTRUMENT).on(INSTRUMENT.ID.eq(INSTRUMENT_MEME.INSTRUMENT_ID))
      .where(CHAIN_INSTRUMENT.CHAIN_ID.eq(chainId))
      .and(INSTRUMENT.TYPE.eq(instrumentType.toString()))
      .groupBy(INSTRUMENT.ID)
      .fetch();
  }

  /**
   Read all instrument records bound to a Chain via ChainLibrary records

   @return array of records
    @param db      context
   @param access  control
   @param chainId of parent
   @param instrumentType of which to read all bound to chain
   */
  private Result<? extends Record> readAllBoundToChainLibrary(DSLContext db, Access access, ULong chainId, InstrumentType instrumentType) throws Exception {
    requireTopLevel(access);
    return selectInstrumentAndMemes(db)
      .from(INSTRUMENT_MEME)
      .join(INSTRUMENT).on(INSTRUMENT.ID.eq(INSTRUMENT_MEME.INSTRUMENT_ID))
      .join(CHAIN_LIBRARY).on(CHAIN_LIBRARY.LIBRARY_ID.eq(INSTRUMENT.LIBRARY_ID))
      .where(CHAIN_LIBRARY.CHAIN_ID.eq(chainId))
      .and(INSTRUMENT.TYPE.eq(instrumentType.toString()))
      .groupBy(INSTRUMENT.ID)
      .fetch();
  }

  /**
   Update a record

   @param db     context
   @param access control
   @param id     of record
   @param entity to update with
   @throws BusinessException if a Business Rule is violated
   @throws Exception         on database failure
   */
  private void update(DSLContext db, Access access, ULong id, Instrument entity) throws Exception {
    entity.validate();

    Map<Field, Object> fieldValues = entity.updatableFieldValueMap();
    fieldValues.put(INSTRUMENT.ID, id);

    if (access.isTopLevel())
      requireExists("Library",
        db.select(LIBRARY.ID).from(LIBRARY)
          .where(LIBRARY.ID.eq(entity.getLibraryId()))
          .fetchOne());
    else
      requireExists("Library",
        db.select(LIBRARY.ID).from(LIBRARY)
          .where(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
          .and(LIBRARY.ID.eq(entity.getLibraryId()))
          .fetchOne());
    fieldValues.put(INSTRUMENT.USER_ID, access.getUserId());

    if (0 == executeUpdate(db, INSTRUMENT, fieldValues))
      throw new BusinessException("No records updated.");
  }

  /**
   Delete an Instrument

   @param db context
   @param id to delete
   @throws Exception         if database failure
   @throws ConfigException   if not configured properly
   @throws BusinessException if fails business rule
   */
  private void delete(DSLContext db, Access access, ULong id) throws Exception {
    if (!access.isTopLevel())
      requireExists("Instrument belonging to you", db.select(INSTRUMENT.fields()).from(INSTRUMENT)
        .join(LIBRARY).on(INSTRUMENT.LIBRARY_ID.eq(LIBRARY.ID))
        .where(INSTRUMENT.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .and(INSTRUMENT.USER_ID.eq(access.getUserId()))
        .fetchOne());

    requireNotExists("Audio in Instrument", db.select(AUDIO.ID)
      .from(AUDIO)
      .where(AUDIO.INSTRUMENT_ID.eq(id))
      .fetch());

    requireNotExists("Meme in Instrument", db.select(INSTRUMENT_MEME.ID)
      .from(INSTRUMENT_MEME)
      .where(INSTRUMENT_MEME.INSTRUMENT_ID.eq(id))
      .fetch());

    db.deleteFrom(INSTRUMENT)
      .where(INSTRUMENT.ID.eq(id))
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

  /**
   This is used to select many Instrument records
   with a virtual column containing a CSV of its meme names

   @param db context
   @return jOOQ select step
   */
  private static SelectFromStep<?> selectInstrumentAndMemes(DSLContext db) {
    return db.select(
      INSTRUMENT.ID,
      INSTRUMENT.DENSITY,
      INSTRUMENT.DESCRIPTION,
      INSTRUMENT.USER_ID,
      INSTRUMENT.LIBRARY_ID,
      INSTRUMENT.TYPE,
      INSTRUMENT.CREATED_AT,
      INSTRUMENT.UPDATED_AT,
      groupConcat(INSTRUMENT_MEME.NAME, ",").as(MemeEntity.KEY_MANY)
    );
  }


}
