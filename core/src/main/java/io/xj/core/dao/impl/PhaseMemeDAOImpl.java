// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao.impl;

import io.xj.core.Tables;
import io.xj.core.access.impl.Access;
import io.xj.core.dao.PhaseMemeDAO;
import io.xj.core.exception.BusinessException;
import io.xj.core.exception.ConfigException;
import io.xj.core.model.phase_meme.PhaseMeme;
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
import static io.xj.core.tables.Pattern.PATTERN;
import static io.xj.core.tables.Phase.PHASE;
import static io.xj.core.tables.PhaseMeme.PHASE_MEME;

/**
 PhaseMeme DAO
 <p>
 future: more specific permissions of user (artist) access by per-entity ownership
 */
public class PhaseMemeDAOImpl extends DAOImpl implements PhaseMemeDAO {

  @Inject
  public PhaseMemeDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public PhaseMeme create(Access access, PhaseMeme entity) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(create(tx.getContext(), access, entity));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public PhaseMeme readOne(Access access, BigInteger id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOne(tx.getContext(), access, ULong.valueOf(id)));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Collection<PhaseMeme> readAll(Access access, BigInteger phaseId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAll(tx.getContext(), access, ULong.valueOf(phaseId)));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void delete(Access access, BigInteger id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      delete(tx.getContext(), access, ULong.valueOf(id));
      tx.success();
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  /**
   Create a new Phase Meme record

   @param db     context
   @param access control
   @param entity for new PhaseMeme
   @return new record
   @throws Exception         if database failure
   @throws ConfigException   if not configured properly
   @throws BusinessException if fails business rule
   */
  private static PhaseMeme create(DSLContext db, Access access, PhaseMeme entity) throws Exception {
    entity.validate();

    Map<Field, Object> fieldValues = fieldValueMap(entity);

    if (access.isTopLevel())
      requireExists("Phase", db.selectCount().from(PHASE)
        .where(PHASE.ID.eq(ULong.valueOf(entity.getPhaseId())))
        .fetchOne(0, int.class));
    else
      requireExists("Phase", db.selectCount().from(PHASE)
        .join(PATTERN).on(PATTERN.ID.eq(PHASE.PATTERN_ID))
        .join(LIBRARY).on(PATTERN.LIBRARY_ID.eq(LIBRARY.ID))
        .where(PHASE.ID.eq(ULong.valueOf(entity.getPhaseId())))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne(0, int.class));

    if (null != db.selectFrom(PHASE_MEME)
      .where(PHASE_MEME.PHASE_ID.eq(ULong.valueOf(entity.getPhaseId())))
      .and(PHASE_MEME.NAME.eq(entity.getName()))
      .fetchOne())
      throw new BusinessException("Phase Meme already exists!");

    return modelFrom(executeCreate(db, PHASE_MEME, fieldValues), PhaseMeme.class);
  }

  /**
   Read one Phase Meme where able

   @param db     context
   @param access control
   @param id     of record
   @return record
   */
  private static PhaseMeme readOne(DSLContext db, Access access, ULong id) throws BusinessException {
    if (access.isTopLevel())
      return modelFrom(db.selectFrom(PHASE_MEME)
        .where(PHASE_MEME.ID.eq(id))
        .fetchOne(), PhaseMeme.class);
    else
      return modelFrom(db.select(PHASE_MEME.fields()).from(PHASE_MEME)
        .join(PHASE).on(PHASE.ID.eq(PHASE_MEME.PHASE_ID))
        .join(PATTERN).on(PATTERN.ID.eq(PHASE.PATTERN_ID))
        .join(LIBRARY).on(PATTERN.LIBRARY_ID.eq(LIBRARY.ID))
        .where(PHASE_MEME.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne(), PhaseMeme.class);
  }

  /**
   Read all Memes of an Phase where able

   @param db      context
   @param access  control
   @param phaseId to readMany memes for
   @return array of phase memes
   */
  private static Collection<PhaseMeme> readAll(DSLContext db, Access access, ULong phaseId) throws BusinessException {
    if (access.isTopLevel())
      return modelsFrom(db.selectFrom(PHASE_MEME)
        .where(PHASE_MEME.PHASE_ID.eq(phaseId))
        .fetch(), PhaseMeme.class);
    else
      return modelsFrom(db.select(PHASE_MEME.fields()).from(PHASE_MEME)
        .join(PHASE).on(PHASE.ID.eq(PHASE_MEME.PHASE_ID))
        .join(PATTERN).on(PATTERN.ID.eq(PHASE.PATTERN_ID))
        .join(LIBRARY).on(PATTERN.LIBRARY_ID.eq(LIBRARY.ID))
        .where(PHASE.ID.eq(phaseId))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .fetch(), PhaseMeme.class);
  }

  /**
   Delete an PhaseMeme record

   @param db     context
   @param access control
   @param id     to delete
   @throws BusinessException if failure
   */
  private static void delete(DSLContext db, Access access, ULong id) throws BusinessException {
    if (!access.isTopLevel())
      requireExists("Phase Meme", db.selectCount().from(PHASE_MEME)
        .join(PHASE).on(PHASE.ID.eq(PHASE_MEME.PHASE_ID))
        .join(PATTERN).on(PATTERN.ID.eq(PHASE.PATTERN_ID))
        .join(LIBRARY).on(PATTERN.LIBRARY_ID.eq(LIBRARY.ID))
        .where(PHASE_MEME.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne(0, int.class));

    db.deleteFrom(PHASE_MEME)
      .where(PHASE_MEME.ID.eq(id))
      .execute();
  }

  /**
   Only certain (writable) fields are mapped back to jOOQ records--
   Read-only fields are excluded from here.

   @param entity to source values from
   @return values mapped to record fields
   */
  private static Map<Field, Object> fieldValueMap(PhaseMeme entity) {
    Map<Field, Object> fieldValues = Maps.newHashMap();
    fieldValues.put(Tables.PHASE_MEME.PHASE_ID, ULong.valueOf(entity.getPhaseId()));
    fieldValues.put(Tables.PHASE_MEME.NAME, entity.getName());
    return fieldValues;
  }


}
