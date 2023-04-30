// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.manager;

import io.xj.hub.access.HubAccess;
import io.xj.hub.enums.UserRoleType;
import io.xj.hub.persistence.HubSqlStoreProvider;
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
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static io.xj.hub.Tables.LIBRARY;
import static io.xj.hub.tables.Account.ACCOUNT;

@Service
public class LibraryManagerImpl extends HubPersistenceServiceImpl implements LibraryManager {
  private final InstrumentManager instrumentManager;
  private final ProgramManager programManager;

  public LibraryManagerImpl(
    EntityFactory entityFactory,
    HubSqlStoreProvider sqlStoreProvider,
    InstrumentManager instrumentManager,
    ProgramManager programManager
  ) {
    super(entityFactory, sqlStoreProvider);
    this.instrumentManager = instrumentManager;
    this.programManager = programManager;
  }

  @Override
  public Library create(HubAccess access, Library entity) throws ManagerException, JsonapiException, ValueException {
    Library record = validate(entity);
    var db = sqlStoreProvider.getDSL();

    requireParentExists(db, access, entity);

    return modelFrom(Library.class, executeCreate(db, LIBRARY, record));
  }

  @Override
  @Nullable
  public Library readOne(HubAccess access, UUID id) throws ManagerException {
    return readOne(sqlStoreProvider.getDSL(), access, id);
  }

  @Override
  public Collection<Library> readMany(HubAccess access, Collection<UUID> parentIds) throws ManagerException {
    try (var selectLibrary = sqlStoreProvider.getDSL().select(LIBRARY.fields())) {
      if (Objects.nonNull(parentIds) && !parentIds.isEmpty()) {
        if (access.isTopLevel())
          return modelsFrom(Library.class, selectLibrary
            .from(LIBRARY)
            .where(LIBRARY.ACCOUNT_ID.in(parentIds))
            .and(LIBRARY.IS_DELETED.eq(false))
            .fetch());
        else
          return modelsFrom(Library.class, selectLibrary
            .from(LIBRARY)
            .where(LIBRARY.ACCOUNT_ID.in(parentIds))
            .and(LIBRARY.IS_DELETED.eq(false))
            .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
            .fetch());
      } else {
        if (access.isTopLevel())
          return modelsFrom(Library.class, selectLibrary
            .from(LIBRARY)
            .where(LIBRARY.IS_DELETED.eq(false))
            .fetch());
        else
          return modelsFrom(Library.class, selectLibrary
            .from(LIBRARY)
            .where(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
            .and(LIBRARY.IS_DELETED.eq(false))
            .fetch());
      }
    } catch (Exception e) {
      throw new ManagerException(e);
    }
  }

  @Override
  public Library update(HubAccess access, UUID id, Library rawLibrary) throws ManagerException, JsonapiException, ValueException {
    DSLContext db = sqlStoreProvider.getDSL();

    Library record = validate(rawLibrary);
    try {
      Entities.setId(record, id); //prevent changing id
    } catch (EntityException e) {
      throw new ManagerException(e);
    }

    if (!access.isTopLevel())
      try (
        var selectLibraryCount = db.selectCount();
        var selectAccountCount = db.selectCount()
      ) {
        requireExists("Library",
          selectLibraryCount
            .from(LIBRARY)
            .where(LIBRARY.ID.eq(id))
            .and(LIBRARY.IS_DELETED.eq(false))
            .fetchOne(0, int.class));
        requireExists("Account",
          selectAccountCount
            .from(ACCOUNT)
            .where(ACCOUNT.ID.in(access.getAccountIds()))
            .fetchOne(0, int.class));
      } catch (Exception e) {
        throw new ManagerException(e);
      }

    executeUpdate(db, LIBRARY, id, record);
    return record;
  }

  @Override
  public void destroy(HubAccess access, UUID id) throws ManagerException {
    DSLContext db = sqlStoreProvider.getDSL();

    requireAny(access, UserRoleType.Artist, UserRoleType.Engineer);

    try (var updateLibrary = db.update(LIBRARY)
      .set(LIBRARY.IS_DELETED, true)) {
      updateLibrary
        .where(LIBRARY.ID.eq(id))
        .execute();
    } catch (Exception e) {
      throw new ManagerException(e);
    }
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
    sqlStoreProvider.getDSL().transaction(ctx -> {
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
      try (var selectCount = db.selectCount()) {
        requireExists("Account",
          selectCount.from(ACCOUNT)
            .where(ACCOUNT.ID.in(access.getAccountIds()))
            .and(ACCOUNT.ID.eq(library.getAccountId()))
            .fetchOne(0, int.class));
      } catch (Exception e) {
        throw new ManagerException(e);
      }
  }

  private Library readOne(DSLContext db, HubAccess access, UUID id) throws ManagerException {
    if (access.isTopLevel())
      try (var selectLibrary = db.selectFrom(LIBRARY)) {
        return modelFrom(Library.class, selectLibrary
          .where(LIBRARY.ID.eq(id))
          .and(LIBRARY.IS_DELETED.eq(false))
          .fetchOne());
      } catch (Exception e) {
        throw new ManagerException(e);
      }
    else
      try (var selectLibrary = db.select(LIBRARY.fields())) {
        return modelFrom(Library.class, selectLibrary
          .from(LIBRARY)
          .where(LIBRARY.ID.eq(id))
          .and(LIBRARY.IS_DELETED.eq(false))
          .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
          .fetchOne());
      } catch (Exception e) {
        throw new ManagerException(e);
      }
  }
}
