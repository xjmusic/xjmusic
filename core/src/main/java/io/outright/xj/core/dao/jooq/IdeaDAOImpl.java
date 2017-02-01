// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.dao.jooq;

import io.outright.xj.core.app.access.AccessControl;
import io.outright.xj.core.app.db.SQLDatabaseProvider;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.app.exception.ConfigException;
import io.outright.xj.core.app.exception.DatabaseException;
import io.outright.xj.core.dao.IdeaDAO;
import io.outright.xj.core.model.idea.IdeaWrapper;
import io.outright.xj.core.tables.records.IdeaRecord;
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

import static io.outright.xj.core.Tables.CHOICE;
import static io.outright.xj.core.Tables.IDEA;
import static io.outright.xj.core.tables.IdeaMeme.IDEA_MEME;
import static io.outright.xj.core.tables.Library.LIBRARY;
import static io.outright.xj.core.tables.Phase.PHASE;

public class IdeaDAOImpl implements IdeaDAO {
  //  private static Logger log = LoggerFactory.getLogger(IdeaDAOImpl.class);
  private SQLDatabaseProvider dbProvider;

  @Inject
  public IdeaDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public JSONObject create(AccessControl access, IdeaWrapper data) throws DatabaseException, ConfigException, BusinessException {
    Connection conn = dbProvider.getConnectionTransaction();
    DSLContext db = dbProvider.getContext(conn);
    IdeaRecord record;

    try {
      record = db.newRecord(IDEA);
      data.validate();
      data.getIdea().intoFieldValueMap().forEach(record::setValue);

      if (access.isAdmin()) {
        // Admin can create idea in any existing library, with any user.
        assertRecordExists(db.select(LIBRARY.ID).from(LIBRARY)
          .where(LIBRARY.ID.eq(data.getIdea().getLibraryId()))
          .fetchOne(), "Library");
      } else {
        // Not admin, must have account access, created by self user.
        assertRecordExists(db.select(LIBRARY.ID).from(LIBRARY)
          .where(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
          .and(LIBRARY.ID.eq(data.getIdea().getLibraryId()))
          .fetchOne(), "Library");
        record.setUserId(access.getUserId());
      }

      record.store();

      dbProvider.commitAndClose(conn);
    } catch (Exception e) {
      dbProvider.rollbackAndClose(conn);
      throw e;
    }

    return JSON.objectFromRecord(record);
  }

  @Override
  @Nullable
  public JSONObject readOneAble(AccessControl access, ULong id) throws DatabaseException {
    Connection conn = dbProvider.getConnection();
    DSLContext db = dbProvider.getContext(conn);

    JSONObject result;
    try {
      if (access.isAdmin()) {
        result = JSON.objectFromRecord(db.selectFrom(IDEA)
          .where(IDEA.ID.eq(id))
          .fetchOne());
      } else {
        result = JSON.objectFromRecord(db.select(IDEA.fields())
          .from(IDEA)
          .join(LIBRARY).on(LIBRARY.ID.eq(IDEA.LIBRARY_ID))
          .where(IDEA.ID.eq(id))
          .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
          .fetchOne());
      }
    } catch (Exception e) {
      dbProvider.close(conn);
      throw new DatabaseException(e.getClass().getName() + ": " + e.getMessage());
    }

    dbProvider.close(conn);
    return result;
  }

  @Override
  @Nullable
  public JSONArray readAllAble(AccessControl access, ULong libraryId) throws DatabaseException {
    Connection conn = dbProvider.getConnection();
    DSLContext db = dbProvider.getContext(conn);

    JSONArray result;
    try {
      if (access.isAdmin()) {
        result = JSON.arrayFromResultSet(db.select(IDEA.fields())
          .from(IDEA)
          .where(IDEA.LIBRARY_ID.eq(libraryId))
          .fetchResultSet());
      } else {
        result = JSON.arrayFromResultSet(db.select(IDEA.fields())
          .from(IDEA)
          .join(LIBRARY).on(LIBRARY.ID.eq(IDEA.LIBRARY_ID))
          .where(IDEA.LIBRARY_ID.eq(libraryId))
          .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
          .fetchResultSet());
      }

    } catch (SQLException e) {
      dbProvider.close(conn);
      throw new DatabaseException("SQLException: " + e);
    }

    dbProvider.close(conn);
    return result;
  }

  @Override
  public void update(AccessControl access, ULong id, IdeaWrapper data) throws DatabaseException, ConfigException, BusinessException {
    Connection conn = dbProvider.getConnectionTransaction();
    DSLContext db = dbProvider.getContext(conn);
    IdeaRecord record;

    try {
      record = db.newRecord(IDEA);
      record.setId(id);
      data.validate();
      data.getIdea().intoFieldValueMap().forEach(record::setValue);

      if (access.isAdmin()) {
        // Admin can create idea in any existing library, with any user.
        assertRecordExists(db.select(LIBRARY.ID).from(LIBRARY)
          .where(LIBRARY.ID.eq(data.getIdea().getLibraryId()))
          .fetchOne(), "Library");
      } else {
        // Not admin, must have account access, created by self user.
        assertRecordExists(db.select(LIBRARY.ID).from(LIBRARY)
          .where(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
          .and(LIBRARY.ID.eq(data.getIdea().getLibraryId()))
          .fetchOne(), "Library");
        record.setUserId(access.getUserId());
      }

      if (db.executeUpdate(record)==0) {
        throw new DatabaseException("No records updated.");
      }

      dbProvider.commitAndClose(conn);
    } catch (Exception e) {
      dbProvider.rollbackAndClose(conn);
      throw e;
    }
  }

  @Override
  public void delete(ULong ideaId) throws DatabaseException, ConfigException, BusinessException {
    Connection conn = dbProvider.getConnectionTransaction();
    DSLContext db = dbProvider.getContext(conn);

    try {
      delete(db, ideaId);
      dbProvider.commitAndClose(conn);
    } catch (Exception e) {
      dbProvider.rollbackAndClose(conn);
      throw e;
    }
  }

  /**
   * Delete an Idea
   *
   * @param db     context
   * @param ideaId to delete
   * @throws DatabaseException if database failure
   * @throws ConfigException   if not configured properly
   * @throws BusinessException if fails business rule
   */
  private void delete(DSLContext db, ULong ideaId) throws DatabaseException, ConfigException, BusinessException {
    assertEmptyResultSet(db.select(PHASE.ID)
      .from(PHASE)
      .where(PHASE.IDEA_ID.eq(ideaId))
      .fetchResultSet());

    assertEmptyResultSet(db.select(CHOICE.ID)
      .from(CHOICE)
      .where(CHOICE.IDEA_ID.eq(ideaId))
      .fetchResultSet());

    assertEmptyResultSet(db.select(IDEA_MEME.ID)
      .from(IDEA_MEME)
      .where(IDEA_MEME.IDEA_ID.eq(ideaId))
      .fetchResultSet());

    db.deleteFrom(IDEA)
      .where(IDEA.ID.eq(ideaId))
      .andNotExists(
        db.select(PHASE.ID)
          .from(PHASE)
          .where(PHASE.IDEA_ID.eq(ideaId))
      )
      .andNotExists(
        db.select(CHOICE.ID)
          .from(CHOICE)
          .where(CHOICE.IDEA_ID.eq(ideaId))
      )
      .andNotExists(
        db.select(IDEA_MEME.ID)
          .from(IDEA_MEME)
          .where(IDEA_MEME.IDEA_ID.eq(ideaId))
      )
      .execute();
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
        throw new BusinessException("Cannot delete Idea which has one or more " + resultSet.getMetaData().getTableName(1) + ".");
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
