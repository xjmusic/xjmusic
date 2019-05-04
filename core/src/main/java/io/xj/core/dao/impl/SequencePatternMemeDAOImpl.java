// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao.impl;

import io.xj.core.Tables;
import io.xj.core.access.impl.Access;
import io.xj.core.dao.SequencePatternMemeDAO;
import io.xj.core.exception.CoreException;
import io.xj.core.exception.CoreException;
import io.xj.core.model.sequence_pattern_meme.SequencePatternMeme;
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
import static io.xj.core.tables.SequencePattern.SEQUENCE_PATTERN;
import static io.xj.core.tables.SequencePatternMeme.SEQUENCE_PATTERN_MEME;

/**
 SequencePatternMeme DAO
 <p>
 future: more specific permissions of user (artist) access by per-entity ownership
 */
public class SequencePatternMemeDAOImpl extends DAOImpl implements SequencePatternMemeDAO {

  @Inject
  public SequencePatternMemeDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  /**
   Create a new Pattern Meme record

   @param db     context
   @param access control
   @param entity for new SequencePatternMeme
   @return new record
   @throws CoreException         if database failure
   @throws CoreException   if not configured properly
   @throws CoreException if fails business rule
   */
  private static SequencePatternMeme create(DSLContext db, Access access, SequencePatternMeme entity) throws CoreException {
    entity.validate();

    Map<Field, Object> fieldValues = fieldValueMap(entity);

    if (access.isTopLevel())
      requireExists("Sequence-Pattern", db.selectCount().from(SEQUENCE_PATTERN)
        .where(SEQUENCE_PATTERN.ID.eq(ULong.valueOf(entity.getSequencePatternId())))
        .fetchOne(0, int.class));
    else
      requireExists("Sequence-Pattern", db.selectCount().from(SEQUENCE_PATTERN)
        .join(SEQUENCE).on(SEQUENCE.ID.eq(SEQUENCE_PATTERN.SEQUENCE_ID))
        .join(LIBRARY).on(SEQUENCE.LIBRARY_ID.eq(LIBRARY.ID))
        .where(SEQUENCE_PATTERN.ID.eq(ULong.valueOf(entity.getSequencePatternId())))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne(0, int.class));

    if (null != db.selectFrom(SEQUENCE_PATTERN_MEME)
      .where(SEQUENCE_PATTERN_MEME.SEQUENCE_PATTERN_ID.eq(ULong.valueOf(entity.getSequencePatternId())))
      .and(SEQUENCE_PATTERN_MEME.NAME.eq(entity.getName()))
      .fetchOne())
      throw new CoreException("Pattern Meme already exists!");

    return modelFrom(executeCreate(db, SEQUENCE_PATTERN_MEME, fieldValues), SequencePatternMeme.class);
  }

  /**
   Read one Pattern Meme where able

   @param db     context
   @param access control
   @param id     of record
   @return record
   */
  private static SequencePatternMeme readOne(DSLContext db, Access access, ULong id) throws CoreException {
    if (access.isTopLevel())
      return modelFrom(db.selectFrom(SEQUENCE_PATTERN_MEME)
        .where(SEQUENCE_PATTERN_MEME.ID.eq(id))
        .fetchOne(), SequencePatternMeme.class);
    else
      return modelFrom(db.select(SEQUENCE_PATTERN_MEME.fields()).from(SEQUENCE_PATTERN_MEME)
        .join(SEQUENCE_PATTERN).on(SEQUENCE_PATTERN.ID.eq(SEQUENCE_PATTERN_MEME.SEQUENCE_PATTERN_ID))
        .join(SEQUENCE).on(SEQUENCE.ID.eq(SEQUENCE_PATTERN.SEQUENCE_ID))
        .join(LIBRARY).on(SEQUENCE.LIBRARY_ID.eq(LIBRARY.ID))
        .where(SEQUENCE_PATTERN_MEME.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne(), SequencePatternMeme.class);
  }

  /**
   Read all Memes of an Pattern where able

   @param db       context
   @param access   control
   @param sequencePatternIds to readMany memes for
   @return array of pattern memes
   */
  private static Collection<SequencePatternMeme> readAll(DSLContext db, Access access, Collection<ULong> sequencePatternIds) throws CoreException {
    if (access.isTopLevel())
      return modelsFrom(db.selectFrom(SEQUENCE_PATTERN_MEME)
        .where(SEQUENCE_PATTERN_MEME.SEQUENCE_PATTERN_ID.in(sequencePatternIds))
        .fetch(), SequencePatternMeme.class);
    else
      return modelsFrom(db.select(SEQUENCE_PATTERN_MEME.fields()).from(SEQUENCE_PATTERN_MEME)
        .join(SEQUENCE_PATTERN).on(SEQUENCE_PATTERN.ID.eq(SEQUENCE_PATTERN_MEME.SEQUENCE_PATTERN_ID))
        .join(SEQUENCE).on(SEQUENCE.ID.eq(SEQUENCE_PATTERN.SEQUENCE_ID))
        .join(LIBRARY).on(SEQUENCE.LIBRARY_ID.eq(LIBRARY.ID))
        .where(SEQUENCE_PATTERN.ID.in(sequencePatternIds))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .fetch(), SequencePatternMeme.class);
  }

  /**
   Delete an SequencePatternMeme record

   @param db     context
   @param access control
   @param id     to delete
   @throws CoreException if failure
   */
  private static void delete(DSLContext db, Access access, ULong id) throws CoreException {
    if (!access.isTopLevel())
      requireExists("Sequence Pattern Meme", db.selectCount().from(SEQUENCE_PATTERN_MEME)
        .join(SEQUENCE_PATTERN).on(SEQUENCE_PATTERN.ID.eq(SEQUENCE_PATTERN_MEME.SEQUENCE_PATTERN_ID))
        .join(SEQUENCE).on(SEQUENCE.ID.eq(SEQUENCE_PATTERN.SEQUENCE_ID))
        .join(LIBRARY).on(SEQUENCE.LIBRARY_ID.eq(LIBRARY.ID))
        .where(SEQUENCE_PATTERN_MEME.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne(0, int.class));

    db.deleteFrom(SEQUENCE_PATTERN_MEME)
      .where(SEQUENCE_PATTERN_MEME.ID.eq(id))
      .execute();
  }

  /**
   Only certain (writable) fields are mapped back to jOOQ records--
   Read-only fields are excluded from here.

   @param entity to source values from
   @return values mapped to record fields
   */
  private static Map<Field, Object> fieldValueMap(SequencePatternMeme entity) {
    Map<Field, Object> fieldValues = Maps.newHashMap();
    fieldValues.put(Tables.SEQUENCE_PATTERN_MEME.SEQUENCE_PATTERN_ID, ULong.valueOf(entity.getSequencePatternId()));
    fieldValues.put(Tables.SEQUENCE_PATTERN_MEME.NAME, entity.getName());
    return fieldValues;
  }

  @Override
  public SequencePatternMeme create(Access access, SequencePatternMeme entity) throws CoreException {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(create(tx.getContext(), access, entity));
    } catch (CoreException e) {
      throw tx.failure(e);
    }
  }

  @Override
  public SequencePatternMeme readOne(Access access, BigInteger id) throws CoreException {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOne(tx.getContext(), access, ULong.valueOf(id)));
    } catch (CoreException e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Collection<SequencePatternMeme> readAll(Access access, Collection<BigInteger> parentIds) throws CoreException {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAll(tx.getContext(), access, uLongValuesOf(parentIds)));
    } catch (CoreException e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void update(Access access, BigInteger id, SequencePatternMeme entity) throws CoreException {
    throw new CoreException("Not allowed to update SequencePatternMeme record.");

  }

  @Override
  public void destroy(Access access, BigInteger id) throws CoreException {
    SQLConnection tx = dbProvider.getConnection();
    try {
      delete(tx.getContext(), access, ULong.valueOf(id));
      tx.success();
    } catch (CoreException e) {
      throw tx.failure(e);
    }
  }


}
