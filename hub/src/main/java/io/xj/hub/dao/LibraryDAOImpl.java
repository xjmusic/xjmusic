// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.dao;

import com.google.inject.Inject;
import io.xj.hub.tables.pojos.Library;
import io.xj.hub.access.HubAccess;
import io.xj.hub.persistence.HubDatabaseProvider;
import io.xj.lib.entity.Entities;
import io.xj.lib.entity.EntityException;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.JsonapiException;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.util.Value;
import io.xj.lib.util.ValueException;
import org.jooq.DSLContext;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

import static io.xj.hub.Tables.LIBRARY;
import static io.xj.hub.Tables.PROGRAM;
import static io.xj.hub.tables.Account.ACCOUNT;
import static io.xj.hub.tables.Instrument.INSTRUMENT;

public class LibraryDAOImpl extends DAOImpl<Library> implements LibraryDAO {

  @Inject
  public LibraryDAOImpl(
    JsonapiPayloadFactory payloadFactory,
    EntityFactory entityFactory,
    HubDatabaseProvider dbProvider
  ) {
    super(payloadFactory, entityFactory);
    this.dbProvider = dbProvider;
  }

  @Override
  public Library create(HubAccess hubAccess, Library entity) throws DAOException, JsonapiException, ValueException {
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
  public Library update(HubAccess hubAccess, UUID id, Library rawLibrary) throws DAOException, JsonapiException, ValueException {
    Library record = validate(rawLibrary);
    try {
      Entities.setId(record, id); //prevent changing id
    } catch (EntityException e) {
      throw new DAOException(e);
    }

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

    var library = record;
    executeUpdate(dbProvider.getDSL(), LIBRARY, id, library);
    return library;
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

  /**
   Validate a library record

   @param record to validate
   @throws DAOException if invalid
   */
  public Library validate(Library record) throws DAOException {
    try {
      Value.require(record.getAccountId(), "Account ID");
      Value.require(record.getName(), "Name");
      return record;

    } catch (ValueException e) {
      throw new DAOException(e);
    }
  }

}
