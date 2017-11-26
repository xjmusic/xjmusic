// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.dao.impl;

import io.xj.core.app.access.impl.Access;
import io.xj.core.app.exception.BusinessException;
import io.xj.core.app.exception.ConfigException;
import io.xj.core.dao.PhaseChordDAO;
import io.xj.core.database.sql.impl.SQLConnection;
import io.xj.core.database.sql.SQLDatabaseProvider;
import io.xj.core.model.phase_chord.PhaseChord;
import io.xj.core.tables.Phase;
import io.xj.core.tables.records.PhaseChordRecord;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Result;
import org.jooq.types.ULong;

import com.google.inject.Inject;

import javax.annotation.Nullable;
import java.sql.SQLException;
import java.util.Map;

import static io.xj.core.Tables.PHASE;
import static io.xj.core.Tables.PHASE_CHORD;
import static io.xj.core.tables.Idea.IDEA;
import static io.xj.core.tables.Library.LIBRARY;

public class PhaseChordDAOImpl extends DAOImpl implements PhaseChordDAO {

  @Inject
  public PhaseChordDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public PhaseChordRecord create(Access access, PhaseChord entity) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(createRecord(tx.getContext(), access, entity));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public PhaseChordRecord readOne(Access access, ULong id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOneRecord(tx.getContext(), access, id));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public Result<PhaseChordRecord> readAll(Access access, ULong phaseId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAll(tx.getContext(), access, phaseId));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void update(Access access, ULong id, PhaseChord entity) throws Exception {
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
   Create a new Phase Chord

   @param db     context
   @param access control
   @param entity for new phase
   @return newly readMany record
   @throws BusinessException if failure
   */
  private PhaseChordRecord createRecord(DSLContext db, Access access, PhaseChord entity) throws BusinessException {
    entity.validate();

    Map<Field, Object> fieldValues = entity.updatableFieldValueMap();

    if (access.isTopLevel())
      requireExists("Phase", db.selectCount().from(PHASE)
        .where(PHASE.ID.eq(entity.getPhaseId()))
        .fetchOne(0, int.class));
    else
      requireExists("Phase", db.selectCount().from(PHASE)
        .join(IDEA).on(IDEA.ID.eq(PHASE.IDEA_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(IDEA.LIBRARY_ID))
        .where(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .and(PHASE.ID.eq(entity.getPhaseId()))
        .fetchOne(0, int.class));

    return executeCreate(db, PHASE_CHORD, fieldValues);
  }

  /**
   Read one Chord if able

   @param db     context
   @param access control
   @param id     of phase
   @return phase
   */
  private PhaseChordRecord readOneRecord(DSLContext db, Access access, ULong id) {
    if (access.isTopLevel())
      return db.selectFrom(PHASE_CHORD)
        .where(PHASE_CHORD.ID.eq(id))
        .fetchOne();
    else
      return recordInto(PHASE_CHORD, db.select(PHASE_CHORD.fields())
        .from(PHASE_CHORD)
        .join(PHASE).on(PHASE.ID.eq(PHASE_CHORD.PHASE_ID))
        .join(IDEA).on(IDEA.ID.eq(PHASE.IDEA_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(IDEA.LIBRARY_ID))
        .where(PHASE_CHORD.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne());
  }

  /**
   Read all Chord able for an Idea

   @param db      context
   @param access  control
   @param phaseId to readMany all phase of
   @return array of phases
   @throws SQLException on failure
   */
  private Result<PhaseChordRecord> readAll(DSLContext db, Access access, ULong phaseId) throws SQLException {
    if (access.isTopLevel())
      return resultInto(PHASE_CHORD, db.select(PHASE_CHORD.fields())
        .from(PHASE_CHORD)
        .where(PHASE_CHORD.PHASE_ID.eq(phaseId))
        .orderBy(PHASE_CHORD.POSITION)
        .fetch());
    else
      return resultInto(PHASE_CHORD, db.select(PHASE_CHORD.fields())
        .from(PHASE_CHORD)
        .join(PHASE).on(PHASE.ID.eq(PHASE_CHORD.PHASE_ID))
        .join(IDEA).on(IDEA.ID.eq(PHASE.IDEA_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(IDEA.LIBRARY_ID))
        .where(PHASE_CHORD.PHASE_ID.eq(phaseId))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .orderBy(PHASE_CHORD.POSITION)
        .fetch());
  }

  /**
   Update a Chord record

   @param db     context
   @param access control
   @param id     to update
   @param entity to update with
   @throws BusinessException if failure
   */
  private void update(DSLContext db, Access access, ULong id, PhaseChord entity) throws BusinessException {
    entity.validate();

    Map<Field, Object> fieldValues = entity.updatableFieldValueMap();
    fieldValues.put(PHASE_CHORD.ID, id);

    if (access.isTopLevel())
      requireExists("Phase", db.selectCount().from(PHASE)
        .where(PHASE.ID.eq(entity.getPhaseId()))
        .fetchOne(0, int.class));
    else
      requireExists("Phase", db.selectCount().from(PHASE)
        .join(IDEA).on(IDEA.ID.eq(PHASE.IDEA_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(IDEA.LIBRARY_ID))
        .where(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .and(PHASE.ID.eq(entity.getPhaseId()))
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
  private void delete(Access access, DSLContext db, ULong id) throws Exception {
    if (!access.isTopLevel())
      requireExists("Phase Chord", db.selectCount().from(PHASE_CHORD)
        .join(Phase.PHASE).on(Phase.PHASE.ID.eq(PHASE_CHORD.PHASE_ID))
        .join(IDEA).on(IDEA.ID.eq(Phase.PHASE.IDEA_ID))
        .join(LIBRARY).on(IDEA.LIBRARY_ID.eq(LIBRARY.ID))
        .where(PHASE_CHORD.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne(0, int.class));

    db.deleteFrom(PHASE_CHORD)
      .where(PHASE_CHORD.ID.eq(id))
      .execute();
  }

}
