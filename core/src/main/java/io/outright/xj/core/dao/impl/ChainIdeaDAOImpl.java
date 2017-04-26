// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.dao.impl;

import io.outright.xj.core.app.access.impl.AccessControl;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.app.exception.ConfigException;
import io.outright.xj.core.dao.ChainIdeaDAO;
import io.outright.xj.core.db.sql.SQLConnection;
import io.outright.xj.core.db.sql.SQLDatabaseProvider;
import io.outright.xj.core.model.chain_idea.ChainIdea;
import io.outright.xj.core.model.chain_idea.ChainIdeaWrapper;
import io.outright.xj.core.transport.JSON;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.types.ULong;

import com.google.inject.Inject;

import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.Map;

import static io.outright.xj.core.tables.Chain.CHAIN;
import static io.outright.xj.core.tables.ChainIdea.CHAIN_IDEA;
import static io.outright.xj.core.tables.Idea.IDEA;
import static io.outright.xj.core.tables.Library.LIBRARY;

public class ChainIdeaDAOImpl extends DAOImpl implements ChainIdeaDAO {

  @Inject
  public ChainIdeaDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public JSONObject create(AccessControl access, ChainIdeaWrapper data) throws Exception {
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
      return tx.success(readOne(tx.getContext(), access, id));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public JSONArray readAllIn(AccessControl access, ULong chainId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAllIn(tx.getContext(), access, chainId));
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
   * Create a new Chain Idea record
   *
   * @param db   context
   * @param data for new ChainIdea
   * @return new record
   * @throws Exception         if database failure
   * @throws ConfigException   if not configured properly
   * @throws BusinessException if fails business rule
   */
  private JSONObject create(DSLContext db, AccessControl access, ChainIdeaWrapper data) throws Exception {
    ChainIdea model = data.validate();
    Map<Field, Object> fieldValues = model.intoFieldValueMap();

    if (access.isTopLevel()) {
      requireRecordExists("Chain", db.select(CHAIN.ID).from(CHAIN)
        .where(CHAIN.ID.eq(model.getChainId()))
        .fetchOne());
      requireRecordExists("Idea", db.select(IDEA.ID).from(IDEA)
        .where(IDEA.ID.eq(model.getIdeaId()))
        .fetchOne());
    } else {
      requireRecordExists("Chain", db.select(CHAIN.ID).from(CHAIN)
        .where(CHAIN.ACCOUNT_ID.in(access.getAccounts()))
        .and(CHAIN.ID.eq(model.getChainId()))
        .fetchOne());
      requireRecordExists("Idea", db.select(IDEA.ID).from(IDEA)
        .join(LIBRARY).on(LIBRARY.ID.eq(IDEA.LIBRARY_ID))
        .where(IDEA.ID.eq(model.getIdeaId()))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne());
    }

    if (db.selectFrom(CHAIN_IDEA)
      .where(CHAIN_IDEA.CHAIN_ID.eq(model.getChainId()))
      .and(CHAIN_IDEA.IDEA_ID.eq(model.getIdeaId()))
      .fetchOne() != null) {
      throw new BusinessException("Idea already added to Chain!");
    }

    return JSON.objectFromRecord(executeCreate(db, CHAIN_IDEA, fieldValues));
  }

  /**
   * Read one record
   *
   * @param db     context
   * @param access control
   * @param id     of record
   * @return record
   */
  private JSONObject readOne(DSLContext db, AccessControl access, ULong id) {
    if (access.isTopLevel()) {
      return JSON.objectFromRecord(db.selectFrom(CHAIN_IDEA)
        .where(CHAIN_IDEA.ID.eq(id))
        .fetchOne());
    } else {
      return JSON.objectFromRecord(db.select(CHAIN_IDEA.fields()).from(CHAIN_IDEA)
        .join(IDEA).on(IDEA.ID.eq(CHAIN_IDEA.IDEA_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(IDEA.LIBRARY_ID))
        .where(CHAIN_IDEA.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne());
    }
  }

  /**
   * Read all records in parent record
   *
   * @param db      context
   * @param access  control
   * @param chainId of parent
   * @return array of child records
   * @throws SQLException on failure
   */
  private JSONArray readAllIn(DSLContext db, AccessControl access, ULong chainId) throws SQLException {
    if (access.isTopLevel()) {
      return JSON.arrayFromResultSet(db.selectFrom(CHAIN_IDEA)
        .where(CHAIN_IDEA.CHAIN_ID.eq(chainId))
        .fetchResultSet());
    } else {
      return JSON.arrayFromResultSet(db.select(CHAIN_IDEA.fields()).from(CHAIN_IDEA)
        .join(IDEA).on(IDEA.ID.eq(CHAIN_IDEA.IDEA_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(IDEA.LIBRARY_ID))
        .where(CHAIN_IDEA.CHAIN_ID.eq(chainId))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetchResultSet());
    }
  }

  /**
   * Delete a record
   *
   * @param db     context
   * @param access control
   * @param id     of record
   * @throws BusinessException on failure
   */
  private void delete(DSLContext db, AccessControl access, ULong id) throws BusinessException {
    // TODO: fail if no chainIdea is deleted
    if (access.isTopLevel()) {
      requireRecordExists("Chain Idea", db.selectFrom(CHAIN_IDEA)
        .where(CHAIN_IDEA.ID.eq(id))
        .fetchOne());
    } else {
      requireRecordExists("Chain Idea", db.select(CHAIN_IDEA.fields()).from(CHAIN_IDEA)
        .join(IDEA).on(IDEA.ID.eq(CHAIN_IDEA.IDEA_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(IDEA.LIBRARY_ID))
        .where(CHAIN_IDEA.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne());
    }

    db.deleteFrom(CHAIN_IDEA)
      .where(CHAIN_IDEA.ID.eq(id))
      .execute();
  }

}
