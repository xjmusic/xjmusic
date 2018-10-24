//  Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao.impl;

import com.google.api.client.util.Maps;
import com.google.inject.Inject;
import io.xj.core.access.impl.Access;
import io.xj.core.dao.SequencePatternDAO;
import io.xj.core.exception.BusinessException;
import io.xj.core.exception.ConfigException;
import io.xj.core.model.sequence_pattern.SequencePattern;
import io.xj.core.persistence.sql.SQLDatabaseProvider;
import io.xj.core.persistence.sql.impl.SQLConnection;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.types.ULong;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Map;

import static io.xj.core.Tables.PATTERN;
import static io.xj.core.tables.Library.LIBRARY;
import static io.xj.core.tables.Sequence.SEQUENCE;
import static io.xj.core.tables.SequencePattern.SEQUENCE_PATTERN;

/**
 SequencePattern DAO
 <p>
 future: more specific permissions of user (artist) access by per-entity ownership
 */
public class SequencePatternDAOImpl extends DAOImpl implements SequencePatternDAO {

  @Inject
  public SequencePatternDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  /**
   Create a new Sequence Pattern record

   @param db     context
   @param access control
   @param entity for new SequencePattern
   @return new record
   @throws Exception         if database failure
   @throws ConfigException   if not configured properly
   @throws BusinessException if fails business rule
   */
  private static SequencePattern create(DSLContext db, Access access, SequencePattern entity) throws Exception {
    entity.validate();

    Map<Field, Object> fieldValues = fieldValueMap(entity);

    if (access.isTopLevel())
      requireExists("Sequence", db.selectCount().from(SEQUENCE)
        .where(SEQUENCE.ID.eq(ULong.valueOf(entity.getSequenceId())))
        .fetchOne(0, int.class));
    else
      requireExists("Sequence", db.selectCount().from(SEQUENCE)
        .join(LIBRARY).on(SEQUENCE.LIBRARY_ID.eq(LIBRARY.ID))
        .where(SEQUENCE.ID.eq(ULong.valueOf(entity.getSequenceId())))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne(0, int.class));

    if (access.isTopLevel())
      requireExists("Pattern", db.selectCount().from(PATTERN)
        .where(PATTERN.ID.eq(ULong.valueOf(entity.getPatternId())))
        .fetchOne(0, int.class));
    else
      requireExists("Pattern", db.selectCount().from(PATTERN)
        .join(SEQUENCE).on(PATTERN.SEQUENCE_ID.eq(SEQUENCE.ID))
        .join(LIBRARY).on(SEQUENCE.LIBRARY_ID.eq(LIBRARY.ID))
        .where(PATTERN.ID.eq(ULong.valueOf(entity.getPatternId())))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne(0, int.class));

    return modelFrom(executeCreate(db, SEQUENCE_PATTERN, fieldValues), SequencePattern.class);
  }

  /**
   Read one Sequence Pattern where able

   @param db     context
   @param access control
   @param id     of record
   @return record
   */
  private static SequencePattern readOne(DSLContext db, Access access, ULong id) throws BusinessException {
    if (access.isTopLevel())
      return modelFrom(db.selectFrom(SEQUENCE_PATTERN)
        .where(SEQUENCE_PATTERN.ID.eq(id))
        .fetchOne(), SequencePattern.class);
    else
      return modelFrom(db.select(SEQUENCE_PATTERN.fields()).from(SEQUENCE_PATTERN)
        .join(SEQUENCE).on(SEQUENCE.ID.eq(SEQUENCE_PATTERN.SEQUENCE_ID))
        .join(LIBRARY).on(SEQUENCE.LIBRARY_ID.eq(LIBRARY.ID))
        .where(SEQUENCE_PATTERN.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne(), SequencePattern.class);
  }

  /**
   Read all Patterns of an Sequence where able

   @param db          context
   @param access      control
   @param sequenceIds to readMany patterns for
   @return array of sequence patterns
   */
  private static Collection<SequencePattern> readAll(DSLContext db, Access access, Collection<ULong> sequenceIds) throws BusinessException {
    if (access.isTopLevel())
      return modelsFrom(db.selectFrom(SEQUENCE_PATTERN)
        .where(SEQUENCE_PATTERN.SEQUENCE_ID.in(sequenceIds))
        .orderBy(SEQUENCE_PATTERN.OFFSET.asc())
        .fetch(), SequencePattern.class);
    else
      return modelsFrom(db.select(SEQUENCE_PATTERN.fields()).from(SEQUENCE_PATTERN)
        .join(SEQUENCE).on(SEQUENCE.ID.eq(SEQUENCE_PATTERN.SEQUENCE_ID))
        .join(LIBRARY).on(SEQUENCE.LIBRARY_ID.eq(LIBRARY.ID))
        .where(SEQUENCE.ID.in(sequenceIds))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .orderBy(SEQUENCE_PATTERN.OFFSET.asc())
        .fetch(), SequencePattern.class);
  }

  /**
   Delete an SequencePattern record

   @param db     context
   @param access control
   @param id     to delete
   @throws BusinessException if failure
   */
  private static void delete(DSLContext db, Access access, ULong id) throws BusinessException {
    if (!access.isTopLevel())
      requireExists("Sequence Pattern", db.selectCount().from(SEQUENCE_PATTERN)
        .join(SEQUENCE).on(SEQUENCE.ID.eq(SEQUENCE_PATTERN.SEQUENCE_ID))
        .join(LIBRARY).on(SEQUENCE.LIBRARY_ID.eq(LIBRARY.ID))
        .where(SEQUENCE_PATTERN.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne(0, int.class));

    db.deleteFrom(SEQUENCE_PATTERN)
      .where(SEQUENCE_PATTERN.ID.eq(id))
      .execute();
  }

  /**
   Only certain (writable) fields are mapped back to jOOQ records--
   Read-only fields are excluded from here.

   @param entity to source values from
   @return values mapped to record fields
   */
  private static Map<Field, Object> fieldValueMap(SequencePattern entity) {
    Map<Field, Object> fieldValues = Maps.newHashMap();
    fieldValues.put(SEQUENCE_PATTERN.SEQUENCE_ID, ULong.valueOf(entity.getSequenceId()));
    fieldValues.put(SEQUENCE_PATTERN.PATTERN_ID, ULong.valueOf(entity.getPatternId()));
    fieldValues.put(SEQUENCE_PATTERN.OFFSET, ULong.valueOf(entity.getOffset()));
    return fieldValues;
  }

  @Override
  public SequencePattern create(Access access, SequencePattern entity) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(create(tx.getContext(), access, entity));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public SequencePattern readOne(Access access, BigInteger id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOne(tx.getContext(), access, ULong.valueOf(id)));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Collection<SequencePattern> readAll(Access access, Collection<BigInteger> parentIds) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAll(tx.getContext(), access, uLongValuesOf(parentIds)));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void update(Access access, BigInteger id, SequencePattern entity) throws Exception {
    throw new BusinessException("Not allowed to update SequencePattern record.");
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
