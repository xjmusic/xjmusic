// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao.impl;

import com.google.inject.Inject;
import io.xj.core.access.Access;
import io.xj.core.dao.DAORecord;
import io.xj.core.dao.InstrumentAudioEventDAO;
import io.xj.core.exception.CoreException;
import io.xj.core.model.InstrumentAudioEvent;
import io.xj.core.persistence.sql.SQLDatabaseProvider;

import javax.annotation.Nullable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.UUID;

import static io.xj.core.Tables.INSTRUMENT_AUDIO_EVENT;

public class InstrumentAudioEventDAOImpl extends DAOImpl<InstrumentAudioEvent> implements InstrumentAudioEventDAO {

  @Inject
  public InstrumentAudioEventDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public InstrumentAudioEvent create(Access access, InstrumentAudioEvent entity) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      entity.validate();
      requireTopLevel(access);
      return DAORecord.modelFrom(InstrumentAudioEvent.class,
        executeCreate(connection, INSTRUMENT_AUDIO_EVENT, entity));

    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  @Nullable
  public InstrumentAudioEvent readOne(Access access, UUID id) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      requireUser(access);
      return DAORecord.modelFrom(InstrumentAudioEvent.class,
        DAORecord.DSL(connection).selectFrom(INSTRUMENT_AUDIO_EVENT)
          .where(INSTRUMENT_AUDIO_EVENT.ID.eq(id))
          .fetchOne());
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  @Nullable
  public Collection<InstrumentAudioEvent> readMany(Access access, Collection<UUID> parentIds) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      requireUser(access);
      return DAORecord.modelsFrom(InstrumentAudioEvent.class,
        DAORecord.DSL(connection).selectFrom(INSTRUMENT_AUDIO_EVENT)
          .where(INSTRUMENT_AUDIO_EVENT.INSTRUMENT_ID.in(parentIds))
          .fetch());
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  public void update(Access access, UUID id, InstrumentAudioEvent entity) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      entity.validate();
      requireTopLevel(access);
      executeUpdate(connection, INSTRUMENT_AUDIO_EVENT, id, entity);
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  public void destroy(Access access, UUID id) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      requireLibrary(access);
      DAORecord.DSL(connection).deleteFrom(INSTRUMENT_AUDIO_EVENT)
        .where(INSTRUMENT_AUDIO_EVENT.ID.eq(id))
        .execute();
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  public InstrumentAudioEvent newInstance() {
    return new InstrumentAudioEvent();
  }

}
