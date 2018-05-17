// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao.impl;

import io.xj.core.access.impl.Access;
import io.xj.core.dao.PatternChordDAO;
import io.xj.core.exception.BusinessException;
import io.xj.core.exception.ConfigException;
import io.xj.core.model.pattern_chord.PatternChord;
import io.xj.core.persistence.sql.SQLDatabaseProvider;
import io.xj.core.persistence.sql.impl.SQLConnection;
import io.xj.core.tables.Pattern;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.types.ULong;

import com.google.api.client.util.Maps;
import com.google.inject.Inject;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Map;

import static io.xj.core.Tables.PATTERN;
import static io.xj.core.Tables.PATTERN_CHORD;
import static io.xj.core.tables.Library.LIBRARY;
import static io.xj.core.tables.Sequence.SEQUENCE;

public class PatternChordDAOImpl extends DAOImpl implements PatternChordDAO {

  @Inject
  public PatternChordDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  /**
   Create a new Pattern Chord

   @param db     context
   @param access control
   @param entity for new pattern
   @return newly readMany record
   @throws BusinessException if failure
   */
  private static PatternChord createRecord(DSLContext db, Access access, PatternChord entity) throws BusinessException {
    entity.validate();

    Map<Field, Object> fieldValues = fieldValueMap(entity);

    if (access.isTopLevel())
      requireExists("Pattern", db.selectCount().from(PATTERN)
        .where(PATTERN.ID.eq(ULong.valueOf(entity.getPatternId())))
        .fetchOne(0, int.class));
    else
      requireExists("Pattern", db.selectCount().from(PATTERN)
        .join(SEQUENCE).on(SEQUENCE.ID.eq(PATTERN.SEQUENCE_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(SEQUENCE.LIBRARY_ID))
        .where(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .and(PATTERN.ID.eq(ULong.valueOf(entity.getPatternId())))
        .fetchOne(0, int.class));

    return modelFrom(executeCreate(db, PATTERN_CHORD, fieldValues), PatternChord.class);
  }

  /**
   Read one Chord if able

   @param db     context
   @param access control
   @param id     of pattern
   @return pattern
   */
  private static PatternChord readOne(DSLContext db, Access access, ULong id) throws BusinessException {
    if (access.isTopLevel())
      return modelFrom(db.selectFrom(PATTERN_CHORD)
        .where(PATTERN_CHORD.ID.eq(id))
        .fetchOne(), PatternChord.class);
    else
      return modelFrom(db.select(PATTERN_CHORD.fields())
        .from(PATTERN_CHORD)
        .join(PATTERN).on(PATTERN.ID.eq(PATTERN_CHORD.PATTERN_ID))
        .join(SEQUENCE).on(SEQUENCE.ID.eq(PATTERN.SEQUENCE_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(SEQUENCE.LIBRARY_ID))
        .where(PATTERN_CHORD.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne(), PatternChord.class);
  }

  /**
   Read all Chord able for a Pattern

   @param db       context
   @param access   control
   @param patternIds to readMany all pattern of
   @return array of patterns
   */
  private static Collection<PatternChord> readAll(DSLContext db, Access access, Collection<ULong> patternIds) throws BusinessException {
    if (access.isTopLevel())
      return modelsFrom(db.select(PATTERN_CHORD.fields())
        .from(PATTERN_CHORD)
        .where(PATTERN_CHORD.PATTERN_ID.in(patternIds))
        .orderBy(PATTERN_CHORD.POSITION)
        .fetch(), PatternChord.class);
    else
      return modelsFrom(db.select(PATTERN_CHORD.fields())
        .from(PATTERN_CHORD)
        .join(PATTERN).on(PATTERN.ID.eq(PATTERN_CHORD.PATTERN_ID))
        .join(SEQUENCE).on(SEQUENCE.ID.eq(PATTERN.SEQUENCE_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(SEQUENCE.LIBRARY_ID))
        .where(PATTERN_CHORD.PATTERN_ID.in(patternIds))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .orderBy(PATTERN_CHORD.POSITION)
        .fetch(), PatternChord.class);
  }

  /**
   Update a Chord record

   @param db     context
   @param access control
   @param id     to update
   @param entity to update with
   @throws BusinessException if failure
   */
  private static void update(DSLContext db, Access access, BigInteger id, PatternChord entity) throws BusinessException {
    entity.validate();

    Map<Field, Object> fieldValues = fieldValueMap(entity);
    fieldValues.put(PATTERN_CHORD.ID, ULong.valueOf(id));

    if (access.isTopLevel())
      requireExists("Pattern", db.selectCount().from(PATTERN)
        .where(PATTERN.ID.eq(ULong.valueOf(entity.getPatternId())))
        .fetchOne(0, int.class));
    else
      requireExists("Pattern", db.selectCount().from(PATTERN)
        .join(SEQUENCE).on(SEQUENCE.ID.eq(PATTERN.SEQUENCE_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(SEQUENCE.LIBRARY_ID))
        .where(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .and(PATTERN.ID.eq(ULong.valueOf(entity.getPatternId())))
        .fetchOne(0, int.class));

    if (0 == executeUpdate(db, PATTERN_CHORD, fieldValues))
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
  private static void delete(Access access, DSLContext db, BigInteger id) throws Exception {
    if (!access.isTopLevel())
      requireExists("Pattern Chord", db.selectCount().from(PATTERN_CHORD)
        .join(Pattern.PATTERN).on(Pattern.PATTERN.ID.eq(PATTERN_CHORD.PATTERN_ID))
        .join(SEQUENCE).on(SEQUENCE.ID.eq(Pattern.PATTERN.SEQUENCE_ID))
        .join(LIBRARY).on(SEQUENCE.LIBRARY_ID.eq(LIBRARY.ID))
        .where(PATTERN_CHORD.ID.eq(ULong.valueOf(id)))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne(0, int.class));

    db.deleteFrom(PATTERN_CHORD)
      .where(PATTERN_CHORD.ID.eq(ULong.valueOf(id)))
      .execute();
  }

  /**
   Only certain (writable) fields are mapped back to jOOQ records--
   Read-only fields are excluded from here.

   @param entity to source values from
   @return values mapped to record fields
   */
  private static Map<Field, Object> fieldValueMap(PatternChord entity) {
    Map<Field, Object> fieldValues = Maps.newHashMap();
    fieldValues.put(PATTERN_CHORD.NAME, entity.getName());
    fieldValues.put(PATTERN_CHORD.PATTERN_ID, entity.getPatternId());
    fieldValues.put(PATTERN_CHORD.POSITION, entity.getPosition());
    return fieldValues;
  }

  @Override
  public PatternChord create(Access access, PatternChord entity) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(createRecord(tx.getContext(), access, entity));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public PatternChord readOne(Access access, BigInteger id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOne(tx.getContext(), access, ULong.valueOf(id)));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public Collection<PatternChord> readAll(Access access, Collection<BigInteger> parentIds) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAll(tx.getContext(), access, uLongValuesOf(parentIds)));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void update(Access access, BigInteger id, PatternChord entity) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      update(tx.getContext(), access, id, entity);
      tx.success();
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void destroy(Access access, BigInteger id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      delete(access, tx.getContext(), id);
      tx.success();
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

}
