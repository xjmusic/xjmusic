// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import com.google.inject.Inject;
import io.xj.core.access.Access;
import io.xj.core.exception.CoreException;
import io.xj.core.model.ProgramSequencePatternEvent;
import io.xj.core.persistence.SQLDatabaseProvider;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.UUID;

import static io.xj.core.Tables.PROGRAM_SEQUENCE_PATTERN_EVENT;

public class ProgramSequencePatternEventDAOImpl extends DAOImpl<ProgramSequencePatternEvent> implements ProgramSequencePatternEventDAO {

  @Inject
  public ProgramSequencePatternEventDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public ProgramSequencePatternEvent create(Access access, ProgramSequencePatternEvent entity) throws CoreException {
    entity.validate();
    requireArtist(access);
    return DAO.modelFrom(ProgramSequencePatternEvent.class,
      executeCreate(dbProvider.getDSL(), PROGRAM_SEQUENCE_PATTERN_EVENT, entity));

  }

  @Override
  @Nullable
  public ProgramSequencePatternEvent readOne(Access access, UUID id) throws CoreException {
    requireArtist(access);
    return DAO.modelFrom(ProgramSequencePatternEvent.class,
      dbProvider.getDSL().selectFrom(PROGRAM_SEQUENCE_PATTERN_EVENT)
        .where(PROGRAM_SEQUENCE_PATTERN_EVENT.ID.eq(id))
        .fetchOne());
  }

  @Override
  @Nullable
  public Collection<ProgramSequencePatternEvent> readMany(Access access, Collection<UUID> parentIds) throws CoreException {
    requireArtist(access);
    return DAO.modelsFrom(ProgramSequencePatternEvent.class,
      dbProvider.getDSL().selectFrom(PROGRAM_SEQUENCE_PATTERN_EVENT)
        .where(PROGRAM_SEQUENCE_PATTERN_EVENT.PROGRAM_SEQUENCE_PATTERN_ID.in(parentIds))
        .fetch());
  }

  @Override
  public void update(Access access, UUID id, ProgramSequencePatternEvent entity) throws CoreException {
    entity.validate();
    requireArtist(access);
    executeUpdate(dbProvider.getDSL(), PROGRAM_SEQUENCE_PATTERN_EVENT, id, entity);
  }

  @Override
  public void destroy(Access access, UUID id) throws CoreException {
    requireArtist(access);
    dbProvider.getDSL().deleteFrom(PROGRAM_SEQUENCE_PATTERN_EVENT)
      .where(PROGRAM_SEQUENCE_PATTERN_EVENT.ID.eq(id))
      .execute();
  }

  @Override
  public ProgramSequencePatternEvent newInstance() {
    return new ProgramSequencePatternEvent();
  }

}
