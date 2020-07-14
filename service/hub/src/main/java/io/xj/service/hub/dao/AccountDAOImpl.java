// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.dao;

import com.google.inject.Inject;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.PayloadFactory;
import io.xj.lib.jsonapi.JsonApiException;
import io.xj.lib.util.ValueException;
import io.xj.service.hub.access.HubAccess;
import io.xj.service.hub.entity.Account;
import io.xj.service.hub.persistence.HubDatabaseProvider;
import org.jooq.DSLContext;

import java.util.Collection;
import java.util.UUID;

import static io.xj.service.hub.Tables.ACCOUNT;
import static io.xj.service.hub.tables.AccountUser.ACCOUNT_USER;
import static io.xj.service.hub.tables.Library.LIBRARY;

public class AccountDAOImpl extends DAOImpl<Account> implements AccountDAO {

  @Inject
  public AccountDAOImpl(
    PayloadFactory payloadFactory,
    EntityFactory entityFactory,
    HubDatabaseProvider dbProvider
  ) {
    super(payloadFactory, entityFactory);
    this.dbProvider = dbProvider;
  }

  @Override
  public Account create(HubAccess hubAccess, Account entity) throws DAOException, JsonApiException, ValueException {
    entity.validate();
    requireTopLevel(hubAccess);

    return modelFrom(Account.class,
      executeCreate(dbProvider.getDSL(), ACCOUNT, entity));
  }

  @Override
  public Account readOne(HubAccess hubAccess, UUID id) throws DAOException {
    if (hubAccess.isTopLevel())
      return modelFrom(Account.class,
        dbProvider.getDSL().selectFrom(ACCOUNT)
          .where(ACCOUNT.ID.eq(id))
          .fetchOne());
    else
      return modelFrom(Account.class,
        dbProvider.getDSL().select(ACCOUNT.fields())
          .from(ACCOUNT)
          .where(ACCOUNT.ID.eq(id))
          .and(ACCOUNT.ID.in(hubAccess.getAccountIds()))
          .fetchOne());
  }

  @Override
  public Collection<Account> readMany(HubAccess hubAccess, Collection<UUID> parentIds) throws DAOException {
    if (hubAccess.isTopLevel())
      return modelsFrom(Account.class,
        dbProvider.getDSL().selectFrom(ACCOUNT)
          .fetch());
    else
      return modelsFrom(Account.class,
        dbProvider.getDSL().selectFrom(ACCOUNT)
          .where(ACCOUNT.ID.in(hubAccess.getAccountIds()))
          .fetch());
  }

  @Override
  public void update(HubAccess hubAccess, UUID id, Account entity) throws DAOException, JsonApiException, ValueException {
    requireTopLevel(hubAccess);
    entity.validate();
    executeUpdate(dbProvider.getDSL(), ACCOUNT, id, entity);
  }

  @Override
  public void destroy(HubAccess hubAccess, UUID id) throws DAOException {
    DSLContext db = dbProvider.getDSL();

    requireTopLevel(hubAccess);

    requireNotExists("Library in Account", db.select(LIBRARY.ID)
      .from(LIBRARY)
      .where(LIBRARY.ACCOUNT_ID.eq(id))
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
