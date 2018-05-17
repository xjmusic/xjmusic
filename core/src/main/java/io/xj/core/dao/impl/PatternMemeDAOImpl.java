// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao.impl;

import io.xj.core.Tables;
import io.xj.core.access.impl.Access;
import io.xj.core.dao.PatternMemeDAO;
import io.xj.core.exception.BusinessException;
import io.xj.core.exception.ConfigException;
import io.xj.core.model.pattern_meme.PatternMeme;
import io.xj.core.persistence.sql.SQLDatabaseProvider;
import io.xj.core.persistence.sql.impl.SQLConnection;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.types.ULong;

import com.google.api.client.util.Maps;
import com.google.inject.Inject;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Map;

import static io.xj.core.tables.Library.LIBRARY;
import static io.xj.core.tables.Sequence.SEQUENCE;
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
  private static PatternMeme create(DSLContext db, Access access, PatternMeme entity) throws Exception {
    entity.validate();

    Map<Field, Object> fieldValues = fieldValueMap(entity);

    if (access.isTopLevel())
      requireExists("Pattern", db.selectCount().from(PATTERN)
        .where(PATTERN.ID.eq(ULong.valueOf(entity.getPatternId())))
        .fetchOne(0, int.class));
    else
      requireExists("Pattern", db.selectCount().from(PATTERN)
        .join(SEQUENCE).on(SEQUENCE.ID.eq(PATTERN.SEQUENCE_ID))
        .join(LIBRARY).on(SEQUENCE.LIBRARY_ID.eq(LIBRARY.ID))
        .where(PATTERN.ID.eq(ULong.valueOf(entity.getPatternId())))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne(0, int.class));

    if (null != db.selectFrom(PATTERN_MEME)
      .where(PATTERN_MEME.PATTERN_ID.eq(ULong.valueOf(entity.getPatternId())))
      .and(PATTERN_MEME.NAME.eq(entity.getName()))
      .fetchOne())
      throw new BusinessException("Pattern Meme already exists!");

    return modelFrom(executeCreate(db, PATTERN_MEME, fieldValues), PatternMeme.class);
  }

  /**
   Read one Pattern Meme where able

   @param db     context
   @param access control
   @param id     of record
   @return record
   */
  private static PatternMeme readOne(DSLContext db, Access access, ULong id) throws BusinessException {
    if (access.isTopLevel())
      return modelFrom(db.selectFrom(PATTERN_MEME)
        .where(PATTERN_MEME.ID.eq(id))
        .fetchOne(), PatternMeme.class);
    else
      return modelFrom(db.select(PATTERN_MEME.fields()).from(PATTERN_MEME)
        .join(PATTERN).on(PATTERN.ID.eq(PATTERN_MEME.PATTERN_ID))
        .join(SEQUENCE).on(SEQUENCE.ID.eq(PATTERN.SEQUENCE_ID))
        .join(LIBRARY).on(SEQUENCE.LIBRARY_ID.eq(LIBRARY.ID))
        .where(PATTERN_MEME.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne(), PatternMeme.class);
  }

  /**
   Read all Memes of an Pattern where able

   @param db       context
   @param access   control
   @param patternIds to readMany memes for
   @return array of pattern memes
   */
  private static Collection<PatternMeme> readAll(DSLContext db, Access access, Collection<ULong> patternIds) throws BusinessException {
    if (access.isTopLevel())
      return modelsFrom(db.selectFrom(PATTERN_MEME)
        .where(PATTERN_MEME.PATTERN_ID.in(patternIds))
        .fetch(), PatternMeme.class);
    else
      return modelsFrom(db.select(PATTERN_MEME.fields()).from(PATTERN_MEME)
        .join(PATTERN).on(PATTERN.ID.eq(PATTERN_MEME.PATTERN_ID))
        .join(SEQUENCE).on(SEQUENCE.ID.eq(PATTERN.SEQUENCE_ID))
        .join(LIBRARY).on(SEQUENCE.LIBRARY_ID.eq(LIBRARY.ID))
        .where(PATTERN.ID.in(patternIds))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .fetch(), PatternMeme.class);
  }

  /**
   Delete an PatternMeme record

   @param db     context
   @param access control
   @param id     to delete
   @throws BusinessException if failure
   */
  private static void delete(DSLContext db, Access access, ULong id) throws BusinessException {
    if (!access.isTopLevel())
      requireExists("Pattern Meme", db.selectCount().from(PATTERN_MEME)
        .join(PATTERN).on(PATTERN.ID.eq(PATTERN_MEME.PATTERN_ID))
        .join(SEQUENCE).on(SEQUENCE.ID.eq(PATTERN.SEQUENCE_ID))
        .join(LIBRARY).on(SEQUENCE.LIBRARY_ID.eq(LIBRARY.ID))
        .where(PATTERN_MEME.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne(0, int.class));

    db.deleteFrom(PATTERN_MEME)
      .where(PATTERN_MEME.ID.eq(id))
      .execute();
  }

  /**
   Only certain (writable) fields are mapped back to jOOQ records--
   Read-only fields are excluded from here.

   @param entity to source values from
   @return values mapped to record fields
   */
  private static Map<Field, Object> fieldValueMap(PatternMeme entity) {
    Map<Field, Object> fieldValues = Maps.newHashMap();
    fieldValues.put(Tables.PATTERN_MEME.PATTERN_ID, ULong.valueOf(entity.getPatternId()));
    fieldValues.put(Tables.PATTERN_MEME.NAME, entity.getName());
    return fieldValues;
  }

  @Override
  public PatternMeme create(Access access, PatternMeme entity) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(create(tx.getContext(), access, entity));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public PatternMeme readOne(Access access, BigInteger id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOne(tx.getContext(), access, ULong.valueOf(id)));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Collection<PatternMeme> readAll(Access access, Collection<BigInteger> parentIds) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAll(tx.getContext(), access, uLongValuesOf(parentIds)));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void update(Access access, BigInteger id, PatternMeme entity) throws Exception {
    throw new BusinessException("Not allowed to update PatternMeme record.");

  }

  @Override
  public void destroy(Access access, BigInteger id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      delete(tx.getContext(), access, ULong.valueOf(id));
      tx.success();
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }


}
