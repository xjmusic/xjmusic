// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.dao;

import com.google.inject.Inject;
import io.xj.lib.rest_api.PayloadFactory;
import io.xj.lib.rest_api.RestApiException;
import io.xj.lib.util.ValueException;
import io.xj.service.hub.HubException;
import io.xj.service.hub.access.Access;
import io.xj.service.hub.model.AccountUser;
import io.xj.service.hub.persistence.SQLDatabaseProvider;

import java.util.Collection;
import java.util.UUID;

import static io.xj.service.hub.tables.AccountUser.ACCOUNT_USER;

public class AccountUserDAOImpl extends DAOImpl<AccountUser> implements AccountUserDAO {

  @Inject
  public AccountUserDAOImpl(
    PayloadFactory payloadFactory,
    SQLDatabaseProvider dbProvider
  ) {
    super(payloadFactory);
    this.dbProvider = dbProvider;
  }

  @Override
  public AccountUser create(Access access, AccountUser entity) throws HubException, RestApiException, ValueException {
    entity.validate();
    requireTopLevel(access);

    if (null != dbProvider.getDSL().selectFrom(ACCOUNT_USER)
      .where(ACCOUNT_USER.ACCOUNT_ID.eq(entity.getAccountId()))
      .and(ACCOUNT_USER.USER_ID.eq(entity.getUserId()))
      .fetchOne())
      throw new HubException("Account User already exists!");

    return modelFrom(AccountUser.class, executeCreate(dbProvider.getDSL(), ACCOUNT_USER, entity));
  }

  @Override
  public AccountUser readOne(Access access, UUID id) throws HubException {
    if (access.isTopLevel())
      return modelFrom(AccountUser.class, dbProvider.getDSL().selectFrom(ACCOUNT_USER)
        .where(ACCOUNT_USER.ID.eq(id))
        .fetchOne());
    else
      return modelFrom(AccountUser.class, dbProvider.getDSL().selectFrom(ACCOUNT_USER)
        .where(ACCOUNT_USER.ID.eq(id))
        .and(ACCOUNT_USER.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne());
  }

  @Override
  public Collection<AccountUser> readMany(Access access, Collection<UUID> parentIds) throws HubException {
    if (access.isTopLevel())
      return modelsFrom(AccountUser.class, dbProvider.getDSL().selectFrom(ACCOUNT_USER)
        .where(ACCOUNT_USER.ACCOUNT_ID.in(parentIds))
        .fetch());
    else
      return modelsFrom(AccountUser.class, dbProvider.getDSL().selectFrom(ACCOUNT_USER)
        .where(ACCOUNT_USER.ACCOUNT_ID.in(parentIds))
        .and(ACCOUNT_USER.ACCOUNT_ID.in(access.getAccountIds()))
        .fetch());
  }

  @Override
  public void update(Access access, UUID id, AccountUser entity) throws HubException {
    throw new HubException("Not allowed to update AccountUser record.");
  }

  @Override
  public void destroy(Access access, UUID id) throws HubException {
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
