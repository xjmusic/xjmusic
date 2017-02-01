// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.dao.jooq;

import io.outright.xj.core.app.access.AccessControl;
import io.outright.xj.core.app.db.SQLDatabaseProvider;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.app.exception.ConfigException;
import io.outright.xj.core.app.exception.DatabaseException;
import io.outright.xj.core.dao.IdeaMemeDAO;
import io.outright.xj.core.model.idea_meme.IdeaMemeWrapper;
import io.outright.xj.core.tables.records.IdeaMemeRecord;
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
import static io.outright.xj.core.tables.IdeaMeme.IDEA_MEME;
import static io.outright.xj.core.tables.Library.LIBRARY;

/**
 * IdeaMeme DAO
 * <p>
 * TODO [core] more specific permissions of user (artist) access by per-entity ownership
 */
public class IdeaMemeDAOImpl implements IdeaMemeDAO {
  private static Logger log = LoggerFactory.getLogger(IdeaMemeDAOImpl.class);
  private SQLDatabaseProvider dbProvider;

  @Inject
  public IdeaMemeDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public JSONObject readOneAble(AccessControl access, ULong id) throws DatabaseException {
    Connection conn = dbProvider.getConnection();
    DSLContext db = dbProvider.getContext(conn);

    JSONObject result;
    if (access.isAdmin()) {
      result = JSON.objectFromRecord(db.selectFrom(IDEA_MEME)
        .where(IDEA_MEME.ID.eq(id))
        .fetchOne());
    } else {
      result = JSON.objectFromRecord(db.select(IDEA_MEME.fields()).from(IDEA_MEME)
        .join(IDEA).on(IDEA.ID.eq(IDEA_MEME.IDEA_ID))
        .join(LIBRARY).on(IDEA.LIBRARY_ID.eq(LIBRARY.ID))
        .where(IDEA_MEME.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne());
    }
    dbProvider.close(conn);
    return result;
  }

  @Override
  public JSONArray readAllAble(AccessControl access, ULong ideaId) throws DatabaseException {
    Connection conn = dbProvider.getConnection();
    DSLContext db = dbProvider.getContext(conn);

    JSONArray result;
    try {
      if (access.isAdmin()) {
        result = JSON.arrayFromResultSet(db.selectFrom(IDEA_MEME)
          .where(IDEA_MEME.IDEA_ID.eq(ideaId))
          .fetchResultSet());
      } else {
        result = JSON.arrayFromResultSet(db.select(IDEA_MEME.fields()).from(IDEA_MEME)
          .join(IDEA).on(IDEA.ID.eq(IDEA_MEME.IDEA_ID))
          .join(LIBRARY).on(IDEA.LIBRARY_ID.eq(LIBRARY.ID))
          .where(IDEA.ID.eq(ideaId))
          .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
          .fetchResultSet());
      }
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
      // TODO: fail if no ideaMeme is deleted
      if (!access.isAdmin()) {
        Record record = db.select(IDEA_MEME.ID).from(IDEA_MEME)
          .join(IDEA).on(IDEA.ID.eq(IDEA_MEME.IDEA_ID))
          .join(LIBRARY).on(IDEA.LIBRARY_ID.eq(LIBRARY.ID))
          .where(IDEA_MEME.ID.eq(id))
          .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
          .fetchOne();
        assertRecordExists(record, "Idea Meme");
      }

      db.deleteFrom(IDEA_MEME)
        .where(IDEA_MEME.ID.eq(id))
        .execute();

      dbProvider.commitAndClose(conn);

    } catch (Exception e) {
      dbProvider.rollbackAndClose(conn);
      throw e;
    }
  }

  @Override
  public JSONObject create(AccessControl access, IdeaMemeWrapper data) throws DatabaseException, ConfigException, BusinessException {
    Connection conn = dbProvider.getConnectionTransaction();
    DSLContext db = dbProvider.getContext(conn);
    JSONObject result;

    try {
      result = JSON.objectFromRecord(create(db, access, data));
      dbProvider.commitAndClose(conn);
    } catch (Exception e) {
      dbProvider.rollbackAndClose(conn);
      throw e;
    }
    return result;
  }

  /**
   * Create a new Idea Meme record
   *
   * @param db     context
   * @param access control
   * @param data   for new IdeaMeme
   * @return new record
   * @throws DatabaseException if database failure
   * @throws ConfigException   if not configured properly
   * @throws BusinessException if fails business rule
   */
  private IdeaMemeRecord create(DSLContext db, AccessControl access, IdeaMemeWrapper data) throws DatabaseException, ConfigException, BusinessException {
    data.validate();

    ULong ideaId = ULong.valueOf(data.getIdeaMeme().getIdeaId());
    String name = data.getIdeaMeme().getName();

    if (access.isAdmin()) {
      assertRecordExists(db.select(IDEA.ID).from(IDEA)
        .where(IDEA.ID.eq(ideaId))
        .fetchOne(), "Idea");
    } else {
      assertRecordExists(db.select(IDEA.ID).from(IDEA)
        .join(LIBRARY).on(IDEA.LIBRARY_ID.eq(LIBRARY.ID))
        .where(IDEA.ID.eq(ideaId))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne(), "Idea");
    }

    if (db.selectFrom(IDEA_MEME)
      .where(IDEA_MEME.IDEA_ID.eq(ideaId))
      .and(IDEA_MEME.NAME.eq(name))
      .fetchOne() != null) {
      throw new BusinessException("Idea Meme already exists!");
    }

    IdeaMemeRecord record;
    record = db.newRecord(IDEA_MEME);
    data.getIdeaMeme().intoFieldValueMap().forEach(record::setValue);

    try {
      record.store();
    } catch (Exception e) {
      log.warn("Cannot create IdeaMeme", e.getMessage());
      throw new BusinessException("Cannot create Idea Meme. Please ensure name+ideaId are valid and unique.");
    }

    return record;
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
