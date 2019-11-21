// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao.impl;

import com.google.inject.Inject;
import io.xj.core.access.Access;
import io.xj.core.dao.AccountDAO;
import io.xj.core.dao.DAORecord;
import io.xj.core.exception.CoreException;
import io.xj.core.model.Account;
import io.xj.core.persistence.sql.SQLDatabaseProvider;
import org.jooq.DSLContext;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.UUID;

import static io.xj.core.Tables.ACCOUNT;
import static io.xj.core.tables.AccountUser.ACCOUNT_USER;
import static io.xj.core.tables.Chain.CHAIN;
import static io.xj.core.tables.Library.LIBRARY;

public class AccountDAOImpl extends DAOImpl<Account> implements AccountDAO {

  @Inject
  public AccountDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public Account create(Access access, Account entity) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      entity.validate();
      requireTopLevel(access);

      return DAORecord.modelFrom(Account.class,
        executeCreate(connection, ACCOUNT, entity));

    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  public Account readOne(Access access, UUID id) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      if (access.isTopLevel())
        return DAORecord.modelFrom(Account.class,
          DAORecord.DSL(connection).selectFrom(ACCOUNT)
            .where(ACCOUNT.ID.eq(id))
            .fetchOne());
      else
        return DAORecord.modelFrom(Account.class,
          DAORecord.DSL(connection).select(ACCOUNT.fields())
            .from(ACCOUNT)
            .where(ACCOUNT.ID.eq(id))
            .and(ACCOUNT.ID.in(access.getAccountIds()))
            .fetchOne());
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  public Collection<Account> readMany(Access access, Collection<UUID> parentIds) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      if (access.isTopLevel())
        return DAORecord.modelsFrom(Account.class,
          DAORecord.DSL(connection).selectFrom(ACCOUNT)
            .fetch());
      else
        return DAORecord.modelsFrom(Account.class,
          DAORecord.DSL(connection).selectFrom(ACCOUNT)
            .where(ACCOUNT.ID.in(access.getAccountIds()))
            .fetch());
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  public void update(Access access, UUID id, Account entity) throws CoreException {
    requireTopLevel(access);
    try (Connection connection = dbProvider.getConnection()) {
      entity.validate();

      executeUpdate(connection, ACCOUNT, id, entity);
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  public void destroy(Access access, UUID id) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      DSLContext db = DAORecord.DSL(connection);

      requireTopLevel(access);

      requireNotExists("Library in Account", db.select(LIBRARY.ID)
        .from(LIBRARY)
        .where(LIBRARY.ACCOUNT_ID.eq(id))
        .fetch());

      requireNotExists("Chain in Account", db.select(CHAIN.ID)
        .from(CHAIN)
        .where(CHAIN.ACCOUNT_ID.eq(id))
        .fetch());

      requireNotExists("User in Account", db.select(ACCOUNT_USER.ID)
        .from(ACCOUNT_USER)
        .where(ACCOUNT_USER.ACCOUNT_ID.eq(id))
        .fetch());

      db.deleteFrom(ACCOUNT)
        .where(ACCOUNT.ID.eq(id))
        .andNotExists(
          db.select(LIBRARY.ID)
            .from(LIBRARY)
            .where(LIBRARY.ACCOUNT_ID.eq(id)))
        .andNotExists(
          db.select(CHAIN.ID)
            .from(CHAIN)
            .where(CHAIN.ACCOUNT_ID.eq(id)))
        .andNotExists(
          db.select(ACCOUNT_USER.ID)
            .from(ACCOUNT_USER)
            .where(ACCOUNT_USER.ACCOUNT_ID.eq(id)))
        .execute();
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  public Account newInstance() {
    return new Account();
  }

}
