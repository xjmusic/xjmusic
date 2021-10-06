// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.dao;

import com.google.inject.Inject;
import io.xj.hub.Tables;
import io.xj.hub.access.HubAccess;
import io.xj.hub.persistence.HubDatabaseProvider;
import io.xj.hub.tables.AccountUser;
import io.xj.hub.tables.Library;
import io.xj.hub.tables.pojos.Account;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.JsonapiException;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.util.ValueException;
import io.xj.lib.util.Values;
import org.jooq.DSLContext;

import java.util.Collection;
import java.util.UUID;

public class AccountDAOImpl extends DAOImpl<Account> implements AccountDAO {

  @Inject
  public AccountDAOImpl(
    JsonapiPayloadFactory payloadFactory,
    EntityFactory entityFactory,
    HubDatabaseProvider dbProvider
  ) {
    super(payloadFactory, entityFactory);
    this.dbProvider = dbProvider;
  }

  @Override
  public Account create(HubAccess hubAccess, Account entity) throws DAOException, JsonapiException, ValueException {
    validate(entity);
    requireTopLevel(hubAccess);

    return modelFrom(Account.class,
      executeCreate(dbProvider.getDSL(), Tables.ACCOUNT, entity));
  }

  @Override
  public Account readOne(HubAccess hubAccess, UUID id) throws DAOException {
    if (hubAccess.isTopLevel())
      return modelFrom(Account.class,
        dbProvider.getDSL().selectFrom(Tables.ACCOUNT)
          .where(Tables.ACCOUNT.ID.eq(id))
          .fetchOne());
    else
      return modelFrom(Account.class,
        dbProvider.getDSL().select(Tables.ACCOUNT.fields())
          .from(Tables.ACCOUNT)
          .where(Tables.ACCOUNT.ID.eq(id))
          .and(Tables.ACCOUNT.ID.in(hubAccess.getAccountIds()))
          .fetchOne());
  }

  @Override
  public Collection<Account> readMany(HubAccess hubAccess, Collection<UUID> parentIds) throws DAOException {
    if (hubAccess.isTopLevel())
      return modelsFrom(Account.class,
        dbProvider.getDSL().selectFrom(Tables.ACCOUNT)
          .fetch());
    else
      return modelsFrom(Account.class,
        dbProvider.getDSL().selectFrom(Tables.ACCOUNT)
          .where(Tables.ACCOUNT.ID.in(hubAccess.getAccountIds()))
          .fetch());
  }

  @Override
  public Account update(HubAccess hubAccess, UUID id, Account entity) throws DAOException, JsonapiException, ValueException {
    requireTopLevel(hubAccess);
    validate(entity);
    executeUpdate(dbProvider.getDSL(), Tables.ACCOUNT, id, entity);
    return entity;
  }

  @Override
  public void destroy(HubAccess hubAccess, UUID id) throws DAOException {
    DSLContext db = dbProvider.getDSL();

    requireTopLevel(hubAccess);

    requireNotExists("Library in Account", db.select(Library.LIBRARY.ID)
      .from(Library.LIBRARY)
      .where(Library.LIBRARY.ACCOUNT_ID.eq(id))
      .fetch());

    requireNotExists("User in Account", db.select(AccountUser.ACCOUNT_USER.ID)
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
   @throws DAOException if invalid
   */
  public void validate(Account record) throws DAOException {
    try {
      Values.require(record.getName(), "Account name");

    } catch (ValueException e) {
      throw new DAOException(e);
    }
  }

}
