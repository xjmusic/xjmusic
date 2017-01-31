// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.dao.jooq;

import io.outright.xj.core.app.access.AccessControl;
import io.outright.xj.core.app.db.SQLDatabaseProvider;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.app.exception.ConfigException;
import io.outright.xj.core.app.exception.DatabaseException;
import io.outright.xj.core.dao.AccountUserDAO;
import io.outright.xj.core.model.account_user.AccountUserWrapper;
import io.outright.xj.core.tables.records.AccountUserRecord;
import io.outright.xj.core.transport.JSON;

import com.google.inject.Inject;
import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

import static io.outright.xj.core.tables.AccountUser.ACCOUNT_USER;

public class AccountUserDAOImpl implements AccountUserDAO {
  private static Logger log = LoggerFactory.getLogger(AccountUserDAOImpl.class);
  private SQLDatabaseProvider dbProvider;

  @Inject
  public AccountUserDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public JSONObject readOneAble(AccessControl access, ULong id) throws DatabaseException {
    Connection conn = dbProvider.getConnection();
    DSLContext db = dbProvider.getContext(conn);

    JSONObject result;
    if (access.isAdmin()) {
      result = JSON.objectFromRecord(db.selectFrom(ACCOUNT_USER)
        .where(ACCOUNT_USER.ID.eq(id))
        .fetchOne());
    } else {
      result = JSON.objectFromRecord(db.selectFrom(ACCOUNT_USER)
        .where(ACCOUNT_USER.ID.eq(id))
        .and(ACCOUNT_USER.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne());
    }
    dbProvider.close(conn);
    return result;
  }

  @Override
  public JSONArray readAllAble(AccessControl access, ULong accountId) throws DatabaseException {
    Connection conn = dbProvider.getConnection();
    DSLContext db = dbProvider.getContext(conn);

    JSONArray result;
    try {
      if (access.isAdmin()) {
        result = JSON.arrayFromResultSet(db.selectFrom(ACCOUNT_USER)
          .where(ACCOUNT_USER.ACCOUNT_ID.eq(accountId))
          .fetchResultSet());
      } else {
        result = JSON.arrayFromResultSet(db.selectFrom(ACCOUNT_USER)
          .where(ACCOUNT_USER.ACCOUNT_ID.eq(accountId))
          .and(ACCOUNT_USER.ACCOUNT_ID.in(access.getAccounts()))
          .fetchResultSet());
      }
      dbProvider.close(conn);
      return result;

    } catch (SQLException e) {
      dbProvider.close(conn);
      throw new DatabaseException("SQLException: " + e);
    }
  }

  @Override
  public void delete(ULong id) throws DatabaseException, ConfigException, BusinessException {
    Connection conn = dbProvider.getConnectionTransaction();
    DSLContext db = dbProvider.getContext(conn);

    try {
      // TODO: fail if no accountUser is deleted
      db.deleteFrom(ACCOUNT_USER)
        .where(ACCOUNT_USER.ID.eq(id))
        .execute();
      dbProvider.commitAndClose(conn);

    } catch (Exception e) {
      dbProvider.rollbackAndClose(conn);
      throw e;
    }
  }

  @Override
  public JSONObject create(AccountUserWrapper data) throws DatabaseException, ConfigException, BusinessException {
    Connection conn = dbProvider.getConnectionTransaction();
    DSLContext db = dbProvider.getContext(conn);
    JSONObject result;

    try {
      result = JSON.objectFromRecord(create(db, data));
      dbProvider.commitAndClose(conn);
    } catch (Exception e) {
      dbProvider.rollbackAndClose(conn);
      throw e;
    }
    return result;
  }

  /**
   * Create a new Account User record
   * @param db context
   * @param data for new AccountUser
   * @return new record
   * @throws DatabaseException if database failure
   * @throws ConfigException if not configured properly
   * @throws BusinessException if fails business rule
   */
  private AccountUserRecord create(DSLContext db, AccountUserWrapper data) throws DatabaseException, ConfigException, BusinessException  {
    data.validate();

    ULong accountId = ULong.valueOf(data.getAccountUser().getAccountId());
    ULong userId = ULong.valueOf(data.getAccountUser().getUserId());

    if (db.selectFrom(ACCOUNT_USER)
      .where(ACCOUNT_USER.ACCOUNT_ID.eq(accountId))
      .and(ACCOUNT_USER.USER_ID.eq(userId))
      .fetchOne()!=null) {
      throw new BusinessException("Account User already exists!");
    }

    AccountUserRecord record;
    record = db.newRecord(ACCOUNT_USER);
    data.getAccountUser().intoFieldValueMap().forEach(record::setValue);

    try {
      record.store();
    } catch (Exception e) {
      log.warn("Cannot create AccountUser", e.getMessage());
      throw new BusinessException("Cannot create Account User. Please ensure userId+accountId are valid and unique.");
    }

    return record;
  }

}
