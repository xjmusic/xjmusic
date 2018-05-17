// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao.impl;

import io.xj.core.access.impl.Access;
import io.xj.core.dao.SegmentMessageDAO;
import io.xj.core.exception.BusinessException;
import io.xj.core.exception.ConfigException;
import io.xj.core.model.segment_message.SegmentMessage;
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

import static io.xj.core.Tables.SEGMENT;
import static io.xj.core.Tables.SEGMENT_MESSAGE;
import static io.xj.core.tables.Chain.CHAIN;

public class SegmentMessageDAOImpl extends DAOImpl implements SegmentMessageDAO {

  @Inject
  public SegmentMessageDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  /**
   Create a record

   @param db     context
   @param access control
   @param entity for new record
   @return newly readMany record
   @throws BusinessException on failure
   */
  private static SegmentMessage create(DSLContext db, Access access, SegmentMessage entity) throws BusinessException {
    entity.validate();

    Map<Field, Object> fieldValues = fieldValueMap(entity);

    requireTopLevel(access);
    requireExists("Segment",
      db.selectCount().from(SEGMENT)
        .where(SEGMENT.ID.eq(ULong.valueOf(entity.getSegmentId())))
        .fetchOne(0, int.class));

    return modelFrom(executeCreate(db, SEGMENT_MESSAGE, fieldValues), SegmentMessage.class);
  }

  /**
   Read one record

   @param db     context
   @param access control
   @param id     of record
   @return record
   */
  @Nullable
  private static SegmentMessage readOne(DSLContext db, Access access, ULong id) throws Exception {
    if (access.isTopLevel())
      return modelFrom(db.selectFrom(SEGMENT_MESSAGE)
        .where(SEGMENT_MESSAGE.ID.eq(id))
        .fetchOne(), SegmentMessage.class);
    else
      return modelFrom(db.select(SEGMENT_MESSAGE.fields())
        .from(SEGMENT_MESSAGE)
        .join(SEGMENT).on(SEGMENT.ID.eq(SEGMENT_MESSAGE.SEGMENT_ID))
        .join(CHAIN).on(CHAIN.ID.eq(SEGMENT.CHAIN_ID))
        .where(SEGMENT_MESSAGE.ID.eq(id))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne(), SegmentMessage.class);
  }

  /**
   Read all records in parent record by id

   @param db      context
   @param access  control
   @param segmentIds of parent
   @return array of records
   */
  private static Collection<SegmentMessage> readAll(DSLContext db, Access access, Collection<ULong> segmentIds) throws Exception {
    requireAccessToSegments(db, access, segmentIds);

    return modelsFrom(db.select(SEGMENT_MESSAGE.fields())
      .from(SEGMENT_MESSAGE)
      .where(SEGMENT_MESSAGE.SEGMENT_ID.in(segmentIds))
      .orderBy(SEGMENT_MESSAGE.TYPE)
      .fetch(), SegmentMessage.class);
  }

  /**
   Delete an SegmentMessage

   @param db context
   @param id to delete
   @throws Exception         if database failure
   @throws ConfigException   if not configured properly
   @throws BusinessException if fails business rule
   */
  private static void delete(DSLContext db, Access access, ULong id) throws Exception {
    requireTopLevel(access);

    requireExists("SegmentMessage", db.selectCount().from(SEGMENT_MESSAGE)
      .where(SEGMENT_MESSAGE.ID.eq(id))
      .fetchOne(0, int.class));

    db.deleteFrom(SEGMENT_MESSAGE)
      .where(SEGMENT_MESSAGE.ID.eq(id))
      .execute();
  }

  /**
   Only certain (writable) fields are mapped back to jOOQ records--
   Read-only fields are excluded from here.

   @param entity to source values from
   @return values mapped to record fields
   */
  private static Map<Field, Object> fieldValueMap(SegmentMessage entity) {
    Map<Field, Object> fieldValues = Maps.newHashMap();
    fieldValues.put(SEGMENT_MESSAGE.SEGMENT_ID, ULong.valueOf(entity.getSegmentId()));
    fieldValues.put(SEGMENT_MESSAGE.BODY, entity.getBody());
    fieldValues.put(SEGMENT_MESSAGE.TYPE, entity.getType());
    return fieldValues;
  }

  @Override
  public SegmentMessage create(Access access, SegmentMessage entity) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(create(tx.getContext(), access, entity));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public SegmentMessage readOne(Access access, BigInteger id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOne(tx.getContext(), access, ULong.valueOf(id)));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public Collection<SegmentMessage> readAll(Access access, Collection<BigInteger> parentIds) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAll(tx.getContext(), access, uLongValuesOf(parentIds)));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void update(Access access, BigInteger id, SegmentMessage entity) throws Exception {
    throw new BusinessException("Not allowed to update SegmentMessage record.");
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
