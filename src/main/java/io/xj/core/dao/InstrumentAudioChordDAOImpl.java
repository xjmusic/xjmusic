// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import com.google.inject.Inject;
import io.xj.core.access.Access;
import io.xj.core.exception.CoreException;
import io.xj.core.model.InstrumentAudioChord;
import io.xj.core.persistence.sql.SQLDatabaseProvider;

import javax.annotation.Nullable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.UUID;

import static io.xj.core.Tables.INSTRUMENT_AUDIO_CHORD;

public class InstrumentAudioChordDAOImpl extends DAOImpl<InstrumentAudioChord> implements InstrumentAudioChordDAO {

  @Inject
  public InstrumentAudioChordDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public InstrumentAudioChord create(Access access, InstrumentAudioChord entity) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      entity.validate();
      requireTopLevel(access);
      return DAORecord.modelFrom(InstrumentAudioChord.class,
        executeCreate(connection, INSTRUMENT_AUDIO_CHORD, entity));

    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  @Nullable
  public InstrumentAudioChord readOne(Access access, UUID id) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      requireUser(access);
      return DAORecord.modelFrom(InstrumentAudioChord.class,
        DAORecord.DSL(connection).selectFrom(INSTRUMENT_AUDIO_CHORD)
          .where(INSTRUMENT_AUDIO_CHORD.ID.eq(id))
          .fetchOne());
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  @Nullable
  public Collection<InstrumentAudioChord> readMany(Access access, Collection<UUID> parentIds) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      requireUser(access);
      return DAORecord.modelsFrom(InstrumentAudioChord.class,
        DAORecord.DSL(connection).selectFrom(INSTRUMENT_AUDIO_CHORD)
          .where(INSTRUMENT_AUDIO_CHORD.INSTRUMENT_ID.in(parentIds))
          .fetch());
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  public void update(Access access, UUID id, InstrumentAudioChord entity) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      entity.validate();
      requireTopLevel(access);
      executeUpdate(connection, INSTRUMENT_AUDIO_CHORD, id, entity);
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  public void destroy(Access access, UUID id) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      requireLibrary(access);
      DAORecord.DSL(connection).deleteFrom(INSTRUMENT_AUDIO_CHORD)
        .where(INSTRUMENT_AUDIO_CHORD.ID.eq(id))
        .execute();
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  public InstrumentAudioChord newInstance() {
    return new InstrumentAudioChord();
  }

}
