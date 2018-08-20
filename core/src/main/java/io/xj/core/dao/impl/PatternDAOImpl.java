// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao.impl;

import io.xj.core.access.impl.Access;
import io.xj.core.dao.PatternDAO;
import io.xj.core.exception.BusinessException;
import io.xj.core.exception.ConfigException;
import io.xj.core.model.pattern.Pattern;
import io.xj.core.model.pattern.PatternState;
import io.xj.core.model.pattern.PatternType;
import io.xj.core.model.sequence.SequenceType;
import io.xj.core.model.user_role.UserRoleType;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static io.xj.core.Tables.PATTERN_CHORD;
import static io.xj.core.Tables.PATTERN_EVENT;
import static io.xj.core.tables.Library.LIBRARY;
import static io.xj.core.tables.Pattern.PATTERN;
import static io.xj.core.tables.PatternMeme.PATTERN_MEME;
import static io.xj.core.tables.Sequence.SEQUENCE;

public class PatternDAOImpl extends DAOImpl implements PatternDAO {
  private static final Logger log = LoggerFactory.getLogger(PatternDAOImpl.class);
  private static final Collection<PatternType> patternTypesAllowedInRhythmOrDetailSequences = ImmutableList.of(PatternType.Intro, PatternType.Loop, PatternType.Outro);
  private final WorkManager workManager;

  @Inject
  public PatternDAOImpl(
    SQLDatabaseProvider dbProvider,
    WorkManager workManager
  ) {
    this.workManager = workManager;
    this.dbProvider = dbProvider;
  }

  /**
   Create a new Pattern

   @param db     context
   @param access control
   @param entity for new pattern
   @return newly readMany record
   @throws BusinessException if failure
   */
  private static Pattern create(DSLContext db, Access access, Pattern entity) throws Exception {
    entity.validate();

    Map<Field, Object> fieldValues = fieldValueMap(entity);

    // Common for Create/Update
    deepValidate(db, access, entity);

    return modelFrom(executeCreate(db, PATTERN, fieldValues), Pattern.class);
  }

  /**
   Read one Pattern if able

   @param db     context
   @param access control
   @param id     of pattern
   @return pattern
   */
  private static Pattern readOne(DSLContext db, Access access, ULong id) throws BusinessException {
    if (access.isTopLevel())
      return modelFrom(db.selectFrom(PATTERN)
        .where(PATTERN.ID.eq(id))
        .fetchOne(), Pattern.class);
    else
      return modelFrom(db.select(PATTERN.fields())
        .from(PATTERN)
        .join(SEQUENCE).on(SEQUENCE.ID.eq(PATTERN.SEQUENCE_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(SEQUENCE.LIBRARY_ID))
        .where(PATTERN.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne(), Pattern.class);
  }

  /**
   Read one Pattern if able

   @param db                    context
   @param access                control
   @param sequenceId            of sequence in which to read pattern
   @param sequencePatternOffset of pattern in sequence
   @return pattern record
   */
  @Nullable
  private static Collection<Pattern> readAllAtSequenceOffset(DSLContext db, Access access, BigInteger sequenceId, BigInteger sequencePatternOffset) throws BusinessException {
    if (access.isTopLevel())
      return modelsFrom(db.selectFrom(PATTERN)
        .where(PATTERN.SEQUENCE_ID.eq(ULong.valueOf(sequenceId)))
        .and(PATTERN.OFFSET.eq(ULong.valueOf(sequencePatternOffset)))
        .and(PATTERN.STATE.notEqual(String.valueOf(PatternState.Erase)))
        .fetch(), Pattern.class);
    else
      return modelsFrom(db.select(PATTERN.fields())
        .from(PATTERN)
        .join(SEQUENCE).on(SEQUENCE.ID.eq(PATTERN.SEQUENCE_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(SEQUENCE.LIBRARY_ID))
        .where(PATTERN.SEQUENCE_ID.eq(ULong.valueOf(sequenceId)))
        .and(PATTERN.OFFSET.eq(ULong.valueOf(sequencePatternOffset)))
        .and(PATTERN.STATE.notEqual(String.valueOf(PatternState.Erase)))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .fetch(), Pattern.class);
  }

  /**
   Read all Pattern able for an Sequence

   @param db         context
   @param access     control
   @param sequenceId to readMany all pattern of
   @return array of patterns
   */
  private static Collection<Pattern> readAll(DSLContext db, Access access, Collection<ULong> sequenceId) throws BusinessException {
    if (access.isTopLevel())
      return modelsFrom(db.select(PATTERN.fields())
        .from(PATTERN)
        .where(PATTERN.SEQUENCE_ID.in(sequenceId))
        .and(PATTERN.STATE.notEqual(String.valueOf(PatternState.Erase)))
        .fetch(), Pattern.class);
    else
      return modelsFrom(db.select(PATTERN.fields())
        .from(PATTERN)
        .join(SEQUENCE).on(SEQUENCE.ID.eq(PATTERN.SEQUENCE_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(SEQUENCE.LIBRARY_ID))
        .where(PATTERN.SEQUENCE_ID.in(sequenceId))
        .and(PATTERN.STATE.notEqual(String.valueOf(PatternState.Erase)))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .fetch(), Pattern.class);
  }

  /**
   Read all records in a given state

   @param db     context
   @param access control
   @param state  to read patterns in
   @return array of records
   */
  private static Collection<Pattern> readAllInState(DSLContext db, Access access, PatternState state) throws Exception {
    requireRole("platform access", access, UserRoleType.Admin, UserRoleType.Engineer);

    return modelsFrom(db.select(PATTERN.fields())
      .from(PATTERN)
      .where(PATTERN.STATE.eq(state.toString()))
      .or(PATTERN.STATE.eq(state.toString().toLowerCase(Locale.ENGLISH)))
      .fetch(), Pattern.class);
  }

  /**
   Update a Pattern record

   @param db     context
   @param access control
   @param id     to update
   @param entity to update with
   @throws BusinessException if failure
   */
  private static void update(DSLContext db, Access access, ULong id, Pattern entity) throws Exception {
    entity.validate();

    Map<Field, Object> fieldValues = fieldValueMap(entity);
    fieldValues.put(PATTERN.ID, id);

    // Common for Create/Update
    deepValidate(db, access, entity);

    if (0 == executeUpdate(db, PATTERN, fieldValues))
      throw new BusinessException("No records updated.");
  }

  /**
   Delete an Pattern

   @param db context
   @param id to delete
   @throws Exception         if database failure
   @throws ConfigException   if not configured properly
   @throws BusinessException if fails business rule
   */
  private static void destroy(Access access, DSLContext db, ULong id) throws Exception {
    if (!access.isTopLevel())
      requireExists("Pattern", db.selectCount().from(PATTERN)
        .join(SEQUENCE).on(SEQUENCE.ID.eq(PATTERN.SEQUENCE_ID))
        .join(LIBRARY).on(SEQUENCE.LIBRARY_ID.eq(LIBRARY.ID))
        .where(PATTERN.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne(0, int.class));

    db.deleteFrom(PATTERN_EVENT)
      .where(PATTERN_EVENT.PATTERN_ID.eq(id))
      .execute();

    db.deleteFrom(PATTERN_MEME)
      .where(PATTERN_MEME.PATTERN_ID.eq(id))
      .execute();

    db.deleteFrom(PATTERN_CHORD)
      .where(PATTERN_CHORD.PATTERN_ID.eq(id))
      .execute();

    db.deleteFrom(PATTERN)
      .where(PATTERN.ID.eq(id))
      .execute();
  }

  /**
   Provides consistent validation of a model for Creation/Update

   @param db     context
   @param access control
   @param entity to validate
   @throws BusinessException if invalid
   */
  private static void deepValidate(DSLContext db, Access access, Pattern entity) throws BusinessException {
    // actually select the parent sequence for validation
    Record sequence;
    if (access.isTopLevel())
      sequence = db.select(SEQUENCE.ID, SEQUENCE.TYPE).from(SEQUENCE)
        .where(SEQUENCE.ID.eq(ULong.valueOf(entity.getSequenceId())))
        .fetchOne();
    else
      sequence = db.select(SEQUENCE.ID, SEQUENCE.TYPE).from(SEQUENCE)
        .join(LIBRARY).on(LIBRARY.ID.eq(SEQUENCE.LIBRARY_ID))
        .where(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .and(SEQUENCE.ID.eq(ULong.valueOf(entity.getSequenceId())))
        .fetchOne();

    requireExists("Sequence", sequence);

    // [#199] Macro-type Sequence `total` not required; still
    // it is required for other types of Sequence
    SequenceType sequenceType = SequenceType.validate(sequence.get(SEQUENCE.TYPE));
    if (!Objects.equals(sequenceType, SequenceType.Macro)) {
      requireGreaterThanZero("for a pattern of a non-macro-type sequence, total (# beats)", entity.getTotal());
    }

    // [#153976073] Artist wants Pattern to have
    // type Macro or Main (for Macro- or Main-type sequences), or
    // type Intro, Loop, or Outro (for Rhythm or Detail-type Sequence)
    // in order to create a composition that is dynamic when chosen to fill a Segment.
    switch (sequenceType) {

      case Macro:
      case Main:
        require(String.format("%s-type Pattern in %s-type Sequence", sequenceType, sequenceType), Objects.equals(sequenceType.toString(), entity.getType().toString()));
        break;

      case Rhythm:
      case Detail:
        require(String.format("Pattern of type (%s) in %s-type Sequence", CSV.joinEnum(PatternType.valuesForDetailSequence()), sequenceType), patternTypesAllowedInRhythmOrDetailSequences.contains(entity.getType()));
        break;
    }
  }

  /**
   Only certain (writable) fields are mapped back to jOOQ records--
   Read-only fields are excluded from here.

   @param entity to source values from
   @return values mapped to record fields
   */
  private static Map<Field, Object> fieldValueMap(Pattern entity) {
    Map<Field, Object> fieldValues = Maps.newHashMap();
    fieldValues.put(PATTERN.SEQUENCE_ID, entity.getSequenceId());
    fieldValues.put(PATTERN.TYPE, entity.getType());
    fieldValues.put(PATTERN.STATE, entity.getState());
    fieldValues.put(PATTERN.OFFSET, entity.getOffset());
    fieldValues.put(PATTERN.TOTAL, valueOrNull(entity.getTotal()));
    fieldValues.put(PATTERN.METER_SUPER, valueOrNull(entity.getMeterSuper()));
    fieldValues.put(PATTERN.METER_SUB, valueOrNull(entity.getMeterSub()));
    fieldValues.put(PATTERN.METER_SWING, valueOrNull(entity.getMeterSwing()));
    fieldValues.put(PATTERN.NAME, valueOrNull(entity.getName()));
    fieldValues.put(PATTERN.KEY, valueOrNull(entity.getKey()));
    fieldValues.put(PATTERN.TEMPO, valueOrNull(entity.getTempo()));
    fieldValues.put(PATTERN.DENSITY, valueOrNull(entity.getDensity()));
    return fieldValues;
  }

  /**
   Update an pattern to Erase state

   @param db context
   @param id to delete
   @throws Exception         if database failure
   @throws ConfigException   if not configured properly
   @throws BusinessException if fails business rule
   */
  private void erase(Access access, DSLContext db, ULong id) throws Exception {
    if (access.isTopLevel()) requireExists("Pattern", db.selectCount().from(PATTERN)
      .where(PATTERN.ID.eq(id))
      .fetchOne(0, int.class));
    else requireExists("Pattern", db.selectCount().from(PATTERN)
      .join(SEQUENCE).on(SEQUENCE.ID.eq(PATTERN.SEQUENCE_ID))
      .join(LIBRARY).on(LIBRARY.ID.eq(SEQUENCE.LIBRARY_ID))
      .where(PATTERN.ID.eq(id))
      .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
      .fetchOne(0, int.class));

    // Update pattern state to Erase
    Map<Field, Object> fieldValues = com.google.common.collect.Maps.newHashMap();
    fieldValues.put(PATTERN.ID, id);
    fieldValues.put(PATTERN.STATE, PatternState.Erase);

    if (0 == executeUpdate(db, PATTERN, fieldValues))
      throw new BusinessException("No records updated.");

    // Schedule pattern deletion job
    try {
      workManager.doPatternErase(id.toBigInteger());
    } catch (Exception e) {
      log.error("Failed to start PatternErase work after updating Pattern to Erase state. See the elusive [#153492153] Entity erase job can be spawned without an error", e);
    }
  }

  @Override
  public Pattern create(Access access, Pattern entity) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(create(tx.getContext(), access, entity));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Pattern clone(Access access, BigInteger cloneId, Pattern entity) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(clone(tx.getContext(), access, cloneId, entity));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public Pattern readOne(Access access, BigInteger id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOne(tx.getContext(), access, ULong.valueOf(id)));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Nullable
  @Override
  public Collection<Pattern> readAllAtSequenceOffset(Access access, BigInteger sequenceId, BigInteger sequencePatternOffset) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAllAtSequenceOffset(tx.getContext(), access, sequenceId, sequencePatternOffset));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public Collection<Pattern> readAll(Access access, Collection<BigInteger> parentIds) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAll(tx.getContext(), access, uLongValuesOf(parentIds)));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Collection<Pattern> readAllInState(Access access, PatternState state) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAllInState(tx.getContext(), access, state));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void erase(Access access, BigInteger id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      erase(access, tx.getContext(), ULong.valueOf(id));
      tx.success();
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void update(Access access, BigInteger id, Pattern entity) throws Exception {
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
      destroy(access, tx.getContext(), ULong.valueOf(id));
      tx.success();
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  /**
   Clone a Pattern into a new Pattern

   @param db      context
   @param access  control
   @param cloneId of pattern to clone
   @param entity  for the new Account User.
   @return newly readMany record
   @throws BusinessException on failure
   */
  private Pattern clone(DSLContext db, Access access, BigInteger cloneId, Pattern entity) throws Exception {
    Pattern from = readOne(db, access, ULong.valueOf(cloneId));
    if (Objects.isNull(from))
      throw new BusinessException("Can't clone nonexistent Pattern");

    entity.setDensity(from.getDensity());
    entity.setKey(from.getKey());
    entity.setTempo(from.getTempo());
    entity.setTotal(from.getTotal());

    Pattern result = create(db, access, entity);
    workManager.doPatternClone(cloneId, result.getId());
    return result;
  }


}
