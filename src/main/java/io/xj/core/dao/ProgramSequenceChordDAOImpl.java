// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import com.google.inject.Inject;
import io.xj.core.access.Access;
import io.xj.core.exception.CoreException;
import io.xj.core.model.ProgramSequenceChord;
import io.xj.core.persistence.sql.SQLDatabaseProvider;

import javax.annotation.Nullable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.UUID;

import static io.xj.core.Tables.PROGRAM_SEQUENCE_CHORD;

public class ProgramSequenceChordDAOImpl extends DAOImpl<ProgramSequenceChord> implements ProgramSequenceChordDAO {

  @Inject
  public ProgramSequenceChordDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public ProgramSequenceChord create(Access access, ProgramSequenceChord entity) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      entity.validate();
      requireTopLevel(access);
      return DAORecord.modelFrom(ProgramSequenceChord.class,
        executeCreate(connection, PROGRAM_SEQUENCE_CHORD, entity));

    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  @Nullable
  public ProgramSequenceChord readOne(Access access, UUID id) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      requireUser(access);
      return DAORecord.modelFrom(ProgramSequenceChord.class,
        DAORecord.DSL(connection).selectFrom(PROGRAM_SEQUENCE_CHORD)
          .where(PROGRAM_SEQUENCE_CHORD.ID.eq(id))
          .fetchOne());
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  @Nullable
  public Collection<ProgramSequenceChord> readMany(Access access, Collection<UUID> parentIds) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      requireUser(access);
      return DAORecord.modelsFrom(ProgramSequenceChord.class,
        DAORecord.DSL(connection).selectFrom(PROGRAM_SEQUENCE_CHORD)
          .where(PROGRAM_SEQUENCE_CHORD.PROGRAM_ID.in(parentIds))
          .fetch());
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  public void update(Access access, UUID id, ProgramSequenceChord entity) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      entity.validate();
      requireTopLevel(access);
      executeUpdate(connection, PROGRAM_SEQUENCE_CHORD, id, entity);
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  public void destroy(Access access, UUID id) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      requireLibrary(access);
      DAORecord.DSL(connection).deleteFrom(PROGRAM_SEQUENCE_CHORD)
        .where(PROGRAM_SEQUENCE_CHORD.ID.eq(id))
        .execute();
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  public ProgramSequenceChord newInstance() {
    return new ProgramSequenceChord();
  }

}
