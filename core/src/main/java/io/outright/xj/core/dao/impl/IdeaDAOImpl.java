// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.dao.impl;

import io.outright.xj.core.app.access.AccessControl;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.app.exception.ConfigException;
import io.outright.xj.core.app.exception.DatabaseException;
import io.outright.xj.core.dao.IdeaDAO;
import io.outright.xj.core.db.sql.SQLConnection;
import io.outright.xj.core.db.sql.SQLDatabaseProvider;
import io.outright.xj.core.model.idea.IdeaWrapper;
import io.outright.xj.core.tables.records.IdeaRecord;
import io.outright.xj.core.transport.JSON;

import com.google.inject.Inject;
import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.annotation.Nullable;
import java.sql.SQLException;

import static io.outright.xj.core.Tables.CHOICE;
import static io.outright.xj.core.Tables.IDEA;
import static io.outright.xj.core.tables.IdeaMeme.IDEA_MEME;
import static io.outright.xj.core.tables.Library.LIBRARY;
import static io.outright.xj.core.tables.Phase.PHASE;

public class IdeaDAOImpl extends DAOImpl implements IdeaDAO {
  //  private static Logger log = LoggerFactory.getLogger(IdeaDAOImpl.class);

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
  public JSONArray readAllIn(AccessControl access, ULong libraryId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAllIn(tx.getContext(), access, libraryId));
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
   * Create a record
   * @param db context
   * @param access control
   * @param data for new record
   * @return newly created record
   * @throws BusinessException on failure
   */
  private JSONObject create(DSLContext db, AccessControl access, IdeaWrapper data) throws BusinessException {
    IdeaRecord record;
    record = db.newRecord(IDEA);
    data.validate();
    data.getIdea().intoFieldValueMap().forEach(record::setValue);
    if (access.isAdmin()) {
      // Admin can create idea in any existing library, with any user.
      requireRecordExists("Library",
        db.select(LIBRARY.ID).from(LIBRARY)
          .where(LIBRARY.ID.eq(data.getIdea().getLibraryId()))
          .fetchOne());
    } else {
      // Not admin, must have account access, created by self user.
      requireRecordExists("Library",
        db.select(LIBRARY.ID).from(LIBRARY)
          .where(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
          .and(LIBRARY.ID.eq(data.getIdea().getLibraryId()))
          .fetchOne());
      record.setUserId(access.getUserId());
    }

    record.store();

    return JSON.objectFromRecord(record);
  }

  /**
   * Read one record
   * @param db context
   * @param access control
   * @param id of record
   * @return record
   */
  @Nullable
  private JSONObject readOne(DSLContext db, AccessControl access, ULong id) {
    if (access.isAdmin()) {
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
   * Read all records in parent record by id
   * @param db context
   * @param access control
   * @param libraryId of parent
   * @return array of records
   */
  private JSONArray readAllIn(DSLContext db, AccessControl access, ULong libraryId) throws SQLException {
    if (access.isAdmin()) {
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
   * Update a record
   * @param db context
   * @param access control
   * @param id of record
   * @param data to update with
   * @throws BusinessException if a Business Rule is violated
   * @throws Exception on database failure
   */
  private void update(DSLContext db, AccessControl access, ULong id, IdeaWrapper data) throws Exception {
    data.validate();

    IdeaRecord record;
    record = db.newRecord(IDEA);
    record.setId(id);
    data.getIdea().intoFieldValueMap().forEach(record::setValue);

    if (access.isAdmin()) {
      // Admin can create idea in any existing library, with any user.
      requireRecordExists("Library",
        db.select(LIBRARY.ID).from(LIBRARY)
          .where(LIBRARY.ID.eq(data.getIdea().getLibraryId()))
          .fetchOne());
    } else {
      // Not admin, must have account access, created by self user.
      requireRecordExists("Library",
        db.select(LIBRARY.ID).from(LIBRARY)
          .where(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
          .and(LIBRARY.ID.eq(data.getIdea().getLibraryId()))
          .fetchOne());
      record.setUserId(access.getUserId());
    }

    if (db.executeUpdate(record)==0) {
      throw new DatabaseException("No records updated.");
    }
  }

  /**
   * Delete an Idea
   *
   * @param db     context
   * @param ideaId to delete
   * @throws Exception if database failure
   * @throws ConfigException   if not configured properly
   * @throws BusinessException if fails business rule
   */
  private void delete(DSLContext db, AccessControl access, ULong ideaId) throws Exception {


    requireEmptyResultSet(db.select(PHASE.ID)
      .from(PHASE)
      .where(PHASE.IDEA_ID.eq(ideaId))
      .fetchResultSet());

    requireEmptyResultSet(db.select(CHOICE.ID)
      .from(CHOICE)
      .where(CHOICE.IDEA_ID.eq(ideaId))
      .fetchResultSet());

    requireEmptyResultSet(db.select(IDEA_MEME.ID)
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

}
