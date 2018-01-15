// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao.impl;

import io.xj.core.access.impl.Access;
import io.xj.core.dao.InstrumentMemeDAO;
import io.xj.core.exception.BusinessException;
import io.xj.core.exception.ConfigException;
import io.xj.core.model.instrument_meme.InstrumentMeme;
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
import java.util.Objects;

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
  private static InstrumentMeme create(DSLContext db, Access access, InstrumentMeme entity) throws Exception {
    entity.validate();

    Map<Field, Object> fieldValues = fieldValueMap(entity);

    if (access.isTopLevel())
      requireExists("Instrument", db.selectCount().from(INSTRUMENT)
        .where(INSTRUMENT.ID.eq(ULong.valueOf(entity.getInstrumentId())))
        .fetchOne(0, int.class));
    else
      requireExists("Instrument", db.selectCount().from(INSTRUMENT)
        .join(LIBRARY).on(INSTRUMENT.LIBRARY_ID.eq(LIBRARY.ID))
        .where(INSTRUMENT.ID.eq(ULong.valueOf(entity.getInstrumentId())))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne(0, int.class));

    if (Objects.nonNull(db.selectFrom(INSTRUMENT_MEME)
      .where(INSTRUMENT_MEME.INSTRUMENT_ID.eq(ULong.valueOf(entity.getInstrumentId())))
      .and(INSTRUMENT_MEME.NAME.eq(entity.getName()))
      .fetchOne()))
      throw new BusinessException("Instrument Meme already exists!");

    return modelFrom(executeCreate(db, INSTRUMENT_MEME, fieldValues), InstrumentMeme.class);
  }

  /**
   Read one Instrument Meme where able

   @param db     context
   @param access control
   @param id     of record
   @return record
   */
  private static InstrumentMeme readOne(DSLContext db, Access access, ULong id) throws BusinessException {
    if (access.isTopLevel())
      return modelFrom(db.selectFrom(INSTRUMENT_MEME)
        .where(INSTRUMENT_MEME.ID.eq(id))
        .fetchOne(), InstrumentMeme.class);
    else
      return modelFrom(db.select(INSTRUMENT_MEME.fields()).from(INSTRUMENT_MEME)
        .join(INSTRUMENT).on(INSTRUMENT.ID.eq(INSTRUMENT_MEME.INSTRUMENT_ID))
        .join(LIBRARY).on(INSTRUMENT.LIBRARY_ID.eq(LIBRARY.ID))
        .where(INSTRUMENT_MEME.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne(), InstrumentMeme.class);
  }

  /**
   Read all Memes of an Instrument where able

   @param db           context
   @param access       control
   @param instrumentId to readMany memes for
   @return array of instrument memes
   */
  private static Collection<InstrumentMeme> readAll(DSLContext db, Access access, Collection<ULong> instrumentId) throws BusinessException {
    if (access.isTopLevel())
      return modelsFrom(db.selectFrom(INSTRUMENT_MEME)
        .where(INSTRUMENT_MEME.INSTRUMENT_ID.in(instrumentId))
        .fetch(), InstrumentMeme.class);
    else
      return modelsFrom(db.select(INSTRUMENT_MEME.fields()).from(INSTRUMENT_MEME)
        .join(INSTRUMENT).on(INSTRUMENT.ID.eq(INSTRUMENT_MEME.INSTRUMENT_ID))
        .join(LIBRARY).on(INSTRUMENT.LIBRARY_ID.eq(LIBRARY.ID))
        .where(INSTRUMENT.ID.in(instrumentId))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .fetch(), InstrumentMeme.class);
  }

  /**
   Delete an InstrumentMeme record

   @param db     context
   @param access control
   @param id     to delete
   @throws BusinessException if failure
   */
  private static void delete(DSLContext db, Access access, ULong id) throws BusinessException {
    if (!access.isTopLevel())
      requireExists("Instrument Meme", db.selectCount().from(INSTRUMENT_MEME)
        .join(INSTRUMENT).on(INSTRUMENT.ID.eq(INSTRUMENT_MEME.INSTRUMENT_ID))
        .join(LIBRARY).on(INSTRUMENT.LIBRARY_ID.eq(LIBRARY.ID))
        .where(INSTRUMENT_MEME.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne(0, int.class));

    db.deleteFrom(INSTRUMENT_MEME)
      .where(INSTRUMENT_MEME.ID.eq(id))
      .execute();
  }

  /**
   Only certain (writable) fields are mapped back to jOOQ records--
   Read-only fields are excluded from here.

   @param entity to source values from
   @return values mapped to record fields
   */
  private static Map<Field, Object> fieldValueMap(InstrumentMeme entity) {
    Map<Field, Object> fieldValues = Maps.newHashMap();
    fieldValues.put(INSTRUMENT_MEME.INSTRUMENT_ID, ULong.valueOf(entity.getInstrumentId()));
    fieldValues.put(INSTRUMENT_MEME.NAME, entity.getName());
    return fieldValues;
  }

  @Override
  public InstrumentMeme create(Access access, InstrumentMeme entity) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(create(tx.getContext(), access, entity));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public InstrumentMeme readOne(Access access, BigInteger id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOne(tx.getContext(), access, ULong.valueOf(id)));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Collection<InstrumentMeme> readAll(Access access, Collection<BigInteger> parentIds) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAll(tx.getContext(), access, uLongValuesOf(parentIds)));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void update(Access access, BigInteger id, InstrumentMeme entity) throws Exception {
    throw new BusinessException("Not allowed to update InstrumentMeme record.");
  }

  @Override
  public void destroy(Access access, BigInteger id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      delete(tx.getContext(), access, ULong.valueOf(id));
      tx.success();
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

}
