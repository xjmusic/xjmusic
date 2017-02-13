// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.dao.impl;

import io.outright.xj.core.app.access.AccessControl;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.app.exception.ConfigException;
import io.outright.xj.core.dao.PhaseMemeDAO;
import io.outright.xj.core.db.sql.SQLConnection;
import io.outright.xj.core.db.sql.SQLDatabaseProvider;
import io.outright.xj.core.model.phase_meme.PhaseMeme;
import io.outright.xj.core.model.phase_meme.PhaseMemeWrapper;
import io.outright.xj.core.transport.JSON;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.types.ULong;

import com.google.inject.Inject;

import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.Map;

import static io.outright.xj.core.tables.Idea.IDEA;
import static io.outright.xj.core.tables.Library.LIBRARY;
import static io.outright.xj.core.tables.Phase.PHASE;
import static io.outright.xj.core.tables.PhaseMeme.PHASE_MEME;

/**
 * PhaseMeme DAO
 * <p>
 * TODO [core] more specific permissions of user (artist) access by per-entity ownership
 */
public class PhaseMemeDAOImpl extends DAOImpl implements PhaseMemeDAO {

  @Inject
  public PhaseMemeDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public JSONObject create(AccessControl access, PhaseMemeWrapper data) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(create(tx.getContext(), access, data));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public JSONObject readOne(AccessControl access, ULong id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOne(tx.getContext(), access, id));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public JSONArray readAllIn(AccessControl access, ULong phaseId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAllAble(tx.getContext(), access, phaseId));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void delete(AccessControl access, ULong id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      delete(tx.getContext(), access, id);
      tx.success();
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  /**
   * Create a new Phase Meme record
   *
   * @param db     context
   * @param access control
   * @param data   for new PhaseMeme
   * @return new record
   * @throws Exception if database failure
   * @throws ConfigException   if not configured properly
   * @throws BusinessException if fails business rule
   */
  private JSONObject create(DSLContext db, AccessControl access, PhaseMemeWrapper data) throws Exception {
    PhaseMeme model = data.validate();
    Map<Field, Object> fieldValues = model.intoFieldValueMap();

    if (access.isTopLevel()) {
      requireRecordExists("Phase", db.select(PHASE.ID).from(PHASE)
        .where(PHASE.ID.eq(model.getPhaseId()))
        .fetchOne());
    } else {
      requireRecordExists("Phase", db.select(PHASE.ID).from(PHASE)
        .join(IDEA).on(IDEA.ID.eq(PHASE.IDEA_ID))
        .join(LIBRARY).on(IDEA.LIBRARY_ID.eq(LIBRARY.ID))
        .where(PHASE.ID.eq(model.getPhaseId()))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne());
    }

    if (db.selectFrom(PHASE_MEME)
      .where(PHASE_MEME.PHASE_ID.eq(model.getPhaseId()))
      .and(PHASE_MEME.NAME.eq(model.getName()))
      .fetchOne() != null) {
      throw new BusinessException("Phase Meme already exists!");
    }

    return JSON.objectFromRecord(executeCreate(db, PHASE_MEME, fieldValues));
  }

  /**
   * Read one Phase Meme where able
   *
   * @param db     context
   * @param access control
   * @param id     of record
   * @return record
   */
  private JSONObject readOne(DSLContext db, AccessControl access, ULong id) throws SQLException {
    if (access.isTopLevel()) {
      return JSON.objectFromRecord(db.selectFrom(PHASE_MEME)
        .where(PHASE_MEME.ID.eq(id))
        .fetchOne());
    } else {
      return JSON.objectFromRecord(db.select(PHASE_MEME.fields()).from(PHASE_MEME)
        .join(PHASE).on(PHASE.ID.eq(PHASE_MEME.PHASE_ID))
        .join(IDEA).on(IDEA.ID.eq(PHASE.IDEA_ID))
        .join(LIBRARY).on(IDEA.LIBRARY_ID.eq(LIBRARY.ID))
        .where(PHASE_MEME.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne());
    }
  }

  /**
   * Read all Memes of an Phase where able
   *
   * @param db     context
   * @param access control
   * @param phaseId to read memes for
   * @return array of phase memes
   * @throws SQLException if failure
   */
  private JSONArray readAllAble(DSLContext db, AccessControl access, ULong phaseId) throws SQLException {
    if (access.isTopLevel()) {
      return JSON.arrayFromResultSet(db.selectFrom(PHASE_MEME)
        .where(PHASE_MEME.PHASE_ID.eq(phaseId))
        .fetchResultSet());
    } else {
      return JSON.arrayFromResultSet(db.select(PHASE_MEME.fields()).from(PHASE_MEME)
        .join(PHASE).on(PHASE.ID.eq(PHASE_MEME.PHASE_ID))
        .join(IDEA).on(IDEA.ID.eq(PHASE.IDEA_ID))
        .join(LIBRARY).on(IDEA.LIBRARY_ID.eq(LIBRARY.ID))
        .where(PHASE.ID.eq(phaseId))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetchResultSet());
    }
  }

  /**
   * Delete an PhaseMeme record
   *
   * @param db     context
   * @param access control
   * @param id     to delete
   * @throws BusinessException if failure
   */
  // TODO: fail if no phaseMeme is deleted
  private void delete(DSLContext db, AccessControl access, ULong id) throws BusinessException {
    if (!access.isTopLevel()) {
      Record record = db.select(PHASE_MEME.ID).from(PHASE_MEME)
        .join(PHASE).on(PHASE.ID.eq(PHASE_MEME.PHASE_ID))
        .join(IDEA).on(IDEA.ID.eq(PHASE.IDEA_ID))
        .join(LIBRARY).on(IDEA.LIBRARY_ID.eq(LIBRARY.ID))
        .where(PHASE_MEME.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne();
      requireRecordExists("Phase Meme", record);
    }

    db.deleteFrom(PHASE_MEME)
      .where(PHASE_MEME.ID.eq(id))
      .execute();
  }

}
