// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao.impl;

import io.xj.core.access.impl.Access;
import io.xj.core.dao.SequenceMemeDAO;
import io.xj.core.exception.CoreException;
import io.xj.core.exception.CoreException;
import io.xj.core.model.sequence_meme.SequenceMeme;
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
import static io.xj.core.tables.SequenceMeme.SEQUENCE_MEME;

/**
 SequenceMeme DAO
 <p>
 future: more specific permissions of user (artist) access by per-entity ownership
 */
public class SequenceMemeDAOImpl extends DAOImpl implements SequenceMemeDAO {

  @Inject
  public SequenceMemeDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  /**
   Create a new Sequence Meme record

   @param db     context
   @param access control
   @param entity for new SequenceMeme
   @return new record
   @throws CoreException         if database failure
   @throws CoreException   if not configured properly
   @throws CoreException if fails business rule
   */
  private static SequenceMeme create(DSLContext db, Access access, SequenceMeme entity) throws CoreException {
    entity.validate();

    Map<Field, Object> fieldValues = fieldValueMap(entity);

    if (access.isTopLevel())
      requireExists("Sequence", db.selectCount().from(SEQUENCE)
        .where(SEQUENCE.ID.eq(ULong.valueOf(entity.getSequenceId())))
        .fetchOne(0, int.class));
    else
      requireExists("Sequence", db.selectCount().from(SEQUENCE)
        .join(LIBRARY).on(SEQUENCE.LIBRARY_ID.eq(LIBRARY.ID))
        .where(SEQUENCE.ID.eq(ULong.valueOf(entity.getSequenceId())))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne(0, int.class));

    if (null != db.selectFrom(SEQUENCE_MEME)
      .where(SEQUENCE_MEME.SEQUENCE_ID.eq(ULong.valueOf(entity.getSequenceId())))
      .and(SEQUENCE_MEME.NAME.eq(entity.getName()))
      .fetchOne())
      throw new CoreException("Sequence Meme already exists!");

    return modelFrom(executeCreate(db, SEQUENCE_MEME, fieldValues), SequenceMeme.class);
  }

  /**
   Read one Sequence Meme where able

   @param db     context
   @param access control
   @param id     of record
   @return record
   */
  private static SequenceMeme readOne(DSLContext db, Access access, ULong id) throws CoreException {
    if (access.isTopLevel())
      return modelFrom(db.selectFrom(SEQUENCE_MEME)
        .where(SEQUENCE_MEME.ID.eq(id))
        .fetchOne(), SequenceMeme.class);
    else
      return modelFrom(db.select(SEQUENCE_MEME.fields()).from(SEQUENCE_MEME)
        .join(SEQUENCE).on(SEQUENCE.ID.eq(SEQUENCE_MEME.SEQUENCE_ID))
        .join(LIBRARY).on(SEQUENCE.LIBRARY_ID.eq(LIBRARY.ID))
        .where(SEQUENCE_MEME.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne(), SequenceMeme.class);
  }

  /**
   Read all Memes of an Sequence where able

   @param db         context
   @param access     control
   @param sequenceIds to readMany memes for
   @return array of sequence memes
   */
  private static Collection<SequenceMeme> readAll(DSLContext db, Access access, Collection<ULong> sequenceIds) throws CoreException {
    if (access.isTopLevel())
      return modelsFrom(db.selectFrom(SEQUENCE_MEME)
        .where(SEQUENCE_MEME.SEQUENCE_ID.in(sequenceIds))
        .fetch(), SequenceMeme.class);
    else
      return modelsFrom(db.select(SEQUENCE_MEME.fields()).from(SEQUENCE_MEME)
        .join(SEQUENCE).on(SEQUENCE.ID.eq(SEQUENCE_MEME.SEQUENCE_ID))
        .join(LIBRARY).on(SEQUENCE.LIBRARY_ID.eq(LIBRARY.ID))
        .where(SEQUENCE.ID.in(sequenceIds))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .fetch(), SequenceMeme.class);
  }

  /**
   Delete an SequenceMeme record

   @param db     context
   @param access control
   @param id     to delete
   @throws CoreException if failure
   */
  private static void delete(DSLContext db, Access access, ULong id) throws CoreException {
    if (!access.isTopLevel())
      requireExists("Sequence Meme", db.selectCount().from(SEQUENCE_MEME)
        .join(SEQUENCE).on(SEQUENCE.ID.eq(SEQUENCE_MEME.SEQUENCE_ID))
        .join(LIBRARY).on(SEQUENCE.LIBRARY_ID.eq(LIBRARY.ID))
        .where(SEQUENCE_MEME.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne(0, int.class));

    db.deleteFrom(SEQUENCE_MEME)
      .where(SEQUENCE_MEME.ID.eq(id))
      .execute();
  }

  /**
   Only certain (writable) fields are mapped back to jOOQ records--
   Read-only fields are excluded from here.

   @param entity to source values from
   @return values mapped to record fields
   */
  private static Map<Field, Object> fieldValueMap(SequenceMeme entity) {
    Map<Field, Object> fieldValues = Maps.newHashMap();
    fieldValues.put(SEQUENCE_MEME.SEQUENCE_ID, ULong.valueOf(entity.getSequenceId()));
    fieldValues.put(SEQUENCE_MEME.NAME, entity.getName());
    return fieldValues;
  }

  @Override
  public SequenceMeme create(Access access, SequenceMeme entity) throws CoreException {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(create(tx.getContext(), access, entity));
    } catch (CoreException e) {
      throw tx.failure(e);
    }
  }

  @Override
  public SequenceMeme readOne(Access access, BigInteger id) throws CoreException {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOne(tx.getContext(), access, ULong.valueOf(id)));
    } catch (CoreException e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Collection<SequenceMeme> readAll(Access access, Collection<BigInteger> parentIds) throws CoreException {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAll(tx.getContext(), access, uLongValuesOf(parentIds)));
    } catch (CoreException e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void update(Access access, BigInteger id, SequenceMeme entity) throws CoreException {
    throw new CoreException("Not allowed to update SequenceMeme record.");
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
