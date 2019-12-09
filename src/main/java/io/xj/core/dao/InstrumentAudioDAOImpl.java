// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import com.google.inject.Inject;
import io.xj.core.access.Access;
import io.xj.core.exception.CoreException;
import io.xj.core.model.InstrumentAudio;
import io.xj.core.persistence.sql.SQLDatabaseProvider;

import javax.annotation.Nullable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.UUID;

import static io.xj.core.Tables.INSTRUMENT_AUDIO;

public class InstrumentAudioDAOImpl extends DAOImpl<InstrumentAudio> implements InstrumentAudioDAO {

  @Inject
  public InstrumentAudioDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public InstrumentAudio create(Access access, InstrumentAudio entity) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      entity.validate();
      requireTopLevel(access);
      return DAORecord.modelFrom(InstrumentAudio.class,
        executeCreate(connection, INSTRUMENT_AUDIO, entity));

    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  @Nullable
  public InstrumentAudio readOne(Access access, UUID id) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      requireUser(access);
      return DAORecord.modelFrom(InstrumentAudio.class,
        DAORecord.DSL(connection).selectFrom(INSTRUMENT_AUDIO)
          .where(INSTRUMENT_AUDIO.ID.eq(id))
          .fetchOne());
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  @Nullable
  public Collection<InstrumentAudio> readMany(Access access, Collection<UUID> parentIds) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      requireUser(access);
      return DAORecord.modelsFrom(InstrumentAudio.class,
        DAORecord.DSL(connection).selectFrom(INSTRUMENT_AUDIO)
          .where(INSTRUMENT_AUDIO.INSTRUMENT_ID.in(parentIds))
          .fetch());
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  public void update(Access access, UUID id, InstrumentAudio entity) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      entity.validate();
      requireTopLevel(access);
      executeUpdate(connection, INSTRUMENT_AUDIO, id, entity);
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  public void destroy(Access access, UUID id) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      requireLibrary(access);
      DAORecord.DSL(connection).deleteFrom(INSTRUMENT_AUDIO)
        .where(INSTRUMENT_AUDIO.ID.eq(id))
        .execute();
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  public InstrumentAudio newInstance() {
    return new InstrumentAudio();
  }

}
