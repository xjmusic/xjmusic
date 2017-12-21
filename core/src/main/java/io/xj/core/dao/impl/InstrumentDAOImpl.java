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
import io.xj.core.work.WorkManager;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.types.ULong;

import com.google.api.client.util.Maps;
import com.google.inject.Inject;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import static io.xj.core.Tables.ARRANGEMENT;
import static io.xj.core.Tables.AUDIO;
import static io.xj.core.Tables.CHAIN_INSTRUMENT;
import static io.xj.core.Tables.INSTRUMENT;
import static io.xj.core.Tables.INSTRUMENT_MEME;
import static io.xj.core.Tables.LIBRARY;
import static io.xj.core.tables.ChainLibrary.CHAIN_LIBRARY;

public class InstrumentDAOImpl extends DAOImpl implements InstrumentDAO {
  private final WorkManager workManager;

  @Inject
  public InstrumentDAOImpl(
    SQLDatabaseProvider dbProvider,
    WorkManager workManager
  ) {
    this.workManager = workManager;
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
  public Instrument clone(Access access, BigInteger cloneId, Instrument entity) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(clone(tx.getContext(), access, cloneId, entity));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public Instrument readOne(Access access, BigInteger id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOne(tx.getContext(), access, ULong.valueOf(id)));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Collection<Instrument> readAllInAccount(Access access, BigInteger accountId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAllInAccount(tx.getContext(), access, ULong.valueOf(accountId)));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Collection<Instrument> readAllInLibrary(Access access, BigInteger libraryId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAllInLibrary(tx.getContext(), access, ULong.valueOf(libraryId)));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Collection<Instrument> readAll(Access access) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAll(tx.getContext(), access));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Collection<Instrument> readAllBoundToChain(Access access, BigInteger chainId, InstrumentType instrumentType) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAllBoundToChain(tx.getContext(), access, ULong.valueOf(chainId), instrumentType));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Collection<Instrument> readAllBoundToChainLibrary(Access access, BigInteger chainId, InstrumentType instrumentType) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAllBoundToChainLibrary(tx.getContext(), access, ULong.valueOf(chainId), instrumentType));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void update(Access access, BigInteger instrumentId, Instrument entity) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      update(tx.getContext(), access, ULong.valueOf(instrumentId), entity);
      tx.success();
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
   Create a record

   @param db     context
   @param access control
   @param entity for new record
   @return newly readMany record
   @throws BusinessException on failure
   */
  private static Instrument create(DSLContext db, Access access, Instrument entity) throws BusinessException {
    entity.validate();

    Map<Field, Object> fieldValues = fieldValueMap(entity);

    if (access.isTopLevel())
      requireExists("Library",
        db.selectCount().from(LIBRARY)
          .where(LIBRARY.ID.eq(ULong.valueOf(entity.getLibraryId())))
          .fetchOne(0, int.class));
    else
      requireExists("Library",
        db.selectCount().from(LIBRARY)
          .where(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
          .and(LIBRARY.ID.eq(ULong.valueOf(entity.getLibraryId())))
          .fetchOne(0, int.class));
    fieldValues.put(INSTRUMENT.USER_ID, access.getUserId());

    return modelFrom(executeCreate(db, INSTRUMENT, fieldValues), Instrument.class);
  }

  /**
   Clone a Instrument into a new Instrument

   @param db      context
   @param access  control
   @param cloneId of instrument to clone
   @param entity  for the new Account User.
   @return newly readMany record
   @throws BusinessException on failure
   */
  private Instrument clone(DSLContext db, Access access, BigInteger cloneId, Instrument entity) throws BusinessException {
    Instrument from = readOne(db, access, ULong.valueOf(cloneId));
    if (Objects.isNull(from)) throw new BusinessException("Can't clone nonexistent Instrument");

    entity.setUserId(from.getUserId());
    entity.setDensity(from.getDensity());
    entity.setUserId(from.getUserId());
    entity.setTypeEnum(from.getType());

    Instrument result = create(db, access, entity);
    workManager.scheduleInstrumentClone(0, cloneId, result.getId());
    return result;
  }

  /**
   Read one record

   @param db     context
   @param access control
   @param id     of record
   @return record
   */
  @Nullable
  private static Instrument readOne(DSLContext db, Access access, ULong id) throws BusinessException {
    if (access.isTopLevel())
      return modelFrom(db.selectFrom(INSTRUMENT)
        .where(INSTRUMENT.ID.eq(id))
        .fetchOne(), Instrument.class);
    else
      return modelFrom(db.select(INSTRUMENT.fields())
        .from(INSTRUMENT)
        .join(LIBRARY).on(LIBRARY.ID.eq(INSTRUMENT.LIBRARY_ID))
        .where(INSTRUMENT.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne(), Instrument.class);
  }

  /**
   Read all records in parent record by id

   @param db        context
   @param access    control
   @param accountId of parent
   @return array of records
   */
  private static Collection<Instrument> readAllInAccount(DSLContext db, Access access, ULong accountId) throws BusinessException {
    if (access.isTopLevel())
      return modelsFrom(db.select(INSTRUMENT.fields())
        .from(INSTRUMENT)
        .join(LIBRARY).on(INSTRUMENT.LIBRARY_ID.eq(LIBRARY.ID))
        .where(LIBRARY.ACCOUNT_ID.eq(accountId))
        .orderBy(INSTRUMENT.TYPE, INSTRUMENT.DESCRIPTION)
        .fetch(), Instrument.class);
    else
      return modelsFrom(db.select(INSTRUMENT.fields())
        .from(INSTRUMENT)
        .join(LIBRARY).on(INSTRUMENT.LIBRARY_ID.eq(LIBRARY.ID))
        .where(LIBRARY.ACCOUNT_ID.in(accountId))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .orderBy(INSTRUMENT.TYPE, INSTRUMENT.DESCRIPTION)
        .fetch(), Instrument.class);
  }

  /**
   Read all records in parent record by id

   @param db        context
   @param access    control
   @param libraryId of parent
   @return array of records
   */
  private static Collection<Instrument> readAllInLibrary(DSLContext db, Access access, ULong libraryId) throws BusinessException {
    if (access.isTopLevel())
      return modelsFrom(db.select(INSTRUMENT.fields())
        .from(INSTRUMENT)
        .where(INSTRUMENT.LIBRARY_ID.eq(libraryId))
        .orderBy(INSTRUMENT.TYPE, INSTRUMENT.DESCRIPTION)
        .fetch(), Instrument.class);
    else
      return modelsFrom(db.select(INSTRUMENT.fields())
        .from(INSTRUMENT)
        .join(LIBRARY).on(LIBRARY.ID.eq(INSTRUMENT.LIBRARY_ID))
        .where(INSTRUMENT.LIBRARY_ID.eq(libraryId))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .orderBy(INSTRUMENT.TYPE, INSTRUMENT.DESCRIPTION)
        .fetch(), Instrument.class);
  }

  /**
   Read all records visible to given access

   @param db     context
   @param access control
   @return array of records
   */
  private static Collection<Instrument> readAll(DSLContext db, Access access) throws BusinessException {
    if (access.isTopLevel())
      return modelsFrom(db.select(INSTRUMENT.fields())
        .from(INSTRUMENT)
        .orderBy(INSTRUMENT.TYPE, INSTRUMENT.DESCRIPTION)
        .fetch(), Instrument.class);
    else
      return modelsFrom(db.select(INSTRUMENT.fields())
        .from(INSTRUMENT)
        .join(LIBRARY).on(LIBRARY.ID.eq(INSTRUMENT.LIBRARY_ID))
        .where(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .orderBy(INSTRUMENT.TYPE, INSTRUMENT.DESCRIPTION)
        .fetch(), Instrument.class);
  }

  /**
   Read all instrument records bound to a Chain via ChainInstrument records

   @param db             context
   @param access         control
   @param chainId        of parent
   @param instrumentType of which to read all bound to chain
   @return array of records
   */
  private static Collection<Instrument> readAllBoundToChain(DSLContext db, Access access, ULong chainId, InstrumentType
    instrumentType) throws Exception {
    requireTopLevel(access);
    return modelsFrom(db.select(INSTRUMENT.fields()).from(INSTRUMENT)
      .join(CHAIN_INSTRUMENT).on(CHAIN_INSTRUMENT.INSTRUMENT_ID.eq(INSTRUMENT.ID))
      .where(CHAIN_INSTRUMENT.CHAIN_ID.eq(chainId))
      .and(INSTRUMENT.TYPE.eq(instrumentType.toString()))
      .fetch(), Instrument.class);
  }

  /**
   Read all instrument records bound to a Chain via ChainLibrary records

   @param db             context
   @param access         control
   @param chainId        of parent
   @param instrumentType of which to read all bound to chain
   @return array of records
   */
  private static Collection<Instrument> readAllBoundToChainLibrary(DSLContext db, Access access, ULong chainId, InstrumentType instrumentType) throws Exception {
    requireTopLevel(access);
    return modelsFrom(db.select(INSTRUMENT.fields()).from(INSTRUMENT)
      .join(CHAIN_LIBRARY).on(CHAIN_LIBRARY.LIBRARY_ID.eq(INSTRUMENT.LIBRARY_ID))
      .where(CHAIN_LIBRARY.CHAIN_ID.eq(chainId))
      .and(INSTRUMENT.TYPE.eq(instrumentType.toString()))
      .fetch(), Instrument.class);
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
  private static void update(DSLContext db, Access access, ULong id, Instrument entity) throws Exception {
    entity.validate();

    Map<Field, Object> fieldValues = fieldValueMap(entity);
    fieldValues.put(INSTRUMENT.ID, id);

    if (access.isTopLevel())
      requireExists("Library",
        db.selectCount().from(LIBRARY)
          .where(LIBRARY.ID.eq(ULong.valueOf(entity.getLibraryId())))
          .fetchOne(0, int.class));
    else
      requireExists("Library",
        db.selectCount().from(LIBRARY)
          .where(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
          .and(LIBRARY.ID.eq(ULong.valueOf(entity.getLibraryId())))
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
  private static void delete(DSLContext db, Access access, ULong id) throws Exception {
    if (!access.isTopLevel())
      requireExists("Instrument belonging to you", db.selectCount().from(INSTRUMENT)
        .join(LIBRARY).on(INSTRUMENT.LIBRARY_ID.eq(LIBRARY.ID))
        .where(INSTRUMENT.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(idCollection(access.getAccountIds())))
        .and(INSTRUMENT.USER_ID.eq(ULong.valueOf(access.getUserId())))
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

  /**
   Only certain (writable) fields are mapped back to jOOQ records--
   Read-only fields are excluded from here.

   @param entity to source values from
   @return values mapped to record fields
   */
  private static Map<Field, Object> fieldValueMap(Instrument entity) {
    Map<Field, Object> fieldValues = Maps.newHashMap();
    fieldValues.put(INSTRUMENT.DESCRIPTION, entity.getDescription());
    fieldValues.put(INSTRUMENT.LIBRARY_ID, entity.getLibraryId());
    fieldValues.put(INSTRUMENT.USER_ID, entity.getUserId());
    fieldValues.put(INSTRUMENT.TYPE, entity.getType());
    fieldValues.put(INSTRUMENT.DENSITY, entity.getDensity());
    return fieldValues;
  }


}
