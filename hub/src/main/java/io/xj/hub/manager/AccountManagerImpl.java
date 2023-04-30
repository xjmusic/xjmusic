// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.manager;

import io.xj.hub.Tables;
import io.xj.hub.access.HubAccess;
import io.xj.hub.persistence.HubSqlStoreProvider;
import io.xj.hub.persistence.HubPersistenceServiceImpl;
import io.xj.hub.tables.AccountUser;
import io.xj.hub.tables.Library;
import io.xj.hub.tables.pojos.Account;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.JsonapiException;
import io.xj.lib.util.ValueException;
import io.xj.lib.util.Values;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.UUID;

@Service
public class AccountManagerImpl extends HubPersistenceServiceImpl implements AccountManager {

  public AccountManagerImpl(
    EntityFactory entityFactory,
    HubSqlStoreProvider sqlStoreProvider
  ) {
    super(entityFactory, sqlStoreProvider);
  }

  @Override
  public Account create(HubAccess access, Account entity) throws ManagerException, JsonapiException, ValueException {
    validate(entity);
    requireTopLevel(access);

    return modelFrom(Account.class,
      executeCreate(sqlStoreProvider.getDSL(), Tables.ACCOUNT, entity));
  }

  @Override
  public Account readOne(HubAccess access, UUID id) throws ManagerException {
    try (var selectAccount = sqlStoreProvider.getDSL().selectFrom(Tables.ACCOUNT)) {
      if (access.isTopLevel())
        return modelFrom(Account.class, selectAccount
          .where(Tables.ACCOUNT.ID.eq(id))
          .fetchOne());
      else return modelFrom(Account.class, selectAccount
        .where(Tables.ACCOUNT.ID.eq(id))
        .and(Tables.ACCOUNT.ID.in(access.getAccountIds()))
        .fetchOne());

    } catch (Exception e) {
      throw new ManagerException(e);
    }
  }

  @Override
  public Collection<Account> readMany(HubAccess access, Collection<UUID> parentIds) throws ManagerException {
    try (var selectAccount = sqlStoreProvider.getDSL().selectFrom(Tables.ACCOUNT)) {
      if (access.isTopLevel())
        return modelsFrom(Account.class,
          selectAccount
            .fetch());
      else
        return modelsFrom(Account.class,
          selectAccount
            .where(Tables.ACCOUNT.ID.in(access.getAccountIds()))
            .fetch());

    } catch (Exception e) {
      throw new ManagerException(e);
    }
  }

  @Override
  public Account update(HubAccess access, UUID id, Account entity) throws ManagerException, JsonapiException, ValueException {
    requireTopLevel(access);
    validate(entity);
    executeUpdate(sqlStoreProvider.getDSL(), Tables.ACCOUNT, id, entity);
    return entity;
  }

  @Override
  public void destroy(HubAccess access, UUID id) throws ManagerException {
    DSLContext db = sqlStoreProvider.getDSL();

    requireTopLevel(access);

    try (var selectLibrary = db.select(Library.LIBRARY.ID)) {
      requireNotExists("Account still has libraries!",
        selectLibrary
          .from(Library.LIBRARY)
          .where(Library.LIBRARY.ACCOUNT_ID.eq(id))
          .fetch());
    } catch (Exception e) {
      throw new ManagerException(e);
    }

    try (var selectAccountUser = db.select(AccountUser.ACCOUNT_USER.ID)) {
      requireNotExists("Account still has user access!",
        selectAccountUser
          .from(AccountUser.ACCOUNT_USER)
          .where(AccountUser.ACCOUNT_USER.ACCOUNT_ID.eq(id))
          .fetch());
    } catch (Exception e) {
      throw new ManagerException(e);
    }

    try (
      var deleteAccount = db.deleteFrom(Tables.ACCOUNT);
      var selectLibrary = db.select(Library.LIBRARY.ID);
      var selectAccountUser = db.select(AccountUser.ACCOUNT_USER.ID)
    ) {
      deleteAccount
        .where(Tables.ACCOUNT.ID.eq(id))
        .andNotExists(
          selectLibrary
            .from(Library.LIBRARY)
            .where(Library.LIBRARY.ACCOUNT_ID.eq(id)))
        .andNotExists(
          selectAccountUser
            .from(AccountUser.ACCOUNT_USER)
            .where(AccountUser.ACCOUNT_USER.ACCOUNT_ID.eq(id)))
        .execute();
    } catch (Exception e) {
      throw new ManagerException(e);
    }
  }

  @Override
  public Account newInstance() {
    return new Account();
  }

  /**
   Validate data

   @param record to validate
   @throws ManagerException if invalid
   */
  public void validate(Account record) throws ManagerException {
    try {
      Values.require(record.getName(), "Account name");

    } catch (ValueException e) {
      throw new ManagerException(e);
    }
  }

}
