// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao.impl;

import io.xj.core.access.impl.Access;
import io.xj.core.dao.SegmentChordDAO;
import io.xj.core.exception.BusinessException;
import io.xj.core.exception.ConfigException;
import io.xj.core.model.segment_chord.SegmentChord;
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
import static io.xj.core.Tables.SEGMENT_CHORD;
import static io.xj.core.tables.Chain.CHAIN;

public class SegmentChordDAOImpl extends DAOImpl implements SegmentChordDAO {

  @Inject
  public SegmentChordDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  /**
   Create a new Segment Chord

   @param db     context
   @param access control
   @param entity for new segment
   @return newly readMany record
   @throws BusinessException if failure
   */
  private static SegmentChord createRecord(DSLContext db, Access access, SegmentChord entity) throws BusinessException {
    entity.validate();

    Map<Field, Object> fieldValues = fieldValueMap(entity);

    requireTopLevel(access);

    requireExists("Segment", db.selectCount().from(SEGMENT)
      .where(SEGMENT.ID.eq(ULong.valueOf(entity.getSegmentId())))
      .fetchOne(0, int.class));

    return modelFrom(executeCreate(db, SEGMENT_CHORD, fieldValues), SegmentChord.class);
  }

  /**
   Read one Chord if able

   @param db     context
   @param access control
   @param id     of segment
   @return segment
   */
  private static SegmentChord readOne(DSLContext db, Access access, ULong id) throws BusinessException {
    if (access.isTopLevel())
      return modelFrom(db.selectFrom(SEGMENT_CHORD)
        .where(SEGMENT_CHORD.ID.eq(id))
        .fetchOne(), SegmentChord.class);
    else
      return modelFrom(db.select(SEGMENT_CHORD.fields())
        .from(SEGMENT_CHORD)
        .join(SEGMENT).on(SEGMENT.ID.eq(SEGMENT_CHORD.SEGMENT_ID))
        .join(CHAIN).on(CHAIN.ID.eq(SEGMENT.CHAIN_ID))
        .where(SEGMENT_CHORD.ID.eq(id))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne(), SegmentChord.class);
  }

  /**
   Read all Chord able for an Chain

   @param db     context
   @param access control
   @param segmentId to readMany all segment of
   @return array of segments
   */
  private static Collection<SegmentChord> readAll(DSLContext db, Access access, Collection<ULong> segmentId) throws BusinessException {
    if (access.isTopLevel())
      return modelsFrom(db.select(SEGMENT_CHORD.fields()).from(SEGMENT_CHORD)
        .where(SEGMENT_CHORD.SEGMENT_ID.in(segmentId))
        .orderBy(SEGMENT_CHORD.POSITION)
        .fetch(), SegmentChord.class);
    else
      return modelsFrom(db.select(SEGMENT_CHORD.fields()).from(SEGMENT_CHORD)
        .join(SEGMENT).on(SEGMENT.ID.eq(SEGMENT_CHORD.SEGMENT_ID))
        .join(CHAIN).on(CHAIN.ID.eq(SEGMENT.CHAIN_ID))
        .where(SEGMENT_CHORD.SEGMENT_ID.in(segmentId))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccountIds()))
        .orderBy(SEGMENT_CHORD.POSITION)
        .fetch(), SegmentChord.class);
  }

  /**
   Read all records in parent records by ids
   order by position ascending

   @param db      context
   @param access  control
   @param segmentIds id of parent's parent (the chain)
   @return array of records
   */
  private static Collection<SegmentChord> readAllInSegments(DSLContext db, Access access, Collection<ULong> segmentIds) throws Exception {
    requireAccessToSegments(db, access, segmentIds);

    return modelsFrom(db.select(SEGMENT_CHORD.fields()).from(SEGMENT_CHORD)
      .join(SEGMENT).on(SEGMENT.ID.eq(SEGMENT_CHORD.SEGMENT_ID))
      .where(SEGMENT.ID.in(segmentIds))
      .orderBy(SEGMENT_CHORD.POSITION.desc())
      .fetch(), SegmentChord.class);
  }

  /**
   Update a Chord record

   @param db     context
   @param access control
   @param id     to update
   @param entity to update with
   @throws BusinessException if failure
   */
  private static void update(DSLContext db, Access access, ULong id, SegmentChord entity) throws BusinessException {
    entity.validate();

    Map<Field, Object> fieldValues = fieldValueMap(entity);
    fieldValues.put(SEGMENT_CHORD.ID, id);

    requireTopLevel(access);

    requireExists("existing SegmentChord with immutable Segment membership",
      db.selectCount().from(SEGMENT_CHORD)
        .where(SEGMENT_CHORD.ID.eq(id))
        .and(SEGMENT_CHORD.SEGMENT_ID.eq(ULong.valueOf(entity.getSegmentId())))
        .fetchOne(0, int.class));

    if (0 == executeUpdate(db, SEGMENT_CHORD, fieldValues))
      throw new BusinessException("No records updated.");
  }

  /**
   Delete an Chord

   @param db context
   @param id to delete
   @throws Exception         if database failure
   @throws ConfigException   if not configured properly
   @throws BusinessException if fails business rule
   */
  private static void delete(Access access, DSLContext db, ULong id) throws Exception {
    requireTopLevel(access);

    db.deleteFrom(SEGMENT_CHORD)
      .where(SEGMENT_CHORD.ID.eq(id))
      .execute();
  }

  /**
   Only certain (writable) fields are mapped back to jOOQ records--
   Read-only fields are excluded from here.

   @param entity to source values from
   @return values mapped to record fields
   */
  private static Map<Field, Object> fieldValueMap(SegmentChord entity) {
    Map<Field, Object> fieldValues = Maps.newHashMap();
    fieldValues.put(SEGMENT_CHORD.NAME, entity.getName());
    fieldValues.put(SEGMENT_CHORD.SEGMENT_ID, ULong.valueOf(entity.getSegmentId()));
    fieldValues.put(SEGMENT_CHORD.POSITION, entity.getPosition());
    return fieldValues;
  }

  @Override
  public SegmentChord create(Access access, SegmentChord entity) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(createRecord(tx.getContext(), access, entity));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public SegmentChord readOne(Access access, BigInteger id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOne(tx.getContext(), access, ULong.valueOf(id)));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public Collection<SegmentChord> readAll(Access access, Collection<BigInteger> parentIds) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAll(tx.getContext(), access, uLongValuesOf(parentIds)));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Collection<SegmentChord> readAllInSegments(Access access, Collection<BigInteger> segmentIds) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAllInSegments(tx.getContext(), access, idCollection(segmentIds)));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void update(Access access, BigInteger id, SegmentChord entity) throws Exception {
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
      delete(access, tx.getContext(), ULong.valueOf(id));
      tx.success();
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

}
