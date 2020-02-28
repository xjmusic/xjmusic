// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.core.dao;

import com.google.inject.Inject;
import io.xj.lib.core.access.Access;
import io.xj.lib.core.exception.CoreException;
import io.xj.lib.core.model.AccountUser;
import io.xj.lib.core.persistence.SQLDatabaseProvider;

import java.util.Collection;
import java.util.UUID;

import static io.xj.lib.core.tables.AccountUser.ACCOUNT_USER;

public class AccountUserDAOImpl extends DAOImpl<AccountUser> implements AccountUserDAO {

  @Inject
  public AccountUserDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public AccountUser create(Access access, AccountUser entity) throws CoreException {
    entity.validate();

    requireTopLevel(access);

    if (null != dbProvider.getDSL().selectFrom(ACCOUNT_USER)
      .where(ACCOUNT_USER.ACCOUNT_ID.eq(entity.getAccountId()))
      .and(ACCOUNT_USER.USER_ID.eq(entity.getUserId()))
      .fetchOne())
      throw new CoreException("Account User already exists!");

    return DAO.modelFrom(AccountUser.class, executeCreate(dbProvider.getDSL(), ACCOUNT_USER, entity));
  }

  @Override
  public AccountUser readOne(Access access, UUID id) throws CoreException {
    if (access.isTopLevel())
      return DAO.modelFrom(AccountUser.class, dbProvider.getDSL().selectFrom(ACCOUNT_USER)
        .where(ACCOUNT_USER.ID.eq(id))
        .fetchOne());
    else
      return DAO.modelFrom(AccountUser.class, dbProvider.getDSL().selectFrom(ACCOUNT_USER)
        .where(ACCOUNT_USER.ID.eq(id))
        .and(ACCOUNT_USER.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne());
  }

  @Override
  public Collection<AccountUser> readMany(Access access, Collection<UUID> parentIds) throws CoreException {
    if (access.isTopLevel())
      return DAO.modelsFrom(AccountUser.class, dbProvider.getDSL().selectFrom(ACCOUNT_USER)
        .where(ACCOUNT_USER.ACCOUNT_ID.in(parentIds))
        .fetch());
    else
      return DAO.modelsFrom(AccountUser.class, dbProvider.getDSL().selectFrom(ACCOUNT_USER)
        .where(ACCOUNT_USER.ACCOUNT_ID.in(parentIds))
        .and(ACCOUNT_USER.ACCOUNT_ID.in(access.getAccountIds()))
        .fetch());
  }

  @Override
  public void update(Access access, UUID id, AccountUser entity) throws CoreException {
    throw new CoreException("Not allowed to update AccountUser record.");
  }

  @Override
  public void destroy(Access access, UUID id) throws CoreException {
    requireTopLevel(access);
    dbProvider.getDSL().deleteFrom(ACCOUNT_USER)
      .where(ACCOUNT_USER.ID.eq(id))
      .execute();
  }

  @Override
  public AccountUser newInstance() {
    return new AccountUser();
  }

}
