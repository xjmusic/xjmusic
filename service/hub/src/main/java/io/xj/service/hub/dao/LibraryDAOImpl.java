// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.dao;

import com.google.inject.Inject;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.PayloadFactory;
import io.xj.lib.jsonapi.JsonApiException;
import io.xj.lib.util.ValueException;
import io.xj.service.hub.access.HubAccess;
import io.xj.service.hub.entity.Library;
import io.xj.service.hub.persistence.HubDatabaseProvider;
import org.jooq.DSLContext;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

import static io.xj.service.hub.Tables.LIBRARY;
import static io.xj.service.hub.Tables.PROGRAM;
import static io.xj.service.hub.tables.Account.ACCOUNT;
import static io.xj.service.hub.tables.Instrument.INSTRUMENT;

public class LibraryDAOImpl extends DAOImpl<Library> implements LibraryDAO {

  @Inject
  public LibraryDAOImpl(
    PayloadFactory payloadFactory,
    EntityFactory entityFactory,
    HubDatabaseProvider dbProvider
  ) {
    super(payloadFactory, entityFactory);
    this.dbProvider = dbProvider;
  }

  @Override
  public Library create(HubAccess hubAccess, Library entity) throws DAOException, JsonApiException, ValueException {
    entity.validate();

    if (!hubAccess.isTopLevel())
      requireExists("Account",
        dbProvider.getDSL().selectCount().from(ACCOUNT)
          .where(ACCOUNT.ID.in(hubAccess.getAccountIds()))
          .fetchOne(0, int.class));

    return modelFrom(Library.class, executeCreate(dbProvider.getDSL(), LIBRARY, entity));
  }

  @Override
  @Nullable
  public Library readOne(HubAccess hubAccess, UUID id) throws DAOException {
    if (hubAccess.isTopLevel())
      return modelFrom(Library.class, dbProvider.getDSL().selectFrom(LIBRARY)
        .where(LIBRARY.ID.eq(id))
        .fetchOne());
    else
      return modelFrom(Library.class, dbProvider.getDSL().select(LIBRARY.fields())
        .from(LIBRARY)
        .where(LIBRARY.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(hubAccess.getAccountIds()))
        .fetchOne());

  }

  @Override
  public Collection<Library> readMany(HubAccess hubAccess, Collection<UUID> parentIds) throws DAOException {
    if (Objects.nonNull(parentIds) && !parentIds.isEmpty()) {
      if (hubAccess.isTopLevel())
        return modelsFrom(Library.class, dbProvider.getDSL().select(LIBRARY.fields())
          .from(LIBRARY)
          .where(LIBRARY.ACCOUNT_ID.in(parentIds))
          .fetch());
      else
        return modelsFrom(Library.class, dbProvider.getDSL().select(LIBRARY.fields())
          .from(LIBRARY)
          .where(LIBRARY.ACCOUNT_ID.in(parentIds))
          .and(LIBRARY.ACCOUNT_ID.in(hubAccess.getAccountIds()))
          .fetch());
    } else {
      if (hubAccess.isTopLevel())
        return modelsFrom(Library.class, dbProvider.getDSL().select(LIBRARY.fields())
          .from(LIBRARY)
          .fetch());
      else
        return modelsFrom(Library.class, dbProvider.getDSL().select(LIBRARY.fields())
          .from(LIBRARY)
          .where(LIBRARY.ACCOUNT_ID.in(hubAccess.getAccountIds()))
          .fetch());
    }

  }

  @Override
  public void update(HubAccess hubAccess, UUID id, Library entity) throws DAOException, JsonApiException, ValueException {
    entity.validate();
    entity.setId(id); //prevent changing id

    if (!hubAccess.isTopLevel()) {
      requireExists("Library",
        dbProvider.getDSL().selectCount().from(LIBRARY)
          .where(LIBRARY.ID.eq(id))
          .fetchOne(0, int.class));
      requireExists("Account",
        dbProvider.getDSL().selectCount().from(ACCOUNT)
          .where(ACCOUNT.ID.in(hubAccess.getAccountIds()))
          .fetchOne(0, int.class));
    }

    executeUpdate(dbProvider.getDSL(), LIBRARY, id, entity);
  }

  @Override
  public void destroy(HubAccess hubAccess, UUID id) throws DAOException {
    DSLContext db = dbProvider.getDSL();
    requireTopLevel(hubAccess);

    requireNotExists("Program in Library", db.select(PROGRAM.ID)
      .from(PROGRAM)
      .where(PROGRAM.LIBRARY_ID.eq(id))
      .fetch().into(PROGRAM));

    requireNotExists("Instrument in Library", db.select(INSTRUMENT.ID)
      .from(INSTRUMENT)
      .where(INSTRUMENT.LIBRARY_ID.eq(id))
      .fetch().into(INSTRUMENT));

    db.deleteFrom(LIBRARY)
      .where(LIBRARY.ID.eq(id))
      .andNotExists(
        db.select(PROGRAM.ID)
          .from(PROGRAM)
          .where(PROGRAM.LIBRARY_ID.eq(id))
      )
      .andNotExists(
        db.select(INSTRUMENT.ID)
          .from(INSTRUMENT)
          .where(INSTRUMENT.LIBRARY_ID.eq(id))
      )
      .execute();
  }

  @Override
  public Library newInstance() {
    return new Library();
  }
}
