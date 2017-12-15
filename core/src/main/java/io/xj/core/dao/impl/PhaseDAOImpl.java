// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao.impl;

import io.xj.core.access.impl.Access;
import io.xj.core.dao.PhaseDAO;
import io.xj.core.exception.BusinessException;
import io.xj.core.exception.ConfigException;
import io.xj.core.model.pattern.PatternType;
import io.xj.core.model.phase.Phase;
import io.xj.core.persistence.sql.SQLDatabaseProvider;
import io.xj.core.persistence.sql.impl.SQLConnection;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.types.ULong;

import com.google.api.client.util.Maps;
import com.google.inject.Inject;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import static io.xj.core.tables.Library.LIBRARY;
import static io.xj.core.tables.Pattern.PATTERN;
import static io.xj.core.tables.Phase.PHASE;
import static io.xj.core.tables.PhaseChord.PHASE_CHORD;
import static io.xj.core.tables.PhaseMeme.PHASE_MEME;
import static io.xj.core.tables.Voice.VOICE;

public class PhaseDAOImpl extends DAOImpl implements PhaseDAO {

  @Inject
  public PhaseDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public Phase create(Access access, Phase entity) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(create(tx.getContext(), access, entity));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public Phase readOne(Access access, BigInteger id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOne(tx.getContext(), access, id));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Nullable
  @Override
  public Phase readOneForPattern(Access access, BigInteger patternId, BigInteger patternPhaseOffset) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOneForPattern(tx.getContext(), access, patternId, patternPhaseOffset));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public Collection<Phase> readAll(Access access, BigInteger patternId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAll(tx.getContext(), access, patternId));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void update(Access access, BigInteger id, Phase entity) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      update(tx.getContext(), access, ULong.valueOf(id), entity);
      tx.success();
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void delete(Access access, BigInteger id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      delete(access, tx.getContext(), ULong.valueOf(id));
      tx.success();
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  /**
   Create a new Phase

   @param db     context
   @param access control
   @param entity for new phase
   @return newly readMany record
   @throws BusinessException if failure
   */
  private static Phase create(DSLContext db, Access access, Phase entity) throws Exception {
    entity.validate();

    Map<Field, Object> fieldValues = fieldValueMap(entity);

    // [#237] shouldn't be able to create phase with same offset in pattern
    requireNotExists("phase with same offset in pattern",
      db.select(PHASE.ID).from(PHASE)
        .where(PHASE.PATTERN_ID.eq(ULong.valueOf(entity.getPatternId())))
        .and(PHASE.OFFSET.eq(ULong.valueOf(entity.getOffset())))
        .fetch());

    // Common for Create/Update
    deepValidate(db, access, entity);

    return modelFrom(executeCreate(db, PHASE, fieldValues), Phase.class);
  }

  /**
   Read one Phase if able

   @param db     context
   @param access control
   @param id     of phase
   @return phase
   */
  private static Phase readOne(DSLContext db, Access access, BigInteger id) throws BusinessException {
    if (access.isTopLevel())
      return modelFrom(db.selectFrom(PHASE)
        .where(PHASE.ID.eq(ULong.valueOf(id)))
        .fetchOne(), Phase.class);
    else
      return modelFrom(db.select(PHASE.fields())
        .from(PHASE)
        .join(PATTERN).on(PATTERN.ID.eq(PHASE.PATTERN_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(PATTERN.LIBRARY_ID))
        .where(PHASE.ID.eq(ULong.valueOf(id)))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne(), Phase.class);
  }

  /**
   Read one Phase if able

   @param db                 context
   @param access             control
   @param patternId          of pattern in which to read phase
   @param patternPhaseOffset of phase in pattern
   @return phase record
   */
  @Nullable
  private static Phase readOneForPattern(DSLContext db, Access access, BigInteger patternId, BigInteger patternPhaseOffset) throws BusinessException {
    if (access.isTopLevel())
      return modelFrom(db.selectFrom(PHASE)
        .where(PHASE.PATTERN_ID.eq(ULong.valueOf(patternId)))
        .and(PHASE.OFFSET.eq(ULong.valueOf(patternPhaseOffset)))
        .fetchOne(), Phase.class);
    else
      return modelFrom(db.select(PHASE.fields())
        .from(PHASE)
        .join(PATTERN).on(PATTERN.ID.eq(PHASE.PATTERN_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(PATTERN.LIBRARY_ID))
        .where(PHASE.PATTERN_ID.eq(ULong.valueOf(patternId)))
        .and(PHASE.OFFSET.eq(ULong.valueOf(patternPhaseOffset)))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne(), Phase.class);
  }

  /**
   Read all Phase able for an Pattern

   @param db        context
   @param access    control
   @param patternId to readMany all phase of
   @return array of phases
   */
  private static Collection<Phase> readAll(DSLContext db, Access access, BigInteger patternId) throws BusinessException {
    if (access.isTopLevel())
      return modelsFrom(db.select(PHASE.fields())
        .from(PHASE)
        .where(PHASE.PATTERN_ID.eq(ULong.valueOf(patternId)))
        .fetch(), Phase.class);
    else
      return modelsFrom(db.select(PHASE.fields())
        .from(PHASE)
        .join(PATTERN).on(PATTERN.ID.eq(PHASE.PATTERN_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(PATTERN.LIBRARY_ID))
        .where(PHASE.PATTERN_ID.eq(ULong.valueOf(patternId)))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .fetch(), Phase.class);
  }

  /**
   Update a Phase record

   @param db     context
   @param access control
   @param id     to update
   @param entity to update with
   @throws BusinessException if failure
   */
  private static void update(DSLContext db, Access access, ULong id, Phase entity) throws Exception {
    entity.validate();

    Map<Field, Object> fieldValues = fieldValueMap(entity);
    fieldValues.put(PHASE.ID, id);

    // Common for Create/Update
    deepValidate(db, access, entity);

    if (0 == executeUpdate(db, PHASE, fieldValues))
      throw new BusinessException("No records updated.");
  }

  /**
   Delete an Phase

   @param db context
   @param id to delete
   @throws Exception         if database failure
   @throws ConfigException   if not configured properly
   @throws BusinessException if fails business rule
   */
  private static void delete(Access access, DSLContext db, ULong id) throws Exception {
    requireNotExists("Voice in Phase", db.select(VOICE.ID)
      .from(VOICE)
      .where(VOICE.PHASE_ID.eq(id))
      .fetch());

    requireNotExists("Meme in Phase", db.select(PHASE_MEME.ID)
      .from(PHASE_MEME)
      .where(PHASE_MEME.PHASE_ID.eq(id))
      .fetch());

    requireNotExists("Chord in Phase", db.select(PHASE_CHORD.ID)
      .from(PHASE_CHORD)
      .where(PHASE_CHORD.PHASE_ID.eq(id))
      .fetch());

    if (!access.isTopLevel())
      requireExists("Phase", db.selectCount().from(PHASE)
        .join(PATTERN).on(PATTERN.ID.eq(PHASE.PATTERN_ID))
        .join(LIBRARY).on(PATTERN.LIBRARY_ID.eq(LIBRARY.ID))
        .where(PHASE.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne(0, int.class));

    db.deleteFrom(PHASE)
      .where(PHASE.ID.eq(id))
      .andNotExists(
        db.select(VOICE.ID)
          .from(VOICE)
          .where(VOICE.PHASE_ID.eq(id))
      )
      .andNotExists(
        db.select(PHASE_MEME.ID)
          .from(PHASE_MEME)
          .where(PHASE_MEME.PHASE_ID.eq(id))
      )
      .andNotExists(
        db.select(PHASE_CHORD.ID)
          .from(PHASE_CHORD)
          .where(PHASE_CHORD.PHASE_ID.eq(id))
      )
      .execute();
  }

  /**
   Provides consistent validation of a model for Creation/Update

   @param db     context
   @param access control
   @param entity to validate
   @throws BusinessException if invalid
   */
  private static void deepValidate(DSLContext db, Access access, Phase entity) throws BusinessException {
    // actually select the parent pattern for validation
    Record pattern;
    if (access.isTopLevel())
      pattern = db.select(PATTERN.ID, PATTERN.TYPE).from(PATTERN)
        .where(PATTERN.ID.eq(ULong.valueOf(entity.getPatternId())))
        .fetchOne();
    else
      pattern = db.select(PATTERN.ID, PATTERN.TYPE).from(PATTERN)
        .join(LIBRARY).on(LIBRARY.ID.eq(PATTERN.LIBRARY_ID))
        .where(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .and(PATTERN.ID.eq(ULong.valueOf(entity.getPatternId())))
        .fetchOne();

    requireExists("Pattern", pattern);

    // [#199] Macro-type Pattern `total` not required; still is required for other types of Pattern
    if (!Objects.equals(pattern.get(PATTERN.TYPE), PatternType.Macro.toString())) {
      String msg = "for a phase of a non-macro-type pattern, total (# beats)";
      requireNonNull(msg, entity.getTotal());
      requireGreaterThanZero(msg, entity.getTotal());
    }
  }

  /**
   Only certain (writable) fields are mapped back to jOOQ records--
   Read-only fields are excluded from here.

   @param entity to source values from
   @return values mapped to record fields
   */
  private static Map<Field, Object> fieldValueMap(Phase entity) {
    Map<Field, Object> fieldValues = Maps.newHashMap();
    fieldValues.put(PHASE.PATTERN_ID, entity.getPatternId());
    fieldValues.put(PHASE.OFFSET, entity.getOffset());
    fieldValues.put(PHASE.TOTAL, valueOrNull(entity.getTotal()));
    fieldValues.put(PHASE.NAME, valueOrNull(entity.getName()));
    fieldValues.put(PHASE.KEY, valueOrNull(entity.getKey()));
    fieldValues.put(PHASE.TEMPO, valueOrNull(entity.getTempo()));
    fieldValues.put(PHASE.DENSITY, valueOrNull(entity.getDensity()));
    return fieldValues;
  }


}
