// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.dao.impl;

import io.outright.xj.core.app.access.AccessControl;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.app.exception.ConfigException;
import io.outright.xj.core.dao.VoiceDAO;
import io.outright.xj.core.db.sql.SQLConnection;
import io.outright.xj.core.db.sql.SQLDatabaseProvider;
import io.outright.xj.core.model.voice.Voice;
import io.outright.xj.core.model.voice.VoiceWrapper;
import io.outright.xj.core.transport.JSON;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.types.ULong;

import com.google.inject.Inject;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.annotation.Nullable;
import java.sql.SQLException;
import java.util.Map;

import static io.outright.xj.core.Tables.VOICE_EVENT;
import static io.outright.xj.core.tables.Idea.IDEA;
import static io.outright.xj.core.tables.Library.LIBRARY;
import static io.outright.xj.core.tables.Phase.PHASE;
import static io.outright.xj.core.tables.Voice.VOICE;

public class VoiceDAOImpl extends DAOImpl implements VoiceDAO {

  @Inject
  public VoiceDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public JSONObject create(AccessControl access, VoiceWrapper data) throws Exception {
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
  public void update(AccessControl access, ULong id, VoiceWrapper data) throws Exception {
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
   * Create a new Voice
   *
   * @param db     context
   * @param access control
   * @param data   for new voice
   * @return newly created record
   * @throws BusinessException if failure
   */
  private JSONObject create(DSLContext db, AccessControl access, VoiceWrapper data) throws BusinessException {
    Voice model = data.validate();
    Map<Field, Object> fieldValues = model.intoFieldValueMap();

    if (access.isTopLevel()) {
      requireRecordExists("Phase", db.select(PHASE.ID).from(PHASE)
        .where(PHASE.ID.eq(model.getPhaseId()))
        .fetchOne());
    } else {
      requireRecordExists("Phase", db.select(PHASE.ID).from(PHASE)
        .join(IDEA).on(IDEA.ID.eq(PHASE.IDEA_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(IDEA.LIBRARY_ID))
        .where(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .and(PHASE.ID.eq(model.getPhaseId()))
        .fetchOne());
    }

    return JSON.objectFromRecord(executeCreate(db, VOICE, fieldValues));
  }

  /**
   * Read one Voice if able
   *
   * @param db     context
   * @param access control
   * @param id     of voice
   * @return voice
   */
  private JSONObject readOne(DSLContext db, AccessControl access, ULong id) {
    if (access.isTopLevel()) {
      return JSON.objectFromRecord(db.selectFrom(VOICE)
        .where(VOICE.ID.eq(id))
        .fetchOne());
    } else {
      return JSON.objectFromRecord(db.select(VOICE.fields())
        .from(VOICE)
        .join(PHASE).on(PHASE.ID.eq(VOICE.PHASE_ID))
        .join(IDEA).on(IDEA.ID.eq(PHASE.IDEA_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(IDEA.LIBRARY_ID))
        .where(VOICE.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne());
    }
  }

  /**
   * Read all Voice able for an Phase
   *
   * @param db      context
   * @param access  control
   * @param phaseId to read all voice of
   * @return array of voices
   * @throws SQLException on failure
   */
  private JSONArray readAllIn(DSLContext db, AccessControl access, ULong phaseId) throws SQLException {
    if (access.isTopLevel()) {
      return JSON.arrayFromResultSet(db.select(VOICE.fields())
        .from(VOICE)
        .where(VOICE.PHASE_ID.eq(phaseId))
        .fetchResultSet());
    } else {
      return JSON.arrayFromResultSet(db.select(VOICE.fields())
        .from(VOICE)
        .join(PHASE).on(PHASE.ID.eq(VOICE.PHASE_ID))
        .join(IDEA).on(IDEA.ID.eq(PHASE.IDEA_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(IDEA.LIBRARY_ID))
        .where(VOICE.PHASE_ID.eq(phaseId))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetchResultSet());
    }
  }

  /**
   * Update a Voice record
   *
   * @param db     context
   * @param access control
   * @param id     to update
   * @param data   to update with
   * @throws BusinessException if failure
   */
  private void update(DSLContext db, AccessControl access, ULong id, VoiceWrapper data) throws Exception {
    Voice model = data.validate();
    Map<Field, Object> fieldValues = model.intoFieldValueMap();
    fieldValues.put(VOICE.ID, id);

    if (access.isTopLevel()) {
      requireRecordExists("Phase", db.select(PHASE.ID).from(PHASE)
        .where(PHASE.ID.eq(model.getPhaseId()))
        .fetchOne());
    } else {
      requireRecordExists("Phase", db.select(PHASE.ID).from(PHASE)
        .join(IDEA).on(IDEA.ID.eq(PHASE.IDEA_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(IDEA.LIBRARY_ID))
        .where(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .and(PHASE.ID.eq(model.getPhaseId()))
        .fetchOne());
    }

    if (executeUpdate(db, VOICE, fieldValues) == 0) {
      throw new BusinessException("No records updated.");
    }
  }

  /**
   * Delete an Voice
   *
   * @param db context
   * @param id to delete
   * @throws Exception         if database failure
   * @throws ConfigException   if not configured properly
   * @throws BusinessException if fails business rule
   */
  private void delete(AccessControl access, DSLContext db, ULong id) throws Exception {
    requireEmptyResultSet(db.select(VOICE_EVENT.ID)
      .from(VOICE_EVENT)
      .where(VOICE_EVENT.VOICE_ID.eq(id))
      .fetchResultSet());

    if (!access.isTopLevel()) {
      requireRecordExists("Voice", db.select(VOICE.ID).from(VOICE)
        .join(PHASE).on(PHASE.ID.eq(VOICE.PHASE_ID))
        .join(IDEA).on(IDEA.ID.eq(PHASE.IDEA_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(IDEA.LIBRARY_ID))
        .where(VOICE.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne());
    }

    db.deleteFrom(VOICE)
      .where(VOICE.ID.eq(id))
      .andNotExists(
        db.select(VOICE_EVENT.ID)
          .from(VOICE_EVENT)
          .where(VOICE_EVENT.VOICE_ID.eq(id))
      )
      .execute();
  }

}
