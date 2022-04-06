// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.manager;

import com.google.inject.Inject;
import io.xj.hub.access.HubAccess;
import io.xj.hub.enums.UserRoleType;
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
import org.jooq.impl.DSL;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static io.xj.hub.Tables.LIBRARY;
import static io.xj.hub.tables.Account.ACCOUNT;

public class LibraryManagerImpl extends HubPersistenceServiceImpl<Library> implements LibraryManager {
  private final InstrumentManager instrumentManager;
  private final ProgramManager programManager;

  @Inject
  public LibraryManagerImpl(
    EntityFactory entityFactory,
    HubDatabaseProvider dbProvider,
    InstrumentManager instrumentManager,
    ProgramManager programManager
  ) {
    super(entityFactory, dbProvider);
    this.instrumentManager = instrumentManager;
    this.programManager = programManager;
  }

  @Override
  public Library create(HubAccess access, Library entity) throws ManagerException, JsonapiException, ValueException {
    Library record = validate(entity);
    var db = dbProvider.getDSL();

    requireParentExists(db, access, entity);

    return modelFrom(Library.class, executeCreate(db, LIBRARY, record));
  }

  @Override
  @Nullable
  public Library readOne(HubAccess access, UUID id) throws ManagerException {
    return readOne(dbProvider.getDSL(), access, id);
  }

  @Override
  public Collection<Library> readMany(HubAccess access, Collection<UUID> parentIds) throws ManagerException {
    if (Objects.nonNull(parentIds) && !parentIds.isEmpty()) {
      if (access.isTopLevel())
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
          .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
          .fetch());
    } else {
      if (access.isTopLevel())
        return modelsFrom(Library.class, dbProvider.getDSL().select(LIBRARY.fields())
          .from(LIBRARY)
          .where(LIBRARY.IS_DELETED.eq(false))
          .fetch());
      else
        return modelsFrom(Library.class, dbProvider.getDSL().select(LIBRARY.fields())
          .from(LIBRARY)
          .where(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
          .and(LIBRARY.IS_DELETED.eq(false))
          .fetch());
    }

  }

  @Override
  public Library update(HubAccess access, UUID id, Library rawLibrary) throws ManagerException, JsonapiException, ValueException {
    Library record = validate(rawLibrary);
    try {
      Entities.setId(record, id); //prevent changing id
    } catch (EntityException e) {
      throw new ManagerException(e);
    }

    if (!access.isTopLevel()) {
      requireExists("Library",
        dbProvider.getDSL().selectCount().from(LIBRARY)
          .where(LIBRARY.ID.eq(id))
          .and(LIBRARY.IS_DELETED.eq(false))
          .fetchOne(0, int.class));
      requireExists("Account",
        dbProvider.getDSL().selectCount().from(ACCOUNT)
          .where(ACCOUNT.ID.in(access.getAccountIds()))
          .fetchOne(0, int.class));
    }

    executeUpdate(dbProvider.getDSL(), LIBRARY, id, record);
    return record;
  }

  @Override
  public void destroy(HubAccess access, UUID id) throws ManagerException {
    DSLContext db = dbProvider.getDSL();

    requireAny(access, UserRoleType.Artist, UserRoleType.Engineer);

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

  @Override
  public ManagerCloner<Library> clone(HubAccess access, UUID cloneId, Library to) throws ManagerException {
    var programs = programManager.readMany(access, List.of(cloneId));
    var instruments = instrumentManager.readMany(access, List.of(cloneId));

    requireArtist(access);
    AtomicReference<Library> result = new AtomicReference<>();
    AtomicReference<ManagerCloner<Library>> cloner = new AtomicReference<>();
    dbProvider.getDSL().transaction(ctx -> {
      DSLContext db = DSL.using(ctx);

      Library from = readOne(db, access, cloneId);
      if (Objects.isNull(from))
        throw new ManagerException("Can't clone nonexistent Library");

      // When not set, clone inherits attribute values from original record
      entityFactory.setAllEmptyAttributes(from, to);
      Library library = validate(to);
      requireParentExists(db, access, library);

      // Create main entity
      result.set(modelFrom(Library.class, executeCreate(db, LIBRARY, library)));
      UUID newLibraryId = result.get().getId();

      // Prepare to clone sub-entities
      cloner.set(new ManagerCloner<>(result.get(), this));

      for (var program : programs) {
        program.setLibraryId(newLibraryId);
        var programCloner = programManager.clone(db, access, program.getId(), program);
        cloner.get().addChildClones(List.of(programCloner.getClone()));
        cloner.get().addChildClones(programCloner.getChildClones());
      }

      for (var instrument : instruments) {
        instrument.setLibraryId(newLibraryId);
        var instrumentCloner = instrumentManager.clone(db, access, instrument.getId(), instrument);
        cloner.get().addChildClones(List.of(instrumentCloner.getClone()));
        cloner.get().addChildClones(instrumentCloner.getChildClones());
      }
    });
    return cloner.get();
  }

  private void requireParentExists(DSLContext db, HubAccess access, Library library) throws ManagerException {
    if (!access.isTopLevel())
      requireExists("Account",
        db.selectCount().from(ACCOUNT)
          .where(ACCOUNT.ID.in(access.getAccountIds()))
          .and(ACCOUNT.ID.eq(library.getAccountId()))
          .fetchOne(0, int.class));
  }

  private Library readOne(DSLContext db, HubAccess access, UUID id) throws ManagerException {
    if (access.isTopLevel())
      return modelFrom(Library.class, db.selectFrom(LIBRARY)
        .where(LIBRARY.ID.eq(id))
        .and(LIBRARY.IS_DELETED.eq(false))
        .fetchOne());
    else
      return modelFrom(Library.class, db.select(LIBRARY.fields())
        .from(LIBRARY)
        .where(LIBRARY.ID.eq(id))
        .and(LIBRARY.IS_DELETED.eq(false))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne());
  }

}
