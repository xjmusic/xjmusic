//  Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao.impl;

import com.google.api.client.util.Maps;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import io.xj.core.access.impl.Access;
import io.xj.core.dao.SequencePatternDAO;
import io.xj.core.exception.CoreException;
import io.xj.core.model.pattern.PatternState;
import io.xj.core.model.sequence.SequenceType;
import io.xj.core.model.sequence_pattern.SequencePattern;
import io.xj.core.persistence.sql.SQLDatabaseProvider;
import io.xj.core.persistence.sql.impl.SQLConnection;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.types.ULong;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import static io.xj.core.tables.Library.LIBRARY;
import static io.xj.core.tables.Pattern.PATTERN;
import static io.xj.core.tables.Sequence.SEQUENCE;
import static io.xj.core.tables.SequencePattern.SEQUENCE_PATTERN;
import static io.xj.core.tables.SequencePatternMeme.SEQUENCE_PATTERN_MEME;

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
   @throws CoreException if database failure
   @throws CoreException if not configured properly
   @throws CoreException if fails business rule
   */
  private static SequencePattern create(DSLContext db, Access access, SequencePattern entity) throws CoreException {
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
  private static SequencePattern readOne(DSLContext db, Access access, ULong id) throws CoreException {
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
  private static Collection<SequencePattern> readAll(DSLContext db, Access access, Collection<ULong> sequenceIds) throws CoreException {
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
   Read all sequence-patterns at a particular sequence offset
   <p>
   If the pattern is a macro or main type, sequence_pattern relations are mandatory
   otherwise sequence_pattern relations are ignored
   <p>
   [#161076729] Artist wants rhythm patterns to require no sequence-pattern bindings, to keep things simple

   @param db                    context
   @param access                control
   @param sequenceId            of sequence in which to read pattern
   @param sequencePatternOffset of pattern in sequence
   @return pattern record
   */
  private static Collection<SequencePattern> readAllAtSequenceOffset(DSLContext db, Access access, BigInteger sequenceId, BigInteger sequencePatternOffset) throws CoreException {
    String type = db.select(SEQUENCE.TYPE)
      .from(SEQUENCE)
      .where(SEQUENCE.ID.eq(ULong.valueOf(sequenceId)))
      .fetchOne(SEQUENCE.TYPE);
    if (Objects.isNull(type)) {
      throw new CoreException("That sequence does not exist");
    }
    SequenceType sequenceType = SequenceType.validate(type);
    Collection<SequencePattern> sequencePatterns = Lists.newArrayList();
    switch (sequenceType) {
      case Macro:
      case Main:
        if (access.isTopLevel())
          sequencePatterns = modelsFrom(db.select(SEQUENCE_PATTERN.fields())
            .from(SEQUENCE_PATTERN)
            .join(PATTERN).on(PATTERN.ID.eq(SEQUENCE_PATTERN.PATTERN_ID))
            .where(SEQUENCE_PATTERN.SEQUENCE_ID.eq(ULong.valueOf(sequenceId)))
            .and(PATTERN.STATE.notEqual(String.valueOf(PatternState.Erase)))
            .and(SEQUENCE_PATTERN.OFFSET.eq(ULong.valueOf(sequencePatternOffset)))
            .fetch(), SequencePattern.class);
        else
          sequencePatterns = modelsFrom(db.select(SEQUENCE_PATTERN.fields())
            .from(SEQUENCE_PATTERN)
            .join(PATTERN).on(PATTERN.ID.eq(SEQUENCE_PATTERN.PATTERN_ID))
            .join(SEQUENCE).on(SEQUENCE.ID.eq(PATTERN.SEQUENCE_ID))
            .join(LIBRARY).on(LIBRARY.ID.eq(SEQUENCE.LIBRARY_ID))
            .where(PATTERN.SEQUENCE_ID.eq(ULong.valueOf(sequenceId)))
            .and(PATTERN.STATE.notEqual(String.valueOf(PatternState.Erase)))
            .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
            .and(SEQUENCE_PATTERN.OFFSET.eq(ULong.valueOf(sequencePatternOffset)))
            .fetch(), SequencePattern.class);
        break;
      case Rhythm:
      case Detail:
        if (access.isTopLevel())
          sequencePatterns = modelsFrom(db.select(SEQUENCE_PATTERN.fields())
            .from(SEQUENCE_PATTERN)
            .join(PATTERN).on(PATTERN.ID.eq(SEQUENCE_PATTERN.PATTERN_ID))
            .where(PATTERN.SEQUENCE_ID.eq(ULong.valueOf(sequenceId)))
            .and(PATTERN.STATE.notEqual(String.valueOf(PatternState.Erase)))
            .and(SEQUENCE_PATTERN.OFFSET.eq(ULong.valueOf(sequencePatternOffset)))
            .fetch(), SequencePattern.class);
        else
          sequencePatterns = modelsFrom(db.select(SEQUENCE_PATTERN.fields())
            .from(SEQUENCE_PATTERN)
            .join(PATTERN).on(PATTERN.ID.eq(SEQUENCE_PATTERN.PATTERN_ID))
            .join(SEQUENCE).on(SEQUENCE.ID.eq(PATTERN.SEQUENCE_ID))
            .join(LIBRARY).on(LIBRARY.ID.eq(SEQUENCE.LIBRARY_ID))
            .where(PATTERN.SEQUENCE_ID.eq(ULong.valueOf(sequenceId)))
            .and(PATTERN.STATE.notEqual(String.valueOf(PatternState.Erase)))
            .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
            .and(SEQUENCE_PATTERN.OFFSET.eq(ULong.valueOf(sequencePatternOffset)))
            .fetch(), SequencePattern.class);
        break;
    }
    return sequencePatterns;
  }


  /**
   Delete an SequencePattern record

   @param db     context
   @param access control
   @param id     to delete
   @throws CoreException if failure
   */
  private static void delete(DSLContext db, Access access, ULong id) throws CoreException {
    if (!access.isTopLevel())
      requireExists("Sequence Pattern", db.selectCount().from(SEQUENCE_PATTERN)
        .join(SEQUENCE).on(SEQUENCE.ID.eq(SEQUENCE_PATTERN.SEQUENCE_ID))
        .join(LIBRARY).on(SEQUENCE.LIBRARY_ID.eq(LIBRARY.ID))
        .where(SEQUENCE_PATTERN.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne(0, int.class));

    db.deleteFrom(SEQUENCE_PATTERN_MEME)
      .where(SEQUENCE_PATTERN_MEME.SEQUENCE_PATTERN_ID.eq(id)).execute();

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
  public SequencePattern create(Access access, SequencePattern entity) throws CoreException {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(create(tx.getContext(), access, entity));
    } catch (CoreException e) {
      throw tx.failure(e);
    }
  }

  @Override
  public SequencePattern readOne(Access access, BigInteger id) throws CoreException {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOne(tx.getContext(), access, ULong.valueOf(id)));
    } catch (CoreException e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Collection<SequencePattern> readAll(Access access, Collection<BigInteger> parentIds) throws CoreException {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAll(tx.getContext(), access, uLongValuesOf(parentIds)));
    } catch (CoreException e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void update(Access access, BigInteger id, SequencePattern entity) throws CoreException {
    throw new CoreException("Not allowed to update SequencePattern record.");
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


  @Nullable
  @Override
  public Collection<SequencePattern> readAllAtSequenceOffset(Access access, BigInteger sequenceId, BigInteger sequencePatternOffset) throws CoreException {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAllAtSequenceOffset(tx.getContext(), access, sequenceId, sequencePatternOffset));
    } catch (CoreException e) {
      throw tx.failure(e);
    }
  }

}
