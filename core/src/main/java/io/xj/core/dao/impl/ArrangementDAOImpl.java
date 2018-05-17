// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao.impl;

import io.xj.core.access.impl.Access;
import io.xj.core.dao.ArrangementDAO;
import io.xj.core.exception.BusinessException;
import io.xj.core.exception.ConfigException;
import io.xj.core.model.arrangement.Arrangement;
import io.xj.core.persistence.sql.SQLDatabaseProvider;
import io.xj.core.persistence.sql.impl.SQLConnection;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.types.ULong;

import com.google.api.client.util.Maps;
import com.google.inject.Inject;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Map;

import static io.xj.core.Tables.ARRANGEMENT;
import static io.xj.core.Tables.CHAIN;
import static io.xj.core.Tables.CHOICE;
import static io.xj.core.Tables.SEGMENT;

public class ArrangementDAOImpl extends DAOImpl implements ArrangementDAO {

  @Inject
  public ArrangementDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  /**
   Create a new record

   @param db     context
   @param access control
   @param entity for new record
   @return newly readMany record
   @throws BusinessException if a Business Rule is violated
   */
  private static Arrangement create(DSLContext db, Access access, Arrangement entity) throws BusinessException {
    entity.validate();

    Map<Field, Object> fieldValues = fieldValueMap(entity);

    requireTopLevel(access);

    requireExists("Choice", db.selectCount().from(CHOICE)
      .where(CHOICE.ID.eq(ULong.valueOf(entity.getChoiceId())))
      .fetchOne(0, int.class));

    return modelFrom(executeCreate(db, ARRANGEMENT, fieldValues), Arrangement.class);
  }

  /**
   Read one record

   @param db     context
   @param access control
   @param id     of record
   @return record
   */
  @Nullable
  private static Arrangement readOne(DSLContext db, Access access, ULong id) throws BusinessException {
    if (access.isTopLevel())
      return modelFrom(db.selectFrom(ARRANGEMENT)
        .where(ARRANGEMENT.ID.eq(id))
        .fetchOne(), Arrangement.class);
    else
      return modelFrom(db.select(ARRANGEMENT.fields())
        .from(ARRANGEMENT)
        .join(CHOICE).on(CHOICE.ID.eq(ARRANGEMENT.CHOICE_ID))
        .join(SEGMENT).on(SEGMENT.ID.eq(CHOICE.SEGMENT_ID))
        .join(CHAIN).on(CHAIN.ID.eq(SEGMENT.CHAIN_ID))
        .where(ARRANGEMENT.ID.eq(id))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne(), Arrangement.class);
  }

  /**
   Read all records in parent by id

   @param db        context
   @param access    control
   @param choiceIds of parent
   @return array of records
   */
  private static Collection<Arrangement> readAll(DSLContext db, Access access, Collection<ULong> choiceIds) throws BusinessException {
    if (access.isTopLevel())
      return modelsFrom(db.select(ARRANGEMENT.fields())
        .from(ARRANGEMENT)
        .where(ARRANGEMENT.CHOICE_ID.in(choiceIds))
        .fetch(), Arrangement.class);
    else
      return modelsFrom(db.select(ARRANGEMENT.fields())
        .from(ARRANGEMENT)
        .join(CHOICE).on(CHOICE.ID.eq(ARRANGEMENT.CHOICE_ID))
        .join(SEGMENT).on(SEGMENT.ID.eq(CHOICE.SEGMENT_ID))
        .join(CHAIN).on(CHAIN.ID.eq(SEGMENT.CHAIN_ID))
        .where(ARRANGEMENT.CHOICE_ID.in(choiceIds))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccountIds()))
        .fetch(), Arrangement.class);
  }

  /**
   Read all records in parent records by ids

   @param db      context
   @param access  control
   @param segmentIds id of parent's parent (the chain)
   @return array of records
   */
  private static Collection<Arrangement> readAllInSegments(DSLContext db, Access access, Collection<ULong> segmentIds) throws Exception {
    requireAccessToSegments(db, access, segmentIds);

    return modelsFrom(db.select(ARRANGEMENT.fields())
      .from(ARRANGEMENT)
      .join(CHOICE).on(CHOICE.ID.eq(ARRANGEMENT.CHOICE_ID))
      .where(CHOICE.SEGMENT_ID.in(segmentIds))
      .fetch(), Arrangement.class);
  }

  /**
   Update a record

   @param db     context
   @param access control
   @param id     of record
   @param entity to update with
   */
  private static void update(DSLContext db, Access access, ULong id, Arrangement entity) throws BusinessException {
    entity.validate();

    Map<Field, Object> fieldValues = fieldValueMap(entity);
    fieldValues.put(ARRANGEMENT.ID, id);

    requireTopLevel(access);

    requireExists("existing Arrangement with immutable Choice membership",
      db.selectCount().from(ARRANGEMENT)
        .where(ARRANGEMENT.ID.eq(id))
        .and(ARRANGEMENT.CHOICE_ID.eq(ULong.valueOf(entity.getChoiceId())))
        .fetchOne(0, int.class));

    if (0 == executeUpdate(db, ARRANGEMENT, fieldValues))
      throw new BusinessException("No records updated.");
  }

  /**
   Delete a Arrangement

   @param db     context
   @param access control
   @param id     to delete
   @throws Exception         if database failure
   @throws ConfigException   if not configured properly
   @throws BusinessException if fails business rule
   */
  private static void delete(DSLContext db, Access access, ULong id) throws Exception {
    requireTopLevel(access);

    requireExists("Arrangement", db.selectCount().from(ARRANGEMENT)
      .where(ARRANGEMENT.ID.eq(id))
      .fetchOne(0, int.class));

    db.deleteFrom(ARRANGEMENT)
      .where(ARRANGEMENT.ID.eq(id))
      .execute();
  }

  /**
   Only certain (writable) fields are mapped back to jOOQ records--
   Read-only fields are excluded from here.

   @param entity to source values from
   @return values mapped to record fields
   */
  private static Map<Field, Object> fieldValueMap(Arrangement entity) {
    Map<Field, Object> fieldValues = Maps.newHashMap();
    fieldValues.put(ARRANGEMENT.CHOICE_ID, ULong.valueOf(entity.getChoiceId()));
    fieldValues.put(ARRANGEMENT.VOICE_ID, ULong.valueOf(entity.getVoiceId()));
    fieldValues.put(ARRANGEMENT.INSTRUMENT_ID, ULong.valueOf(entity.getInstrumentId()));
    return fieldValues;
  }

  @Override
  public Arrangement create(Access access, Arrangement entity) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(create(tx.getContext(), access, entity));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public Arrangement readOne(Access access, BigInteger id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOne(tx.getContext(), access, ULong.valueOf(id)));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public Collection<Arrangement> readAll(Access access, Collection<BigInteger> parentIds) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAll(tx.getContext(), access, uLongValuesOf(parentIds)));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Collection<Arrangement> readAllInSegments(Access access, Collection<BigInteger> segmentIds) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAllInSegments(tx.getContext(), access, idCollection(segmentIds)));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void update(Access access, BigInteger id, Arrangement entity) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      update(tx.getContext(), access, ULong.valueOf(id), entity);
      tx.success();
    } catch (Exception e) {
      throw tx.failure(e);
    }
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
