// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.manager;

import com.google.inject.Inject;
import io.xj.hub.access.HubAccess;
import io.xj.hub.persistence.HubDatabaseProvider;
import io.xj.hub.persistence.HubPersistenceServiceImpl;
import io.xj.hub.tables.pojos.AccountUser;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.JsonapiException;
import io.xj.lib.util.ValueException;
import io.xj.lib.util.Values;

import java.util.Collection;
import java.util.UUID;

import static io.xj.hub.tables.AccountUser.ACCOUNT_USER;

public class AccountUserManagerImpl extends HubPersistenceServiceImpl<AccountUser> implements AccountUserManager {

  @Inject
  public AccountUserManagerImpl(
    EntityFactory entityFactory,
    HubDatabaseProvider dbProvider
  ) {
    super(entityFactory, dbProvider);
  }

  @Override
  public AccountUser create(HubAccess access, AccountUser entity) throws ManagerException, JsonapiException, ValueException {
    validate(entity);
    requireTopLevel(access);

    if (null != dbProvider.getDSL().selectFrom(ACCOUNT_USER)
      .where(ACCOUNT_USER.ACCOUNT_ID.eq(entity.getAccountId()))
      .and(ACCOUNT_USER.USER_ID.eq(entity.getUserId()))
      .fetchOne())
      throw new ManagerException("Account User already exists!");

    return modelFrom(AccountUser.class, executeCreate(dbProvider.getDSL(), ACCOUNT_USER, entity));
  }

  @Override
  public AccountUser readOne(HubAccess access, UUID id) throws ManagerException {
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
  public Collection<AccountUser> readMany(HubAccess access, Collection<UUID> parentIds) throws ManagerException {
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
  public AccountUser update(HubAccess access, UUID id, AccountUser entity) throws ManagerException {
    throw new ManagerException("Not allowed to update AccountUser record.");
  }

  @Override
  public void destroy(HubAccess access, UUID id) throws ManagerException {
    requireTopLevel(access);
    dbProvider.getDSL().deleteFrom(ACCOUNT_USER)
      .where(ACCOUNT_USER.ID.eq(id))
      .execute();
  }

  @Override
  public AccountUser newInstance() {
    return new AccountUser();
  }


  /**
   Validate data

   @param record to validate
   @throws ManagerException if invalid
   */
  public void validate(AccountUser record) throws ManagerException {
    try {
      Values.require(record.getAccountId(), "Account ID");
      Values.require(record.getUserId(), "User ID");

    } catch (ValueException e) {
      throw new ManagerException(e);
    }
  }

}
