// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.dao.impl;

import io.outright.xj.core.app.access.AccessControl;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.app.exception.ConfigException;
import io.outright.xj.core.app.exception.DatabaseException;
import io.outright.xj.core.dao.LibraryDAO;
import io.outright.xj.core.db.sql.SQLConnection;
import io.outright.xj.core.db.sql.SQLDatabaseProvider;
import io.outright.xj.core.model.library.Library;
import io.outright.xj.core.model.library.LibraryWrapper;
import io.outright.xj.core.tables.records.LibraryRecord;
import io.outright.xj.core.transport.JSON;

import com.google.inject.Inject;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.types.ULong;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.annotation.Nullable;
import java.sql.SQLException;
import java.util.Map;

import static io.outright.xj.core.Tables.IDEA;
import static io.outright.xj.core.Tables.LIBRARY;
import static io.outright.xj.core.tables.Account.ACCOUNT;
import static io.outright.xj.core.tables.Instrument.INSTRUMENT;

public class LibraryDAOImpl extends DAOImpl implements LibraryDAO {

  @Inject
  public LibraryDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public JSONObject create(AccessControl access, LibraryWrapper data) throws Exception {
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
  public JSONArray readAllIn(AccessControl access, ULong accountId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAllIn(tx.getContext(), access, accountId));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void update(AccessControl access, ULong id, LibraryWrapper data) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      update(tx.getContext(), access, id, data);
      tx.success();
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void delete(AccessControl access, ULong libraryId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      delete(tx.getContext(), access, libraryId);
      tx.success();
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  /**
   * Create a new record
   *
   * @param db     context
   * @param access control
   * @param data   for new record
   * @return newly created record
   * @throws BusinessException if a Business Rule is violated
   */
  private JSONObject create(DSLContext db, AccessControl access, LibraryWrapper data) throws BusinessException {
    Library model = data.validate();
    Map<Field, Object> fieldValues = model.intoFieldValueMap();

    requireTopLevel(access);

    return JSON.objectFromRecord(executeCreate(db, LIBRARY, fieldValues));
  }

  /**
   * Read one record
   *
   * @param db     context
   * @param access control
   * @param id     of record
   * @return record
   */
  private JSONObject readOne(DSLContext db, AccessControl access, ULong id) {
    if (access.isTopLevel()) {
      return JSON.objectFromRecord(db.selectFrom(LIBRARY)
        .where(LIBRARY.ID.eq(id))
        .fetchOne());
    } else {
      return JSON.objectFromRecord(db.select(LIBRARY.fields())
        .from(LIBRARY)
        .where(LIBRARY.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne());
    }
  }

  /**
   * Read all records in parent by id
   *
   * @param db        context
   * @param access    control
   * @param accountId of parent
   * @return array of records
   */
  private JSONArray readAllIn(DSLContext db, AccessControl access, ULong accountId) throws SQLException {
    if (access.isTopLevel()) {
      return JSON.arrayFromResultSet(db.select(LIBRARY.fields())
        .from(LIBRARY)
        .where(LIBRARY.ACCOUNT_ID.eq(accountId))
        .fetchResultSet());
    } else {
      return JSON.arrayFromResultSet(db.select(LIBRARY.fields())
        .from(LIBRARY)
        .where(LIBRARY.ACCOUNT_ID.eq(accountId))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetchResultSet());
    }
  }

  /**
   * Update a record
   *
   * @param db     context
   * @param access control
   * @param id     of record
   * @param data   to update with
   * @throws BusinessException if a Business Rule is violated
   */
  private void update(DSLContext db, AccessControl access, ULong id, LibraryWrapper data) throws BusinessException, DatabaseException {
    Library model = data.validate();
    Map<Field, Object> fieldValues = model.intoFieldValueMap();
    fieldValues.put(LIBRARY.ID, id);

    requireTopLevel(access);

    requireRecordExists("Account",
      db.selectFrom(ACCOUNT)
        .where(ACCOUNT.ID.eq(model.getAccountId()))
        .fetchOne());

    if (executeUpdate(db, LIBRARY, fieldValues) == 0) {
      throw new BusinessException("No records updated.");
    }
  }

  /**
   * Delete a Library
   *
   * @param db        context
   * @param access    control
   * @param libraryId to delete
   * @throws Exception if database failure
   * @throws ConfigException   if not configured properly
   * @throws BusinessException if fails business rule
   */
  private void delete(DSLContext db, AccessControl access, ULong libraryId) throws Exception {
    requireTopLevel(access);

    requireEmptyResultSet(db.select(IDEA.ID)
      .from(IDEA)
      .where(IDEA.LIBRARY_ID.eq(libraryId))
      .fetchResultSet());

    requireEmptyResultSet(db.select(INSTRUMENT.ID)
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

}
