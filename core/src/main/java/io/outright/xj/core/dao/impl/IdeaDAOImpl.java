// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.dao.impl;

import io.outright.xj.core.Tables;
import io.outright.xj.core.app.access.impl.AccessControl;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.app.exception.ConfigException;
import io.outright.xj.core.dao.IdeaDAO;
import io.outright.xj.core.db.sql.SQLConnection;
import io.outright.xj.core.db.sql.SQLDatabaseProvider;
import io.outright.xj.core.model.idea.Idea;
import io.outright.xj.core.model.idea.IdeaWrapper;
import io.outright.xj.core.transport.JSON;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.types.ULong;

import com.google.inject.Inject;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.annotation.Nullable;
import java.sql.SQLException;
import java.util.Map;

import static io.outright.xj.core.Tables.CHOICE;
import static io.outright.xj.core.Tables.IDEA;
import static io.outright.xj.core.tables.IdeaMeme.IDEA_MEME;
import static io.outright.xj.core.tables.Library.LIBRARY;
import static io.outright.xj.core.tables.Phase.PHASE;

public class IdeaDAOImpl extends DAOImpl implements IdeaDAO {

  @Inject
  public IdeaDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public JSONObject create(AccessControl access, IdeaWrapper data) throws Exception {
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
  public JSONArray readAllInAccount(AccessControl access, ULong accountId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAllInAccount(tx.getContext(), access, accountId));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public JSONArray readAllInLibrary(AccessControl access, ULong libraryId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAllInLibrary(tx.getContext(), access, libraryId));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void update(AccessControl access, ULong id, IdeaWrapper data) throws Exception {
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
      delete(tx.getContext(), access, id);
      tx.success();
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  /**
   Create a record

   @param db     context
   @param access control
   @param data   for new record
   @return newly created record
   @throws BusinessException on failure
   */
  private JSONObject create(DSLContext db, AccessControl access, IdeaWrapper data) throws BusinessException {
    Idea model = data.validate();
    Map<Field, Object> fieldValues = model.intoFieldValueMap();

    if (access.isTopLevel()) {
      requireRecordExists("Library",
        db.select(LIBRARY.ID).from(LIBRARY)
          .where(LIBRARY.ID.eq(model.getLibraryId()))
          .fetchOne());
    } else {
      requireRecordExists("Library",
        db.select(LIBRARY.ID).from(LIBRARY)
          .where(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
          .and(LIBRARY.ID.eq(model.getLibraryId()))
          .fetchOne());
      fieldValues.put(IDEA.USER_ID, access.getUserId());
    }

    return JSON.objectFromRecord(executeCreate(db, IDEA, fieldValues));
  }

  /**
   Read one record

   @param db     context
   @param access control
   @param id     of record
   @return record
   */
  @Nullable
  private JSONObject readOne(DSLContext db, AccessControl access, ULong id) {
    if (access.isTopLevel()) {
      return JSON.objectFromRecord(db.selectFrom(IDEA)
        .where(IDEA.ID.eq(id))
        .fetchOne());
    } else {
      return JSON.objectFromRecord(db.select(IDEA.fields())
        .from(IDEA)
        .join(LIBRARY).on(LIBRARY.ID.eq(IDEA.LIBRARY_ID))
        .where(IDEA.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne());
    }
  }

  /**
   Read all records in parent record by id

   @param db        context
   @param access    control
   @param accountId of parent
   @return array of records
   */
  private JSONArray readAllInAccount(DSLContext db, AccessControl access, ULong accountId) throws SQLException {
    if (access.isTopLevel()) {
      return JSON.arrayFromResultSet(db.select(IDEA.fields())
        .from(IDEA)
        .join(Tables.LIBRARY).on(IDEA.LIBRARY_ID.eq(Tables.LIBRARY.ID))
        .where(Tables.LIBRARY.ACCOUNT_ID.eq(accountId))
        .fetchResultSet());
    } else {
      return JSON.arrayFromResultSet(db.select(IDEA.fields())
        .from(IDEA)
        .join(Tables.LIBRARY).on(IDEA.LIBRARY_ID.eq(Tables.LIBRARY.ID))
        .where(Tables.LIBRARY.ACCOUNT_ID.in(accountId))
        .and(Tables.LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetchResultSet());
    }
  }

  /**
   Read all records in parent record by id

   @param db        context
   @param access    control
   @param libraryId of parent
   @return array of records
   */
  private JSONArray readAllInLibrary(DSLContext db, AccessControl access, ULong libraryId) throws SQLException {
    if (access.isTopLevel()) {
      return JSON.arrayFromResultSet(db.select(IDEA.fields())
        .from(IDEA)
        .where(IDEA.LIBRARY_ID.eq(libraryId))
        .fetchResultSet());
    } else {
      return JSON.arrayFromResultSet(db.select(IDEA.fields())
        .from(IDEA)
        .join(LIBRARY).on(LIBRARY.ID.eq(IDEA.LIBRARY_ID))
        .where(IDEA.LIBRARY_ID.eq(libraryId))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetchResultSet());
    }
  }

  /**
   Update a record

   @param db     context
   @param access control
   @param id     of record
   @param data   to update with
   @throws BusinessException if a Business Rule is violated
   @throws Exception         on database failure
   */
  private void update(DSLContext db, AccessControl access, ULong id, IdeaWrapper data) throws Exception {
    Idea model = data.validate();
    Map<Field, Object> fieldValues = model.intoFieldValueMap();
    fieldValues.put(IDEA.ID, id);

    if (access.isTopLevel()) {
      requireRecordExists("Library",
        db.select(LIBRARY.ID).from(LIBRARY)
          .where(LIBRARY.ID.eq(model.getLibraryId()))
          .fetchOne());
    } else {
      requireRecordExists("Library",
        db.select(LIBRARY.ID).from(LIBRARY)
          .where(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
          .and(LIBRARY.ID.eq(model.getLibraryId()))
          .fetchOne());
      fieldValues.put(IDEA.USER_ID, access.getUserId());
    }

    if (executeUpdate(db, IDEA, fieldValues) == 0) {
      throw new BusinessException("No records updated.");
    }
  }

  /**
   Delete an Idea

   @param db context
   @param id to delete
   @throws Exception         if database failure
   @throws ConfigException   if not configured properly
   @throws BusinessException if fails business rule
   */
  private void delete(DSLContext db, AccessControl access, ULong id) throws Exception {
    if (!access.isTopLevel()) {
      Record record = db.select(IDEA.fields()).from(IDEA)
        .join(LIBRARY).on(IDEA.LIBRARY_ID.eq(LIBRARY.ID))
        .where(IDEA.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .and(IDEA.USER_ID.eq(access.getUserId()))
        .fetchOne();
      requireRecordExists("Idea belonging to you", record);
    }

    requireEmptyResultSet(db.select(PHASE.ID)
      .from(PHASE)
      .where(PHASE.IDEA_ID.eq(id))
      .fetchResultSet());

    requireEmptyResultSet(db.select(CHOICE.ID)
      .from(CHOICE)
      .where(CHOICE.IDEA_ID.eq(id))
      .fetchResultSet());

    requireEmptyResultSet(db.select(IDEA_MEME.ID)
      .from(IDEA_MEME)
      .where(IDEA_MEME.IDEA_ID.eq(id))
      .fetchResultSet());

    db.deleteFrom(IDEA)
      .where(IDEA.ID.eq(id))
      .andNotExists(
        db.select(PHASE.ID)
          .from(PHASE)
          .where(PHASE.IDEA_ID.eq(id))
      )
      .andNotExists(
        db.select(CHOICE.ID)
          .from(CHOICE)
          .where(CHOICE.IDEA_ID.eq(id))
      )
      .andNotExists(
        db.select(IDEA_MEME.ID)
          .from(IDEA_MEME)
          .where(IDEA_MEME.IDEA_ID.eq(id))
      )
      .execute();
  }

}
