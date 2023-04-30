// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.manager;

import io.xj.hub.access.HubAccess;
import io.xj.hub.persistence.HubSqlStoreProvider;
import io.xj.hub.persistence.HubPersistenceServiceImpl;
import io.xj.hub.tables.pojos.AccountUser;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.JsonapiException;
import io.xj.lib.util.ValueException;
import io.xj.lib.util.Values;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.UUID;

import static io.xj.hub.tables.AccountUser.ACCOUNT_USER;

@Service
public class AccountUserManagerImpl extends HubPersistenceServiceImpl implements AccountUserManager {

  public AccountUserManagerImpl(
    EntityFactory entityFactory,
    HubSqlStoreProvider sqlStoreProvider
  ) {
    super(entityFactory, sqlStoreProvider);
  }

  @Override
  public AccountUser create(HubAccess access, AccountUser entity) throws ManagerException, JsonapiException, ValueException {
    validate(entity);
    requireTopLevel(access);

    try (var selectAccountUser = sqlStoreProvider.getDSL().selectFrom(ACCOUNT_USER)) {
      if (null != selectAccountUser
        .where(ACCOUNT_USER.ACCOUNT_ID.eq(entity.getAccountId()))
        .and(ACCOUNT_USER.USER_ID.eq(entity.getUserId()))
        .fetchOne())
        throw new ManagerException("Account User already exists!");
    } catch (Exception e) {
      throw new ManagerException(e);
    }

    return modelFrom(AccountUser.class, executeCreate(sqlStoreProvider.getDSL(), ACCOUNT_USER, entity));
  }

  @Override
  public AccountUser readOne(HubAccess access, UUID id) throws ManagerException {
    try (var selectAccountUser = sqlStoreProvider.getDSL().selectFrom(ACCOUNT_USER)) {
      if (access.isTopLevel())
        return modelFrom(AccountUser.class, selectAccountUser
          .where(ACCOUNT_USER.ID.eq(id))
          .fetchOne());
      else
        return modelFrom(AccountUser.class, selectAccountUser
          .where(ACCOUNT_USER.ID.eq(id))
          .and(ACCOUNT_USER.ACCOUNT_ID.in(access.getAccountIds()))
          .fetchOne());
    } catch (Exception e) {
      throw new ManagerException(e);
    }
  }

  @Override
  public Collection<AccountUser> readMany(HubAccess access, Collection<UUID> parentIds) throws ManagerException {
    try (var selectAccountUser = sqlStoreProvider.getDSL().selectFrom(ACCOUNT_USER)) {
      if (access.isTopLevel())
        return modelsFrom(AccountUser.class, selectAccountUser
          .where(ACCOUNT_USER.ACCOUNT_ID.in(parentIds))
          .fetch());
      else
        return modelsFrom(AccountUser.class, selectAccountUser
          .where(ACCOUNT_USER.ACCOUNT_ID.in(parentIds))
          .and(ACCOUNT_USER.ACCOUNT_ID.in(access.getAccountIds()))
          .fetch());
    } catch (Exception e) {
      throw new ManagerException(e);
    }
  }

  @Override
  public AccountUser update(HubAccess access, UUID id, AccountUser entity) throws ManagerException {
    throw new ManagerException("Not allowed to update AccountUser record.");
  }

  @Override
  public void destroy(HubAccess access, UUID id) throws ManagerException {
    requireTopLevel(access);
    try (var selectAccountUser = sqlStoreProvider.getDSL().deleteFrom(ACCOUNT_USER)) {
      selectAccountUser
        .where(ACCOUNT_USER.ID.eq(id))
        .execute();
    } catch (Exception e) {
      throw new ManagerException(e);
    }
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
