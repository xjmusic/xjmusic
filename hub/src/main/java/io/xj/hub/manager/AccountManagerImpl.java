// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.manager;

import com.google.inject.Inject;
import io.xj.hub.Tables;
import io.xj.hub.access.HubAccess;
import io.xj.hub.persistence.HubDatabaseProvider;
import io.xj.hub.persistence.HubPersistenceServiceImpl;
import io.xj.hub.tables.AccountUser;
import io.xj.hub.tables.Library;
import io.xj.hub.tables.pojos.Account;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.JsonapiException;
import io.xj.lib.util.ValueException;
import io.xj.lib.util.Values;
import org.jooq.DSLContext;

import java.util.Collection;
import java.util.UUID;

public class AccountManagerImpl extends HubPersistenceServiceImpl<Account> implements AccountManager {

  @Inject
  public AccountManagerImpl(
    EntityFactory entityFactory,
    HubDatabaseProvider dbProvider
  ) {
    super(entityFactory, dbProvider);
  }

  @Override
  public Account create(HubAccess access, Account entity) throws ManagerException, JsonapiException, ValueException {
    validate(entity);
    requireTopLevel(access);

    return modelFrom(Account.class,
      executeCreate(dbProvider.getDSL(), Tables.ACCOUNT, entity));
  }

  @Override
  public Account readOne(HubAccess access, UUID id) throws ManagerException {
    if (access.isTopLevel())
      return modelFrom(Account.class,
        dbProvider.getDSL().selectFrom(Tables.ACCOUNT)
          .where(Tables.ACCOUNT.ID.eq(id))
          .fetchOne());
    else
      return modelFrom(Account.class,
        dbProvider.getDSL().select(Tables.ACCOUNT.fields())
          .from(Tables.ACCOUNT)
          .where(Tables.ACCOUNT.ID.eq(id))
          .and(Tables.ACCOUNT.ID.in(access.getAccountIds()))
          .fetchOne());
  }

  @Override
  public Collection<Account> readMany(HubAccess access, Collection<UUID> parentIds) throws ManagerException {
    if (access.isTopLevel())
      return modelsFrom(Account.class,
        dbProvider.getDSL().selectFrom(Tables.ACCOUNT)
          .fetch());
    else
      return modelsFrom(Account.class,
        dbProvider.getDSL().selectFrom(Tables.ACCOUNT)
          .where(Tables.ACCOUNT.ID.in(access.getAccountIds()))
          .fetch());
  }

  @Override
  public Account update(HubAccess access, UUID id, Account entity) throws ManagerException, JsonapiException, ValueException {
    requireTopLevel(access);
    validate(entity);
    executeUpdate(dbProvider.getDSL(), Tables.ACCOUNT, id, entity);
    return entity;
  }

  @Override
  public void destroy(HubAccess access, UUID id) throws ManagerException {
    DSLContext db = dbProvider.getDSL();

    requireTopLevel(access);

    requireNotExists("Account still has libraries!", db.select(Library.LIBRARY.ID)
      .from(Library.LIBRARY)
      .where(Library.LIBRARY.ACCOUNT_ID.eq(id))
      .fetch());

    requireNotExists("Account still has user access!", db.select(AccountUser.ACCOUNT_USER.ID)
      .from(AccountUser.ACCOUNT_USER)
      .where(AccountUser.ACCOUNT_USER.ACCOUNT_ID.eq(id))
      .fetch());

    db.deleteFrom(Tables.ACCOUNT)
      .where(Tables.ACCOUNT.ID.eq(id))
      .andNotExists(
        db.select(Library.LIBRARY.ID)
          .from(Library.LIBRARY)
          .where(Library.LIBRARY.ACCOUNT_ID.eq(id)))
      .andNotExists(
        db.select(AccountUser.ACCOUNT_USER.ID)
          .from(AccountUser.ACCOUNT_USER)
          .where(AccountUser.ACCOUNT_USER.ACCOUNT_ID.eq(id)))
      .execute();
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
