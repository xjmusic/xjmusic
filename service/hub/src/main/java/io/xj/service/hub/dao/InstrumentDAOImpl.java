// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.dao;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.protobuf.MessageLite;
import datadog.trace.api.Trace;
import com.typesafe.config.Config;
import io.xj.Instrument;
import io.xj.InstrumentAudio;
import io.xj.InstrumentAudioChord;
import io.xj.InstrumentAudioEvent;
import io.xj.InstrumentMeme;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.JsonApiException;
import io.xj.lib.jsonapi.PayloadFactory;
import io.xj.lib.util.Value;
import io.xj.lib.util.ValueException;
import io.xj.service.hub.access.HubAccess;
import io.xj.service.hub.persistence.HubDatabaseProvider;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static io.xj.service.hub.Tables.INSTRUMENT;
import static io.xj.service.hub.Tables.INSTRUMENT_AUDIO;
import static io.xj.service.hub.Tables.INSTRUMENT_AUDIO_CHORD;
import static io.xj.service.hub.Tables.INSTRUMENT_AUDIO_EVENT;
import static io.xj.service.hub.Tables.INSTRUMENT_MEME;
import static io.xj.service.hub.Tables.LIBRARY;

public class InstrumentDAOImpl extends DAOImpl<Instrument> implements InstrumentDAO {

  private final Config config;

  @Inject
  public InstrumentDAOImpl(
    Config config,
    PayloadFactory payloadFactory,
    EntityFactory entityFactory,
    HubDatabaseProvider dbProvider
  ) {
    super(payloadFactory, entityFactory);
    this.config = config;
    this.dbProvider = dbProvider;
  }

  @Override
  public Instrument create(HubAccess hubAccess, Instrument rawInstrument) throws DAOException, JsonApiException, ValueException {
    DSLContext db = dbProvider.getDSL();
    Instrument instrument = validate(rawInstrument.toBuilder()).build();
    requireArtist(hubAccess);
    requireParentExists(db, hubAccess, instrument); // This entity's parent is a Library
    validateConfig(instrument);
    return modelFrom(Instrument.class, executeCreate(db, INSTRUMENT, instrument));
  }

  @Override
  public Instrument clone(HubAccess hubAccess, String rawCloneId, Instrument rawInstrument) throws DAOException {
    requireArtist(hubAccess);
    AtomicReference<Instrument> result = new AtomicReference<>();
    dbProvider.getDSL().transaction(ctx -> {
      DSLContext db = DSL.using(ctx);

      Instrument from = readOne(db, hubAccess, rawCloneId);
      if (Objects.isNull(from))
        throw new DAOException("Can't clone nonexistent Instrument");

      // Inherits state, type if none specified
      Instrument.Builder instrumentBuilder = rawInstrument.toBuilder();
      if (Value.isEmpty(instrumentBuilder.getType())) instrumentBuilder.setType(from.getType());
      if (Value.isEmpty(instrumentBuilder.getState())) instrumentBuilder.setState(from.getState());
      if (Value.isEmpty(instrumentBuilder.getDensity())) instrumentBuilder.setDensity(from.getDensity());
      if (Value.isEmpty(instrumentBuilder.getName())) instrumentBuilder.setName(from.getName());
      Instrument instrument = validate(instrumentBuilder).build();
      requireParentExists(db, hubAccess, instrument);
      validateConfig(instrument);

      result.set(modelFrom(Instrument.class, executeCreate(db, INSTRUMENT, instrument)));
      UUID cloneId = UUID.fromString(rawCloneId);
      UUID sourceId = UUID.fromString(result.get().getId());
      DAOCloner<Object> cloner = new DAOCloner<>(result.get(), this);
      cloner.clone(db, INSTRUMENT_MEME, INSTRUMENT_MEME.ID, ImmutableSet.of(), INSTRUMENT_MEME.INSTRUMENT_ID, cloneId, sourceId);
      cloner.clone(db, INSTRUMENT_AUDIO, INSTRUMENT_AUDIO.ID, ImmutableSet.of(), INSTRUMENT_AUDIO.INSTRUMENT_ID, cloneId, sourceId);
      cloner.clone(db, INSTRUMENT_AUDIO_EVENT, INSTRUMENT_AUDIO_EVENT.ID, ImmutableSet.of(INSTRUMENT_AUDIO_EVENT.INSTRUMENT_AUDIO_ID), INSTRUMENT_AUDIO_EVENT.INSTRUMENT_ID, cloneId, sourceId);
      cloner.clone(db, INSTRUMENT_AUDIO_CHORD, INSTRUMENT_AUDIO_CHORD.ID, ImmutableSet.of(INSTRUMENT_AUDIO_CHORD.INSTRUMENT_AUDIO_ID), INSTRUMENT_AUDIO_CHORD.INSTRUMENT_ID, cloneId, sourceId);
    });
    return result.get();
  }

  /**
   [#175347578] validate TypeSafe instrument config

   @param instrument config to validate
   */
  private void validateConfig(Instrument instrument) throws ValueException {
    new InstrumentConfig(instrument, config);
  }

  @Override
  @Nullable
  public Instrument readOne(HubAccess hubAccess, String id) throws DAOException {
    return readOne(dbProvider.getDSL(), hubAccess, id);
  }

  @Override
  public void destroy(HubAccess hubAccess, String rawId) throws DAOException {
    DSLContext db = dbProvider.getDSL();
    UUID id = UUID.fromString(rawId);

    if (!hubAccess.isTopLevel())
      requireExists("Instrument belonging to you", db.selectCount().from(INSTRUMENT)
        .join(LIBRARY).on(INSTRUMENT.LIBRARY_ID.eq(LIBRARY.ID))
        .where(INSTRUMENT.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(hubAccess.getAccountIds()))
        .fetchOne(0, int.class));

    //
    // [#170299297] Cannot delete Instruments that have a Meme-- otherwise, destroy all inner entities
    //

    requireNotExists("Instrument Memes", db.selectCount().from(INSTRUMENT_MEME)
      .where(INSTRUMENT_MEME.INSTRUMENT_ID.eq(id))
      .fetchOne(0, int.class));

    db.deleteFrom(INSTRUMENT_AUDIO_CHORD)
      .where(INSTRUMENT_AUDIO_CHORD.INSTRUMENT_ID.eq(id))
      .execute();

    db.deleteFrom(INSTRUMENT_AUDIO_EVENT)
      .where(INSTRUMENT_AUDIO_EVENT.INSTRUMENT_ID.eq(id))
      .execute();

    db.deleteFrom(INSTRUMENT_AUDIO)
      .where(INSTRUMENT_AUDIO.INSTRUMENT_ID.eq(id))
      .execute();

    db.deleteFrom(INSTRUMENT)
      .where(INSTRUMENT.ID.eq(id))
      .execute();
  }

  @Override
  public Instrument newInstance() {
    return Instrument.getDefaultInstance();
  }

  @Override
  public Collection<Instrument> readManyInAccount(HubAccess hubAccess, String accountId) throws DAOException {
    if (hubAccess.isTopLevel())
      return modelsFrom(Instrument.class, dbProvider.getDSL().select(INSTRUMENT.fields())
        .from(INSTRUMENT)
        .join(LIBRARY).on(INSTRUMENT.LIBRARY_ID.eq(LIBRARY.ID))
        .where(LIBRARY.ACCOUNT_ID.eq(UUID.fromString(accountId)))
        .orderBy(INSTRUMENT.TYPE, INSTRUMENT.NAME)
        .fetch());
    else
      return modelsFrom(Instrument.class, dbProvider.getDSL().select(INSTRUMENT.fields())
        .from(INSTRUMENT)
        .join(LIBRARY).on(INSTRUMENT.LIBRARY_ID.eq(LIBRARY.ID))
        .where(LIBRARY.ACCOUNT_ID.in(Collections.singleton(accountId)))
        .and(LIBRARY.ACCOUNT_ID.in(hubAccess.getAccountIds()))
        .orderBy(INSTRUMENT.TYPE, INSTRUMENT.NAME)
        .fetch());
  }

  @Override
  public Collection<Instrument> readMany(HubAccess hubAccess, Collection<String> parentIds) throws DAOException {
    if (hubAccess.isTopLevel())
      return modelsFrom(Instrument.class, dbProvider.getDSL().select(INSTRUMENT.fields())
        .from(INSTRUMENT)
        .where(INSTRUMENT.LIBRARY_ID.in(parentIds))
        .orderBy(INSTRUMENT.TYPE, INSTRUMENT.NAME)
        .fetch());
    else
      return modelsFrom(Instrument.class, dbProvider.getDSL().select(INSTRUMENT.fields())
        .from(INSTRUMENT)
        .join(LIBRARY).on(LIBRARY.ID.eq(INSTRUMENT.LIBRARY_ID))
        .where(INSTRUMENT.LIBRARY_ID.in(parentIds))
        .and(LIBRARY.ACCOUNT_ID.in(hubAccess.getAccountIds()))
        .orderBy(INSTRUMENT.TYPE, INSTRUMENT.NAME)
        .fetch());
  }

  @Override
  public <N extends MessageLite> Collection<N> readManyWithChildEntities(HubAccess hubAccess, Collection<String> instrumentIds) throws DAOException {
    DSLContext db = dbProvider.getDSL();

    if (!hubAccess.isTopLevel())
      for (String instrumentId : instrumentIds)
        requireExists("hubAccess via account", db.selectCount().from(INSTRUMENT)
          .join(LIBRARY).on(LIBRARY.ID.eq(INSTRUMENT.LIBRARY_ID))
          .where(INSTRUMENT.ID.eq(UUID.fromString(instrumentId)))
          .and(LIBRARY.ACCOUNT_ID.in(hubAccess.getAccountIds()))
          .fetchOne(0, int.class));

    Collection<MessageLite> entities = Lists.newArrayList();
    entities.addAll(modelsFrom(Instrument.class,
      db.selectFrom(INSTRUMENT)
        .where(INSTRUMENT.ID.in(instrumentIds)).fetch()));
    entities.addAll(modelsFrom(InstrumentMeme.class,
      db.selectFrom(INSTRUMENT_MEME)
        .where(INSTRUMENT_MEME.INSTRUMENT_ID.in(instrumentIds))));
    entities.addAll(modelsFrom(InstrumentAudioChord.class,
      db.selectFrom(INSTRUMENT_AUDIO_CHORD)
        .where(INSTRUMENT_AUDIO_CHORD.INSTRUMENT_ID.in(instrumentIds))));
    entities.addAll(modelsFrom(InstrumentAudioEvent.class,
      db.selectFrom(INSTRUMENT_AUDIO_EVENT)
        .where(INSTRUMENT_AUDIO_EVENT.INSTRUMENT_ID.in(instrumentIds))));
    entities.addAll(modelsFrom(InstrumentAudio.class,
      db.selectFrom(INSTRUMENT_AUDIO)
        .where(INSTRUMENT_AUDIO.INSTRUMENT_ID.in(instrumentIds))));
    //noinspection unchecked
    return (Collection<N>) entities;
  }

  @Override
  public Collection<Instrument> readMany(HubAccess hubAccess) throws DAOException {
    if (hubAccess.isTopLevel())
      return modelsFrom(Instrument.class, dbProvider.getDSL().select(INSTRUMENT.fields())
        .from(INSTRUMENT)
        .orderBy(INSTRUMENT.TYPE, INSTRUMENT.NAME)
        .fetch());
    else
      return modelsFrom(Instrument.class, dbProvider.getDSL().select(INSTRUMENT.fields())
        .from(INSTRUMENT)
        .join(LIBRARY).on(LIBRARY.ID.eq(INSTRUMENT.LIBRARY_ID))
        .where(LIBRARY.ACCOUNT_ID.in(hubAccess.getAccountIds()))
        .orderBy(INSTRUMENT.TYPE, INSTRUMENT.NAME)
        .fetch());
  }

  @Override
  public Collection<String> readIdsInLibraries(HubAccess hubAccess, Collection<String> parentIds) throws DAOException {
    requireArtist(hubAccess);
    return DAO.idsFrom(dbProvider.getDSL().select(INSTRUMENT.ID)
      .from(INSTRUMENT)
      .where(INSTRUMENT.LIBRARY_ID.in(parentIds))
      .and(INSTRUMENT.STATE.equal(Instrument.State.Published.toString()))
      .fetch());
  }

  @Override
  public void update(HubAccess hubAccess, String id, Instrument rawInstrument) throws DAOException, JsonApiException, ValueException {
    DSLContext db = dbProvider.getDSL();
    Instrument.Builder instrumentBuilder = rawInstrument.toBuilder();
    instrumentBuilder.setId(id); // prevent changing id
    Instrument instrument = validate(instrumentBuilder).build();
    requireArtist(hubAccess);
    requireParentExists(db, hubAccess, instrument);
    validateConfig(instrument);
    executeUpdate(db, INSTRUMENT, id, instrument);
  }

  /**
   Read one record

   @param db        DSL context
   @param hubAccess control
   @param id        to read
   @return record
   @throws DAOException on failure
   */
  private Instrument readOne(DSLContext db, HubAccess hubAccess, String id) throws DAOException {
    if (hubAccess.isTopLevel())
      return modelFrom(Instrument.class, db.selectFrom(INSTRUMENT)
        .where(INSTRUMENT.ID.eq(UUID.fromString(id)))
        .fetchOne());
    else
      return modelFrom(Instrument.class, db.select(INSTRUMENT.fields())
        .from(INSTRUMENT)
        .join(LIBRARY).on(LIBRARY.ID.eq(INSTRUMENT.LIBRARY_ID))
        .where(INSTRUMENT.ID.eq(UUID.fromString(id)))
        .and(LIBRARY.ACCOUNT_ID.in(hubAccess.getAccountIds()))
        .fetchOne());
  }

  /**
   Require parent instrument exists of a given possible entity in a DSL context

   @param db        DSL context
   @param hubAccess control
   @param entity    to validate
   @throws DAOException if parent does not exist
   */
  private void requireParentExists(DSLContext db, HubAccess hubAccess, Instrument entity) throws DAOException {
    if (hubAccess.isTopLevel())
      requireExists("Library", db.selectCount().from(LIBRARY)
        .where(LIBRARY.ID.eq(UUID.fromString(entity.getLibraryId())))
        .fetchOne(0, int.class));
    else
      requireExists("Library", db.selectCount().from(LIBRARY)
        .where(LIBRARY.ACCOUNT_ID.in(hubAccess.getAccountIds()))
        .and(LIBRARY.ID.eq(UUID.fromString(entity.getLibraryId())))
        .fetchOne(0, int.class));
  }

  /**
   Validate data

   @param record to validate
   @throws DAOException if invalid
   */
  public Instrument.Builder validate(Instrument.Builder record) throws DAOException {
    try {
      Value.require(record.getLibraryId(), "Library ID");
      Value.require(record.getName(), "Name");
      Value.require(record.getType(), "Type");
      Value.require(record.getState(), "State");
      if (Objects.isNull(record.getConfig())) record.setConfig("");
      return record;

    } catch (ValueException e) {
      throw new DAOException(e);
    }
  }

}
