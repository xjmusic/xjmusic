// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.dao;

import com.google.inject.Inject;
import io.xj.AccountUser;
import io.xj.hub.access.HubAccess;
import io.xj.hub.persistence.HubDatabaseProvider;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.JsonApiException;
import io.xj.lib.jsonapi.PayloadFactory;
import io.xj.lib.util.Value;
import io.xj.lib.util.ValueException;

import java.util.Collection;
import java.util.UUID;

import static io.xj.hub.tables.AccountUser.ACCOUNT_USER;

public class AccountUserDAOImpl extends DAOImpl<AccountUser> implements AccountUserDAO {

  @Inject
  public AccountUserDAOImpl(
    PayloadFactory payloadFactory,
    EntityFactory entityFactory,
    HubDatabaseProvider dbProvider
  ) {
    super(payloadFactory, entityFactory);
    this.dbProvider = dbProvider;
  }

  @Override
  public AccountUser create(HubAccess hubAccess, AccountUser entity) throws DAOException, JsonApiException, ValueException {
    validate(entity);
    requireTopLevel(hubAccess);

    if (null != dbProvider.getDSL().selectFrom(ACCOUNT_USER)
      .where(ACCOUNT_USER.ACCOUNT_ID.eq(UUID.fromString(entity.getAccountId())))
      .and(ACCOUNT_USER.USER_ID.eq(UUID.fromString(entity.getUserId())))
      .fetchOne())
      throw new DAOException("Account User already exists!");

    return modelFrom(AccountUser.class, executeCreate(dbProvider.getDSL(), ACCOUNT_USER, entity));
  }

  @Override
  public AccountUser readOne(HubAccess hubAccess, String id) throws DAOException {
    if (hubAccess.isTopLevel())
      return modelFrom(AccountUser.class, dbProvider.getDSL().selectFrom(ACCOUNT_USER)
        .where(ACCOUNT_USER.ID.eq(UUID.fromString(id)))
        .fetchOne());
    else
      return modelFrom(AccountUser.class, dbProvider.getDSL().selectFrom(ACCOUNT_USER)
        .where(ACCOUNT_USER.ID.eq(UUID.fromString(id)))
        .and(ACCOUNT_USER.ACCOUNT_ID.in(hubAccess.getAccountIds()))
        .fetchOne());
  }

  @Override
  public Collection<AccountUser> readMany(HubAccess hubAccess, Collection<String> parentIds) throws DAOException {
    if (hubAccess.isTopLevel())
      return modelsFrom(AccountUser.class, dbProvider.getDSL().selectFrom(ACCOUNT_USER)
        .where(ACCOUNT_USER.ACCOUNT_ID.in(parentIds))
        .fetch());
    else
      return modelsFrom(AccountUser.class, dbProvider.getDSL().selectFrom(ACCOUNT_USER)
        .where(ACCOUNT_USER.ACCOUNT_ID.in(parentIds))
        .and(ACCOUNT_USER.ACCOUNT_ID.in(hubAccess.getAccountIds()))
        .fetch());
  }

  @Override
  public AccountUser update(HubAccess hubAccess, String id, AccountUser entity) throws DAOException {
    throw new DAOException("Not allowed to update AccountUser record.");
  }

  @Override
  public void destroy(HubAccess hubAccess, String id) throws DAOException {
    requireTopLevel(hubAccess);
    dbProvider.getDSL().deleteFrom(ACCOUNT_USER)
      .where(ACCOUNT_USER.ID.eq(UUID.fromString(id)))
      .execute();
  }

  @Override
  public AccountUser newInstance() {
    return AccountUser.getDefaultInstance();
  }


  /**
   Validate data

   @param record to validate
   @throws DAOException if invalid
   */
  public void validate(AccountUser record) throws DAOException {
    try {
      Value.require(record.getAccountId(), "Account ID");
      Value.require(record.getUserId(), "User ID");

    } catch (ValueException e) {
      throw new DAOException(e);
    }
  }

}
