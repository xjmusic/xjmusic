// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao.impl;

import io.xj.core.access.impl.Access;
import io.xj.core.dao.PatternMemeDAO;
import io.xj.core.exception.BusinessException;
import io.xj.core.exception.ConfigException;
import io.xj.core.model.pattern_meme.PatternMeme;
import io.xj.core.persistence.sql.SQLDatabaseProvider;
import io.xj.core.persistence.sql.impl.SQLConnection;
import io.xj.core.tables.records.PatternMemeRecord;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.types.ULong;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

import static io.xj.core.tables.Library.LIBRARY;
import static io.xj.core.tables.Pattern.PATTERN;
import static io.xj.core.tables.PatternMeme.PATTERN_MEME;

/**
 PatternMeme DAO
 <p>
 future: more specific permissions of user (artist) access by per-entity ownership
 */
public class PatternMemeDAOImpl extends DAOImpl implements PatternMemeDAO {

  @Inject
  public PatternMemeDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public PatternMemeRecord create(Access access, PatternMeme entity) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(createRecord(tx.getContext(), access, entity));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public PatternMemeRecord readOne(Access access, ULong id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOneRecord(tx.getContext(), access, id));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Collection<PatternMeme> readAll(Access access, ULong patternId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAll(tx.getContext(), access, patternId));
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
   Create a new Pattern Meme record

   @param db     context
   @param access control
   @param entity for new PatternMeme
   @return new record
   @throws Exception         if database failure
   @throws ConfigException   if not configured properly
   @throws BusinessException if fails business rule
   */
  private PatternMemeRecord createRecord(DSLContext db, Access access, PatternMeme entity) throws Exception {
    entity.validate();

    Map<Field, Object> fieldValues = entity.updatableFieldValueMap();

    if (access.isTopLevel())
      requireExists("Pattern", db.selectCount().from(PATTERN)
        .where(PATTERN.ID.eq(entity.getPatternId()))
        .fetchOne(0, int.class));
    else
      requireExists("Pattern", db.selectCount().from(PATTERN)
        .join(LIBRARY).on(PATTERN.LIBRARY_ID.eq(LIBRARY.ID))
        .where(PATTERN.ID.eq(entity.getPatternId()))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne(0, int.class));

    if (null != db.selectFrom(PATTERN_MEME)
      .where(PATTERN_MEME.PATTERN_ID.eq(entity.getPatternId()))
      .and(PATTERN_MEME.NAME.eq(entity.getName()))
      .fetchOne())
      throw new BusinessException("Pattern Meme already exists!");

    return executeCreate(db, PATTERN_MEME, fieldValues);
  }

  /**
   Read one Pattern Meme where able

   @param db     context
   @param access control
   @param id     of record
   @return record
   */
  private PatternMemeRecord readOneRecord(DSLContext db, Access access, ULong id) throws SQLException {
    if (access.isTopLevel())
      return db.selectFrom(PATTERN_MEME)
        .where(PATTERN_MEME.ID.eq(id))
        .fetchOne();
    else
      return recordInto(PATTERN_MEME, db.select(PATTERN_MEME.fields()).from(PATTERN_MEME)
        .join(PATTERN).on(PATTERN.ID.eq(PATTERN_MEME.PATTERN_ID))
        .join(LIBRARY).on(PATTERN.LIBRARY_ID.eq(LIBRARY.ID))
        .where(PATTERN_MEME.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne());
  }

  /**
   Read all Memes of an Pattern where able

   @param db        context
   @param access    control
   @param patternId to readMany memes for
   @return array of pattern memes
   @throws SQLException if failure
   */
  private Collection<PatternMeme> readAll(DSLContext db, Access access, ULong patternId) throws SQLException {
    Collection<PatternMeme> result = Lists.newArrayList();

    if (access.isTopLevel())
      db.selectFrom(PATTERN_MEME)
        .where(PATTERN_MEME.PATTERN_ID.eq(patternId))
        .fetch().forEach((record) -> {
        result.add(new PatternMeme().setFromRecord(record));
      });
    else
      resultInto(PATTERN_MEME, db.select(PATTERN_MEME.fields()).from(PATTERN_MEME)
        .join(PATTERN).on(PATTERN.ID.eq(PATTERN_MEME.PATTERN_ID))
        .join(LIBRARY).on(PATTERN.LIBRARY_ID.eq(LIBRARY.ID))
        .where(PATTERN.ID.eq(patternId))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetch()).forEach((record) -> {
        result.add(new PatternMeme().setFromRecord(record));
      });

    return result;
  }

  /**
   Delete an PatternMeme record

   @param db     context
   @param access control
   @param id     to delete
   @throws BusinessException if failure
   */
  private void delete(DSLContext db, Access access, ULong id) throws BusinessException {
    if (!access.isTopLevel())
      requireExists("Pattern Meme", db.selectCount().from(PATTERN_MEME)
        .join(PATTERN).on(PATTERN.ID.eq(PATTERN_MEME.PATTERN_ID))
        .join(LIBRARY).on(PATTERN.LIBRARY_ID.eq(LIBRARY.ID))
        .where(PATTERN_MEME.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne(0, int.class));

    db.deleteFrom(PATTERN_MEME)
      .where(PATTERN_MEME.ID.eq(id))
      .execute();
  }

}
