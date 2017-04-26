// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.dao.impl;

import io.outright.xj.core.app.access.impl.AccessControl;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.app.exception.ConfigException;
import io.outright.xj.core.app.exception.DatabaseException;
import io.outright.xj.core.dao.ChainConfigDAO;
import io.outright.xj.core.db.sql.SQLConnection;
import io.outright.xj.core.db.sql.SQLDatabaseProvider;
import io.outright.xj.core.model.chain_config.ChainConfig;
import io.outright.xj.core.model.chain_config.ChainConfigWrapper;
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
import static io.outright.xj.core.tables.ChainConfig.CHAIN_CONFIG;

public class ChainConfigDAOImpl extends DAOImpl implements ChainConfigDAO {

  @Inject
  public ChainConfigDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public JSONObject create(AccessControl access, ChainConfigWrapper data) throws Exception {
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
  public void update(AccessControl access, ULong id, ChainConfigWrapper data) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      update(tx.getContext(), access, id, data);
      tx.success();
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
   * Create a new Chain config record
   *
   * @param db   context
   * @param data for new ChainConfig
   * @return new record
   * @throws Exception         if database failure
   * @throws ConfigException   if not configured properly
   * @throws BusinessException if fails business rule
   */
  private JSONObject create(DSLContext db, AccessControl access, ChainConfigWrapper data) throws Exception {
    ChainConfig model = data.validate();
    Map<Field, Object> fieldValues = model.intoFieldValueMap();

    if (access.isTopLevel()) {
      requireRecordExists("Chain", db.select(CHAIN.ID).from(CHAIN)
        .where(CHAIN.ID.eq(model.getChainId()))
        .fetchOne());
    } else {
      requireRecordExists("Chain", db.select(CHAIN.ID).from(CHAIN)
        .where(CHAIN.ACCOUNT_ID.in(access.getAccounts()))
        .and(CHAIN.ID.eq(model.getChainId()))
        .fetchOne());
    }

    if (db.selectFrom(CHAIN_CONFIG)
      .where(CHAIN_CONFIG.CHAIN_ID.eq(model.getChainId()))
      .and(CHAIN_CONFIG.TYPE.eq(model.getType()))
      .fetchOne() != null) {
      throw new BusinessException(model.getType() + " config already exists for this Chain!");
    }

    return JSON.objectFromRecord(executeCreate(db, CHAIN_CONFIG, fieldValues));
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
      return JSON.objectFromRecord(db.selectFrom(CHAIN_CONFIG)
        .where(CHAIN_CONFIG.ID.eq(id))
        .fetchOne());
    } else {
      return JSON.objectFromRecord(db.select(CHAIN_CONFIG.fields()).from(CHAIN_CONFIG)
        .join(CHAIN).on(CHAIN.ID.eq(CHAIN_CONFIG.CHAIN_ID))
        .where(CHAIN_CONFIG.ID.eq(id))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccounts()))
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
      return JSON.arrayFromResultSet(db.selectFrom(CHAIN_CONFIG)
        .where(CHAIN_CONFIG.CHAIN_ID.eq(chainId))
        .fetchResultSet());
    } else {
      return JSON.arrayFromResultSet(db.select(CHAIN_CONFIG.fields()).from(CHAIN_CONFIG)
        .join(CHAIN).on(CHAIN.ID.eq(CHAIN_CONFIG.CHAIN_ID))
        .where(CHAIN.ID.eq(chainId))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccounts()))
        .fetchResultSet());
    }
  }


  /**
   * Update a record
   *
   * @param db     context
   * @param access control
   * @param id     of record
   * @param data   to update with
   * @throws BusinessException if a Business Rule is violated
   */
  private void update(DSLContext db, AccessControl access, ULong id, ChainConfigWrapper data) throws BusinessException, DatabaseException {
    ChainConfig model = data.validate();
    Map<Field, Object> fieldValues = model.intoFieldValueMap();
    fieldValues.put(CHAIN_CONFIG.ID, id);

    if (access.isTopLevel()) {
      requireRecordExists("Chain config", db.selectFrom(CHAIN_CONFIG)
        .where(CHAIN_CONFIG.ID.eq(id))
        .fetchOne());
    } else {
      requireRecordExists("Chain config", db.select(CHAIN_CONFIG.fields()).from(CHAIN_CONFIG)
        .join(CHAIN).on(CHAIN.ID.eq(CHAIN_CONFIG.CHAIN_ID))
        .where(CHAIN_CONFIG.ID.eq(id))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne());
    }

    // [#128] cannot change chainId of a chainConfig
    Object updateChainId = fieldValues.get(CHAIN_CONFIG.CHAIN_ID);
    if (updateChainId != null
      && !updateChainId.equals(model.getChainId())
      ) {
      throw new BusinessException("cannot change chainId of a chainConfig");
    }

    if (executeUpdate(db, CHAIN_CONFIG, fieldValues) == 0) {
      throw new BusinessException("No records updated.");
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
    // TODO: fail if no chainConfig is deleted
    if (access.isTopLevel()) {
      requireRecordExists("Chain config", db.selectFrom(CHAIN_CONFIG)
        .where(CHAIN_CONFIG.ID.eq(id))
        .fetchOne());
    } else {
      requireRecordExists("Chain config", db.select(CHAIN_CONFIG.fields()).from(CHAIN_CONFIG)
        .join(CHAIN).on(CHAIN.ID.eq(CHAIN_CONFIG.CHAIN_ID))
        .where(CHAIN_CONFIG.ID.eq(id))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne());
    }

    db.deleteFrom(CHAIN_CONFIG)
      .where(CHAIN_CONFIG.ID.eq(id))
      .execute();
  }

}
