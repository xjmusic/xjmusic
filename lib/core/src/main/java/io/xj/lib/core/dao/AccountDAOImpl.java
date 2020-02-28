// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.core.dao;

import com.google.inject.Inject;
import io.xj.lib.core.access.Access;
import io.xj.lib.core.exception.CoreException;
import io.xj.lib.core.model.Account;
import io.xj.lib.core.persistence.SQLDatabaseProvider;
import org.jooq.DSLContext;

import java.util.Collection;
import java.util.UUID;

import static io.xj.lib.core.Tables.ACCOUNT;
import static io.xj.lib.core.tables.AccountUser.ACCOUNT_USER;
import static io.xj.lib.core.tables.Chain.CHAIN;
import static io.xj.lib.core.tables.Library.LIBRARY;

public class AccountDAOImpl extends DAOImpl<Account> implements AccountDAO {

  @Inject
  public AccountDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public Account create(Access access, Account entity) throws CoreException {
    entity.validate();
    requireTopLevel(access);

    return DAO.modelFrom(Account.class,
      executeCreate(dbProvider.getDSL(), ACCOUNT, entity));
  }

  @Override
  public Account readOne(Access access, UUID id) throws CoreException {
    if (access.isTopLevel())
      return DAO.modelFrom(Account.class,
        dbProvider.getDSL().selectFrom(ACCOUNT)
          .where(ACCOUNT.ID.eq(id))
          .fetchOne());
    else
      return DAO.modelFrom(Account.class,
        dbProvider.getDSL().select(ACCOUNT.fields())
          .from(ACCOUNT)
          .where(ACCOUNT.ID.eq(id))
          .and(ACCOUNT.ID.in(access.getAccountIds()))
          .fetchOne());
  }

  @Override
  public Collection<Account> readMany(Access access, Collection<UUID> parentIds) throws CoreException {
    if (access.isTopLevel())
      return DAO.modelsFrom(Account.class,
        dbProvider.getDSL().selectFrom(ACCOUNT)
          .fetch());
    else
      return DAO.modelsFrom(Account.class,
        dbProvider.getDSL().selectFrom(ACCOUNT)
          .where(ACCOUNT.ID.in(access.getAccountIds()))
          .fetch());
  }

  @Override
  public void update(Access access, UUID id, Account entity) throws CoreException {
    requireTopLevel(access);
    entity.validate();

    executeUpdate(dbProvider.getDSL(), ACCOUNT, id, entity);
  }

  @Override
  public void destroy(Access access, UUID id) throws CoreException {
    DSLContext db = dbProvider.getDSL();

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
  }

  @Override
  public Account newInstance() {
    return new Account();
  }

}
