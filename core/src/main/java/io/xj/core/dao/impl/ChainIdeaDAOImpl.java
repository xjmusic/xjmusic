// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.dao.impl;

import io.xj.core.app.access.impl.Access;
import io.xj.core.app.exception.BusinessException;
import io.xj.core.app.exception.ConfigException;
import io.xj.core.dao.ChainIdeaDAO;
import io.xj.core.database.sql.impl.SQLConnection;
import io.xj.core.database.sql.SQLDatabaseProvider;
import io.xj.core.model.chain_idea.ChainIdea;
import io.xj.core.tables.records.ChainIdeaRecord;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Result;
import org.jooq.types.ULong;

import com.google.inject.Inject;

import java.sql.SQLException;
import java.util.Map;

import static io.xj.core.tables.Chain.CHAIN;
import static io.xj.core.tables.ChainIdea.CHAIN_IDEA;
import static io.xj.core.tables.Idea.IDEA;
import static io.xj.core.tables.Library.LIBRARY;

public class ChainIdeaDAOImpl extends DAOImpl implements ChainIdeaDAO {

  @Inject
  public ChainIdeaDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public ChainIdeaRecord create(Access access, ChainIdea entity) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(createRecord(tx.getContext(), access, entity));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public ChainIdeaRecord readOne(Access access, ULong id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOneRecord(tx.getContext(), access, id));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Result<ChainIdeaRecord> readAll(Access access, ULong chainId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAll(tx.getContext(), access, chainId));
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
   Create a new Chain Idea record

   @param db     context
   @param entity for new ChainIdea
   @return new record
   @throws Exception         if database failure
   @throws ConfigException   if not configured properly
   @throws BusinessException if fails business rule
   */
  private ChainIdeaRecord createRecord(DSLContext db, Access access, ChainIdea entity) throws Exception {
    entity.validate();

    Map<Field, Object> fieldValues = entity.updatableFieldValueMap();

    if (access.isTopLevel()) {
      requireExists("Chain", db.selectCount().from(CHAIN)
        .where(CHAIN.ID.eq(entity.getChainId()))
        .fetchOne(0, int.class));
      requireExists("Idea", db.selectCount().from(IDEA)
        .where(IDEA.ID.eq(entity.getIdeaId()))
        .fetchOne(0, int.class));
    } else {
      requireExists("Chain", db.selectCount().from(CHAIN)
        .where(CHAIN.ACCOUNT_ID.in(access.getAccounts()))
        .and(CHAIN.ID.eq(entity.getChainId()))
        .fetchOne(0, int.class));
      requireExists("Idea", db.selectCount().from(IDEA)
        .join(LIBRARY).on(LIBRARY.ID.eq(IDEA.LIBRARY_ID))
        .where(IDEA.ID.eq(entity.getIdeaId()))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne(0, int.class));
    }

    if (null != db.selectFrom(CHAIN_IDEA)
      .where(CHAIN_IDEA.CHAIN_ID.eq(entity.getChainId()))
      .and(CHAIN_IDEA.IDEA_ID.eq(entity.getIdeaId()))
      .fetchOne())
      throw new BusinessException("Idea already added to Chain!");

    return executeCreate(db, CHAIN_IDEA, fieldValues);
  }

  /**
   Read one record

   @param db     context
   @param access control
   @param id     of record
   @return record
   */
  private ChainIdeaRecord readOneRecord(DSLContext db, Access access, ULong id) {
    if (access.isTopLevel())
      return db.selectFrom(CHAIN_IDEA)
        .where(CHAIN_IDEA.ID.eq(id))
        .fetchOne();
    else
      return recordInto(CHAIN_IDEA, db.select(CHAIN_IDEA.fields()).from(CHAIN_IDEA)
        .join(IDEA).on(IDEA.ID.eq(CHAIN_IDEA.IDEA_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(IDEA.LIBRARY_ID))
        .where(CHAIN_IDEA.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne());
  }

  /**
   Read all records in parent record

   @param db      context
   @param access  control
   @param chainId of parent
   @return array of child records
   @throws SQLException on failure
   */
  private Result<ChainIdeaRecord> readAll(DSLContext db, Access access, ULong chainId) throws SQLException {
    if (access.isTopLevel())
      return db.selectFrom(CHAIN_IDEA)
        .where(CHAIN_IDEA.CHAIN_ID.eq(chainId))
        .fetch();
    else
      return resultInto(CHAIN_IDEA, db.select(CHAIN_IDEA.fields()).from(CHAIN_IDEA)
        .join(IDEA).on(IDEA.ID.eq(CHAIN_IDEA.IDEA_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(IDEA.LIBRARY_ID))
        .where(CHAIN_IDEA.CHAIN_ID.eq(chainId))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetch());
  }

  /**
   Delete a record

   @param db     context
   @param access control
   @param id     of record
   @throws BusinessException on failure
   */
  private void delete(DSLContext db, Access access, ULong id) throws BusinessException {

    if (access.isTopLevel())
      requireExists("Chain Idea", db.selectCount().from(CHAIN_IDEA)
        .where(CHAIN_IDEA.ID.eq(id))
        .fetchOne(0, int.class));
    else
      requireExists("Chain Idea", db.selectCount().from(CHAIN_IDEA)
        .join(IDEA).on(IDEA.ID.eq(CHAIN_IDEA.IDEA_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(IDEA.LIBRARY_ID))
        .where(CHAIN_IDEA.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne(0, int.class));

    db.deleteFrom(CHAIN_IDEA)
      .where(CHAIN_IDEA.ID.eq(id))
      .execute();
  }

}
