// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.manager;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import io.xj.hub.InstrumentConfig;
import io.xj.hub.access.HubAccess;
import io.xj.hub.enums.InstrumentState;
import io.xj.hub.persistence.HubDatabaseProvider;
import io.xj.hub.persistence.HubPersistenceServiceImpl;
import io.xj.hub.tables.pojos.FeedbackInstrument;
import io.xj.hub.tables.pojos.Instrument;
import io.xj.hub.tables.pojos.InstrumentAudio;
import io.xj.hub.tables.pojos.InstrumentMeme;
import io.xj.lib.entity.Entities;
import io.xj.lib.entity.EntityException;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.JsonapiException;
import io.xj.lib.util.ValueException;
import io.xj.lib.util.Values;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static io.xj.hub.Tables.*;

public class InstrumentManagerImpl extends HubPersistenceServiceImpl<Instrument> implements InstrumentManager {

  @Inject
  public InstrumentManagerImpl(
    EntityFactory entityFactory,
    HubDatabaseProvider dbProvider
  ) {
    super(entityFactory, dbProvider);
  }

  @Override
  public Instrument create(HubAccess access, Instrument rawInstrument) throws ManagerException, JsonapiException, ValueException {
    DSLContext db = dbProvider.getDSL();
    Instrument instrument = validate(rawInstrument);
    requireArtist(access);
    requireParentExists(db, access, instrument); // This entity's parent is a Library
    return modelFrom(Instrument.class, executeCreate(db, INSTRUMENT, instrument));
  }

  @Override
  public ManagerCloner<Instrument> clone(HubAccess access, UUID cloneId, Instrument to) throws ManagerException {
    requireArtist(access);
    AtomicReference<ManagerCloner<Instrument>> result = new AtomicReference<>();
    dbProvider.getDSL().transaction(ctx -> result.set(clone(DSL.using(ctx), access, cloneId, to)));
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
      cloner.clone(db, INSTRUMENT_MEME, INSTRUMENT_MEME.ID, ImmutableSet.of(), INSTRUMENT_MEME.INSTRUMENT_ID, cloneId, result.getId());
      cloner.clone(db, INSTRUMENT_AUDIO, INSTRUMENT_AUDIO.ID, ImmutableSet.of(), INSTRUMENT_AUDIO.INSTRUMENT_ID, cloneId, result.getId());
      return cloner;

    } catch (EntityException e) {
      throw new ManagerException("Failed to clone Instrument!", e);
    }
  }

  @Override
  @Nullable
  public Instrument readOne(HubAccess access, UUID id) throws ManagerException {
    return readOne(dbProvider.getDSL(), access, id);
  }

  @Override
  public void destroy(HubAccess access, UUID id) throws ManagerException {
    DSLContext db = dbProvider.getDSL();

    if (!access.isTopLevel())
      requireExists("Instrument belonging to you", db.selectCount().from(INSTRUMENT)
        .join(LIBRARY).on(INSTRUMENT.LIBRARY_ID.eq(LIBRARY.ID))
        .where(INSTRUMENT.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne(0, int.class));

    db.update(INSTRUMENT)
      .set(INSTRUMENT.IS_DELETED, true)
      .where(INSTRUMENT.ID.eq(id))
      .execute();
  }

  @Override
  public Instrument newInstance() {
    return new Instrument();
  }

  @Override
  public Collection<Instrument> readManyInAccount(HubAccess access, UUID accountId) throws ManagerException {
    if (access.isTopLevel())
      return modelsFrom(Instrument.class, dbProvider.getDSL().select(INSTRUMENT.fields())
        .from(INSTRUMENT)
        .join(LIBRARY).on(INSTRUMENT.LIBRARY_ID.eq(LIBRARY.ID))
        .where(LIBRARY.ACCOUNT_ID.eq(accountId))
        .and(INSTRUMENT.IS_DELETED.eq(false))
        .orderBy(INSTRUMENT.TYPE, INSTRUMENT.NAME)
        .fetch());
    else
      return modelsFrom(Instrument.class, dbProvider.getDSL().select(INSTRUMENT.fields())
        .from(INSTRUMENT)
        .join(LIBRARY).on(INSTRUMENT.LIBRARY_ID.eq(LIBRARY.ID))
        .where(LIBRARY.ACCOUNT_ID.in(Collections.singleton(accountId)))
        .and(INSTRUMENT.IS_DELETED.eq(false))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .orderBy(INSTRUMENT.TYPE, INSTRUMENT.NAME)
        .fetch());
  }

  @Override
  public Collection<Instrument> readMany(HubAccess access, Collection<UUID> parentIds) throws ManagerException {
    if (access.isTopLevel())
      return modelsFrom(Instrument.class, dbProvider.getDSL().select(INSTRUMENT.fields())
        .from(INSTRUMENT)
        .where(INSTRUMENT.LIBRARY_ID.in(parentIds))
        .and(INSTRUMENT.IS_DELETED.eq(false))
        .orderBy(INSTRUMENT.TYPE, INSTRUMENT.NAME)
        .fetch());
    else
      return modelsFrom(Instrument.class, dbProvider.getDSL().select(INSTRUMENT.fields())
        .from(INSTRUMENT)
        .join(LIBRARY).on(LIBRARY.ID.eq(INSTRUMENT.LIBRARY_ID))
        .where(INSTRUMENT.LIBRARY_ID.in(parentIds))
        .and(INSTRUMENT.IS_DELETED.eq(false))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .orderBy(INSTRUMENT.TYPE, INSTRUMENT.NAME)
        .fetch());
  }

  @Override
  public <N> Collection<N> readManyWithChildEntities(HubAccess access, Collection<UUID> instrumentIds) throws ManagerException {
    DSLContext db = dbProvider.getDSL();

    if (!access.isTopLevel())
      for (UUID instrumentId : instrumentIds)
        requireExists("Instrument", db.selectCount().from(INSTRUMENT)
          .join(LIBRARY).on(LIBRARY.ID.eq(INSTRUMENT.LIBRARY_ID))
          .where(INSTRUMENT.ID.eq(instrumentId))
          .and(INSTRUMENT.IS_DELETED.eq(false))
          .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
          .fetchOne(0, int.class));

    Collection<Object> entities = Lists.newArrayList();
    entities.addAll(modelsFrom(Instrument.class,
      db.selectFrom(INSTRUMENT)
        .where(INSTRUMENT.ID.in(instrumentIds)).fetch()));
    entities.addAll(modelsFrom(InstrumentMeme.class,
      db.selectFrom(INSTRUMENT_MEME)
        .where(INSTRUMENT_MEME.INSTRUMENT_ID.in(instrumentIds))));
    entities.addAll(modelsFrom(InstrumentAudio.class,
      db.selectFrom(INSTRUMENT_AUDIO)
        .where(INSTRUMENT_AUDIO.INSTRUMENT_ID.in(instrumentIds))));
    entities.addAll(modelsFrom(FeedbackInstrument.class,
      db.selectFrom(FEEDBACK_INSTRUMENT)
        .where(FEEDBACK_INSTRUMENT.INSTRUMENT_ID.in(instrumentIds))));
    //noinspection unchecked
    return (Collection<N>) entities;
  }

  @Override
  public Collection<Object> readChildEntities(HubAccess access, Collection<UUID> instrumentIds, Collection<String> types) throws ManagerException {
    DSLContext db = dbProvider.getDSL();

    requireRead(db, access, instrumentIds);

    Collection<Object> entities = Lists.newArrayList();

    // InstrumentMeme
    if (types.contains(Entities.toResourceType(InstrumentMeme.class)))
      entities.addAll(modelsFrom(InstrumentMeme.class,
        db.selectFrom(INSTRUMENT_MEME).where(INSTRUMENT_MEME.INSTRUMENT_ID.in(instrumentIds))));

    // InstrumentAudio
    if (types.contains(Entities.toResourceType(InstrumentAudio.class)))
      entities.addAll(modelsFrom(InstrumentAudio.class,
        db.selectFrom(INSTRUMENT_AUDIO).where(INSTRUMENT_AUDIO.INSTRUMENT_ID.in(instrumentIds))));

    // FeedbackInstrument
    if (types.contains(Entities.toResourceType(FeedbackInstrument.class)))
      entities.addAll(modelsFrom(FeedbackInstrument.class,
        db.selectFrom(FEEDBACK_INSTRUMENT).where(FEEDBACK_INSTRUMENT.INSTRUMENT_ID.in(instrumentIds))));

    return entities;
  }

  @Override
  public Collection<Instrument> readMany(HubAccess access) throws ManagerException {
    if (access.isTopLevel())
      return modelsFrom(Instrument.class, dbProvider.getDSL().select(INSTRUMENT.fields())
        .from(INSTRUMENT)
        .where(INSTRUMENT.IS_DELETED.eq(false))
        .orderBy(INSTRUMENT.TYPE, INSTRUMENT.NAME)
        .fetch());
    else
      return modelsFrom(Instrument.class, dbProvider.getDSL().select(INSTRUMENT.fields())
        .from(INSTRUMENT)
        .join(LIBRARY).on(LIBRARY.ID.eq(INSTRUMENT.LIBRARY_ID))
        .where(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .and(INSTRUMENT.IS_DELETED.eq(false))
        .orderBy(INSTRUMENT.TYPE, INSTRUMENT.NAME)
        .fetch());
  }

  @Override
  public Collection<UUID> readIdsInLibraries(HubAccess access, Collection<UUID> parentIds) throws ManagerException {
    requireArtist(access);
    return Manager.idsFrom(dbProvider.getDSL().select(INSTRUMENT.ID)
      .from(INSTRUMENT)
      .where(INSTRUMENT.LIBRARY_ID.in(parentIds))
      .and(INSTRUMENT.IS_DELETED.eq(false))
      .and(INSTRUMENT.STATE.equal(InstrumentState.Published))
      .fetch());
  }

  @Override
  public Instrument update(HubAccess access, UUID id, Instrument rawInstrument) throws ManagerException, JsonapiException, ValueException {
    DSLContext db = dbProvider.getDSL();
    rawInstrument.setId(id); // prevent changing id
    Instrument instrument = validate(rawInstrument);
    requireArtist(access);
    requireParentExists(db, access, instrument);
    executeUpdate(db, INSTRUMENT, id, instrument);
    return instrument;
  }

  /**
   Require read access

   @param db            database context
   @param access     control
   @param instrumentIds to require access to
   */
  private void requireRead(DSLContext db, HubAccess access, Collection<UUID> instrumentIds) throws ManagerException {
    if (!access.isTopLevel())
      for (UUID instrumentId : instrumentIds)
        requireExists("Instrument", db.selectCount().from(INSTRUMENT)
          .join(LIBRARY).on(LIBRARY.ID.eq(INSTRUMENT.LIBRARY_ID))
          .where(INSTRUMENT.ID.eq(instrumentId))
          .and(INSTRUMENT.IS_DELETED.eq(false))
          .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
          .fetchOne(0, int.class));
  }

  /**
   Read one record

   @param db        DSL context
   @param access control
   @param id        to read
   @return record
   @throws ManagerException on failure
   */
  private Instrument readOne(DSLContext db, HubAccess access, UUID id) throws ManagerException {
    requireRead(db, access, List.of(id));
    return modelFrom(Instrument.class, db.selectFrom(INSTRUMENT)
      .where(INSTRUMENT.ID.eq(id))
      .and(INSTRUMENT.IS_DELETED.eq(false))
      .fetchOne());
  }

  /**
   Require parent instrument exists of a given possible entity in a DSL context

   @param db        DSL context
   @param access control
   @param entity    to validate
   @throws ManagerException if parent does not exist
   */
  private void requireParentExists(DSLContext db, HubAccess access, Instrument entity) throws ManagerException {
    if (access.isTopLevel())
      requireExists("Library", db.selectCount().from(LIBRARY)
        .where(LIBRARY.ID.eq(entity.getLibraryId()))
        .fetchOne(0, int.class));
    else
      requireExists("Library", db.selectCount().from(LIBRARY)
        .where(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .and(LIBRARY.ID.eq(entity.getLibraryId()))
        .fetchOne(0, int.class));
  }

  /**
   Validate data

   @param record to validate
   @throws ManagerException if invalid
   */
  public Instrument validate(Instrument record) throws ManagerException {
    try {
      Values.require(record.getLibraryId(), "Library ID");
      Values.require(record.getName(), "Name");
      Values.require(record.getType(), "Type");
      Values.require(record.getType(), "Mode");
      Values.require(record.getState(), "State");

      // overall volume parameter defaults to 1.0 https://www.pivotaltracker.com/story/show/179215413
      if (Values.isUnsetOrZero(record.getVolume()))
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
