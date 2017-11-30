// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.dao.impl;

import io.xj.core.access.impl.Access;
import io.xj.core.exception.BusinessException;
import io.xj.core.exception.ConfigException;
import io.xj.core.dao.IdeaMemeDAO;
import io.xj.core.persistence.sql.impl.SQLConnection;
import io.xj.core.persistence.sql.SQLDatabaseProvider;
import io.xj.core.model.idea_meme.IdeaMeme;
import io.xj.core.tables.records.IdeaMemeRecord;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Result;
import org.jooq.types.ULong;

import com.google.inject.Inject;

import java.sql.SQLException;
import java.util.Map;

import static io.xj.core.tables.Idea.IDEA;
import static io.xj.core.tables.IdeaMeme.IDEA_MEME;
import static io.xj.core.tables.Library.LIBRARY;

/**
 IdeaMeme DAO
 <p>
 future: more specific permissions of user (artist) access by per-entity ownership
 */
public class IdeaMemeDAOImpl extends DAOImpl implements IdeaMemeDAO {

  @Inject
  public IdeaMemeDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public IdeaMemeRecord create(Access access, IdeaMeme entity) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(createRecord(tx.getContext(), access, entity));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public IdeaMemeRecord readOne(Access access, ULong id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOneRecord(tx.getContext(), access, id));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Result<IdeaMemeRecord> readAll(Access access, ULong ideaId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAll(tx.getContext(), access, ideaId));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void delete(Access access, ULong id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      delete(tx.getContext(), access, id);
      tx.success();
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  /**
   Create a new Idea Meme record

   @param db     context
   @param access control
   @param entity for new IdeaMeme
   @return new record
   @throws Exception         if database failure
   @throws ConfigException   if not configured properly
   @throws BusinessException if fails business rule
   */
  private IdeaMemeRecord createRecord(DSLContext db, Access access, IdeaMeme entity) throws Exception {
    entity.validate();

    Map<Field, Object> fieldValues = entity.updatableFieldValueMap();

    if (access.isTopLevel())
      requireExists("Idea", db.selectCount().from(IDEA)
        .where(IDEA.ID.eq(entity.getIdeaId()))
        .fetchOne(0, int.class));
    else
      requireExists("Idea", db.selectCount().from(IDEA)
        .join(LIBRARY).on(IDEA.LIBRARY_ID.eq(LIBRARY.ID))
        .where(IDEA.ID.eq(entity.getIdeaId()))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne(0, int.class));

    if (null != db.selectFrom(IDEA_MEME)
      .where(IDEA_MEME.IDEA_ID.eq(entity.getIdeaId()))
      .and(IDEA_MEME.NAME.eq(entity.getName()))
      .fetchOne())
      throw new BusinessException("Idea Meme already exists!");

    return executeCreate(db, IDEA_MEME, fieldValues);
  }

  /**
   Read one Idea Meme where able

   @param db     context
   @param access control
   @param id     of record
   @return record
   */
  private IdeaMemeRecord readOneRecord(DSLContext db, Access access, ULong id) throws SQLException {
    if (access.isTopLevel())
      return db.selectFrom(IDEA_MEME)
        .where(IDEA_MEME.ID.eq(id))
        .fetchOne();
    else
      return recordInto(IDEA_MEME, db.select(IDEA_MEME.fields()).from(IDEA_MEME)
        .join(IDEA).on(IDEA.ID.eq(IDEA_MEME.IDEA_ID))
        .join(LIBRARY).on(IDEA.LIBRARY_ID.eq(LIBRARY.ID))
        .where(IDEA_MEME.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne());
  }

  /**
   Read all Memes of an Idea where able

   @param db     context
   @param access control
   @param ideaId to readMany memes for
   @return array of idea memes
   @throws SQLException if failure
   */
  private Result<IdeaMemeRecord> readAll(DSLContext db, Access access, ULong ideaId) throws SQLException {
    if (access.isTopLevel())
      return db.selectFrom(IDEA_MEME)
        .where(IDEA_MEME.IDEA_ID.eq(ideaId))
        .fetch();
    else
      return resultInto(IDEA_MEME, db.select(IDEA_MEME.fields()).from(IDEA_MEME)
        .join(IDEA).on(IDEA.ID.eq(IDEA_MEME.IDEA_ID))
        .join(LIBRARY).on(IDEA.LIBRARY_ID.eq(LIBRARY.ID))
        .where(IDEA.ID.eq(ideaId))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetch());
  }

  /**
   Delete an IdeaMeme record

   @param db     context
   @param access control
   @param id     to delete
   @throws BusinessException if failure
   */
  private void delete(DSLContext db, Access access, ULong id) throws BusinessException {
    if (!access.isTopLevel())
      requireExists("Idea Meme", db.selectCount().from(IDEA_MEME)
        .join(IDEA).on(IDEA.ID.eq(IDEA_MEME.IDEA_ID))
        .join(LIBRARY).on(IDEA.LIBRARY_ID.eq(LIBRARY.ID))
        .where(IDEA_MEME.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne(0, int.class));

    db.deleteFrom(IDEA_MEME)
      .where(IDEA_MEME.ID.eq(id))
      .execute();
  }

}
