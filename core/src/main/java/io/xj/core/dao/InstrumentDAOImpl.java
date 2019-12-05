// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import io.xj.core.access.Access;
import io.xj.core.entity.Entity;
import io.xj.core.exception.CoreException;
import io.xj.core.model.Instrument;
import io.xj.core.model.InstrumentAudio;
import io.xj.core.model.InstrumentAudioChord;
import io.xj.core.model.InstrumentAudioEvent;
import io.xj.core.model.InstrumentMeme;
import io.xj.core.model.InstrumentState;
import io.xj.core.persistence.sql.SQLDatabaseProvider;
import org.jooq.DSLContext;

import javax.annotation.Nullable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

import static io.xj.core.Tables.INSTRUMENT;
import static io.xj.core.Tables.INSTRUMENT_AUDIO;
import static io.xj.core.Tables.INSTRUMENT_AUDIO_CHORD;
import static io.xj.core.Tables.INSTRUMENT_AUDIO_EVENT;
import static io.xj.core.Tables.INSTRUMENT_MEME;
import static io.xj.core.Tables.LIBRARY;

public class InstrumentDAOImpl extends DAOImpl<Instrument> implements InstrumentDAO {

  @Inject
  public InstrumentDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  /**
   Read one record

   @param connection context
   @param access     control
   @param id         of record
   @return record
   */
  @Nullable
  private Instrument readOne(Connection connection, Access access, UUID id) throws CoreException {
    if (access.isTopLevel())
      return DAORecord.modelFrom(Instrument.class, DAORecord.DSL(connection).selectFrom(INSTRUMENT)
        .where(INSTRUMENT.ID.eq(id))
        .fetchOne());
    else
      return DAORecord.modelFrom(Instrument.class, DAORecord.DSL(connection).select(INSTRUMENT.fields())
        .from(INSTRUMENT)
        .join(LIBRARY).on(LIBRARY.ID.eq(INSTRUMENT.LIBRARY_ID))
        .where(INSTRUMENT.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne());
  }

  /**
   Create a record

   @param connection context
   @param access     control
   @param entity     for new record
   @return newly readMany record
   @throws CoreException on failure
   */
  private Instrument create(Connection connection, Access access, Instrument entity) throws CoreException {
    entity.validate();


    // This entity's parent is a Library
    requireLibrary(access);

    return DAORecord.modelFrom(Instrument.class, executeCreate(connection, INSTRUMENT, entity));
  }

  /**
   Update a record

   @param connection to SQL database
   @param access     control
   @param id         of record
   @param entity     to update with
   @throws CoreException if a Business Rule is violated
   @throws CoreException on database failure
   */
  private void update(Connection connection, Access access, UUID id, Instrument entity) throws CoreException {
    entity.validate();

    entity.setId(id); // prevent changing id
    requireLibrary(access);

    executeUpdate(connection, INSTRUMENT, id, entity);
  }

  /**
   Clone a Instrument into a new Instrument

   @param connection context
   @param access     control
   @param cloneId    of instrument to clone
   @param entity     for the new Account User.
   @return newly readMany record
   @throws CoreException on failure
   */
  private Instrument clone(Connection connection, Access access, UUID cloneId, Instrument entity) throws CoreException {
    Instrument from = readOne(connection, access, cloneId);
    if (Objects.isNull(from))
      throw new CoreException("Can't clone nonexistent Instrument");

    // Inherits state, type if none specified
    if (Objects.isNull(entity.getType())) entity.setTypeEnum(from.getType());
    if (Objects.isNull(entity.getState())) entity.setStateEnum(from.getState());

    // Instrument must be created(have id) before adding content and updating
    Instrument instrument = create(connection, access, entity);
    // TODO clone all child entities of instrument into new set of entities
    update(connection, access, instrument.getId(), instrument);
    return instrument;
  }

  @Override
  public Instrument create(Access access, Instrument entity) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      return create(connection, access, entity);
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  public Instrument clone(Access access, UUID cloneId, Instrument entity) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      return clone(connection, access, cloneId, entity);
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  @Nullable
  public Instrument readOne(Access access, UUID id) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      return readOne(connection, access, id);
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  public void destroy(Access access, UUID id) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      DSLContext db = DAORecord.DSL(connection);

      if (!access.isTopLevel())
        requireExists("Instrument belonging to you", db.selectCount().from(INSTRUMENT)
          .join(LIBRARY).on(INSTRUMENT.LIBRARY_ID.eq(LIBRARY.ID))
          .where(INSTRUMENT.ID.eq(id))
          .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
          .fetchOne(0, int.class));

      requireNotExists("Audio in Instrument", db.selectCount().from(INSTRUMENT_AUDIO)
        .where(INSTRUMENT_AUDIO.INSTRUMENT_ID.eq(id))
        .fetchOne(0, int.class));

      requireNotExists("MemeEntity in Instrument", db.selectCount().from(INSTRUMENT_MEME)
        .where(INSTRUMENT_MEME.INSTRUMENT_ID.eq(id))
        .fetchOne(0, int.class));

      db.deleteFrom(INSTRUMENT)
        .where(INSTRUMENT.ID.eq(id))
        .execute();
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  public Instrument newInstance() {
    return new Instrument();
  }

  @Override
  public Collection<Instrument> readAllInAccount(Access access, UUID accountId) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      if (access.isTopLevel())
        return DAORecord.modelsFrom(Instrument.class, DAORecord.DSL(connection).select(INSTRUMENT.fields())
          .from(INSTRUMENT)
          .join(LIBRARY).on(INSTRUMENT.LIBRARY_ID.eq(LIBRARY.ID))
          .where(LIBRARY.ACCOUNT_ID.eq(accountId))
          .orderBy(INSTRUMENT.TYPE, INSTRUMENT.NAME)
          .fetch());
      else
        return DAORecord.modelsFrom(Instrument.class, DAORecord.DSL(connection).select(INSTRUMENT.fields())
          .from(INSTRUMENT)
          .join(LIBRARY).on(INSTRUMENT.LIBRARY_ID.eq(LIBRARY.ID))
          .where(LIBRARY.ACCOUNT_ID.in(accountId))
          .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
          .orderBy(INSTRUMENT.TYPE, INSTRUMENT.NAME)
          .fetch());
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  public Collection<Instrument> readMany(Access access, Collection<UUID> parentIds) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      if (access.isTopLevel())
        return DAORecord.modelsFrom(Instrument.class, DAORecord.DSL(connection).select(INSTRUMENT.fields())
          .from(INSTRUMENT)
          .where(INSTRUMENT.LIBRARY_ID.in(parentIds))
          .orderBy(INSTRUMENT.TYPE, INSTRUMENT.NAME)
          .fetch());
      else
        return DAORecord.modelsFrom(Instrument.class, DAORecord.DSL(connection).select(INSTRUMENT.fields())
          .from(INSTRUMENT)
          .join(LIBRARY).on(LIBRARY.ID.eq(INSTRUMENT.LIBRARY_ID))
          .where(INSTRUMENT.LIBRARY_ID.in(parentIds))
          .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
          .orderBy(INSTRUMENT.TYPE, INSTRUMENT.NAME)
          .fetch());
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  public Collection<Entity> readManyWithChildEntities(Access access, Collection<UUID> instrumentIds) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      DSLContext db = DAORecord.DSL(connection);

      if (!access.isTopLevel())
        for (UUID instrumentId : instrumentIds)
          requireExists("access via account", db.selectCount().from(INSTRUMENT)
            .join(LIBRARY).on(LIBRARY.ID.eq(INSTRUMENT.LIBRARY_ID))
            .where(INSTRUMENT.ID.eq(instrumentId))
            .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
            .fetchOne(0, int.class));

      Collection<Entity> entities = Lists.newArrayList();
      entities.addAll(DAORecord.modelsFrom(Instrument.class, db.selectFrom(INSTRUMENT).where(INSTRUMENT.ID.in(instrumentIds)).fetch()));
      entities.addAll(DAORecord.modelsFrom(InstrumentMeme.class, db.selectFrom(INSTRUMENT_MEME).where(INSTRUMENT_MEME.INSTRUMENT_ID.in(instrumentIds))));
      entities.addAll(DAORecord.modelsFrom(InstrumentAudioChord.class, db.selectFrom(INSTRUMENT_AUDIO_CHORD).where(INSTRUMENT_AUDIO_CHORD.INSTRUMENT_ID.in(instrumentIds))));
      entities.addAll(DAORecord.modelsFrom(InstrumentAudioEvent.class, db.selectFrom(INSTRUMENT_AUDIO_EVENT).where(INSTRUMENT_AUDIO_EVENT.INSTRUMENT_ID.in(instrumentIds))));
      entities.addAll(DAORecord.modelsFrom(InstrumentAudio.class, db.selectFrom(INSTRUMENT_AUDIO).where(INSTRUMENT_AUDIO.INSTRUMENT_ID.in(instrumentIds))));
      return entities;

    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  public Collection<Instrument> readAll(Access access) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      if (access.isTopLevel())
        return DAORecord.modelsFrom(Instrument.class, DAORecord.DSL(connection).select(INSTRUMENT.fields())
          .from(INSTRUMENT)
          .orderBy(INSTRUMENT.TYPE, INSTRUMENT.NAME)
          .fetch());
      else
        return DAORecord.modelsFrom(Instrument.class, DAORecord.DSL(connection).select(INSTRUMENT.fields())
          .from(INSTRUMENT)
          .join(LIBRARY).on(LIBRARY.ID.eq(INSTRUMENT.LIBRARY_ID))
          .where(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
          .orderBy(INSTRUMENT.TYPE, INSTRUMENT.NAME)
          .fetch());
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  public Collection<UUID> readIdsInLibraries(Access access, Collection<UUID> parentIds) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      requireLibrary(access);
      return DAORecord.idsFrom(DAORecord.DSL(connection).select(INSTRUMENT.ID)
        .from(INSTRUMENT)
        .where(INSTRUMENT.LIBRARY_ID.in(parentIds))
        .and(INSTRUMENT.STATE.equal(InstrumentState.Published.toString()))
        .fetch());
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  public void update(Access access, UUID id, Instrument entity) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      update(connection, access, id, entity);
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }


}
