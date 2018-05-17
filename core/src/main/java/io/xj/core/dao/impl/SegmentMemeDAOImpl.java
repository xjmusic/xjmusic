// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao.impl;

import io.xj.core.access.impl.Access;
import io.xj.core.dao.SegmentMemeDAO;
import io.xj.core.exception.BusinessException;
import io.xj.core.exception.ConfigException;
import io.xj.core.model.segment_meme.SegmentMeme;
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

import static io.xj.core.tables.Chain.CHAIN;
import static io.xj.core.tables.Segment.SEGMENT;
import static io.xj.core.tables.SegmentMeme.SEGMENT_MEME;

/**
 SegmentMeme DAO
 <p>
 future: more specific permissions of user (artist) access by per-entity ownership
 */
public class SegmentMemeDAOImpl extends DAOImpl implements SegmentMemeDAO {

  @Inject
  public SegmentMemeDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  /**
   Create a new Segment Meme record

   @param db     context
   @param access control
   @param entity for new SegmentMeme
   @return new record
   @throws Exception         if database failure
   @throws ConfigException   if not configured properly
   @throws BusinessException if fails business rule
   */
  private static SegmentMeme createRecord(DSLContext db, Access access, SegmentMeme entity) throws Exception {
    entity.validate();

    Map<Field, Object> fieldValues = fieldValueMap(entity);

    if (access.isTopLevel())
      requireExists("Segment", db.selectCount().from(SEGMENT)
        .where(SEGMENT.ID.eq(ULong.valueOf(entity.getSegmentId())))
        .fetchOne());
    else
      requireExists("Segment", db.selectCount().from(SEGMENT)
        .join(CHAIN).on(SEGMENT.CHAIN_ID.eq(CHAIN.ID))
        .where(SEGMENT.ID.eq(ULong.valueOf(entity.getSegmentId())))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne());

    requireNotExists("Segment Meme", db.selectCount().from(SEGMENT_MEME)
      .where(SEGMENT_MEME.SEGMENT_ID.eq(ULong.valueOf(entity.getSegmentId())))
      .and(SEGMENT_MEME.NAME.eq(entity.getName()))
      .fetchOne(0, int.class));

    return modelFrom(executeCreate(db, SEGMENT_MEME, fieldValues), SegmentMeme.class);
  }

  /**
   Read one Segment Meme where able

   @param db     context
   @param access control
   @param id     of record
   @return record
   */
  private static SegmentMeme readOne(DSLContext db, Access access, ULong id) throws BusinessException {
    if (access.isTopLevel())
      return modelFrom(db.selectFrom(SEGMENT_MEME)
        .where(SEGMENT_MEME.ID.eq(id))
        .fetchOne(), SegmentMeme.class);
    else
      return modelFrom(db.select(SEGMENT_MEME.fields()).from(SEGMENT_MEME)
        .join(SEGMENT).on(SEGMENT.ID.eq(SEGMENT_MEME.SEGMENT_ID))
        .join(CHAIN).on(SEGMENT.CHAIN_ID.eq(CHAIN.ID))
        .where(SEGMENT_MEME.ID.eq(id))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne(), SegmentMeme.class);
  }

  /**
   Read all Memes of an Segment where able

   @param db     context
   @param access control
   @param segmentId to readMany memes for
   @return array of segment memes
   */
  private static Collection<SegmentMeme> readAll(DSLContext db, Access access, Collection<ULong> segmentId) throws BusinessException {
    if (access.isTopLevel())
      return modelsFrom(db.selectFrom(SEGMENT_MEME)
        .where(SEGMENT_MEME.SEGMENT_ID.in(segmentId))
        .fetch(), SegmentMeme.class);
    else
      return modelsFrom(db.select(SEGMENT_MEME.fields()).from(SEGMENT_MEME)
        .join(SEGMENT).on(SEGMENT.ID.eq(SEGMENT_MEME.SEGMENT_ID))
        .join(CHAIN).on(SEGMENT.CHAIN_ID.eq(CHAIN.ID))
        .where(SEGMENT.ID.in(segmentId))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccountIds()))
        .fetch(), SegmentMeme.class);
  }

  /**
   Read all records in parent records by ids

   @param db      context
   @param access  control
   @param segmentIds id of parent's parent (the chain)
   @return array of records
   */
  private static Collection<SegmentMeme> readAllInSegments(DSLContext db, Access access, Collection<ULong> segmentIds) throws Exception {
    requireAccessToSegments(db, access, segmentIds);

    return modelsFrom(db.select(SEGMENT_MEME.fields()).from(SEGMENT_MEME)
      .join(SEGMENT).on(SEGMENT.ID.eq(SEGMENT_MEME.SEGMENT_ID))
      .where(SEGMENT.ID.in(segmentIds))
      .fetch(), SegmentMeme.class);
  }

  /**
   Delete an SegmentMeme record

   @param db     context
   @param access control
   @param id     to delete
   @throws BusinessException if failure
   */
  private static void delete(DSLContext db, Access access, ULong id) throws BusinessException {
    if (!access.isTopLevel())
      requireExists("Segment Meme", db.selectCount().from(SEGMENT_MEME)
        .join(SEGMENT).on(SEGMENT.ID.eq(SEGMENT_MEME.SEGMENT_ID))
        .join(CHAIN).on(SEGMENT.CHAIN_ID.eq(CHAIN.ID))
        .where(SEGMENT_MEME.ID.eq(id))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne(0, int.class));

    db.deleteFrom(SEGMENT_MEME)
      .where(SEGMENT_MEME.ID.eq(id))
      .execute();
  }

  /**
   Only certain (writable) fields are mapped back to jOOQ records--
   Read-only fields are excluded from here.

   @param entity to source values from
   @return values mapped to record fields
   */
  private static Map<Field, Object> fieldValueMap(SegmentMeme entity) {
    Map<Field, Object> fieldValues = Maps.newHashMap();
    fieldValues.put(SEGMENT_MEME.SEGMENT_ID, entity.getSegmentId());
    fieldValues.put(SEGMENT_MEME.NAME, entity.getName());
    return fieldValues;
  }

  @Override
  public SegmentMeme create(Access access, SegmentMeme entity) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(createRecord(tx.getContext(), access, entity));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public SegmentMeme readOne(Access access, BigInteger id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOne(tx.getContext(), access, ULong.valueOf(id)));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Collection<SegmentMeme> readAll(Access access, Collection<BigInteger> parentIds) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAll(tx.getContext(), access, uLongValuesOf(parentIds)));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void update(Access access, BigInteger id, SegmentMeme entity) throws Exception {
    throw new BusinessException("Not allowed to update SegmentMeme record.");
  }

  @Override
  public Collection<SegmentMeme> readAllInSegments(Access access, Collection<BigInteger> segmentIds) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAllInSegments(tx.getContext(), access, idCollection(segmentIds)));
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
