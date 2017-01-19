// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.hub.controller.account_user;

import io.outright.xj.core.app.db.SQLDatabaseProvider;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.app.exception.ConfigException;
import io.outright.xj.core.app.exception.DatabaseException;
import io.outright.xj.core.model.account_user.AccountUserWrapper;
import io.outright.xj.core.tables.records.AccountUserRecord;

import com.google.inject.Inject;
import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.sql.Connection;
import java.sql.ResultSet;

import static io.outright.xj.core.tables.AccountUser.ACCOUNT_USER;

public class AccountUserControllerImpl implements AccountUserController {
  private static Logger log = LoggerFactory.getLogger(AccountUserControllerImpl.class);
  private SQLDatabaseProvider dbProvider;

  @Inject
  public AccountUserControllerImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Nullable
  @Override
  public AccountUserRecord read(ULong id) throws DatabaseException {
    Connection conn = dbProvider.getConnectionTransaction();
    DSLContext db = dbProvider.getContext(conn);

    return db.selectFrom(ACCOUNT_USER)
      .where(ACCOUNT_USER.ID.eq(id))
      .fetchOne();
  }

  @Override
  @Nullable
  public ResultSet readAll(ULong accountId) throws DatabaseException {
    Connection conn = dbProvider.getConnectionTransaction();
    DSLContext db = dbProvider.getContext(conn);

    return db.selectFrom(ACCOUNT_USER)
      .where(ACCOUNT_USER.ACCOUNT_ID.eq(accountId))
      .fetchResultSet();
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
  public AccountUserRecord create(AccountUserWrapper data) throws DatabaseException, ConfigException, BusinessException {
    Connection conn = dbProvider.getConnectionTransaction();
    DSLContext db = dbProvider.getContext(conn);
    AccountUserRecord newAccountUser;

    try {
      data.validate();

      ULong accountId = ULong.valueOf(data.getAccountUser().getAccountId());
      ULong userId = ULong.valueOf(data.getAccountUser().getUserId());

      if (db.selectFrom(ACCOUNT_USER)
        .where(ACCOUNT_USER.ACCOUNT_ID.eq(accountId))
        .and(ACCOUNT_USER.USER_ID.eq(userId))
        .fetchOne()!=null) {
        throw new BusinessException("Account User already exists!");
      }

      newAccountUser = db.newRecord(ACCOUNT_USER);
      newAccountUser.setAccountId(accountId);
      newAccountUser.setUserId(userId);
      try {
        newAccountUser.store();
      } catch (Exception e) {
        log.warn("Cannot create AccountUser", e.getMessage());
        throw new BusinessException("Cannot create Account User. Please ensure userId+accountId are valid and unique.");
      }

      dbProvider.commitAndClose(conn);
    } catch (Exception e) {
      dbProvider.rollbackAndClose(conn);
      throw e;
    }
    return newAccountUser;
  }

}
