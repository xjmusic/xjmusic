// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.manager;

import java.util.Set;
import io.xj.hub.InstrumentConfig;
import io.xj.hub.access.HubAccess;
import io.xj.hub.enums.InstrumentState;
import io.xj.hub.persistence.HubPersistenceServiceImpl;
import io.xj.hub.persistence.HubSqlStoreProvider;
import io.xj.hub.tables.pojos.Instrument;
import io.xj.hub.tables.pojos.InstrumentAudio;
import io.xj.hub.tables.pojos.InstrumentMeme;
import io.xj.lib.entity.Entities;
import io.xj.lib.entity.EntityException;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.JsonapiException;
import io.xj.lib.util.ValueException;
import io.xj.lib.util.ValueUtils;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;

import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static io.xj.hub.Tables.INSTRUMENT;
import static io.xj.hub.Tables.INSTRUMENT_AUDIO;
import static io.xj.hub.Tables.INSTRUMENT_MEME;
import static io.xj.hub.Tables.LIBRARY;

@Service
public class InstrumentManagerImpl extends HubPersistenceServiceImpl implements InstrumentManager {

  public InstrumentManagerImpl(
    EntityFactory entityFactory,
    HubSqlStoreProvider sqlStoreProvider
  ) {
    super(entityFactory, sqlStoreProvider);
  }

  @Override
  public Instrument create(HubAccess access, Instrument rawInstrument) throws ManagerException, JsonapiException, ValueException {
    DSLContext db = sqlStoreProvider.getDSL();
    Instrument instrument = validate(rawInstrument);
    requireArtist(access);
    requireParentExists(db, access, instrument); // This entity's parent is a Library
    return modelFrom(Instrument.class, executeCreate(db, INSTRUMENT, instrument));
  }

  @Override
  public ManagerCloner<Instrument> clone(HubAccess access, UUID cloneId, Instrument to) throws ManagerException {
    requireArtist(access);
    AtomicReference<ManagerCloner<Instrument>> result = new AtomicReference<>();
    sqlStoreProvider.getDSL().transaction(ctx -> result.set(clone(DSL.using(ctx), access, cloneId, to)));
    return result.get();
  }

  @Override
  public ManagerCloner<Instrument> clone(DSLContext db, HubAccess access, UUID cloneId, Instrument to) throws ManagerException {
    try {
      requireArtist(access);

      Instrument from = readOne(db, access, cloneId);
      if (Objects.isNull(from))
        throw new ManagerException("Can't clone nonexistent Instrument");

      // When not set, clone inherits attribute values from original record
      entityFactory.setAllEmptyAttributes(from, to);
      Instrument instrument = validate(to);
      requireParentExists(db, access, instrument);

      var result = modelFrom(Instrument.class, executeCreate(db, INSTRUMENT, instrument));
      ManagerCloner<Instrument> cloner = new ManagerCloner<>(result, this);
      cloner.clone(db, INSTRUMENT_MEME, INSTRUMENT_MEME.ID, Set.of(), INSTRUMENT_MEME.INSTRUMENT_ID, cloneId, result.getId());
      cloner.clone(db, INSTRUMENT_AUDIO, INSTRUMENT_AUDIO.ID, Set.of(), INSTRUMENT_AUDIO.INSTRUMENT_ID, cloneId, result.getId());
      return cloner;

    } catch (EntityException e) {
      throw new ManagerException(e);
    }
  }

  @Override
  @Nullable
  public Instrument readOne(HubAccess access, UUID id) throws ManagerException {
    return readOne(sqlStoreProvider.getDSL(), access, id);
  }

  @Override
  public void destroy(HubAccess access, UUID id) throws ManagerException {
    DSLContext db = sqlStoreProvider.getDSL();

    if (!access.isTopLevel()) try (
      var selectCount = db.selectCount();
      var joinInstrument = selectCount.from(INSTRUMENT).join(LIBRARY).on(INSTRUMENT.LIBRARY_ID.eq(LIBRARY.ID))
    ) {
      requireExists("Instrument belonging to you", joinInstrument
        .where(INSTRUMENT.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne(0, int.class));
    }

    try (var updateInstrument = db.update(INSTRUMENT).set(INSTRUMENT.IS_DELETED, true)) {
      updateInstrument.where(INSTRUMENT.ID.eq(id)).execute();
    }
  }

  @Override
  public Instrument newInstance() {
    return new Instrument();
  }

  @Override
  public Collection<Instrument> readManyInAccount(HubAccess access, UUID accountId) throws ManagerException {
    try (
      var selectInstrument = sqlStoreProvider.getDSL().select(INSTRUMENT.fields());
      var joinLibrary = selectInstrument.from(INSTRUMENT).join(LIBRARY).on(INSTRUMENT.LIBRARY_ID.eq(LIBRARY.ID))
    ) {
      if (access.isTopLevel())
        return modelsFrom(Instrument.class, joinLibrary
          .where(LIBRARY.ACCOUNT_ID.eq(accountId))
          .and(INSTRUMENT.IS_DELETED.eq(false))
          .orderBy(INSTRUMENT.TYPE, INSTRUMENT.NAME)
          .fetch());
      else
        return modelsFrom(Instrument.class, joinLibrary
          .where(LIBRARY.ACCOUNT_ID.in(Collections.singleton(accountId)))
          .and(INSTRUMENT.IS_DELETED.eq(false))
          .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
          .orderBy(INSTRUMENT.TYPE, INSTRUMENT.NAME)
          .fetch());
    }
  }

  @Override
  public Collection<Instrument> readMany(HubAccess access, Collection<UUID> parentIds) throws ManagerException {
    if (access.isTopLevel())
      try (
        var selectInstrument = sqlStoreProvider.getDSL().select(INSTRUMENT.fields())
      ) {
        return modelsFrom(Instrument.class,
          selectInstrument
            .from(INSTRUMENT)
            .where(INSTRUMENT.LIBRARY_ID.in(parentIds))
            .and(INSTRUMENT.IS_DELETED.eq(false))
            .orderBy(INSTRUMENT.TYPE, INSTRUMENT.NAME)
            .fetch());
      }
    else
      try (
        var selectInstrument = sqlStoreProvider.getDSL().select(INSTRUMENT.fields());
        var joinLibrary = selectInstrument.from(INSTRUMENT).join(LIBRARY).on(LIBRARY.ID.eq(INSTRUMENT.LIBRARY_ID))
      ) {
        return modelsFrom(Instrument.class,
          joinLibrary
            .where(INSTRUMENT.LIBRARY_ID.in(parentIds))
            .and(INSTRUMENT.IS_DELETED.eq(false))
            .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
            .orderBy(INSTRUMENT.TYPE, INSTRUMENT.NAME)
            .fetch());
      }
  }

  @Override
  public <N> Collection<N> readManyWithChildEntities(HubAccess access, Collection<UUID> instrumentIds) throws ManagerException {
    DSLContext db = sqlStoreProvider.getDSL();

    requireRead(db, access, instrumentIds);

    Collection<Object> entities = new ArrayList<>();
    try (var selectInstrument = db.selectFrom(INSTRUMENT)) {
      entities.addAll(modelsFrom(Instrument.class, selectInstrument.where(INSTRUMENT.ID.in(instrumentIds)).fetch()));
    }
    try (var selectInstrumentMeme = db.selectFrom(INSTRUMENT_MEME)) {
      entities.addAll(modelsFrom(InstrumentMeme.class, selectInstrumentMeme.where(INSTRUMENT_MEME.INSTRUMENT_ID.in(instrumentIds))));
    }
    try (var selectInstrumentAudio = db.selectFrom(INSTRUMENT_AUDIO)) {
      entities.addAll(modelsFrom(InstrumentAudio.class, selectInstrumentAudio.where(INSTRUMENT_AUDIO.INSTRUMENT_ID.in(instrumentIds))));
    }
    //noinspection unchecked
    return (Collection<N>) entities;
  }

  @Override
  public Collection<Object> readChildEntities(HubAccess access, Collection<UUID> instrumentIds, Collection<String> types) throws ManagerException {
    DSLContext db = sqlStoreProvider.getDSL();

    requireRead(db, access, instrumentIds);

    Collection<Object> entities = new ArrayList<>();

    // InstrumentMeme
    if (types.contains(Entities.toResourceType(InstrumentMeme.class)))
      try (var selectInstrumentMeme = db.selectFrom(INSTRUMENT_MEME)) {
        entities.addAll(modelsFrom(InstrumentMeme.class, selectInstrumentMeme.where(INSTRUMENT_MEME.INSTRUMENT_ID.in(instrumentIds))));
      }

    // InstrumentAudio
    if (types.contains(Entities.toResourceType(InstrumentAudio.class)))
      try (var selectInstrumentAudio = db.selectFrom(INSTRUMENT_AUDIO)) {
        entities.addAll(modelsFrom(InstrumentAudio.class, selectInstrumentAudio.where(INSTRUMENT_AUDIO.INSTRUMENT_ID.in(instrumentIds))));
      }

    return entities;
  }

  @Override
  public Collection<Instrument> readMany(HubAccess access) throws ManagerException {
    try (
      var selectInstrument = sqlStoreProvider.getDSL().select(INSTRUMENT.fields());
      var joinLibrary = selectInstrument.from(INSTRUMENT).join(LIBRARY).on(LIBRARY.ID.eq(INSTRUMENT.LIBRARY_ID))
    ) {
      if (access.isTopLevel())
        return modelsFrom(Instrument.class,
          selectInstrument
            .from(INSTRUMENT)
            .where(INSTRUMENT.IS_DELETED.eq(false))
            .orderBy(INSTRUMENT.TYPE, INSTRUMENT.NAME)
            .fetch());
      else
        return modelsFrom(Instrument.class,
          joinLibrary
            .where(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
            .and(INSTRUMENT.IS_DELETED.eq(false))
            .orderBy(INSTRUMENT.TYPE, INSTRUMENT.NAME)
            .fetch());
    }
  }

  @Override
  public Collection<UUID> readIdsInLibraries(HubAccess access, Collection<UUID> parentIds) throws ManagerException {
    requireArtist(access);
    try (var selectInstrument = sqlStoreProvider.getDSL().select(INSTRUMENT.ID)) {
      return Manager.idsFrom(
        selectInstrument
          .from(INSTRUMENT)
          .where(INSTRUMENT.LIBRARY_ID.in(parentIds))
          .and(INSTRUMENT.IS_DELETED.eq(false))
          .and(INSTRUMENT.STATE.equal(InstrumentState.Published))
          .fetch());
    }
  }

  @Override
  public Instrument update(HubAccess access, UUID id, Instrument rawInstrument) throws ManagerException, JsonapiException, ValueException {
    DSLContext db = sqlStoreProvider.getDSL();
    rawInstrument.setId(id); // prevent changing id
    Instrument instrument = validate(rawInstrument);
    requireArtist(access);
    requireParentExists(db, access, instrument);
    executeUpdate(db, INSTRUMENT, id, instrument);
    return instrument;
  }

  /**
   * Require read access
   *
   * @param db            database context
   * @param access        control
   * @param instrumentIds to require access to
   */
  void requireRead(DSLContext db, HubAccess access, Collection<UUID> instrumentIds) throws ManagerException {
    if (!access.isTopLevel())
      for (UUID instrumentId : instrumentIds)
        try (
          var selectCount = db.selectCount();
          var joinLibrary = selectCount.from(INSTRUMENT).join(LIBRARY).on(LIBRARY.ID.eq(INSTRUMENT.LIBRARY_ID))
        ) {
          requireExists("Instrument", joinLibrary
            .where(INSTRUMENT.ID.eq(instrumentId))
            .and(INSTRUMENT.IS_DELETED.eq(false))
            .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
            .fetchOne(0, int.class));
        }
  }

  /**
   * Read one record
   *
   * @param db     DSL context
   * @param access control
   * @param id     to read
   * @return record
   * @throws ManagerException on failure
   */
  Instrument readOne(DSLContext db, HubAccess access, UUID id) throws ManagerException {
    requireRead(db, access, List.of(id));
    try (var selectInstrument = db.selectFrom(INSTRUMENT)) {
      return modelFrom(Instrument.class, selectInstrument
        .where(INSTRUMENT.ID.eq(id))
        .and(INSTRUMENT.IS_DELETED.eq(false))
        .fetchOne());
    }
  }

  /**
   * Require parent instrument exists of a given possible entity in a DSL context
   *
   * @param db     DSL context
   * @param access control
   * @param entity to validate
   * @throws ManagerException if parent does not exist
   */
  void requireParentExists(DSLContext db, HubAccess access, Instrument entity) throws ManagerException {
    try (var selectCount = db.selectCount()) {
      if (access.isTopLevel())
        requireExists("Library", selectCount.from(LIBRARY)
          .where(LIBRARY.ID.eq(entity.getLibraryId()))
          .fetchOne(0, int.class));
      else
        requireExists("Library", selectCount.from(LIBRARY)
          .where(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
          .and(LIBRARY.ID.eq(entity.getLibraryId()))
          .fetchOne(0, int.class));
    }
  }

  /**
   * Validate data
   *
   * @param record to validate
   * @throws ManagerException if invalid
   */
  public Instrument validate(Instrument record) throws ManagerException {
    try {
      ValueUtils.require(record.getLibraryId(), "Library ID");
      ValueUtils.require(record.getName(), "Name");
      ValueUtils.require(record.getType(), "Type");
      ValueUtils.require(record.getType(), "Mode");
      ValueUtils.require(record.getState(), "State");

      // overall volume parameter defaults to 1.0 https://www.pivotaltracker.com/story/show/179215413
      if (ValueUtils.isUnsetOrZero(record.getVolume()))
        record.setVolume(1.0f);

      // validate TypeSafe chain config https://www.pivotaltracker.com/story/show/175347578
      // Artist saves Instrument, Instrument, or Template config, validate & combine with defaults https://www.pivotaltracker.com/story/show/177129498
      if (Objects.isNull(record.getConfig()))
        record.setConfig(new InstrumentConfig().toString());
      else
        record.setConfig(new InstrumentConfig(record).toString());

      return record;

    } catch (ValueException e) {
      throw new ManagerException(e);
    }
  }

}
