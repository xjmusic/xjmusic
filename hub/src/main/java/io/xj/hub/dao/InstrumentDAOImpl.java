// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.dao;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import io.xj.hub.InstrumentConfig;
import io.xj.hub.access.HubAccess;
import io.xj.hub.enums.InstrumentState;
import io.xj.hub.persistence.HubDatabaseProvider;
import io.xj.hub.tables.pojos.Instrument;
import io.xj.hub.tables.pojos.InstrumentAudio;
import io.xj.hub.tables.pojos.InstrumentMeme;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.JsonapiException;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.util.ValueException;
import io.xj.lib.util.Values;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static io.xj.hub.Tables.*;

public class InstrumentDAOImpl extends DAOImpl<Instrument> implements InstrumentDAO {

  @Inject
  public InstrumentDAOImpl(
    JsonapiPayloadFactory payloadFactory,
    EntityFactory entityFactory,
    HubDatabaseProvider dbProvider
  ) {
    super(payloadFactory, entityFactory);
    this.dbProvider = dbProvider;
  }

  @Override
  public Instrument create(HubAccess hubAccess, Instrument rawInstrument) throws DAOException, JsonapiException, ValueException {
    DSLContext db = dbProvider.getDSL();
    Instrument instrument = validate(rawInstrument);
    requireArtist(hubAccess);
    requireParentExists(db, hubAccess, instrument); // This entity's parent is a Library
    return modelFrom(Instrument.class, executeCreate(db, INSTRUMENT, instrument));
  }

  @Override
  public Instrument clone(HubAccess hubAccess, UUID cloneId, Instrument rawInstrument) throws DAOException {
    requireArtist(hubAccess);
    AtomicReference<Instrument> result = new AtomicReference<>();
    dbProvider.getDSL().transaction(ctx -> {
      DSLContext db = DSL.using(ctx);

      Instrument from = readOne(db, hubAccess, cloneId);
      if (Objects.isNull(from))
        throw new DAOException("Can't clone nonexistent Instrument");

      // Inherits state, type if none specified
      if (Values.isEmpty(rawInstrument.getType())) rawInstrument.setType(from.getType());
      if (Values.isEmpty(rawInstrument.getState())) rawInstrument.setState(from.getState());
      if (Values.isEmpty(rawInstrument.getDensity())) rawInstrument.setDensity(from.getDensity());
      if (Values.isEmpty(rawInstrument.getName())) rawInstrument.setName(from.getName());
      Instrument instrument = validate(rawInstrument);
      requireParentExists(db, hubAccess, instrument);

      result.set(modelFrom(Instrument.class, executeCreate(db, INSTRUMENT, instrument)));
      DAOCloner<Object> cloner = new DAOCloner<>(result.get(), this);
      cloner.clone(db, INSTRUMENT_MEME, INSTRUMENT_MEME.ID, ImmutableSet.of(), INSTRUMENT_MEME.INSTRUMENT_ID, cloneId, result.get().getId());
      cloner.clone(db, INSTRUMENT_AUDIO, INSTRUMENT_AUDIO.ID, ImmutableSet.of(), INSTRUMENT_AUDIO.INSTRUMENT_ID, cloneId, result.get().getId());
    });
    return result.get();
  }

  @Override
  @Nullable
  public Instrument readOne(HubAccess hubAccess, UUID id) throws DAOException {
    return readOne(dbProvider.getDSL(), hubAccess, id);
  }

  @Override
  public void destroy(HubAccess hubAccess, UUID id) throws DAOException {
    DSLContext db = dbProvider.getDSL();

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

    db.deleteFrom(INSTRUMENT_AUDIO)
      .where(INSTRUMENT_AUDIO.INSTRUMENT_ID.eq(id))
      .execute();

    db.deleteFrom(INSTRUMENT)
      .where(INSTRUMENT.ID.eq(id))
      .execute();
  }

  @Override
  public Instrument newInstance() {
    return new Instrument();
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
  public Collection<Instrument> readMany(HubAccess hubAccess, Collection<UUID> parentIds) throws DAOException {
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
  public <N> Collection<N> readManyWithChildEntities(HubAccess hubAccess, Collection<UUID> instrumentIds) throws DAOException {
    DSLContext db = dbProvider.getDSL();

    if (!hubAccess.isTopLevel())
      for (UUID instrumentId : instrumentIds)
        requireExists("hubAccess via account", db.selectCount().from(INSTRUMENT)
          .join(LIBRARY).on(LIBRARY.ID.eq(INSTRUMENT.LIBRARY_ID))
          .where(INSTRUMENT.ID.eq(instrumentId))
          .and(LIBRARY.ACCOUNT_ID.in(hubAccess.getAccountIds()))
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
  public Collection<UUID> readIdsInLibraries(HubAccess hubAccess, Collection<UUID> parentIds) throws DAOException {
    requireArtist(hubAccess);
    return DAO.idsFrom(dbProvider.getDSL().select(INSTRUMENT.ID)
      .from(INSTRUMENT)
      .where(INSTRUMENT.LIBRARY_ID.in(parentIds))
      .and(INSTRUMENT.STATE.equal(InstrumentState.Published))
      .fetch());
  }

  @Override
  public Instrument update(HubAccess hubAccess, UUID id, Instrument rawInstrument) throws DAOException, JsonapiException, ValueException {
    DSLContext db = dbProvider.getDSL();
    rawInstrument.setId(id); // prevent changing id
    Instrument instrument = validate(rawInstrument);
    requireArtist(hubAccess);
    requireParentExists(db, hubAccess, instrument);
    executeUpdate(db, INSTRUMENT, id, instrument);
    return instrument;
  }

  /**
   Read one record

   @param db        DSL context
   @param hubAccess control
   @param id        to read
   @return record
   @throws DAOException on failure
   */
  private Instrument readOne(DSLContext db, HubAccess hubAccess, UUID id) throws DAOException {
    if (hubAccess.isTopLevel())
      return modelFrom(Instrument.class, db.selectFrom(INSTRUMENT)
        .where(INSTRUMENT.ID.eq(id))
        .fetchOne());
    else
      return modelFrom(Instrument.class, db.select(INSTRUMENT.fields())
        .from(INSTRUMENT)
        .join(LIBRARY).on(LIBRARY.ID.eq(INSTRUMENT.LIBRARY_ID))
        .where(INSTRUMENT.ID.eq(id))
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
        .where(LIBRARY.ID.eq(entity.getLibraryId()))
        .fetchOne(0, int.class));
    else
      requireExists("Library", db.selectCount().from(LIBRARY)
        .where(LIBRARY.ACCOUNT_ID.in(hubAccess.getAccountIds()))
        .and(LIBRARY.ID.eq(entity.getLibraryId()))
        .fetchOne(0, int.class));
  }

  /**
   Validate data

   @param record to validate
   @throws DAOException if invalid
   */
  public Instrument validate(Instrument record) throws DAOException {
    try {
      Values.require(record.getLibraryId(), "Library ID");
      Values.require(record.getName(), "Name");
      Values.require(record.getType(), "Type");
      Values.require(record.getState(), "State");

      // [#175347578] validate TypeSafe chain config
      // [#177129498] Artist saves Instrument, Instrument, or Template config, validate & combine with defaults.
      if (Objects.isNull(record.getConfig()))
        record.setConfig(new InstrumentConfig().toString());
      else
        record.setConfig(new InstrumentConfig(record).toString());

      return record;

    } catch (ValueException e) {
      throw new DAOException(e);
    }
  }

}
