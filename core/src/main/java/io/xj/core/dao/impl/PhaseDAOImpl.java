// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao.impl;

import io.xj.core.access.impl.Access;
import io.xj.core.dao.PhaseDAO;
import io.xj.core.exception.BusinessException;
import io.xj.core.exception.ConfigException;
import io.xj.core.model.pattern.PatternType;
import io.xj.core.model.phase.Phase;
import io.xj.core.model.phase.PhaseType;
import io.xj.core.persistence.sql.SQLDatabaseProvider;
import io.xj.core.persistence.sql.impl.SQLConnection;
import io.xj.core.transport.CSV;
import io.xj.core.work.WorkManager;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.types.ULong;

import com.google.api.client.util.Maps;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import static io.xj.core.Tables.PHASE_CHORD;
import static io.xj.core.Tables.PHASE_EVENT;
import static io.xj.core.tables.Library.LIBRARY;
import static io.xj.core.tables.Pattern.PATTERN;
import static io.xj.core.tables.Phase.PHASE;
import static io.xj.core.tables.PhaseMeme.PHASE_MEME;

public class PhaseDAOImpl extends DAOImpl implements PhaseDAO {

  private static final Collection<PhaseType> phaseTypesAllowedInRhythmOrDetailPatterns = ImmutableList.of(PhaseType.Intro, PhaseType.Loop, PhaseType.Outro);
  private final WorkManager workManager;

  @Inject
  public PhaseDAOImpl(
    SQLDatabaseProvider dbProvider,
    WorkManager workManager
  ) {
    this.workManager = workManager;
    this.dbProvider = dbProvider;
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
  private static Phase readOne(DSLContext db, Access access, ULong id) throws BusinessException {
    if (access.isTopLevel())
      return modelFrom(db.selectFrom(PHASE)
        .where(PHASE.ID.eq(id))
        .fetchOne(), Phase.class);
    else
      return modelFrom(db.select(PHASE.fields())
        .from(PHASE)
        .join(PATTERN).on(PATTERN.ID.eq(PHASE.PATTERN_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(PATTERN.LIBRARY_ID))
        .where(PHASE.ID.eq(id))
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
  private static Collection<Phase> readAllAtPatternOffset(DSLContext db, Access access, BigInteger patternId, BigInteger patternPhaseOffset) throws BusinessException {
    if (access.isTopLevel())
      return modelsFrom(db.selectFrom(PHASE)
        .where(PHASE.PATTERN_ID.eq(ULong.valueOf(patternId)))
        .and(PHASE.OFFSET.eq(ULong.valueOf(patternPhaseOffset)))
        .fetch(), Phase.class);
    else
      return modelsFrom(db.select(PHASE.fields())
        .from(PHASE)
        .join(PATTERN).on(PATTERN.ID.eq(PHASE.PATTERN_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(PATTERN.LIBRARY_ID))
        .where(PHASE.PATTERN_ID.eq(ULong.valueOf(patternId)))
        .and(PHASE.OFFSET.eq(ULong.valueOf(patternPhaseOffset)))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .fetch(), Phase.class);
  }

  /**
   Read all Phase able for an Pattern

   @param db        context
   @param access    control
   @param patternId to readMany all phase of
   @return array of phases
   */
  private static Collection<Phase> readAll(DSLContext db, Access access, Collection<ULong> patternId) throws BusinessException {
    if (access.isTopLevel())
      return modelsFrom(db.select(PHASE.fields())
        .from(PHASE)
        .where(PHASE.PATTERN_ID.in(patternId))
        .fetch(), Phase.class);
    else
      return modelsFrom(db.select(PHASE.fields())
        .from(PHASE)
        .join(PATTERN).on(PATTERN.ID.eq(PHASE.PATTERN_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(PATTERN.LIBRARY_ID))
        .where(PHASE.PATTERN_ID.in(patternId))
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
    if (!access.isTopLevel())
      requireExists("Phase", db.selectCount().from(PHASE)
        .join(PATTERN).on(PATTERN.ID.eq(PHASE.PATTERN_ID))
        .join(LIBRARY).on(PATTERN.LIBRARY_ID.eq(LIBRARY.ID))
        .where(PHASE.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne(0, int.class));

    db.deleteFrom(PHASE_EVENT)
      .where(PHASE_EVENT.PHASE_ID.eq(id))
      .execute();

    db.deleteFrom(PHASE_MEME)
      .where(PHASE_MEME.PHASE_ID.eq(id))
      .execute();

    db.deleteFrom(PHASE_CHORD)
      .where(PHASE_CHORD.PHASE_ID.eq(id))
      .execute();

    db.deleteFrom(PHASE)
      .where(PHASE.ID.eq(id))
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

    // [#199] Macro-type Pattern `total` not required; still
    // it is required for other types of Pattern
    PatternType patternType = PatternType.validate(pattern.get(PATTERN.TYPE));
    if (!Objects.equals(patternType, PatternType.Macro)) {
      requireGreaterThanZero("for a phase of a non-macro-type pattern, total (# beats)", entity.getTotal());
    }

    // [#153976073] Artist wants Phase to have
    // type Macro or Main (for Macro- or Main-type patterns), or
    // type Intro, Loop, or Outro (for Rhythm or Detail-type Pattern)
    // in order to create a composition that is dynamic when chosen to fill a Link.
    switch (patternType) {

      case Macro:
      case Main:
        require(String.format("%s-type Phase in %s-type Pattern", patternType, patternType), Objects.equals(patternType.toString(), entity.getType().toString()));
        break;

      case Rhythm:
      case Detail:
        require(String.format("Phase of type (%s) in %s-type Pattern", CSV.joinEnum(PhaseType.valuesForDetailPattern()), patternType), phaseTypesAllowedInRhythmOrDetailPatterns.contains(entity.getType()));
        break;
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
    fieldValues.put(PHASE.TYPE, entity.getType());
    fieldValues.put(PHASE.OFFSET, entity.getOffset());
    fieldValues.put(PHASE.TOTAL, valueOrNull(entity.getTotal()));
    fieldValues.put(PHASE.NAME, valueOrNull(entity.getName()));
    fieldValues.put(PHASE.KEY, valueOrNull(entity.getKey()));
    fieldValues.put(PHASE.TEMPO, valueOrNull(entity.getTempo()));
    fieldValues.put(PHASE.DENSITY, valueOrNull(entity.getDensity()));
    return fieldValues;
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
  public Phase clone(Access access, BigInteger cloneId, Phase entity) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(clone(tx.getContext(), access, cloneId, entity));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public Phase readOne(Access access, BigInteger id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOne(tx.getContext(), access, ULong.valueOf(id)));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Nullable
  @Override
  public Collection<Phase> readAllAtPatternOffset(Access access, BigInteger patternId, BigInteger patternPhaseOffset) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAllAtPatternOffset(tx.getContext(), access, patternId, patternPhaseOffset));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public Collection<Phase> readAll(Access access, Collection<BigInteger> parentIds) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAll(tx.getContext(), access, uLongValuesOf(parentIds)));
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
  public void destroy(Access access, BigInteger id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      delete(access, tx.getContext(), ULong.valueOf(id));
      tx.success();
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  /**
   Clone a Phase into a new Phase

   @param db      context
   @param access  control
   @param cloneId of phase to clone
   @param entity  for the new Account User.
   @return newly readMany record
   @throws BusinessException on failure
   */
  private Phase clone(DSLContext db, Access access, BigInteger cloneId, Phase entity) throws Exception {
    Phase from = readOne(db, access, ULong.valueOf(cloneId));
    if (Objects.isNull(from)) throw new BusinessException("Can't clone nonexistent Phase");

    entity.setDensity(from.getDensity());
    entity.setKey(from.getKey());
    entity.setTempo(from.getTempo());
    entity.setTotal(from.getTotal());

    Phase result = create(db, access, entity);
    workManager.schedulePhaseClone(0, cloneId, result.getId());
    return result;
  }


}
