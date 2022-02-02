// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.manager;

import com.google.inject.Inject;
import io.xj.hub.access.HubAccess;
import io.xj.hub.persistence.HubDatabaseProvider;
import io.xj.hub.persistence.HubPersistenceServiceImpl;
import io.xj.hub.tables.pojos.Library;
import io.xj.lib.entity.Entities;
import io.xj.lib.entity.EntityException;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.JsonapiException;
import io.xj.lib.util.ValueException;
import io.xj.lib.util.Values;
import org.jooq.DSLContext;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

import static io.xj.hub.Tables.LIBRARY;
import static io.xj.hub.tables.Account.ACCOUNT;

public class LibraryManagerImpl extends HubPersistenceServiceImpl<Library> implements LibraryManager {

  @Inject
  public LibraryManagerImpl(
    EntityFactory entityFactory,
    HubDatabaseProvider dbProvider
  ) {
    super(entityFactory, dbProvider);
  }

  @Override
  public Library create(HubAccess hubAccess, Library entity) throws ManagerException, JsonapiException, ValueException {
    Library record = validate(entity);

    if (!hubAccess.isTopLevel())
      requireExists("Account",
        dbProvider.getDSL().selectCount().from(ACCOUNT)
          .where(ACCOUNT.ID.in(hubAccess.getAccountIds()))
          .fetchOne(0, int.class));

    return modelFrom(Library.class, executeCreate(dbProvider.getDSL(), LIBRARY, record));
  }

  @Override
  @Nullable
  public Library readOne(HubAccess hubAccess, UUID id) throws ManagerException {
    if (hubAccess.isTopLevel())
      return modelFrom(Library.class, dbProvider.getDSL().selectFrom(LIBRARY)
        .where(LIBRARY.ID.eq(id))
        .and(LIBRARY.IS_DELETED.eq(false))
        .fetchOne());
    else
      return modelFrom(Library.class, dbProvider.getDSL().select(LIBRARY.fields())
        .from(LIBRARY)
        .where(LIBRARY.ID.eq(id))
        .and(LIBRARY.IS_DELETED.eq(false))
        .and(LIBRARY.ACCOUNT_ID.in(hubAccess.getAccountIds()))
        .fetchOne());

  }

  @Override
  public Collection<Library> readMany(HubAccess hubAccess, Collection<UUID> parentIds) throws ManagerException {
    if (Objects.nonNull(parentIds) && !parentIds.isEmpty()) {
      if (hubAccess.isTopLevel())
        return modelsFrom(Library.class, dbProvider.getDSL().select(LIBRARY.fields())
          .from(LIBRARY)
          .where(LIBRARY.ACCOUNT_ID.in(parentIds))
          .and(LIBRARY.IS_DELETED.eq(false))
          .fetch());
      else
        return modelsFrom(Library.class, dbProvider.getDSL().select(LIBRARY.fields())
          .from(LIBRARY)
          .where(LIBRARY.ACCOUNT_ID.in(parentIds))
          .and(LIBRARY.IS_DELETED.eq(false))
          .and(LIBRARY.ACCOUNT_ID.in(hubAccess.getAccountIds()))
          .fetch());
    } else {
      if (hubAccess.isTopLevel())
        return modelsFrom(Library.class, dbProvider.getDSL().select(LIBRARY.fields())
          .from(LIBRARY)
          .where(LIBRARY.IS_DELETED.eq(false))
          .fetch());
      else
        return modelsFrom(Library.class, dbProvider.getDSL().select(LIBRARY.fields())
          .from(LIBRARY)
          .where(LIBRARY.ACCOUNT_ID.in(hubAccess.getAccountIds()))
          .and(LIBRARY.IS_DELETED.eq(false))
          .fetch());
    }

  }

  @Override
  public Library update(HubAccess hubAccess, UUID id, Library rawLibrary) throws ManagerException, JsonapiException, ValueException {
    Library record = validate(rawLibrary);
    try {
      Entities.setId(record, id); //prevent changing id
    } catch (EntityException e) {
      throw new ManagerException(e);
    }

    if (!hubAccess.isTopLevel()) {
      requireExists("Library",
        dbProvider.getDSL().selectCount().from(LIBRARY)
          .where(LIBRARY.ID.eq(id))
          .and(LIBRARY.IS_DELETED.eq(false))
          .fetchOne(0, int.class));
      requireExists("Account",
        dbProvider.getDSL().selectCount().from(ACCOUNT)
          .where(ACCOUNT.ID.in(hubAccess.getAccountIds()))
          .fetchOne(0, int.class));
    }

    executeUpdate(dbProvider.getDSL(), LIBRARY, id, record);
    return record;
  }

  @Override
  public void destroy(HubAccess hubAccess, UUID id) throws ManagerException {
    DSLContext db = dbProvider.getDSL();
    requireTopLevel(hubAccess);

    db.update(LIBRARY)
      .set(LIBRARY.IS_DELETED, true)
      .where(LIBRARY.ID.eq(id))
      .execute();
  }

  @Override
  public Library newInstance() {
    return new Library();
  }

  /**
   Validate a library record

   @param record to validate
   @throws ManagerException if invalid
   */
  public Library validate(Library record) throws ManagerException {
    try {
      Values.require(record.getAccountId(), "Account ID");
      Values.require(record.getName(), "Name");
      return record;

    } catch (ValueException e) {
      throw new ManagerException(e);
    }
  }

}
