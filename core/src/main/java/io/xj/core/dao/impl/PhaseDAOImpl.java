// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao.impl;

import io.xj.core.access.impl.Access;
import io.xj.core.exception.BusinessException;
import io.xj.core.exception.ConfigException;
import io.xj.core.dao.PhaseDAO;
import io.xj.core.persistence.sql.impl.SQLConnection;
import io.xj.core.persistence.sql.SQLDatabaseProvider;
import io.xj.core.model.pattern.PatternType;
import io.xj.core.model.phase.Phase;
import io.xj.core.tables.records.PhaseRecord;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.types.ULong;

import com.google.inject.Inject;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;

import static io.xj.core.Tables.PHASE_MEME;
import static io.xj.core.tables.Pattern.PATTERN;
import static io.xj.core.tables.Library.LIBRARY;
import static io.xj.core.tables.Phase.PHASE;
import static io.xj.core.tables.PhaseChord.PHASE_CHORD;
import static io.xj.core.tables.Voice.VOICE;

public class PhaseDAOImpl extends DAOImpl implements PhaseDAO {

  @Inject
  public PhaseDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public PhaseRecord create(Access access, Phase entity) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(createRecord(tx.getContext(), access, entity));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public PhaseRecord readOne(Access access, ULong id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOneRecord(tx.getContext(), access, id));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Nullable
  @Override
  public Phase readOneForPattern(Access access, ULong patternId, ULong patternPhaseOffset) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOneForPattern(tx.getContext(), access, patternId, patternPhaseOffset));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public Result<PhaseRecord> readAll(Access access, ULong patternId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAll(tx.getContext(), access, patternId));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void update(Access access, ULong id, Phase entity) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      update(tx.getContext(), access, id, entity);
      tx.success();
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void delete(Access access, ULong id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      delete(access, tx.getContext(), id);
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
  private PhaseRecord createRecord(DSLContext db, Access access, Phase entity) throws Exception {
    entity.validate();

    Map<Field, Object> fieldValues = entity.updatableFieldValueMap();

    // [#237] shouldn't be able to create phase with same offset in pattern
    requireNotExists("phase with same offset in pattern",
      db.select(PHASE.ID).from(PHASE)
        .where(PHASE.PATTERN_ID.eq(entity.getPatternId()))
        .and(PHASE.OFFSET.eq(entity.getOffset()))
        .fetch());

    // Common for Create/Update
    deepValidate(db, access, entity);

    return executeCreate(db, PHASE, fieldValues);
  }

  /**
   Read one Phase if able

   @param db     context
   @param access control
   @param id     of phase
   @return phase
   */
  private PhaseRecord readOneRecord(DSLContext db, Access access, ULong id) {
    if (access.isTopLevel())
      return db.selectFrom(PHASE)
        .where(PHASE.ID.eq(id))
        .fetchOne();
    else
      return recordInto(PHASE, db.select(PHASE.fields())
        .from(PHASE)
        .join(PATTERN).on(PATTERN.ID.eq(PHASE.PATTERN_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(PATTERN.LIBRARY_ID))
        .where(PHASE.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne());
  }

  /**
   Read one Phase if able

   @param db              context
   @param access          control
   @param patternId          of pattern in which to read phase
   @param patternPhaseOffset of phase in pattern
   @return phase record
   */
  @Nullable
  private Phase readOneForPattern(DSLContext db, Access access, ULong patternId, ULong patternPhaseOffset) {
    PhaseRecord result;

    if (access.isTopLevel())
      result = db.selectFrom(PHASE)
        .where(PHASE.PATTERN_ID.eq(patternId))
        .and(PHASE.OFFSET.eq(patternPhaseOffset))
        .fetchOne();
    else
      result = recordInto(PHASE, db.select(PHASE.fields())
        .from(PHASE)
        .join(PATTERN).on(PATTERN.ID.eq(PHASE.PATTERN_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(PATTERN.LIBRARY_ID))
        .where(PHASE.PATTERN_ID.eq(patternId))
        .and(PHASE.OFFSET.eq(patternPhaseOffset))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne());

    if (Objects.isNull(result)) {
      return null;
    }

    return new Phase().setFromRecord(result);
  }

  /**
   Read all Phase able for an Pattern

   @param db     context
   @param access control
   @param patternId to readMany all phase of
   @return array of phases
   */
  private Result<PhaseRecord> readAll(DSLContext db, Access access, ULong patternId) {
    if (access.isTopLevel())
      return resultInto(PHASE, db.select(PHASE.fields())
        .from(PHASE)
        .where(PHASE.PATTERN_ID.eq(patternId))
        .fetch());
    else
      return resultInto(PHASE, db.select(PHASE.fields())
        .from(PHASE)
        .join(PATTERN).on(PATTERN.ID.eq(PHASE.PATTERN_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(PATTERN.LIBRARY_ID))
        .where(PHASE.PATTERN_ID.eq(patternId))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetch());
  }

  /**
   Update a Phase record

   @param db     context
   @param access control
   @param id     to update
   @param entity to update with
   @throws BusinessException if failure
   */
  private void update(DSLContext db, Access access, ULong id, Phase entity) throws Exception {
    entity.validate();

    Map<Field, Object> fieldValues = entity.updatableFieldValueMap();
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
  private void delete(Access access, DSLContext db, ULong id) throws Exception {
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
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
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
  private void deepValidate(DSLContext db, Access access, Phase entity) throws BusinessException {
    // actually select the parent pattern for validation
    Record pattern;
    if (access.isTopLevel())
      pattern = db.select(PATTERN.ID, PATTERN.TYPE).from(PATTERN)
        .where(PATTERN.ID.eq(entity.getPatternId()))
        .fetchOne();
    else
      pattern = db.select(PATTERN.ID, PATTERN.TYPE).from(PATTERN)
        .join(LIBRARY).on(LIBRARY.ID.eq(PATTERN.LIBRARY_ID))
        .where(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .and(PATTERN.ID.eq(entity.getPatternId()))
        .fetchOne();

    requireExists("Pattern", pattern);

    // [#199] Macro-type Pattern `total` not required; still is required for other types of Pattern
    if (!Objects.equals(pattern.get(PATTERN.TYPE), PatternType.Macro.toString())) {
      String msg = "for a phase of a non-macro-type pattern, total (# beats)";
      requireNonNull(msg, entity.getTotal());
      requireGreaterThanZero(msg, entity.getTotal());
    }
  }

}
