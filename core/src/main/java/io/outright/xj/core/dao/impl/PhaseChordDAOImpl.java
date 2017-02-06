// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.dao.impl;

import io.outright.xj.core.app.access.AccessControl;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.app.exception.ConfigException;
import io.outright.xj.core.app.exception.DatabaseException;
import io.outright.xj.core.dao.PhaseChordDAO;
import io.outright.xj.core.db.sql.SQLConnection;
import io.outright.xj.core.db.sql.SQLDatabaseProvider;
import io.outright.xj.core.model.phase_chord.PhaseChordWrapper;
import io.outright.xj.core.tables.Phase;
import io.outright.xj.core.tables.records.PhaseChordRecord;
import io.outright.xj.core.transport.JSON;

import com.google.inject.Inject;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.types.ULong;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.annotation.Nullable;
import java.sql.SQLException;

import static io.outright.xj.core.Tables.PHASE;
import static io.outright.xj.core.Tables.PHASE_CHORD;
import static io.outright.xj.core.tables.Idea.IDEA;
import static io.outright.xj.core.tables.Library.LIBRARY;

public class PhaseChordDAOImpl extends DAOImpl implements PhaseChordDAO {
  //  private static Logger log = LoggerFactory.getLogger(PhaseDAOImpl.class);

  @Inject
  public PhaseChordDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public JSONObject create(AccessControl access, PhaseChordWrapper data) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(create(tx.getContext(), access, data));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public JSONObject readOne(AccessControl access, ULong id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOne(tx.getContext(), access, id));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public JSONArray readAllIn(AccessControl access, ULong phaseId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAllIn(tx.getContext(), access, phaseId));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void update(AccessControl access, ULong id, PhaseChordWrapper data) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      update(tx.getContext(), access, id, data);
      tx.success();
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void delete(AccessControl access, ULong id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      delete(access, tx.getContext(), id);
      tx.success();
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  /**
   * Create a new Phase Chord
   *
   * @param db     context
   * @param access control
   * @param data   for new phase
   * @return newly created record
   * @throws BusinessException if failure
   */
  private JSONObject create(DSLContext db, AccessControl access, PhaseChordWrapper data) throws BusinessException {
    PhaseChordRecord record = db.newRecord(PHASE_CHORD);
    data.validate();
    data.getPhaseChord().intoFieldValueMap().forEach(record::setValue);

    if (access.isAdmin()) {
      requireRecordExists("Phase", db.select(PHASE.ID).from(PHASE)
        .where(PHASE.ID.eq(data.getPhaseChord().getPhaseId()))
        .fetchOne());
    } else {
      requireRecordExists("Phase", db.select(PHASE.ID).from(PHASE)
        .join(IDEA).on(IDEA.ID.eq(PHASE.IDEA_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(IDEA.LIBRARY_ID))
        .where(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .and(PHASE.ID.eq(data.getPhaseChord().getPhaseId()))
        .fetchOne());
    }

    record.store();
    return JSON.objectFromRecord(record);
  }

  /**
   * Read one Chord if able
   *
   * @param db     context
   * @param access control
   * @param id     of phase
   * @return phase
   */
  private JSONObject readOne(DSLContext db, AccessControl access, ULong id) {
    JSONObject result;
    if (access.isAdmin()) {
      result = JSON.objectFromRecord(db.selectFrom(PHASE_CHORD)
        .where(PHASE_CHORD.ID.eq(id))
        .fetchOne());
    } else {
      result = JSON.objectFromRecord(db.select(PHASE_CHORD.fields())
        .from(PHASE_CHORD)
        .join(PHASE).on(PHASE.ID.eq(PHASE_CHORD.PHASE_ID))
        .join(IDEA).on(IDEA.ID.eq(PHASE.IDEA_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(IDEA.LIBRARY_ID))
        .where(PHASE_CHORD.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne());
    }
    return result;
  }

  /**
   * Read all Chord able for an Idea
   *
   * @param db      context
   * @param access  control
   * @param phaseId to read all phase of
   * @return array of phases
   * @throws SQLException on failure
   */
  private JSONArray readAllIn(DSLContext db, AccessControl access, ULong phaseId) throws SQLException {
    JSONArray result;
    if (access.isAdmin()) {
      result = JSON.arrayFromResultSet(db.select(PHASE_CHORD.fields())
        .from(PHASE_CHORD)
        .where(PHASE_CHORD.PHASE_ID.eq(phaseId))
        .orderBy(PHASE_CHORD.POSITION)
        .fetchResultSet());
    } else {
      result = JSON.arrayFromResultSet(db.select(PHASE_CHORD.fields())
        .from(PHASE_CHORD)
        .join(PHASE).on(PHASE.ID.eq(PHASE_CHORD.PHASE_ID))
        .join(IDEA).on(IDEA.ID.eq(PHASE.IDEA_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(IDEA.LIBRARY_ID))
        .where(PHASE_CHORD.PHASE_ID.eq(phaseId))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .orderBy(PHASE_CHORD.POSITION)
        .fetchResultSet());
    }
    return result;
  }

  /**
   * Update a Chord record
   *
   * @param db     context
   * @param access control
   * @param id     to update
   * @param data   to update with
   * @throws BusinessException if failure
   */
  private void update(DSLContext db, AccessControl access, ULong id, PhaseChordWrapper data) throws Exception {
    PhaseChordRecord record;

    record = db.newRecord(PHASE_CHORD);
    record.setId(id);
    data.validate();
    data.getPhaseChord().intoFieldValueMap().forEach(record::setValue);

    if (access.isAdmin()) {
      requireRecordExists("Phase", db.select(PHASE.ID).from(PHASE)
        .where(PHASE.ID.eq(data.getPhaseChord().getPhaseId()))
        .fetchOne());
    } else {
      requireRecordExists("Phase", db.select(PHASE.ID).from(PHASE)
        .join(IDEA).on(IDEA.ID.eq(PHASE.IDEA_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(IDEA.LIBRARY_ID))
        .where(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .and(PHASE.ID.eq(data.getPhaseChord().getPhaseId()))
        .fetchOne());
    }

    if (db.executeUpdate(record) == 0) {
      throw new DatabaseException("No records updated.");
    }
  }

  /**
   * Delete an Chord
   *
   * @param db context
   * @param id to delete
   * @throws Exception if database failure
   * @throws ConfigException   if not configured properly
   * @throws BusinessException if fails business rule
   */
  private void delete(AccessControl access, DSLContext db, ULong id) throws Exception {
    if (!access.isAdmin()) {
      Record record = db.select(PHASE_CHORD.ID).from(PHASE_CHORD)
        .join(Phase.PHASE).on(Phase.PHASE.ID.eq(PHASE_CHORD.PHASE_ID))
        .join(IDEA).on(IDEA.ID.eq(Phase.PHASE.IDEA_ID))
        .join(LIBRARY).on(IDEA.LIBRARY_ID.eq(LIBRARY.ID))
        .where(PHASE_CHORD.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne();
      requireRecordExists("Phase Meme", record);
    }

    db.deleteFrom(PHASE_CHORD)
      .where(PHASE_CHORD.ID.eq(id))
      .execute();
  }

}
