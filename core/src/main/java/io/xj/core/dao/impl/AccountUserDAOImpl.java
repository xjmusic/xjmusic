// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao.impl;

import com.google.inject.Inject;
import io.xj.core.access.Access;
import io.xj.core.dao.AccountUserDAO;
import io.xj.core.dao.DAORecord;
import io.xj.core.exception.CoreException;
import io.xj.core.model.AccountUser;
import io.xj.core.persistence.sql.SQLDatabaseProvider;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.UUID;

import static io.xj.core.tables.AccountUser.ACCOUNT_USER;

public class AccountUserDAOImpl extends DAOImpl<AccountUser> implements AccountUserDAO {

  @Inject
  public AccountUserDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public AccountUser create(Access access, AccountUser entity) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      entity.validate();

      requireTopLevel(access);

      if (null != DAORecord.DSL(connection).selectFrom(ACCOUNT_USER)
        .where(ACCOUNT_USER.ACCOUNT_ID.eq(entity.getAccountId()))
        .and(ACCOUNT_USER.USER_ID.eq(entity.getUserId()))
        .fetchOne())
        throw new CoreException("Account User already exists!");

      return DAORecord.modelFrom(AccountUser.class, executeCreate(connection, ACCOUNT_USER, entity));
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  public AccountUser readOne(Access access, UUID id) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      if (access.isTopLevel())
        return DAORecord.modelFrom(AccountUser.class, DAORecord.DSL(connection).selectFrom(ACCOUNT_USER)
          .where(ACCOUNT_USER.ID.eq(id))
          .fetchOne());
      else
        return DAORecord.modelFrom(AccountUser.class, DAORecord.DSL(connection).selectFrom(ACCOUNT_USER)
          .where(ACCOUNT_USER.ID.eq(id))
          .and(ACCOUNT_USER.ACCOUNT_ID.in(access.getAccountIds()))
          .fetchOne());
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  public Collection<AccountUser> readMany(Access access, Collection<UUID> parentIds) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      if (access.isTopLevel())
        return DAORecord.modelsFrom(AccountUser.class, DAORecord.DSL(connection).selectFrom(ACCOUNT_USER)
          .where(ACCOUNT_USER.ACCOUNT_ID.in(parentIds))
          .fetch());
      else
        return DAORecord.modelsFrom(AccountUser.class, DAORecord.DSL(connection).selectFrom(ACCOUNT_USER)
          .where(ACCOUNT_USER.ACCOUNT_ID.in(parentIds))
          .and(ACCOUNT_USER.ACCOUNT_ID.in(access.getAccountIds()))
          .fetch());
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  public void update(Access access, UUID id, AccountUser entity) throws CoreException {
    throw new CoreException("Not allowed to update AccountUser record.");
  }

  @Override
  public void destroy(Access access, UUID id) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      requireTopLevel(access);
      DAORecord.DSL(connection).deleteFrom(ACCOUNT_USER)
        .where(ACCOUNT_USER.ID.eq(id))
        .execute();
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  public AccountUser newInstance() {
    return new AccountUser();
  }

}
