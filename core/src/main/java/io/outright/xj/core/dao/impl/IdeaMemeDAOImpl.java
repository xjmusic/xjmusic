// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.dao.impl;

import io.outright.xj.core.app.access.AccessControl;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.app.exception.ConfigException;
import io.outright.xj.core.dao.IdeaMemeDAO;
import io.outright.xj.core.db.sql.SQLConnection;
import io.outright.xj.core.db.sql.SQLDatabaseProvider;
import io.outright.xj.core.model.idea_meme.IdeaMeme;
import io.outright.xj.core.model.idea_meme.IdeaMemeWrapper;
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
import static io.outright.xj.core.tables.IdeaMeme.IDEA_MEME;
import static io.outright.xj.core.tables.Library.LIBRARY;

/**
 * IdeaMeme DAO
 * <p>
 * TODO [core] more specific permissions of user (artist) access by per-entity ownership
 */
public class IdeaMemeDAOImpl extends DAOImpl implements IdeaMemeDAO {

  @Inject
  public IdeaMemeDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public JSONObject create(AccessControl access, IdeaMemeWrapper data) throws Exception {
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
      return tx.success(readOneAble(tx.getContext(), access, id));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public JSONArray readAllIn(AccessControl access, ULong ideaId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAllIn(tx.getContext(), access, ideaId));
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
   * Create a new Idea Meme record
   *
   * @param db     context
   * @param access control
   * @param data   for new IdeaMeme
   * @return new record
   * @throws Exception         if database failure
   * @throws ConfigException   if not configured properly
   * @throws BusinessException if fails business rule
   */
  private JSONObject create(DSLContext db, AccessControl access, IdeaMemeWrapper data) throws Exception {
    IdeaMeme model = data.validate();
    Map<Field, Object> fieldValues = model.intoFieldValueMap();

    if (access.isTopLevel()) {
      requireRecordExists("Idea", db.select(IDEA.ID).from(IDEA)
        .where(IDEA.ID.eq(model.getIdeaId()))
        .fetchOne());
    } else {
      requireRecordExists("Idea", db.select(IDEA.ID).from(IDEA)
        .join(LIBRARY).on(IDEA.LIBRARY_ID.eq(LIBRARY.ID))
        .where(IDEA.ID.eq(model.getIdeaId()))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne());
    }

    if (db.selectFrom(IDEA_MEME)
      .where(IDEA_MEME.IDEA_ID.eq(model.getIdeaId()))
      .and(IDEA_MEME.NAME.eq(model.getName()))
      .fetchOne() != null) {
      throw new BusinessException("Idea Meme already exists!");
    }

    return JSON.objectFromRecord(executeCreate(db, IDEA_MEME, fieldValues));
  }

  /**
   * Read one Idea Meme where able
   *
   * @param db     context
   * @param access control
   * @param id     of record
   * @return record
   */
  private JSONObject readOneAble(DSLContext db, AccessControl access, ULong id) throws SQLException {
    if (access.isTopLevel()) {
      return JSON.objectFromRecord(db.selectFrom(IDEA_MEME)
        .where(IDEA_MEME.ID.eq(id))
        .fetchOne());
    } else {
      return JSON.objectFromRecord(db.select(IDEA_MEME.fields()).from(IDEA_MEME)
        .join(IDEA).on(IDEA.ID.eq(IDEA_MEME.IDEA_ID))
        .join(LIBRARY).on(IDEA.LIBRARY_ID.eq(LIBRARY.ID))
        .where(IDEA_MEME.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne());
    }
  }

  /**
   * Read all Memes of an Idea where able
   *
   * @param db     context
   * @param access control
   * @param ideaId to read memes for
   * @return array of idea memes
   * @throws SQLException if failure
   */
  private JSONArray readAllIn(DSLContext db, AccessControl access, ULong ideaId) throws SQLException {
    if (access.isTopLevel()) {
      return JSON.arrayFromResultSet(db.selectFrom(IDEA_MEME)
        .where(IDEA_MEME.IDEA_ID.eq(ideaId))
        .fetchResultSet());
    } else {
      return JSON.arrayFromResultSet(db.select(IDEA_MEME.fields()).from(IDEA_MEME)
        .join(IDEA).on(IDEA.ID.eq(IDEA_MEME.IDEA_ID))
        .join(LIBRARY).on(IDEA.LIBRARY_ID.eq(LIBRARY.ID))
        .where(IDEA.ID.eq(ideaId))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetchResultSet());
    }
  }

  /**
   * Delete an IdeaMeme record
   *
   * @param db     context
   * @param access control
   * @param id     to delete
   * @throws BusinessException if failure
   */
  // TODO: fail if no ideaMeme is deleted
  private void delete(DSLContext db, AccessControl access, ULong id) throws BusinessException {
    if (!access.isTopLevel()) {
      Record record = db.select(IDEA_MEME.ID).from(IDEA_MEME)
        .join(IDEA).on(IDEA.ID.eq(IDEA_MEME.IDEA_ID))
        .join(LIBRARY).on(IDEA.LIBRARY_ID.eq(LIBRARY.ID))
        .where(IDEA_MEME.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne();
      requireRecordExists("Idea Meme", record);
    }

    db.deleteFrom(IDEA_MEME)
      .where(IDEA_MEME.ID.eq(id))
      .execute();
  }

}
