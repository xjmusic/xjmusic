// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.core.dao;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import io.xj.lib.core.access.Access;
import io.xj.lib.core.entity.Entity;
import io.xj.lib.core.exception.CoreException;
import io.xj.lib.core.model.Instrument;
import io.xj.lib.core.model.InstrumentAudio;
import io.xj.lib.core.model.InstrumentAudioChord;
import io.xj.lib.core.model.InstrumentAudioEvent;
import io.xj.lib.core.model.InstrumentMeme;
import io.xj.lib.core.model.InstrumentState;
import io.xj.lib.core.persistence.SQLDatabaseProvider;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static io.xj.lib.core.Tables.INSTRUMENT;
import static io.xj.lib.core.Tables.INSTRUMENT_AUDIO;
import static io.xj.lib.core.Tables.INSTRUMENT_AUDIO_CHORD;
import static io.xj.lib.core.Tables.INSTRUMENT_AUDIO_EVENT;
import static io.xj.lib.core.Tables.INSTRUMENT_MEME;
import static io.xj.lib.core.Tables.LIBRARY;

public class InstrumentDAOImpl extends DAOImpl<Instrument> implements InstrumentDAO {

  @Inject
  public InstrumentDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public Instrument create(Access access, Instrument entity) throws CoreException {
    entity.validate();

    // This entity's parent is a Library
    requireArtist(access);
    DSLContext db = dbProvider.getDSL();
    requireParentExists(db, access, entity);
    return DAO.modelFrom(Instrument.class, executeCreate(db, INSTRUMENT, entity));
  }

  @Override
  public Instrument clone(Access access, UUID cloneId, Instrument entity) throws CoreException {
    requireArtist(access);
    AtomicReference<Instrument> result = new AtomicReference<>();
    dbProvider.getDSL().transaction(ctx -> {
      DSLContext db = DSL.using(ctx);

      Instrument from = readOne(db, access, cloneId);
      if (Objects.isNull(from))
        throw new CoreException("Can't clone nonexistent Instrument");

      // Inherits state, type if none specified
      if (Objects.isNull(entity.getType())) entity.setTypeEnum(from.getType());
      if (Objects.isNull(entity.getState())) entity.setStateEnum(from.getState());
      if (Objects.isNull(entity.getDensity())) entity.setDensity(from.getDensity());
      if (Objects.isNull(entity.getName())) entity.setName(from.getName());
      entity.setUserId(from.getUserId());
      entity.validate();
      requireParentExists(db, access, entity);

      result.set(DAO.modelFrom(Instrument.class, executeCreate(db, INSTRUMENT, entity)));

      DAOCloner<Entity> cloner = new DAOCloner<Entity>(result.get());
      cloner.clone(db, INSTRUMENT_MEME, INSTRUMENT_MEME.ID, ImmutableSet.of(), INSTRUMENT_MEME.INSTRUMENT_ID, cloneId, result.get().getId());
      cloner.clone(db, INSTRUMENT_AUDIO, INSTRUMENT_AUDIO.ID, ImmutableSet.of(), INSTRUMENT_AUDIO.INSTRUMENT_ID, cloneId, result.get().getId());
      cloner.clone(db, INSTRUMENT_AUDIO_EVENT, INSTRUMENT_AUDIO_EVENT.ID, ImmutableSet.of(INSTRUMENT_AUDIO_EVENT.INSTRUMENT_AUDIO_ID), INSTRUMENT_AUDIO_EVENT.INSTRUMENT_ID, cloneId, result.get().getId());
      cloner.clone(db, INSTRUMENT_AUDIO_CHORD, INSTRUMENT_AUDIO_CHORD.ID, ImmutableSet.of(INSTRUMENT_AUDIO_CHORD.INSTRUMENT_AUDIO_ID), INSTRUMENT_AUDIO_CHORD.INSTRUMENT_ID, cloneId, result.get().getId());
    });
    return result.get();
  }

  @Override
  @Nullable
  public Instrument readOne(Access access, UUID id) throws CoreException {
    return readOne(dbProvider.getDSL(), access, id);
  }

  @Override
  public void destroy(Access access, UUID id) throws CoreException {
    DSLContext db = dbProvider.getDSL();

    if (!access.isTopLevel())
      requireExists("Instrument belonging to you", db.selectCount().from(INSTRUMENT)
        .join(LIBRARY).on(INSTRUMENT.LIBRARY_ID.eq(LIBRARY.ID))
        .where(INSTRUMENT.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
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
    return new Instrument();
  }

  @Override
  public Collection<Instrument> readAllInAccount(Access access, UUID accountId) throws CoreException {
    if (access.isTopLevel())
      return DAO.modelsFrom(Instrument.class, dbProvider.getDSL().select(INSTRUMENT.fields())
        .from(INSTRUMENT)
        .join(LIBRARY).on(INSTRUMENT.LIBRARY_ID.eq(LIBRARY.ID))
        .where(LIBRARY.ACCOUNT_ID.eq(accountId))
        .orderBy(INSTRUMENT.TYPE, INSTRUMENT.NAME)
        .fetch());
    else
      return DAO.modelsFrom(Instrument.class, dbProvider.getDSL().select(INSTRUMENT.fields())
        .from(INSTRUMENT)
        .join(LIBRARY).on(INSTRUMENT.LIBRARY_ID.eq(LIBRARY.ID))
        .where(LIBRARY.ACCOUNT_ID.in(accountId))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .orderBy(INSTRUMENT.TYPE, INSTRUMENT.NAME)
        .fetch());
  }

  @Override
  public Collection<Instrument> readMany(Access access, Collection<UUID> parentIds) throws CoreException {
    if (access.isTopLevel())
      return DAO.modelsFrom(Instrument.class, dbProvider.getDSL().select(INSTRUMENT.fields())
        .from(INSTRUMENT)
        .where(INSTRUMENT.LIBRARY_ID.in(parentIds))
        .orderBy(INSTRUMENT.TYPE, INSTRUMENT.NAME)
        .fetch());
    else
      return DAO.modelsFrom(Instrument.class, dbProvider.getDSL().select(INSTRUMENT.fields())
        .from(INSTRUMENT)
        .join(LIBRARY).on(LIBRARY.ID.eq(INSTRUMENT.LIBRARY_ID))
        .where(INSTRUMENT.LIBRARY_ID.in(parentIds))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .orderBy(INSTRUMENT.TYPE, INSTRUMENT.NAME)
        .fetch());
  }

  @Override
  public Collection<Entity> readManyWithChildEntities(Access access, Collection<UUID> instrumentIds) throws CoreException {
    DSLContext db = dbProvider.getDSL();

    if (!access.isTopLevel())
      for (UUID instrumentId : instrumentIds)
        requireExists("access via account", db.selectCount().from(INSTRUMENT)
          .join(LIBRARY).on(LIBRARY.ID.eq(INSTRUMENT.LIBRARY_ID))
          .where(INSTRUMENT.ID.eq(instrumentId))
          .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
          .fetchOne(0, int.class));

    Collection<Entity> entities = Lists.newArrayList();
    entities.addAll(DAO.modelsFrom(Instrument.class, db.selectFrom(INSTRUMENT).where(INSTRUMENT.ID.in(instrumentIds)).fetch()));
    entities.addAll(DAO.modelsFrom(InstrumentMeme.class, db.selectFrom(INSTRUMENT_MEME).where(INSTRUMENT_MEME.INSTRUMENT_ID.in(instrumentIds))));
    entities.addAll(DAO.modelsFrom(InstrumentAudioChord.class, db.selectFrom(INSTRUMENT_AUDIO_CHORD).where(INSTRUMENT_AUDIO_CHORD.INSTRUMENT_ID.in(instrumentIds))));
    entities.addAll(DAO.modelsFrom(InstrumentAudioEvent.class, db.selectFrom(INSTRUMENT_AUDIO_EVENT).where(INSTRUMENT_AUDIO_EVENT.INSTRUMENT_ID.in(instrumentIds))));
    entities.addAll(DAO.modelsFrom(InstrumentAudio.class, db.selectFrom(INSTRUMENT_AUDIO).where(INSTRUMENT_AUDIO.INSTRUMENT_ID.in(instrumentIds))));
    return entities;
  }

  @Override
  public Collection<Instrument> readAll(Access access) throws CoreException {
    if (access.isTopLevel())
      return DAO.modelsFrom(Instrument.class, dbProvider.getDSL().select(INSTRUMENT.fields())
        .from(INSTRUMENT)
        .orderBy(INSTRUMENT.TYPE, INSTRUMENT.NAME)
        .fetch());
    else
      return DAO.modelsFrom(Instrument.class, dbProvider.getDSL().select(INSTRUMENT.fields())
        .from(INSTRUMENT)
        .join(LIBRARY).on(LIBRARY.ID.eq(INSTRUMENT.LIBRARY_ID))
        .where(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .orderBy(INSTRUMENT.TYPE, INSTRUMENT.NAME)
        .fetch());
  }

  @Override
  public Collection<UUID> readIdsInLibraries(Access access, Collection<UUID> parentIds) throws CoreException {
    requireArtist(access);
    return DAO.idsFrom(dbProvider.getDSL().select(INSTRUMENT.ID)
      .from(INSTRUMENT)
      .where(INSTRUMENT.LIBRARY_ID.in(parentIds))
      .and(INSTRUMENT.STATE.equal(InstrumentState.Published.toString()))
      .fetch());
  }

  @Override
  public void update(Access access, UUID id, Instrument entity) throws CoreException {
    entity.validate();

    entity.setId(id); // prevent changing id
    requireArtist(access);

    DSLContext db = dbProvider.getDSL();
    requireParentExists(db, access, entity);
    executeUpdate(db, INSTRUMENT, id, entity);
  }

  /**
   Read one record

   @param db     DSL context
   @param access control
   @param id     to read
   @return record
   @throws CoreException on failure
   */
  private Instrument readOne(DSLContext db, Access access, UUID id) throws CoreException {
    if (access.isTopLevel())
      return DAO.modelFrom(Instrument.class, db.selectFrom(INSTRUMENT)
        .where(INSTRUMENT.ID.eq(id))
        .fetchOne());
    else
      return DAO.modelFrom(Instrument.class, db.select(INSTRUMENT.fields())
        .from(INSTRUMENT)
        .join(LIBRARY).on(LIBRARY.ID.eq(INSTRUMENT.LIBRARY_ID))
        .where(INSTRUMENT.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne());
  }

  /**
   Require parent instrument exists of a given possible entity in a DSL context

   @param db     DSL context
   @param access control
   @param entity to validate
   @throws CoreException if parent does not exist
   */
  private void requireParentExists(DSLContext db, Access access, Instrument entity) throws CoreException {
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

}
