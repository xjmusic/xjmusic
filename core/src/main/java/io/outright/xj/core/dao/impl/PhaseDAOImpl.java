// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.dao.impl;

import io.outright.xj.core.app.access.AccessControl;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.app.exception.ConfigException;
import io.outright.xj.core.app.exception.DatabaseException;
import io.outright.xj.core.dao.PhaseDAO;
import io.outright.xj.core.db.sql.SQLConnection;
import io.outright.xj.core.db.sql.SQLDatabaseProvider;
import io.outright.xj.core.model.phase.PhaseWrapper;
import io.outright.xj.core.tables.records.PhaseRecord;
import io.outright.xj.core.transport.JSON;

import com.google.inject.Inject;
import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.annotation.Nullable;
import java.sql.SQLException;

import static io.outright.xj.core.Tables.PHASE_MEME;
import static io.outright.xj.core.tables.Idea.IDEA;
import static io.outright.xj.core.tables.Library.LIBRARY;
import static io.outright.xj.core.tables.Phase.PHASE;
import static io.outright.xj.core.tables.PhaseChord.PHASE_CHORD;
import static io.outright.xj.core.tables.Voice.VOICE;

public class PhaseDAOImpl extends DAOImpl implements PhaseDAO {
  //  private static Logger log = LoggerFactory.getLogger(PhaseDAOImpl.class);

  @Inject
  public PhaseDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public JSONObject create(AccessControl access, PhaseWrapper data) throws Exception {
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
  public JSONArray readAllIn(AccessControl access, ULong ideaId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAllIn(tx.getContext(), access, ideaId));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void update(AccessControl access, ULong id, PhaseWrapper data) throws Exception {
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
   * Create a new Phase
   * @param db context
   * @param access control
   * @param data for new phase
   * @return newly created record
   * @throws BusinessException if failure
   */
  private JSONObject create(DSLContext db, AccessControl access, PhaseWrapper data) throws BusinessException {
    PhaseRecord record = db.newRecord(PHASE);
    data.validate();
    data.getPhase().intoFieldValueMap().forEach(record::setValue);

    if (access.isTopLevel()) {
      // Admin can create phase in any existing idea
      requireRecordExists("Idea", db.select(IDEA.ID).from(IDEA)
        .where(IDEA.ID.eq(data.getPhase().getIdeaId()))
        .fetchOne());
    } else {
      // Not admin, must have account access
      requireRecordExists("Idea", db.select(IDEA.ID).from(IDEA)
        .join(LIBRARY).on(LIBRARY.ID.eq(IDEA.LIBRARY_ID))
        .where(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .and(IDEA.ID.eq(data.getPhase().getIdeaId()))
        .fetchOne());
    }

    record.store();
    return JSON.objectFromRecord(record);
  }

  /**
   * Read one Phase if able
   * @param db context
   * @param access control
   * @param id of phase
   * @return phase
   */
  private JSONObject readOne(DSLContext db, AccessControl access, ULong id) {
    JSONObject result;
    if (access.isTopLevel()) {
      result = JSON.objectFromRecord(db.selectFrom(PHASE)
        .where(PHASE.ID.eq(id))
        .fetchOne());
    } else {
      result = JSON.objectFromRecord(db.select(PHASE.fields())
        .from(PHASE)
        .join(IDEA).on(IDEA.ID.eq(PHASE.IDEA_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(IDEA.LIBRARY_ID))
        .where(PHASE.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne());
    }
    return result;
  }

  /**
   * Read all Phase able for an Idea
   * @param db context
   * @param access control
   * @param ideaId to read all phase of
   * @return array of phases
   * @throws SQLException on failure
   */
  private JSONArray readAllIn(DSLContext db, AccessControl access, ULong ideaId) throws SQLException {
    JSONArray result;
    if (access.isTopLevel()) {
      result = JSON.arrayFromResultSet(db.select(PHASE.fields())
        .from(PHASE)
        .where(PHASE.IDEA_ID.eq(ideaId))
        .fetchResultSet());
    } else {
      result = JSON.arrayFromResultSet(db.select(PHASE.fields())
        .from(PHASE)
        .join(IDEA).on(IDEA.ID.eq(PHASE.IDEA_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(IDEA.LIBRARY_ID))
        .where(PHASE.IDEA_ID.eq(ideaId))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetchResultSet());
    }
    return result;
  }

  /**
   * Delete an Phase
   *
   * @param db     context
   * @param id to delete
   * @throws Exception if database failure
   * @throws ConfigException   if not configured properly
   * @throws BusinessException if fails business rule
   */
  private void delete(AccessControl access, DSLContext db, ULong id) throws Exception {
    requireEmptyResultSet(db.select(VOICE.ID)
      .from(VOICE)
      .where(VOICE.PHASE_ID.eq(id))
      .fetchResultSet());

    requireEmptyResultSet(db.select(PHASE_MEME.ID)
      .from(PHASE_MEME)
      .where(PHASE_MEME.PHASE_ID.eq(id))
      .fetchResultSet());

    requireEmptyResultSet(db.select(PHASE_CHORD.ID)
      .from(PHASE_CHORD)
      .where(PHASE_CHORD.PHASE_ID.eq(id))
      .fetchResultSet());

    if (!access.isTopLevel()) {
      requireRecordExists("Phase", db.select(PHASE.ID).from(PHASE)
        .join(IDEA).on(IDEA.ID.eq(PHASE.IDEA_ID))
        .join(LIBRARY).on(IDEA.LIBRARY_ID.eq(LIBRARY.ID))
        .where(PHASE.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne());
    }

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
   * Update a Phase record
   * @param db context
   * @param access control
   * @param id to update
   * @param data to update with
   * @throws BusinessException if failure
   */
  private void update(DSLContext db, AccessControl access, ULong id, PhaseWrapper data) throws Exception {
    PhaseRecord record;

    record = db.newRecord(PHASE);
    record.setId(id);
    data.validate();
    data.getPhase().intoFieldValueMap().forEach(record::setValue);

    if (access.isTopLevel()) {
      // Admin can create phase in any existing idea
      requireRecordExists("Idea",db.select(IDEA.ID).from(IDEA)
        .where(IDEA.ID.eq(data.getPhase().getIdeaId()))
        .fetchOne());
    } else {
      // Not admin, must have account access
      requireRecordExists("Idea", db.select(IDEA.ID).from(IDEA)
        .join(LIBRARY).on(LIBRARY.ID.eq(IDEA.LIBRARY_ID))
        .where(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .and(IDEA.ID.eq(data.getPhase().getIdeaId()))
        .fetchOne());
    }

    if (db.executeUpdate(record)==0) {
      throw new DatabaseException("No records updated.");
    }
  }
}
