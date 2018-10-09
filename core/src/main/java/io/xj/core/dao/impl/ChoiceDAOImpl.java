// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao.impl;

import com.google.api.client.util.Maps;
import com.google.inject.Inject;
import io.xj.core.access.impl.Access;
import io.xj.core.dao.ChoiceDAO;
import io.xj.core.exception.BusinessException;
import io.xj.core.exception.ConfigException;
import io.xj.core.model.choice.Choice;
import io.xj.core.model.sequence.SequenceType;
import io.xj.core.persistence.sql.SQLDatabaseProvider;
import io.xj.core.persistence.sql.impl.SQLConnection;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.SelectOffsetStep;
import org.jooq.types.ULong;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import static io.xj.core.Tables.CHOICE;
import static io.xj.core.Tables.PATTERN;
import static io.xj.core.Tables.SEQUENCE_PATTERN;
import static io.xj.core.tables.Arrangement.ARRANGEMENT;
import static io.xj.core.tables.Chain.CHAIN;
import static io.xj.core.tables.Segment.SEGMENT;
import static org.jooq.impl.DSL.groupConcatDistinct;

public class ChoiceDAOImpl extends DAOImpl implements ChoiceDAO {
  private static final String KEY_AVAILABLE_PATTERN_OFFSETS = "available_pattern_offsets";

  @Inject
  public ChoiceDAOImpl(
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
  private static Choice create(DSLContext db, Access access, Choice entity) throws BusinessException {
    entity.validate();

    Map<Field, Object> fieldValues = fieldValueMap(entity);

    requireTopLevel(access);

    requireExists("Segment", db.selectCount().from(SEGMENT)
      .where(SEGMENT.ID.eq(ULong.valueOf(entity.getSegmentId())))
      .fetchOne(0, int.class));

    return modelFrom(executeCreate(db, CHOICE, fieldValues), Choice.class);
  }

  /**
   Read one record

   @param db     context
   @param access control
   @param id     of record
   @return record
   */
  private static Choice readOne(DSLContext db, Access access, ULong id) throws BusinessException {
    if (access.isTopLevel())
      return modelFrom(db.selectFrom(CHOICE)
        .where(CHOICE.ID.eq(id))
        .fetchOne(), Choice.class);
    else
      return modelFrom(db.select(CHOICE.fields())
        .from(CHOICE)
        .join(SEGMENT).on(SEGMENT.ID.eq(CHOICE.SEGMENT_ID))
        .join(CHAIN).on(CHAIN.ID.eq(SEGMENT.CHAIN_ID))
        .where(CHOICE.ID.eq(id))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne(), Choice.class);
  }

  /**
   Read one record binding an sequence to a segment

   @param db         context
   @param access     control
   @param segmentId  to get choice for
   @param sequenceId to get choice for
   @return record
   */
  private static Choice readOneSegmentSequence(DSLContext db, Access access, ULong segmentId, ULong sequenceId) throws BusinessException {
    requireTopLevel(access);
    return modelFrom(db.selectFrom(CHOICE)
      .where(CHOICE.SEGMENT_ID.eq(segmentId))
      .and(CHOICE.SEQUENCE_ID.eq(sequenceId))
      .fetchOne(), Choice.class);
  }

  /**
   Read one of a specified type from a segment, including its available sequence-pattern offsets
   <p>
   [#157921430] Artist wants to define custom Sequence-Pattern mapping,
   in which patterns are repeated and/or alternated between probabilistically
   during the choice of any given main sequence.

   @param db           context
   @param access       control
   @param segmentId    of record
   @param sequenceType of which to read one segment with available offsets
   @return record
   */
  private static Choice readOneSegmentTypeWithAvailablePatternOffsets(DSLContext db, Access access, ULong segmentId, SequenceType sequenceType) throws BusinessException {
    requireTopLevel(access);

    SelectOffsetStep<?> query = db.select(
      CHOICE.ID,
      CHOICE.SEQUENCE_ID,
      CHOICE.SEGMENT_ID,
      CHOICE.TYPE,
      CHOICE.SEQUENCE_PATTERN_OFFSET,
      CHOICE.TRANSPOSE,
      groupConcatDistinct(SEQUENCE_PATTERN.OFFSET).as(KEY_AVAILABLE_PATTERN_OFFSETS)
    )
      .from(PATTERN)
      .join(CHOICE).on(CHOICE.SEQUENCE_ID.eq(PATTERN.SEQUENCE_ID))
      .leftJoin(SEQUENCE_PATTERN).on(CHOICE.SEQUENCE_ID.eq(SEQUENCE_PATTERN.SEQUENCE_ID))
      .where(CHOICE.SEGMENT_ID.eq(segmentId))
      .and(CHOICE.TYPE.eq(sequenceType.toString()))
      .groupBy(CHOICE.ID, CHOICE.SEGMENT_ID, CHOICE.SEQUENCE_ID, CHOICE.SEQUENCE_PATTERN_OFFSET, CHOICE.TRANSPOSE)
      .limit(1);

    Record record = query.fetchOne();
    Choice model = modelFrom(record, Choice.class);
    if (Objects.nonNull(model)) {
      model.setAvailablePatternOffsets((String) record.get(KEY_AVAILABLE_PATTERN_OFFSETS));
    }

    return model;
  }

  /**
   Read all records in parent by id

   @param db         context
   @param access     control
   @param segmentIds of parent
   @return array of records
   */
  private static Collection<Choice> readAll(DSLContext db, Access access, Collection<ULong> segmentIds) throws BusinessException {
    if (access.isTopLevel())
      return modelsFrom(db.select(CHOICE.fields()).from(CHOICE)
        .where(CHOICE.SEGMENT_ID.in(segmentIds))
        .fetch(), Choice.class);
    else
      return modelsFrom(db.select(CHOICE.fields()).from(CHOICE)
        .join(SEGMENT).on(SEGMENT.ID.eq(CHOICE.SEGMENT_ID))
        .join(CHAIN).on(CHAIN.ID.eq(SEGMENT.CHAIN_ID))
        .where(CHOICE.SEGMENT_ID.in(segmentIds))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccountIds()))
        .fetch(), Choice.class);
  }

  /**
   Read all records in parent records by ids

   @param db         context
   @param access     control
   @param segmentIds id of parent's parent (the chain)
   @return array of records
   */
  private static Collection<Choice> readAllInSegments(DSLContext db, Access access, Collection<ULong> segmentIds) throws Exception {
    requireAccessToSegments(db, access, segmentIds);

    return modelsFrom(db.select(CHOICE.fields()).from(CHOICE)
      .where(CHOICE.SEGMENT_ID.in(segmentIds))
      .orderBy(CHOICE.TYPE)
      .fetch(), Choice.class);
  }

  /**
   Update a record

   @param db     context
   @param access control
   @param id     of record
   @param entity to update with
   @throws BusinessException if a Business Rule is violated
   */
  private static void update(DSLContext db, Access access, ULong id, Choice entity) throws BusinessException {
    entity.validate();

    Map<Field, Object> fieldValues = fieldValueMap(entity);
    fieldValues.put(CHOICE.ID, id);

    requireTopLevel(access);

    requireExists("existing Choice with immutable Segment membership",
      db.selectCount().from(CHOICE)
        .where(CHOICE.ID.eq(id))
        .and(CHOICE.SEGMENT_ID.eq(ULong.valueOf(entity.getSegmentId())))
        .fetchOne(0, int.class));

    if (0 == executeUpdate(db, CHOICE, fieldValues))
      throw new BusinessException("No records updated.");
  }

  /**
   Delete a Choice

   @param db     context
   @param access control
   @param id     to delete
   @throws Exception         if database failure
   @throws ConfigException   if not configured properly
   @throws BusinessException if fails business rule
   */
  private static void delete(DSLContext db, Access access, ULong id) throws Exception {
    requireTopLevel(access);

    requireExists("Choice", db.selectCount().from(CHOICE)
      .where(CHOICE.ID.eq(id))
      .fetchOne(0, int.class));

    requireNotExists("Arrangement in Choice", db.selectCount().from(ARRANGEMENT)
      .where(ARRANGEMENT.CHOICE_ID.eq(id))
      .fetchOne(0, int.class));

    db.deleteFrom(CHOICE)
      .where(CHOICE.ID.eq(id))
      .andNotExists(
        db.select(ARRANGEMENT.ID)
          .from(ARRANGEMENT)
          .where(ARRANGEMENT.CHOICE_ID.eq(id))
      )
      .execute();
  }

  /**
   Only certain (writable) fields are mapped back to jOOQ records--
   Read-only fields are excluded from here.

   @param entity to source values from
   @return values mapped to record fields
   */
  private static Map<Field, Object> fieldValueMap(Choice entity) {
    Map<Field, Object> fieldValues = Maps.newHashMap();
    fieldValues.put(CHOICE.SEGMENT_ID, entity.getSegmentId());
    fieldValues.put(CHOICE.SEQUENCE_ID, entity.getSequenceId());
    fieldValues.put(CHOICE.TYPE, entity.getType());
    fieldValues.put(CHOICE.TRANSPOSE, entity.getTranspose());
    fieldValues.put(CHOICE.SEQUENCE_PATTERN_OFFSET, entity.getSequencePatternOffset());
    return fieldValues;
  }

  @Override
  public Choice create(Access access, Choice entity) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(create(tx.getContext(), access, entity));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public Choice readOne(Access access, BigInteger id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOne(tx.getContext(), access, ULong.valueOf(id)));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Nullable
  @Override
  public Choice readOneSegmentSequence(Access access, BigInteger segmentId, BigInteger sequenceId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOneSegmentSequence(tx.getContext(), access, ULong.valueOf(segmentId), ULong.valueOf(sequenceId)));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public Choice readOneSegmentTypeWithAvailablePatternOffsets(Access access, BigInteger segmentId, SequenceType sequenceType) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOneSegmentTypeWithAvailablePatternOffsets(tx.getContext(), access, ULong.valueOf(segmentId), sequenceType));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public Collection<Choice> readAll(Access access, Collection<BigInteger> parentIds) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAll(tx.getContext(), access, uLongValuesOf(parentIds)));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Collection<Choice> readAllInSegments(Access access, Collection<BigInteger> segmentIds) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAllInSegments(tx.getContext(), access, idCollection(segmentIds)));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void update(Access access, BigInteger id, Choice entity) throws Exception {
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
