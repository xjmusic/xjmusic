// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.dao.jooq;

import io.outright.xj.core.app.access.AccessControl;
import io.outright.xj.core.app.db.SQLDatabaseProvider;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.app.exception.ConfigException;
import io.outright.xj.core.app.exception.DatabaseException;
import io.outright.xj.core.dao.PhaseMemeDAO;
import io.outright.xj.core.model.phase_meme.PhaseMemeWrapper;
import io.outright.xj.core.tables.records.PhaseMemeRecord;
import io.outright.xj.core.transport.JSON;

import com.google.inject.Inject;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.types.ULong;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

import static io.outright.xj.core.tables.Idea.IDEA;
import static io.outright.xj.core.tables.Phase.PHASE;
import static io.outright.xj.core.tables.PhaseMeme.PHASE_MEME;
import static io.outright.xj.core.tables.Library.LIBRARY;

/**
 * PhaseMeme DAO
 * <p>
 * TODO [core] more specific permissions of user (artist) access by per-entity ownership
 */
public class PhaseMemeDAOImpl implements PhaseMemeDAO {
  private static Logger log = LoggerFactory.getLogger(PhaseMemeDAOImpl.class);
  private SQLDatabaseProvider dbProvider;

  @Inject
  public PhaseMemeDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public JSONObject create(AccessControl access, PhaseMemeWrapper data) throws DatabaseException, ConfigException, BusinessException {
    Connection conn = dbProvider.getConnectionTransaction();
    DSLContext db = dbProvider.getContext(conn);
    try {
      JSONObject result = JSON.objectFromRecord(create(db, access, data));
      dbProvider.commitAndClose(conn);
      return result;

    } catch (Exception e) {
      dbProvider.rollbackAndClose(conn);
      throw e;
    }
  }

  @Override
  public JSONObject readOneAble(AccessControl access, ULong id) throws DatabaseException {
    Connection conn = dbProvider.getConnection();
    DSLContext db = dbProvider.getContext(conn);
    try {
      JSONObject result = readOneAble(db, access, id);
      dbProvider.close(conn);
      return result;

    } catch (SQLException e) {
      dbProvider.close(conn);
      throw new DatabaseException("SQLException: " + e);
    }
  }

  @Override
  public JSONArray readAllAble(AccessControl access, ULong phaseId) throws DatabaseException {
    Connection conn = dbProvider.getConnection();
    DSLContext db = dbProvider.getContext(conn);
    try {
      JSONArray result = readAllAble(db, access, phaseId);
      dbProvider.close(conn);
      return result;

    } catch (SQLException e) {
      dbProvider.close(conn);
      throw new DatabaseException("SQLException: " + e);
    }
  }

  @Override
  public void delete(AccessControl access, ULong id) throws DatabaseException, ConfigException, BusinessException {
    Connection conn = dbProvider.getConnectionTransaction();
    DSLContext db = dbProvider.getContext(conn);
    try {
      delete(db, access, id);
      dbProvider.commitAndClose(conn);

    } catch (Exception e) {
      dbProvider.rollbackAndClose(conn);
      throw e;
    }
  }

  /**
   * Create a new Phase Meme record
   *
   * @param db     context
   * @param access control
   * @param data   for new PhaseMeme
   * @return new record
   * @throws DatabaseException if database failure
   * @throws ConfigException   if not configured properly
   * @throws BusinessException if fails business rule
   */
  private PhaseMemeRecord create(DSLContext db, AccessControl access, PhaseMemeWrapper data) throws DatabaseException, ConfigException, BusinessException {
    data.validate();

    ULong phaseId = ULong.valueOf(data.getPhaseMeme().getPhaseId());
    String name = data.getPhaseMeme().getName();

    if (access.isAdmin()) {
      assertRecordExists(db.select(PHASE.ID).from(PHASE)
        .where(PHASE.ID.eq(phaseId))
        .fetchOne(), "Phase");
    } else {
      assertRecordExists(db.select(PHASE.ID).from(PHASE)
        .join(IDEA).on(IDEA.ID.eq(PHASE.IDEA_ID))
        .join(LIBRARY).on(IDEA.LIBRARY_ID.eq(LIBRARY.ID))
        .where(PHASE.ID.eq(phaseId))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne(), "Phase");
    }

    if (db.selectFrom(PHASE_MEME)
      .where(PHASE_MEME.PHASE_ID.eq(phaseId))
      .and(PHASE_MEME.NAME.eq(name))
      .fetchOne() != null) {
      throw new BusinessException("Phase Meme already exists!");
    }

    PhaseMemeRecord record;
    record = db.newRecord(PHASE_MEME);
    data.getPhaseMeme().intoFieldValueMap().forEach(record::setValue);

    try {
      record.store();
    } catch (Exception e) {
      log.warn("Cannot create PhaseMeme", e.getMessage());
      throw new BusinessException("Cannot create Phase Meme. Please ensure name+phaseId are valid and unique.");
    }

    return record;
  }

  /**
   * Read one Phase Meme where able
   *
   * @param db     context
   * @param access control
   * @param id     of record
   * @return record
   */
  private JSONObject readOneAble(DSLContext db, AccessControl access, ULong id) throws SQLException {
    if (access.isAdmin()) {
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
    if (access.isAdmin()) {
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
    if (!access.isAdmin()) {
      Record record = db.select(PHASE_MEME.ID).from(PHASE_MEME)
        .join(PHASE).on(PHASE.ID.eq(PHASE_MEME.PHASE_ID))
        .join(IDEA).on(IDEA.ID.eq(PHASE.IDEA_ID))
        .join(LIBRARY).on(IDEA.LIBRARY_ID.eq(LIBRARY.ID))
        .where(PHASE_MEME.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne();
      assertRecordExists(record, "Phase Meme");
    }

    db.deleteFrom(PHASE_MEME)
      .where(PHASE_MEME.ID.eq(id))
      .execute();
  }

  /**
   * Assert that a record exists
   *
   * @param record     to assert
   * @param recordName name of record (for error message)
   * @throws BusinessException if not exists
   */
  private void assertRecordExists(Record record, String recordName) throws BusinessException {
    if (record == null) {
      throw new BusinessException(recordName + " not found");
    }
  }

}
