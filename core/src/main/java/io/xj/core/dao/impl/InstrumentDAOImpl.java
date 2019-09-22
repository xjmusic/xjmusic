// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao.impl;

import com.google.api.client.util.Maps;
import com.google.inject.Inject;
import io.xj.core.access.impl.Access;
import io.xj.core.dao.InstrumentDAO;
import io.xj.core.exception.CoreException;
import io.xj.core.model.instrument.Instrument;
import io.xj.core.model.instrument.InstrumentFactory;
import io.xj.core.persistence.sql.SQLDatabaseProvider;
import io.xj.core.persistence.sql.impl.SQLConnection;
import io.xj.core.transport.GsonProvider;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.types.ULong;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import static io.xj.core.Tables.AUDIO;
import static io.xj.core.Tables.INSTRUMENT;
import static io.xj.core.Tables.INSTRUMENT_MEME;
import static io.xj.core.Tables.LIBRARY;

public class InstrumentDAOImpl extends DAOImpl implements InstrumentDAO {
  private final GsonProvider gsonProvider;
  private final InstrumentFactory instrumentFactory;

  @Inject
  public InstrumentDAOImpl(
    SQLDatabaseProvider dbProvider,
    GsonProvider gsonProvider,
    InstrumentFactory instrumentFactory
  ) {
    this.instrumentFactory = instrumentFactory;
    this.gsonProvider = gsonProvider;
    this.dbProvider = dbProvider;
  }

  /**
   Delete an Instrument

   @param db context
   @param id to delete
   @throws CoreException if database failure
   @throws CoreException if not configured properly
   @throws CoreException if fails business rule
   */
  private static void delete(DSLContext db, Access access, ULong id) throws CoreException {
    if (!access.isTopLevel())
      requireExists("Instrument belonging to you", db.selectCount().from(INSTRUMENT)
        .join(LIBRARY).on(INSTRUMENT.LIBRARY_ID.eq(LIBRARY.ID))
        .where(INSTRUMENT.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(idCollection(access.getAccountIds())))
        .fetchOne(0, int.class));

    requireNotExists("Audio in Instrument", db.selectCount().from(AUDIO)
      .where(AUDIO.INSTRUMENT_ID.eq(id))
      .fetchOne(0, int.class));

    requireNotExists("MemeEntity in Instrument", db.selectCount().from(INSTRUMENT_MEME)
      .where(INSTRUMENT_MEME.INSTRUMENT_ID.eq(id))
      .fetchOne(0, int.class));

    db.deleteFrom(INSTRUMENT)
      .where(INSTRUMENT.ID.eq(id))
      .execute();
  }

  /**
   Read all records in parent record by id

   @param db        context
   @param access    control
   @param accountId of parent
   @return array of records
   */
  private Collection<Instrument> readAllInAccount(DSLContext db, Access access, ULong accountId) throws CoreException {
    if (access.isTopLevel())
      return modelsFrom(db.select(INSTRUMENT.fields())
        .from(INSTRUMENT)
        .join(LIBRARY).on(INSTRUMENT.LIBRARY_ID.eq(LIBRARY.ID))
        .where(LIBRARY.ACCOUNT_ID.eq(accountId))
        .orderBy(INSTRUMENT.TYPE, INSTRUMENT.NAME)
        .fetch(), instrumentFactory);
    else
      return modelsFrom(db.select(INSTRUMENT.fields())
        .from(INSTRUMENT)
        .join(LIBRARY).on(INSTRUMENT.LIBRARY_ID.eq(LIBRARY.ID))
        .where(LIBRARY.ACCOUNT_ID.in(accountId))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .orderBy(INSTRUMENT.TYPE, INSTRUMENT.NAME)
        .fetch(), instrumentFactory);
  }

  /**
   Read all records in parent record by id

   @param db         context
   @param access     control
   @param libraryIds of parent
   @return array of records
   */
  private Collection<Instrument> readAllInLibraries(DSLContext db, Access access, Collection<ULong> libraryIds) throws CoreException {
    if (access.isTopLevel())
      return modelsFrom(db.select(INSTRUMENT.fields())
        .from(INSTRUMENT)
        .where(INSTRUMENT.LIBRARY_ID.in(libraryIds))
        .orderBy(INSTRUMENT.TYPE, INSTRUMENT.NAME)
        .fetch(), instrumentFactory);
    else
      return modelsFrom(db.select(INSTRUMENT.fields())
        .from(INSTRUMENT)
        .join(LIBRARY).on(LIBRARY.ID.eq(INSTRUMENT.LIBRARY_ID))
        .where(INSTRUMENT.LIBRARY_ID.in(libraryIds))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .orderBy(INSTRUMENT.TYPE, INSTRUMENT.NAME)
        .fetch(), instrumentFactory);
  }

  /**
   Read all records visible to given access

   @param db     context
   @param access control
   @return array of records
   */
  private Collection<Instrument> readAll(DSLContext db, Access access) throws CoreException {
    if (access.isTopLevel())
      return modelsFrom(db.select(INSTRUMENT.fields())
        .from(INSTRUMENT)
        .orderBy(INSTRUMENT.TYPE, INSTRUMENT.NAME)
        .fetch(), instrumentFactory);
    else
      return modelsFrom(db.select(INSTRUMENT.fields())
        .from(INSTRUMENT)
        .join(LIBRARY).on(LIBRARY.ID.eq(INSTRUMENT.LIBRARY_ID))
        .where(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .orderBy(INSTRUMENT.TYPE, INSTRUMENT.NAME)
        .fetch(), instrumentFactory);
  }

  /**
   Read one record

   @param db     context
   @param access control
   @param id     of record
   @return record
   */
  @Nullable
  private Instrument readOne(DSLContext db, Access access, ULong id) throws CoreException {
    if (access.isTopLevel())
      return modelFrom(db.selectFrom(INSTRUMENT)
        .where(INSTRUMENT.ID.eq(id))
        .fetchOne(), instrumentFactory);
    else
      return modelFrom(db.select(INSTRUMENT.fields())
        .from(INSTRUMENT)
        .join(LIBRARY).on(LIBRARY.ID.eq(INSTRUMENT.LIBRARY_ID))
        .where(INSTRUMENT.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne(), instrumentFactory);
  }

  /**
   Create a record

   @param db     context
   @param access control
   @param entity for new record
   @return newly readMany record
   @throws CoreException on failure
   */
  private Instrument create(DSLContext db, Access access, Instrument entity) throws CoreException {
    entity.validate();

    Map<Field, Object> fieldValues = fieldValueMap(entity);

    // This entity's parent is a Library
    requireLibraryAccess(db, access, entity);

    return modelFrom(executeCreate(db, INSTRUMENT, fieldValues), instrumentFactory);
  }

  /**
   Update a record

   @param db     context
   @param access control
   @param id     of record
   @param entity to update with
   @throws CoreException if a Business Rule is violated
   @throws CoreException on database failure
   */
  private void update(DSLContext db, Access access, ULong id, Instrument entity) throws CoreException {
    entity.validate();

    Map<Field, Object> fieldValues = fieldValueMap(entity);
    fieldValues.put(INSTRUMENT.ID, id);

    // This entity's parent is a Library
    requireLibraryAccess(db, access, entity);

    if (0 == executeUpdate(db, INSTRUMENT, fieldValues))
      throw new CoreException("No records updated.");
  }

  /**
   Only certain (writable) fields are mapped back to jOOQ records--
   Read-only fields are excluded from here.

   @param entity to source values from
   @return values mapped to record fields
   */
  private Map<Field, Object> fieldValueMap(Instrument entity) {
    Map<Field, Object> fieldValues = Maps.newHashMap();
    fieldValues.put(INSTRUMENT.CONTENT, gsonProvider.gson().toJson(entity.getContent()));
    fieldValues.put(INSTRUMENT.NAME, entity.getName());
    fieldValues.put(INSTRUMENT.USER_ID, entity.getUserId());
    fieldValues.put(INSTRUMENT.LIBRARY_ID, entity.getLibraryId());
    fieldValues.put(INSTRUMENT.TYPE, entity.getType());
    fieldValues.put(INSTRUMENT.STATE, entity.getState());
    return fieldValues;
  }

  /**
   Clone a Instrument into a new Instrument

   @param db      context
   @param access  control
   @param cloneId of instrument to clone
   @param entity  for the new Account User.
   @return newly readMany record
   @throws CoreException on failure
   */
  private Instrument clone(DSLContext db, Access access, BigInteger cloneId, Instrument entity) throws CoreException {
    Instrument from = readOne(db, access, ULong.valueOf(cloneId));
    if (Objects.isNull(from))
      throw new CoreException("Can't clone nonexistent Instrument");

    // Inherits state, type if none specified
    if (Objects.isNull(entity.getType())) entity.setTypeEnum(from.getType());
    if (Objects.isNull(entity.getState())) entity.setStateEnum(from.getState());

    // Instrument must be created (have id) before adding content and updating
    Instrument instrument = create(db, access, entity);
    instrument.setContentCloned(from);
    update(db, access, ULong.valueOf(instrument.getId()), instrument);
    return instrument;
  }

  @Override
  public Instrument create(Access access, Instrument entity) throws CoreException {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(create(tx.getContext(), access, entity));
    } catch (CoreException e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Instrument clone(Access access, BigInteger cloneId, Instrument entity) throws CoreException {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(clone(tx.getContext(), access, cloneId, entity));
    } catch (CoreException e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public Instrument readOne(Access access, BigInteger id) throws CoreException {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOne(tx.getContext(), access, ULong.valueOf(id)));
    } catch (CoreException e) {
      throw tx.failure(e);
    }
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

  @Override
  public Instrument newInstance() {
    return instrumentFactory.newInstrument();
  }

  @Override
  public Collection<Instrument> readAllInAccount(Access access, BigInteger accountId) throws CoreException {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAllInAccount(tx.getContext(), access, ULong.valueOf(accountId)));
    } catch (CoreException e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Collection<Instrument> readMany(Access access, Collection<BigInteger> parentIds) throws CoreException {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAllInLibraries(tx.getContext(), access, uLongValuesOf(parentIds)));
    } catch (CoreException e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Collection<Instrument> readAll(Access access) throws CoreException {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAll(tx.getContext(), access));
    } catch (CoreException e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void update(Access access, BigInteger id, Instrument entity) throws CoreException {
    SQLConnection tx = dbProvider.getConnection();
    try {
      update(tx.getContext(), access, ULong.valueOf(id), entity);
      tx.success();
    } catch (CoreException e) {
      throw tx.failure(e);
    }
  }


}
