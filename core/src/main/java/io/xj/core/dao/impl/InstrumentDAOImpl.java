// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao.impl;

import io.xj.core.access.impl.Access;
import io.xj.core.dao.InstrumentDAO;
import io.xj.core.exception.BusinessException;
import io.xj.core.exception.ConfigException;
import io.xj.core.model.instrument.Instrument;
import io.xj.core.model.instrument.InstrumentType;
import io.xj.core.persistence.sql.SQLDatabaseProvider;
import io.xj.core.persistence.sql.impl.SQLConnection;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.types.ULong;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;

import static io.xj.core.Tables.ARRANGEMENT;
import static io.xj.core.Tables.AUDIO;
import static io.xj.core.Tables.CHAIN_INSTRUMENT;
import static io.xj.core.Tables.INSTRUMENT;
import static io.xj.core.Tables.INSTRUMENT_MEME;
import static io.xj.core.Tables.LIBRARY;
import static io.xj.core.tables.ChainLibrary.CHAIN_LIBRARY;

public class InstrumentDAOImpl extends DAOImpl implements InstrumentDAO {

  @Inject
  public InstrumentDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public Instrument create(Access access, Instrument entity) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(create(tx.getContext(), access, entity));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public Instrument readOne(Access access, ULong id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOne(tx.getContext(), access, id));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public Collection<Instrument> readAllInAccount(Access access, ULong accountId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAllInAccount(tx.getContext(), access, accountId));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public Collection<Instrument> readAllInLibrary(Access access, ULong libraryId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAllInLibrary(tx.getContext(), access, libraryId));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Collection<Instrument> readAllBoundToChain(Access access, ULong chainId, InstrumentType instrumentType) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAllBoundToChain(tx.getContext(), access, chainId, instrumentType));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Collection<Instrument> readAllBoundToChainLibrary(Access access, ULong chainId, InstrumentType instrumentType) throws Exception {
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

   @return newly readMany record
   @throws BusinessException on failure
    @param db     context
   @param access control
   @param entity for new record
   */
  private Instrument create(DSLContext db, Access access, Instrument entity) throws BusinessException {
    entity.validate();

    Map<Field, Object> fieldValues = entity.updatableFieldValueMap();

    if (access.isTopLevel())
      requireExists("Library",
        db.selectCount().from(LIBRARY)
          .where(LIBRARY.ID.eq(entity.getLibraryId()))
          .fetchOne(0, int.class));
    else
      requireExists("Library",
        db.selectCount().from(LIBRARY)
          .where(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
          .and(LIBRARY.ID.eq(entity.getLibraryId()))
          .fetchOne(0, int.class));
    fieldValues.put(INSTRUMENT.USER_ID, access.getUserId());

    return new Instrument().setFromRecord(executeCreate(db, INSTRUMENT, fieldValues));
  }

  /**
   Read one record

   @return record
    @param db     context
   @param access control
   @param id     of record
   */
  @Nullable
  private Instrument readOne(DSLContext db, Access access, ULong id) {
    if (access.isTopLevel())
      return new Instrument().setFromRecord(db.selectFrom(INSTRUMENT)
        .where(INSTRUMENT.ID.eq(id))
        .fetchOne());
    else
      return new Instrument().setFromRecord(recordInto(INSTRUMENT, db.select(INSTRUMENT.fields())
        .from(INSTRUMENT)
        .join(LIBRARY).on(LIBRARY.ID.eq(INSTRUMENT.LIBRARY_ID))
        .where(INSTRUMENT.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne()));
  }

  /**
   Read all records in parent record by id

   @param db        context
   @param access    control
   @param accountId of parent
   @return array of records
   */
  private Collection<Instrument> readAllInAccount(DSLContext db, Access access, ULong accountId) {
    Collection<Instrument> result = Lists.newArrayList();

    if (access.isTopLevel())
      resultInto(INSTRUMENT, db.select(INSTRUMENT.fields())
        .from(INSTRUMENT)
        .join(LIBRARY).on(INSTRUMENT.LIBRARY_ID.eq(LIBRARY.ID))
        .where(LIBRARY.ACCOUNT_ID.eq(accountId))
        .orderBy(INSTRUMENT.TYPE, INSTRUMENT.DESCRIPTION)
        .fetch()).forEach(record -> result.add(new Instrument().setFromRecord(record)));
    else
      resultInto(INSTRUMENT, db.select(INSTRUMENT.fields())
        .from(INSTRUMENT)
        .join(LIBRARY).on(INSTRUMENT.LIBRARY_ID.eq(LIBRARY.ID))
        .where(LIBRARY.ACCOUNT_ID.in(accountId))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .orderBy(INSTRUMENT.TYPE, INSTRUMENT.DESCRIPTION)
        .fetch()).forEach(record -> result.add(new Instrument().setFromRecord(record)));

    return result;
  }

  /**
   Read all records in parent record by id

   @param db        context
   @param access    control
   @param libraryId of parent
   @return array of records
   */
  private Collection<Instrument> readAllInLibrary(DSLContext db, Access access, ULong libraryId) {
    Collection<Instrument> result = Lists.newArrayList();

    if (access.isTopLevel())
      resultInto(INSTRUMENT, db.select(INSTRUMENT.fields())
        .from(INSTRUMENT)
        .where(INSTRUMENT.LIBRARY_ID.eq(libraryId))
        .orderBy(INSTRUMENT.TYPE, INSTRUMENT.DESCRIPTION)
        .fetch()).forEach(record -> result.add(new Instrument().setFromRecord(record)));
    else
      resultInto(INSTRUMENT, db.select(INSTRUMENT.fields())
        .from(INSTRUMENT)
        .join(LIBRARY).on(LIBRARY.ID.eq(INSTRUMENT.LIBRARY_ID))
        .where(INSTRUMENT.LIBRARY_ID.eq(libraryId))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .orderBy(INSTRUMENT.TYPE, INSTRUMENT.DESCRIPTION)
        .fetch()).forEach(record -> result.add(new Instrument().setFromRecord(record)));

    return result;
  }

  /**
   Read all instrument records bound to a Chain via ChainInstrument records

   @param db             context
   @param access         control
   @param chainId        of parent
   @param instrumentType of which to read all bound to chain
   @return array of records
   */
  private Collection<Instrument> readAllBoundToChain(DSLContext db, Access access, ULong chainId, InstrumentType
    instrumentType) throws Exception {
    requireTopLevel(access);

    Collection<Instrument> result = Lists.newArrayList();

    db.select(INSTRUMENT.fields()).from(INSTRUMENT)
      .join(CHAIN_INSTRUMENT).on(CHAIN_INSTRUMENT.INSTRUMENT_ID.eq(INSTRUMENT.ID))
      .where(CHAIN_INSTRUMENT.CHAIN_ID.eq(chainId))
      .and(INSTRUMENT.TYPE.eq(instrumentType.toString()))
      .fetch().forEach(record -> result.add(new Instrument().setFromRecord(record)));

    return result;
  }

  /**
   Read all instrument records bound to a Chain via ChainLibrary records

   @param db             context
   @param access         control
   @param chainId        of parent
   @param instrumentType of which to read all bound to chain
   @return array of records
   */
  private Collection<Instrument> readAllBoundToChainLibrary(DSLContext db, Access access, ULong chainId, InstrumentType instrumentType) throws Exception {
    requireTopLevel(access);

    Collection<Instrument> result = Lists.newArrayList();

    db.select(INSTRUMENT.fields()).from(INSTRUMENT)
      .join(CHAIN_LIBRARY).on(CHAIN_LIBRARY.LIBRARY_ID.eq(INSTRUMENT.LIBRARY_ID))
      .where(CHAIN_LIBRARY.CHAIN_ID.eq(chainId))
      .and(INSTRUMENT.TYPE.eq(instrumentType.toString()))
      .fetch().forEach(record -> result.add(new Instrument().setFromRecord(record)));

    return result;
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
        db.selectCount().from(LIBRARY)
          .where(LIBRARY.ID.eq(entity.getLibraryId()))
          .fetchOne(0, int.class));
    else
      requireExists("Library",
        db.selectCount().from(LIBRARY)
          .where(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
          .and(LIBRARY.ID.eq(entity.getLibraryId()))
          .fetchOne(0, int.class));
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
      requireExists("Instrument belonging to you", db.selectCount().from(INSTRUMENT)
        .join(LIBRARY).on(INSTRUMENT.LIBRARY_ID.eq(LIBRARY.ID))
        .where(INSTRUMENT.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .and(INSTRUMENT.USER_ID.eq(access.getUserId()))
        .fetchOne(0, int.class));

    requireNotExists("Audio in Instrument", db.selectCount().from(AUDIO)
      .where(AUDIO.INSTRUMENT_ID.eq(id))
      .fetchOne(0, int.class));

    requireNotExists("Meme in Instrument", db.selectCount().from(INSTRUMENT_MEME)
      .where(INSTRUMENT_MEME.INSTRUMENT_ID.eq(id))
      .fetchOne(0, int.class));

    db.deleteFrom(ARRANGEMENT)
      .where(ARRANGEMENT.INSTRUMENT_ID.eq(id))
      .execute();

    db.deleteFrom(INSTRUMENT)
      .where(INSTRUMENT.ID.eq(id))
      .execute();
  }

}
