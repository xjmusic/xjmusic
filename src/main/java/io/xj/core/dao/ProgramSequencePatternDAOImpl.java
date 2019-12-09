// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import com.google.inject.Inject;
import io.xj.core.access.Access;
import io.xj.core.exception.CoreException;
import io.xj.core.model.ProgramSequencePattern;
import io.xj.core.persistence.sql.SQLDatabaseProvider;

import javax.annotation.Nullable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.UUID;

import static io.xj.core.Tables.PROGRAM_SEQUENCE_PATTERN;

public class ProgramSequencePatternDAOImpl extends DAOImpl<ProgramSequencePattern> implements ProgramSequencePatternDAO {

  @Inject
  public ProgramSequencePatternDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public ProgramSequencePattern create(Access access, ProgramSequencePattern entity) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      entity.validate();
      requireTopLevel(access);
      return DAORecord.modelFrom(ProgramSequencePattern.class,
        executeCreate(connection, PROGRAM_SEQUENCE_PATTERN, entity));

    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  @Nullable
  public ProgramSequencePattern readOne(Access access, UUID id) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      requireUser(access);
      return DAORecord.modelFrom(ProgramSequencePattern.class,
        DAORecord.DSL(connection).selectFrom(PROGRAM_SEQUENCE_PATTERN)
          .where(PROGRAM_SEQUENCE_PATTERN.ID.eq(id))
          .fetchOne());
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  @Nullable
  public Collection<ProgramSequencePattern> readMany(Access access, Collection<UUID> parentIds) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      requireUser(access);
      return DAORecord.modelsFrom(ProgramSequencePattern.class,
        DAORecord.DSL(connection).selectFrom(PROGRAM_SEQUENCE_PATTERN)
          .where(PROGRAM_SEQUENCE_PATTERN.PROGRAM_ID.in(parentIds))
          .fetch());
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  public void update(Access access, UUID id, ProgramSequencePattern entity) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      entity.validate();
      requireTopLevel(access);
      executeUpdate(connection, PROGRAM_SEQUENCE_PATTERN, id, entity);
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  public void destroy(Access access, UUID id) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      requireLibrary(access);
      DAORecord.DSL(connection).deleteFrom(PROGRAM_SEQUENCE_PATTERN)
        .where(PROGRAM_SEQUENCE_PATTERN.ID.eq(id))
        .execute();
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  public ProgramSequencePattern newInstance() {
    return new ProgramSequencePattern();
  }

}
