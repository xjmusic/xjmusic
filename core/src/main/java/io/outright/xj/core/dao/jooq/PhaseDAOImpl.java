// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.dao.jooq;

import io.outright.xj.core.app.access.AccessControl;
import io.outright.xj.core.app.db.SQLDatabaseProvider;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.app.exception.ConfigException;
import io.outright.xj.core.app.exception.DatabaseException;
import io.outright.xj.core.dao.PhaseDAO;
import io.outright.xj.core.model.phase.PhaseWrapper;
import io.outright.xj.core.tables.records.PhaseRecord;
import io.outright.xj.core.transport.JSON;

import com.google.inject.Inject;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.types.ULong;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.annotation.Nullable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import static io.outright.xj.core.Tables.PHASE;
import static io.outright.xj.core.Tables.PHASE_MEME;
import static io.outright.xj.core.tables.Idea.IDEA;
import static io.outright.xj.core.tables.Library.LIBRARY;
import static io.outright.xj.core.tables.PhaseChord.PHASE_CHORD;
import static io.outright.xj.core.tables.Voice.VOICE;

public class PhaseDAOImpl implements PhaseDAO {
  //  private static Logger log = LoggerFactory.getLogger(PhaseDAOImpl.class);
  private SQLDatabaseProvider dbProvider;

  @Inject
  public PhaseDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public JSONObject create(AccessControl access, PhaseWrapper data) throws DatabaseException, ConfigException, BusinessException {
    Connection conn = dbProvider.getConnectionTransaction();
    DSLContext db = dbProvider.getContext(conn);

    try {
      PhaseRecord record = create(db, access, data);
      dbProvider.commitAndClose(conn);
      return JSON.objectFromRecord(record);

    } catch (Exception e) {
      dbProvider.rollbackAndClose(conn);
      throw e;
    }
  }

  @Override
  @Nullable
  public JSONObject readOneAble(AccessControl access, ULong id) throws DatabaseException {
    Connection conn = dbProvider.getConnection();
    DSLContext db = dbProvider.getContext(conn);

    try {
      JSONObject result = readOneAble(db, access, id);
      dbProvider.close(conn);
      return result;

    } catch (Exception e) {
      dbProvider.close(conn);
      throw new DatabaseException(e.getClass().getName() + ": " + e.getMessage());
    }

  }

  @Override
  @Nullable
  public JSONArray readAllAble(AccessControl access, ULong ideaId) throws DatabaseException {
    Connection conn = dbProvider.getConnection();
    DSLContext db = dbProvider.getContext(conn);

    try {
      JSONArray result = readAllAble(db, access, ideaId);
      dbProvider.close(conn);
      return result;

    } catch (SQLException e) {
      dbProvider.close(conn);
      throw new DatabaseException("SQLException: " + e);
    }
  }

  @Override
  public void update(AccessControl access, ULong id, PhaseWrapper data) throws DatabaseException, ConfigException, BusinessException {
    Connection conn = dbProvider.getConnectionTransaction();
    DSLContext db = dbProvider.getContext(conn);

    try {
      update(db, access, id, data);
      dbProvider.commitAndClose(conn);

    } catch (Exception e) {
      dbProvider.rollbackAndClose(conn);
      throw e;
    }
  }

  @Override
  public void delete(AccessControl access, ULong id) throws DatabaseException, ConfigException, BusinessException {
    Connection conn = dbProvider.getConnectionTransaction();
    DSLContext db = dbProvider.getContext(conn);

    try {
      delete(db, id);
      dbProvider.commitAndClose(conn);

    } catch (Exception e) {
      dbProvider.rollbackAndClose(conn);
      throw e;
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
  private PhaseRecord create(DSLContext db, AccessControl access, PhaseWrapper data) throws BusinessException {
    PhaseRecord record = db.newRecord(PHASE);
    data.validate();
    data.getPhase().intoFieldValueMap().forEach(record::setValue);

    if (access.isAdmin()) {
      // Admin can create phase in any existing idea
      assertRecordExists(db.select(IDEA.ID).from(IDEA)
        .where(IDEA.ID.eq(data.getPhase().getIdeaId()))
        .fetchOne(), "Idea");
    } else {
      // Not admin, must have account access
      assertRecordExists(db.select(IDEA.ID).from(IDEA)
        .join(LIBRARY).on(LIBRARY.ID.eq(IDEA.LIBRARY_ID))
        .where(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .and(IDEA.ID.eq(data.getPhase().getIdeaId()))
        .fetchOne(), "Idea");
    }

    record.store();
    return record;
  }

  /**
   * Read one Phase if able
   * @param db context
   * @param access control
   * @param id of phase
   * @return phase
   */
  private JSONObject readOneAble(DSLContext db, AccessControl access, ULong id) {
    JSONObject result;
    if (access.isAdmin()) {
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
  private JSONArray readAllAble(DSLContext db, AccessControl access, ULong ideaId) throws SQLException {
    JSONArray result;
    if (access.isAdmin()) {
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
   * @throws DatabaseException if database failure
   * @throws ConfigException   if not configured properly
   * @throws BusinessException if fails business rule
   */
  private void delete(DSLContext db, ULong id) throws DatabaseException, ConfigException, BusinessException {
    assertEmptyResultSet(db.select(VOICE.ID)
      .from(VOICE)
      .where(VOICE.PHASE_ID.eq(id))
      .fetchResultSet());

    assertEmptyResultSet(db.select(PHASE_MEME.ID)
      .from(PHASE_MEME)
      .where(PHASE_MEME.PHASE_ID.eq(id))
      .fetchResultSet());

    assertEmptyResultSet(db.select(PHASE_CHORD.ID)
      .from(PHASE_CHORD)
      .where(PHASE_CHORD.PHASE_ID.eq(id))
      .fetchResultSet());

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
  private void update(DSLContext db, AccessControl access, ULong id, PhaseWrapper data) throws BusinessException, DatabaseException {
    PhaseRecord record;

    record = db.newRecord(PHASE);
    record.setId(id);
    data.validate();
    data.getPhase().intoFieldValueMap().forEach(record::setValue);

    if (access.isAdmin()) {
      // Admin can create phase in any existing idea
      assertRecordExists(db.select(IDEA.ID).from(IDEA)
        .where(IDEA.ID.eq(data.getPhase().getIdeaId()))
        .fetchOne(), "Idea");
    } else {
      // Not admin, must have account access
      assertRecordExists(db.select(IDEA.ID).from(IDEA)
        .join(LIBRARY).on(LIBRARY.ID.eq(IDEA.LIBRARY_ID))
        .where(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .and(IDEA.ID.eq(data.getPhase().getIdeaId()))
        .fetchOne(), "Idea");
    }

    if (db.executeUpdate(record)==0) {
      throw new DatabaseException("No records updated.");
    }
  }


  /**
   * Fail if ResultSet is not empty.
   *
   * @param resultSet to check.
   * @throws BusinessException if result set is not empty.
   * @throws DatabaseException if something goes wrong.
   */
  private void assertEmptyResultSet(ResultSet resultSet) throws BusinessException, DatabaseException {
    try {
      if (resultSet.next()) {
        throw new BusinessException("Cannot delete Phase which has one or more " + resultSet.getMetaData().getTableName(1) + ".");
      }
    } catch (SQLException e) {
      throw new DatabaseException("SQLException: " + e.getMessage());
    }
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
