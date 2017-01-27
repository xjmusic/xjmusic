// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.dao.jooq;

import io.outright.xj.core.app.access.AccessControl;
import io.outright.xj.core.app.db.SQLDatabaseProvider;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.app.exception.ConfigException;
import io.outright.xj.core.app.exception.DatabaseException;
import io.outright.xj.core.dao.LibraryDAO;
import io.outright.xj.core.model.library.LibraryWrapper;
import io.outright.xj.core.tables.records.LibraryRecord;
import io.outright.xj.core.transport.JSON;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.types.ULong;

import com.google.inject.Inject;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.annotation.Nullable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import static io.outright.xj.core.Tables.IDEA;
import static io.outright.xj.core.Tables.LIBRARY;
import static io.outright.xj.core.tables.Account.ACCOUNT;
import static io.outright.xj.core.tables.Instrument.INSTRUMENT;

public class LibraryDAOImpl implements LibraryDAO {
  //  private static Logger log = LoggerFactory.getLogger(LibraryDAOImpl.class);
  private SQLDatabaseProvider dbProvider;

  @Inject
  public LibraryDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public JSONObject create(LibraryWrapper data) throws DatabaseException, ConfigException, BusinessException {
    Connection conn = dbProvider.getConnectionTransaction();
    DSLContext db = dbProvider.getContext(conn);
    LibraryRecord newRecord;

    try {
      data.validate();

      newRecord = db.newRecord(LIBRARY);
      newRecord.setName(data.getLibrary().getName());
      newRecord.setAccountId(data.getLibrary().getAccountId());
      db.insertInto(LIBRARY)
        .columns(LIBRARY.ACCOUNT_ID, LIBRARY.NAME)
        .values(newRecord.getAccountId(), newRecord.getName())
        .execute();
      newRecord.setId(ULong.valueOf(db.lastID()));

      dbProvider.commitAndClose(conn);
    } catch (Exception e) {
      dbProvider.rollbackAndClose(conn);
      throw e;
    }

    return JSON.objectFromRecord(newRecord);
  }

  @Override
  @Nullable
  public JSONObject readOneAble(AccessControl access, ULong libraryId) throws DatabaseException {
    Connection conn = dbProvider.getConnection();
    DSLContext db = dbProvider.getContext(conn);

    JSONObject result;
    try {
      if (access.isAdmin()) {
        result = JSON.objectFromRecord(db.selectFrom(LIBRARY)
          .where(LIBRARY.ID.eq(libraryId))
          .fetchOne());
      } else {
        result = JSON.objectFromRecord(db.select()
          .from(LIBRARY)
          .where(LIBRARY.ID.eq(libraryId))
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
  public JSONArray readAllAble(AccessControl access, ULong accountId) throws DatabaseException {
    Connection conn = dbProvider.getConnection();
    DSLContext db = dbProvider.getContext(conn);

    JSONArray result;
    try {
      if (access.isAdmin()) {
        result = JSON.arrayFromResultSet(db.select(
          LIBRARY.ID,
          LIBRARY.NAME
        )
          .from(LIBRARY)
          .where(LIBRARY.ACCOUNT_ID.eq(accountId))
          .fetchResultSet());
      } else {
        result = JSON.arrayFromResultSet(db.select(
          LIBRARY.ID,
          LIBRARY.NAME
        )
          .from(LIBRARY)
          .where(LIBRARY.ACCOUNT_ID.eq(accountId))
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
  public void update(ULong libraryId, LibraryWrapper data) throws DatabaseException, ConfigException, BusinessException {
    Connection conn = dbProvider.getConnectionTransaction();
    DSLContext db = dbProvider.getContext(conn);

    try {
      data.validate();

      assertRecordExists(db.selectFrom(ACCOUNT).where(ACCOUNT.ID.eq(data.getLibrary().getAccountId())).fetchOne(), "Account");

      db.update(LIBRARY)
        .set(LIBRARY.NAME, data.getLibrary().getName())
        .set(LIBRARY.ACCOUNT_ID, data.getLibrary().getAccountId())
        .where(LIBRARY.ID.eq(libraryId))
        .execute();

      dbProvider.commitAndClose(conn);
    } catch (Exception e) {
      dbProvider.rollbackAndClose(conn);
      throw e;
    }
  }

  @Override
  public void delete(ULong libraryId) throws DatabaseException, ConfigException, BusinessException {
    Connection conn = dbProvider.getConnectionTransaction();
    DSLContext db = dbProvider.getContext(conn);

    try {
      delete(db, libraryId);
      dbProvider.commitAndClose(conn);
    } catch (Exception e) {
      dbProvider.rollbackAndClose(conn);
      throw e;
    }
  }

  /**
   * Delete an Library
   *
   * @param db        context
   * @param libraryId to delete
   * @throws DatabaseException if database failure
   * @throws ConfigException   if not configured properly
   * @throws BusinessException if fails business rule
   */
  private void delete(DSLContext db, ULong libraryId) throws DatabaseException, ConfigException, BusinessException {
    assertEmptyResultSet(db.select(IDEA.ID)
      .from(IDEA)
      .where(IDEA.LIBRARY_ID.eq(libraryId))
      .fetchResultSet());

    assertEmptyResultSet(db.select(INSTRUMENT.ID)
      .from(INSTRUMENT)
      .where(INSTRUMENT.LIBRARY_ID.eq(libraryId))
      .fetchResultSet());

    db.deleteFrom(LIBRARY)
      .where(LIBRARY.ID.eq(libraryId))
      .andNotExists(
        db.select(IDEA.ID)
          .from(IDEA)
          .where(IDEA.LIBRARY_ID.eq(libraryId))
      )
      .andNotExists(
        db.select(INSTRUMENT.ID)
          .from(INSTRUMENT)
          .where(INSTRUMENT.LIBRARY_ID.eq(libraryId))
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
        throw new BusinessException("Cannot delete Library which has one or more " + resultSet.getMetaData().getTableName(1) + ".");
      }
    } catch (SQLException e) {
      throw new DatabaseException("SQLException: " + e.getMessage());
    }
  }

  /**
   * Assert that a record exists
   * @param record to assert
   * @param recordName name of record (for error message)
   * @throws BusinessException if not exists
   */
  private void assertRecordExists(Record record, String recordName) throws BusinessException {
    if (record == null) {
      throw new BusinessException(recordName + " not found");
    }
  }

}
