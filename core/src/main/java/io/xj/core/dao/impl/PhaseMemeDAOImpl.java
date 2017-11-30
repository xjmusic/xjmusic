// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.dao.impl;

import io.xj.core.access.impl.Access;
import io.xj.core.exception.BusinessException;
import io.xj.core.exception.ConfigException;
import io.xj.core.dao.PhaseMemeDAO;
import io.xj.core.database.sql.impl.SQLConnection;
import io.xj.core.database.sql.SQLDatabaseProvider;
import io.xj.core.model.phase_meme.PhaseMeme;
import io.xj.core.tables.records.PhaseMemeRecord;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Result;
import org.jooq.types.ULong;

import com.google.inject.Inject;

import java.sql.SQLException;
import java.util.Map;

import static io.xj.core.tables.Idea.IDEA;
import static io.xj.core.tables.Library.LIBRARY;
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
  public PhaseMemeRecord create(Access access, PhaseMeme entity) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(createRecord(tx.getContext(), access, entity));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public PhaseMemeRecord readOne(Access access, ULong id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOneRecord(tx.getContext(), access, id));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Result<PhaseMemeRecord> readAll(Access access, ULong phaseId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAll(tx.getContext(), access, phaseId));
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
   Create a new Phase Meme record

   @param db     context
   @param access control
   @param entity for new PhaseMeme
   @return new record
   @throws Exception         if database failure
   @throws ConfigException   if not configured properly
   @throws BusinessException if fails business rule
   */
  private PhaseMemeRecord createRecord(DSLContext db, Access access, PhaseMeme entity) throws Exception {
    entity.validate();

    Map<Field, Object> fieldValues = entity.updatableFieldValueMap();

    if (access.isTopLevel())
      requireExists("Phase", db.selectCount().from(PHASE)
        .where(PHASE.ID.eq(entity.getPhaseId()))
        .fetchOne(0, int.class));
    else
      requireExists("Phase", db.selectCount().from(PHASE)
        .join(IDEA).on(IDEA.ID.eq(PHASE.IDEA_ID))
        .join(LIBRARY).on(IDEA.LIBRARY_ID.eq(LIBRARY.ID))
        .where(PHASE.ID.eq(entity.getPhaseId()))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne(0, int.class));

    if (null != db.selectFrom(PHASE_MEME)
      .where(PHASE_MEME.PHASE_ID.eq(entity.getPhaseId()))
      .and(PHASE_MEME.NAME.eq(entity.getName()))
      .fetchOne())
      throw new BusinessException("Phase Meme already exists!");

    return executeCreate(db, PHASE_MEME, fieldValues);
  }

  /**
   Read one Phase Meme where able

   @param db     context
   @param access control
   @param id     of record
   @return record
   */
  private PhaseMemeRecord readOneRecord(DSLContext db, Access access, ULong id) throws SQLException {
    if (access.isTopLevel())
      return db.selectFrom(PHASE_MEME)
        .where(PHASE_MEME.ID.eq(id))
        .fetchOne();
    else
      return recordInto(PHASE_MEME, db.select(PHASE_MEME.fields()).from(PHASE_MEME)
        .join(PHASE).on(PHASE.ID.eq(PHASE_MEME.PHASE_ID))
        .join(IDEA).on(IDEA.ID.eq(PHASE.IDEA_ID))
        .join(LIBRARY).on(IDEA.LIBRARY_ID.eq(LIBRARY.ID))
        .where(PHASE_MEME.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne());
  }

  /**
   Read all Memes of an Phase where able

   @param db      context
   @param access  control
   @param phaseId to readMany memes for
   @return array of phase memes
   @throws SQLException if failure
   */
  private Result<PhaseMemeRecord> readAll(DSLContext db, Access access, ULong phaseId) throws SQLException {
    if (access.isTopLevel())
      return db.selectFrom(PHASE_MEME)
        .where(PHASE_MEME.PHASE_ID.eq(phaseId))
        .fetch();
    else
      return resultInto(PHASE_MEME, db.select(PHASE_MEME.fields()).from(PHASE_MEME)
        .join(PHASE).on(PHASE.ID.eq(PHASE_MEME.PHASE_ID))
        .join(IDEA).on(IDEA.ID.eq(PHASE.IDEA_ID))
        .join(LIBRARY).on(IDEA.LIBRARY_ID.eq(LIBRARY.ID))
        .where(PHASE.ID.eq(phaseId))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetch());
  }

  /**
   Delete an PhaseMeme record

   @param db     context
   @param access control
   @param id     to delete
   @throws BusinessException if failure
   */
  private void delete(DSLContext db, Access access, ULong id) throws BusinessException {
    if (!access.isTopLevel())
      requireExists("Phase Meme", db.selectCount().from(PHASE_MEME)
        .join(PHASE).on(PHASE.ID.eq(PHASE_MEME.PHASE_ID))
        .join(IDEA).on(IDEA.ID.eq(PHASE.IDEA_ID))
        .join(LIBRARY).on(IDEA.LIBRARY_ID.eq(LIBRARY.ID))
        .where(PHASE_MEME.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne(0, int.class));

    db.deleteFrom(PHASE_MEME)
      .where(PHASE_MEME.ID.eq(id))
      .execute();
  }

}
