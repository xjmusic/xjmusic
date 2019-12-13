// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import com.google.inject.Inject;
import io.xj.core.access.Access;
import io.xj.core.exception.CoreException;
import io.xj.core.model.ProgramSequencePattern;
import io.xj.core.persistence.SQLDatabaseProvider;

import javax.annotation.Nullable;
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
    entity.validate();
    requireArtist(access);
    return DAO.modelFrom(ProgramSequencePattern.class,
      executeCreate(dbProvider.getDSL(), PROGRAM_SEQUENCE_PATTERN, entity));

  }

  @Override
  @Nullable
  public ProgramSequencePattern readOne(Access access, UUID id) throws CoreException {
    requireArtist(access);
    return DAO.modelFrom(ProgramSequencePattern.class,
      dbProvider.getDSL().selectFrom(PROGRAM_SEQUENCE_PATTERN)
        .where(PROGRAM_SEQUENCE_PATTERN.ID.eq(id))
        .fetchOne());
  }

  @Override
  @Nullable
  public Collection<ProgramSequencePattern> readMany(Access access, Collection<UUID> parentIds) throws CoreException {
    requireArtist(access);
    return DAO.modelsFrom(ProgramSequencePattern.class,
      dbProvider.getDSL().selectFrom(PROGRAM_SEQUENCE_PATTERN)
        .where(PROGRAM_SEQUENCE_PATTERN.PROGRAM_SEQUENCE_ID.in(parentIds))
        .fetch());
  }

  @Override
  public void update(Access access, UUID id, ProgramSequencePattern entity) throws CoreException {
    entity.validate();
    requireArtist(access);
    executeUpdate(dbProvider.getDSL(), PROGRAM_SEQUENCE_PATTERN, id, entity);
  }

  @Override
  public void destroy(Access access, UUID id) throws CoreException {
    requireArtist(access);
    dbProvider.getDSL().deleteFrom(PROGRAM_SEQUENCE_PATTERN)
      .where(PROGRAM_SEQUENCE_PATTERN.ID.eq(id))
      .execute();
  }

  @Override
  public ProgramSequencePattern newInstance() {
    return new ProgramSequencePattern();
  }

}
