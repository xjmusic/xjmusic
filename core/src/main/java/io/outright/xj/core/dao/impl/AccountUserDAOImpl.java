// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.dao.impl;

import io.outright.xj.core.app.access.AccessControl;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.app.exception.ConfigException;
import io.outright.xj.core.dao.AccountUserDAO;
import io.outright.xj.core.db.sql.SQLConnection;
import io.outright.xj.core.db.sql.SQLDatabaseProvider;
import io.outright.xj.core.model.account_user.AccountUser;
import io.outright.xj.core.model.account_user.AccountUserWrapper;
import io.outright.xj.core.transport.JSON;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.types.ULong;

import com.google.inject.Inject;

import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.Map;

import static io.outright.xj.core.tables.AccountUser.ACCOUNT_USER;

public class AccountUserDAOImpl extends DAOImpl implements AccountUserDAO {

  @Inject
  public AccountUserDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public JSONObject create(AccessControl access, AccountUserWrapper data) throws Exception {
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
  public JSONArray readAllIn(AccessControl access, ULong accountId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAllIn(tx.getContext(), access, accountId));
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
   * Create a new Account User record
   *
   * @param db   context
   * @param data for new AccountUser
   * @return new record
   * @throws Exception if database failure
   * @throws ConfigException   if not configured properly
   * @throws BusinessException if fails business rule
   */
  private JSONObject create(DSLContext db, AccessControl access, AccountUserWrapper data) throws Exception {
    AccountUser model = data.validate();
    Map<Field, Object> fieldValues = model.intoFieldValueMap();

    requireTopLevel(access);

    if (db.selectFrom(ACCOUNT_USER)
      .where(ACCOUNT_USER.ACCOUNT_ID.eq(model.getAccountId()))
      .and(ACCOUNT_USER.USER_ID.eq(model.getUserId()))
      .fetchOne() != null) {
      throw new BusinessException("Account User already exists!");
    }

    return JSON.objectFromRecord(executeCreate(db, ACCOUNT_USER, fieldValues));
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
      return JSON.objectFromRecord(db.selectFrom(ACCOUNT_USER)
        .where(ACCOUNT_USER.ID.eq(id))
        .fetchOne());
    } else {
      return JSON.objectFromRecord(db.selectFrom(ACCOUNT_USER)
        .where(ACCOUNT_USER.ID.eq(id))
        .and(ACCOUNT_USER.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne());
    }
  }

  /**
   * Read all records in parent record
   *
   * @param db        context
   * @param access    control
   * @param accountId of parent
   * @return array of child records
   * @throws SQLException on failure
   */
  private JSONArray readAllIn(DSLContext db, AccessControl access, ULong accountId) throws SQLException {
    if (access.isTopLevel()) {
      return JSON.arrayFromResultSet(db.selectFrom(ACCOUNT_USER)
        .where(ACCOUNT_USER.ACCOUNT_ID.eq(accountId))
        .fetchResultSet());
    } else {
      return JSON.arrayFromResultSet(db.selectFrom(ACCOUNT_USER)
        .where(ACCOUNT_USER.ACCOUNT_ID.eq(accountId))
        .and(ACCOUNT_USER.ACCOUNT_ID.in(access.getAccounts()))
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
    requireTopLevel(access);
    // TODO: fail if no accountUser is deleted
    db.deleteFrom(ACCOUNT_USER)
      .where(ACCOUNT_USER.ID.eq(id))
      .execute();
  }

}
