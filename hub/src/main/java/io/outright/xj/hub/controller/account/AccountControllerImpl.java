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
import org.jooq.types.ULong;

import javax.annotation.Nullable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import static io.outright.xj.core.Tables.ACCOUNT;
import static io.outright.xj.core.tables.AccountUser.ACCOUNT_USER;
import static io.outright.xj.core.tables.Library.LIBRARY;

public class AccountControllerImpl implements AccountController {
//  private static Logger log = LoggerFactory.getLogger(AccountControllerImpl.class);
  private SQLDatabaseProvider dbProvider;

  @Inject
  public AccountControllerImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public AccountRecord create(AccountWrapper data) throws DatabaseException, ConfigException, BusinessException {
    Connection conn = dbProvider.getConnectionTransaction();
    DSLContext db = dbProvider.getContext(conn);
    AccountRecord newAccount;

    try {
      data.validate();

      newAccount = db.newRecord(ACCOUNT);
      newAccount.setName(data.getAccount().getName());
      newAccount.store();

      dbProvider.commitAndClose(conn);
    } catch (Exception e) {
      dbProvider.rollbackAndClose(conn);
      throw e;
    }
    return newAccount;
  }

  @Override
  @Nullable
  public AccountRecord read(ULong accountId) throws DatabaseException {
    Connection conn = dbProvider.getConnectionTransaction();
    DSLContext db = dbProvider.getContext(conn);

    return db.selectFrom(ACCOUNT)
      .where(ACCOUNT.ID.eq(accountId))
      .fetchOne();
  }

  @Override
  @Nullable
  public ResultSet readAll() throws DatabaseException {
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
  public void update(ULong accountId, AccountWrapper data) throws DatabaseException, ConfigException, BusinessException {
    Connection conn = dbProvider.getConnectionTransaction();
    DSLContext db = dbProvider.getContext(conn);

    try {
      data.validate();

      db.update(ACCOUNT)
        .set(ACCOUNT.NAME, data.getAccount().getName())
        .where(ACCOUNT.ID.eq(accountId))
        .execute();

      dbProvider.commitAndClose(conn);
    } catch (Exception e) {
      dbProvider.rollbackAndClose(conn);
      throw e;
    }
  }

  @Override
  public void delete(ULong accountId) throws DatabaseException, ConfigException, BusinessException {
    Connection conn = dbProvider.getConnectionTransaction();
    DSLContext db = dbProvider.getContext(conn);

    try {
      assertEmptyResultSet(db.select(LIBRARY.ID)
        .from(LIBRARY)
        .where(LIBRARY.ACCOUNT_ID.eq(accountId))
        .fetchResultSet());

      assertEmptyResultSet(db.select(ACCOUNT_USER.ID)
        .from(ACCOUNT_USER)
        .where(ACCOUNT_USER.ACCOUNT_ID.eq(accountId))
        .fetchResultSet());

      db.deleteFrom(ACCOUNT)
        .where(ACCOUNT.ID.eq(accountId))
        .andNotExists(
          db.select(LIBRARY.ID)
            .from(LIBRARY)
            .where(LIBRARY.ACCOUNT_ID.eq(accountId))
        )
        .andNotExists(
          db.select(ACCOUNT_USER.ID)
            .from(ACCOUNT_USER)
            .where(ACCOUNT_USER.ACCOUNT_ID.eq(accountId))
        )
        .execute();

      dbProvider.commitAndClose(conn);
    } catch (Exception e) {
      dbProvider.rollbackAndClose(conn);
      throw e;
    }
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

}
