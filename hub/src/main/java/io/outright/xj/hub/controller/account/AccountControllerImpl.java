// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.hub.controller.account;

import io.outright.xj.core.app.db.SQLDatabaseProvider;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.app.exception.ConfigException;
import io.outright.xj.core.app.exception.DatabaseException;
import io.outright.xj.core.model.account.AccountWrapper;
import io.outright.xj.core.tables.records.AccountRecord;

import com.google.inject.Inject;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.types.ULong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import static io.outright.xj.core.Tables.ACCOUNT;
import static io.outright.xj.core.tables.AccountUserRole.ACCOUNT_USER_ROLE;
import static io.outright.xj.core.tables.Library.LIBRARY;

public class AccountControllerImpl implements AccountController {
  private static Logger log = LoggerFactory.getLogger(AccountControllerImpl.class);
  private SQLDatabaseProvider dbProvider;

  @Inject
  public AccountControllerImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  @Nullable
  public Record fetchAccount(ULong accountId) throws DatabaseException {
    Connection conn = dbProvider.getConnectionTransaction();
    DSLContext db = dbProvider.getContext(conn);

    return db.select(
      ACCOUNT.ID,
      ACCOUNT.NAME
    )
      .from(ACCOUNT)
      .where(ACCOUNT.ID.eq(accountId))
      .fetchOne();
  }

  @Nullable
  public ResultSet fetchAccounts() throws DatabaseException {
    Connection conn = dbProvider.getConnectionTransaction();
    DSLContext db = dbProvider.getContext(conn);

    return db.select(
      ACCOUNT.ID,
      ACCOUNT.NAME
    )
      .from(ACCOUNT)
      .fetchResultSet();
  }

  @Override
  public void updateAccount(ULong accountId, AccountWrapper data) throws DatabaseException, ConfigException, BusinessException {
    Connection conn = dbProvider.getConnectionTransaction();
    DSLContext db = dbProvider.getContext(conn);

    try {
      updateAccount(db, accountId, data);
      dbProvider.commitAndClose(conn);
    } catch (Exception e) {
      dbProvider.rollbackAndClose(conn);
      throw e;
    }
  }

  @Override
  public void deleteAccount(ULong accountId) throws DatabaseException, ConfigException, BusinessException {
    Connection conn = dbProvider.getConnectionTransaction();
    DSLContext db = dbProvider.getContext(conn);

    try {
      deleteAccount(db, accountId);
      dbProvider.commitAndClose(conn);
    } catch (Exception e) {
      dbProvider.rollbackAndClose(conn);
      throw e;
    }
  }

  @Override
  public AccountRecord createAccount(AccountWrapper data) throws DatabaseException, ConfigException, BusinessException {
    Connection conn = dbProvider.getConnectionTransaction();
    DSLContext db = dbProvider.getContext(conn);
    AccountRecord newAccount;

    try {
      newAccount = createAccount(db, data);
      dbProvider.commitAndClose(conn);
    } catch (Exception e) {
      dbProvider.rollbackAndClose(conn);
      throw e;
    }
    return newAccount;
  }

  /**
   * Create a new AccountRecord from some data.
   *
   * @param db   context.
   * @param data for new Account.
   * @return accountId of newly created Account record.
   * @throws BusinessException if invalid
   */
  private AccountRecord createAccount(DSLContext db, AccountWrapper data) throws BusinessException {
    data.validate();

    AccountRecord newAccount = db.newRecord(ACCOUNT);
    newAccount.setName(data.getAccount().getName());
    newAccount.store();
    return newAccount;
  }

  /**
   * Update a specified Account.
   *
   * @param db        context.
   * @param accountId specific Account to update.
   * @param data      with which to update Account record.
   * @throws BusinessException if invalid
   */
  private void updateAccount(DSLContext db, ULong accountId, AccountWrapper data) throws BusinessException {
    data.validate();

    db.update(ACCOUNT)
      .set(ACCOUNT.NAME, data.getAccount().getName())
      .where(ACCOUNT.ID.eq(accountId))
      .execute();
  }

  /**
   * Delete the Account ONLY if there are no `library` or `account_user_role` belonging to it.
   *
   * @param db        context.
   */
  private void deleteAccount(DSLContext db, ULong accountId) throws BusinessException, DatabaseException {
    assertEmptyResultSet(db.select(LIBRARY.ID)
      .from(LIBRARY)
      .where(LIBRARY.ACCOUNT_ID.eq(accountId))
      .fetchResultSet());

    assertEmptyResultSet(db.select(ACCOUNT_USER_ROLE.ID)
      .from(ACCOUNT_USER_ROLE)
      .where(ACCOUNT_USER_ROLE.ACCOUNT_ID.eq(accountId))
      .fetchResultSet());

    db.deleteFrom(ACCOUNT)
      .where(ACCOUNT.ID.eq(accountId))
      .andNotExists(
        db.select(LIBRARY.ID)
          .from(LIBRARY)
          .where(LIBRARY.ACCOUNT_ID.eq(accountId))
      )
      .andNotExists(
        db.select(ACCOUNT_USER_ROLE.ID)
          .from(ACCOUNT_USER_ROLE)
          .where(ACCOUNT_USER_ROLE.ACCOUNT_ID.eq(accountId))
      )
      .execute();
  }

  /**
   * Fail if ResultSet is not empty.
   * @param resultSet to check.
   * @throws BusinessException if result set is not empty.
   * @throws DatabaseException if something goes wrong.
   */
  private void assertEmptyResultSet(ResultSet resultSet) throws BusinessException, DatabaseException {
    try {
      if (resultSet.next()) {
        throw new BusinessException("Cannot delete Account which has one or more "+resultSet.getMetaData().getTableName(1)+".");
      }
    } catch (SQLException e) {
      throw new DatabaseException("SQLException: " +e.getMessage());
    }
  }

  /**
   * SQL for "and not exists record belonging to X."
   *
   * @param table      to look up related record.
   * @param refIdName  of "belongs to" id key
   * @param refIdValue of "belongs to" id key
   * @return String SQL
   */
  private String sqlAndNotExists(String table, String refIdName, String refIdValue) {
    return "AND NOT EXISTS (" +
      "SELECT `id` FROM `" + table + "` " +
      "WHERE `" + table + "`.`" + refIdName + "`=" + refIdValue +
      ")";
  }

  /**
   * SQL for "delete a specific record (by id)"
   *
   * @param table to look up related record.
   * @param id    of record to delete
   * @return String SQL
   */
  private String sqlDeleteIdFrom(String table, String id) {
    return "DELETE FROM `" + table + "` WHERE `" + table + "`.`id`=" + id;
  }

}
