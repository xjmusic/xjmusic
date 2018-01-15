// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao.impl;

import io.xj.core.access.impl.Access;
import io.xj.core.dao.PhaseChordDAO;
import io.xj.core.exception.BusinessException;
import io.xj.core.exception.ConfigException;
import io.xj.core.model.phase_chord.PhaseChord;
import io.xj.core.persistence.sql.SQLDatabaseProvider;
import io.xj.core.persistence.sql.impl.SQLConnection;
import io.xj.core.tables.Phase;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.types.ULong;

import com.google.api.client.util.Maps;
import com.google.inject.Inject;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Map;

import static io.xj.core.Tables.PHASE;
import static io.xj.core.Tables.PHASE_CHORD;
import static io.xj.core.tables.Library.LIBRARY;
import static io.xj.core.tables.Pattern.PATTERN;

public class PhaseChordDAOImpl extends DAOImpl implements PhaseChordDAO {

  @Inject
  public PhaseChordDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  /**
   Create a new Phase Chord

   @param db     context
   @param access control
   @param entity for new phase
   @return newly readMany record
   @throws BusinessException if failure
   */
  private static PhaseChord createRecord(DSLContext db, Access access, PhaseChord entity) throws BusinessException {
    entity.validate();

    Map<Field, Object> fieldValues = fieldValueMap(entity);

    if (access.isTopLevel())
      requireExists("Phase", db.selectCount().from(PHASE)
        .where(PHASE.ID.eq(ULong.valueOf(entity.getPhaseId())))
        .fetchOne(0, int.class));
    else
      requireExists("Phase", db.selectCount().from(PHASE)
        .join(PATTERN).on(PATTERN.ID.eq(PHASE.PATTERN_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(PATTERN.LIBRARY_ID))
        .where(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .and(PHASE.ID.eq(ULong.valueOf(entity.getPhaseId())))
        .fetchOne(0, int.class));

    return modelFrom(executeCreate(db, PHASE_CHORD, fieldValues), PhaseChord.class);
  }

  /**
   Read one Chord if able

   @param db     context
   @param access control
   @param id     of phase
   @return phase
   */
  private static PhaseChord readOne(DSLContext db, Access access, ULong id) throws BusinessException {
    if (access.isTopLevel())
      return modelFrom(db.selectFrom(PHASE_CHORD)
        .where(PHASE_CHORD.ID.eq(id))
        .fetchOne(), PhaseChord.class);
    else
      return modelFrom(db.select(PHASE_CHORD.fields())
        .from(PHASE_CHORD)
        .join(PHASE).on(PHASE.ID.eq(PHASE_CHORD.PHASE_ID))
        .join(PATTERN).on(PATTERN.ID.eq(PHASE.PATTERN_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(PATTERN.LIBRARY_ID))
        .where(PHASE_CHORD.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne(), PhaseChord.class);
  }

  /**
   Read all Chord able for a Phase

   @param db       context
   @param access   control
   @param phaseIds to readMany all phase of
   @return array of phases
   */
  private static Collection<PhaseChord> readAll(DSLContext db, Access access, Collection<ULong> phaseIds) throws BusinessException {
    if (access.isTopLevel())
      return modelsFrom(db.select(PHASE_CHORD.fields())
        .from(PHASE_CHORD)
        .where(PHASE_CHORD.PHASE_ID.in(phaseIds))
        .orderBy(PHASE_CHORD.POSITION)
        .fetch(), PhaseChord.class);
    else
      return modelsFrom(db.select(PHASE_CHORD.fields())
        .from(PHASE_CHORD)
        .join(PHASE).on(PHASE.ID.eq(PHASE_CHORD.PHASE_ID))
        .join(PATTERN).on(PATTERN.ID.eq(PHASE.PATTERN_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(PATTERN.LIBRARY_ID))
        .where(PHASE_CHORD.PHASE_ID.in(phaseIds))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .orderBy(PHASE_CHORD.POSITION)
        .fetch(), PhaseChord.class);
  }

  /**
   Update a Chord record

   @param db     context
   @param access control
   @param id     to update
   @param entity to update with
   @throws BusinessException if failure
   */
  private static void update(DSLContext db, Access access, BigInteger id, PhaseChord entity) throws BusinessException {
    entity.validate();

    Map<Field, Object> fieldValues = fieldValueMap(entity);
    fieldValues.put(PHASE_CHORD.ID, ULong.valueOf(id));

    if (access.isTopLevel())
      requireExists("Phase", db.selectCount().from(PHASE)
        .where(PHASE.ID.eq(ULong.valueOf(entity.getPhaseId())))
        .fetchOne(0, int.class));
    else
      requireExists("Phase", db.selectCount().from(PHASE)
        .join(PATTERN).on(PATTERN.ID.eq(PHASE.PATTERN_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(PATTERN.LIBRARY_ID))
        .where(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .and(PHASE.ID.eq(ULong.valueOf(entity.getPhaseId())))
        .fetchOne(0, int.class));

    if (0 == executeUpdate(db, PHASE_CHORD, fieldValues))
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
      requireExists("Phase Chord", db.selectCount().from(PHASE_CHORD)
        .join(Phase.PHASE).on(Phase.PHASE.ID.eq(PHASE_CHORD.PHASE_ID))
        .join(PATTERN).on(PATTERN.ID.eq(Phase.PHASE.PATTERN_ID))
        .join(LIBRARY).on(PATTERN.LIBRARY_ID.eq(LIBRARY.ID))
        .where(PHASE_CHORD.ID.eq(ULong.valueOf(id)))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne(0, int.class));

    db.deleteFrom(PHASE_CHORD)
      .where(PHASE_CHORD.ID.eq(ULong.valueOf(id)))
      .execute();
  }

  /**
   Only certain (writable) fields are mapped back to jOOQ records--
   Read-only fields are excluded from here.

   @param entity to source values from
   @return values mapped to record fields
   */
  private static Map<Field, Object> fieldValueMap(PhaseChord entity) {
    Map<Field, Object> fieldValues = Maps.newHashMap();
    fieldValues.put(PHASE_CHORD.NAME, entity.getName());
    fieldValues.put(PHASE_CHORD.PHASE_ID, entity.getPhaseId());
    fieldValues.put(PHASE_CHORD.POSITION, entity.getPosition());
    return fieldValues;
  }

  @Override
  public PhaseChord create(Access access, PhaseChord entity) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(createRecord(tx.getContext(), access, entity));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public PhaseChord readOne(Access access, BigInteger id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOne(tx.getContext(), access, ULong.valueOf(id)));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public Collection<PhaseChord> readAll(Access access, Collection<BigInteger> parentIds) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAll(tx.getContext(), access, uLongValuesOf(parentIds)));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void update(Access access, BigInteger id, PhaseChord entity) throws Exception {
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
