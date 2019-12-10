// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import com.google.inject.Inject;
import io.xj.core.access.Access;
import io.xj.core.exception.CoreException;
import io.xj.core.model.ProgramSequenceChord;
import io.xj.core.persistence.SQLDatabaseProvider;

import javax.annotation.Nullable;
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
    entity.validate();
    requireTopLevel(access);
    return DAO.modelFrom(ProgramSequenceChord.class,
      executeCreate(PROGRAM_SEQUENCE_CHORD, entity));

  }

  @Override
  @Nullable
  public ProgramSequenceChord readOne(Access access, UUID id) throws CoreException {
    requireUser(access);
    return DAO.modelFrom(ProgramSequenceChord.class,
      dbProvider.getDSL().selectFrom(PROGRAM_SEQUENCE_CHORD)
        .where(PROGRAM_SEQUENCE_CHORD.ID.eq(id))
        .fetchOne());
  }

  @Override
  @Nullable
  public Collection<ProgramSequenceChord> readMany(Access access, Collection<UUID> parentIds) throws CoreException {
    requireUser(access);
    return DAO.modelsFrom(ProgramSequenceChord.class,
      dbProvider.getDSL().selectFrom(PROGRAM_SEQUENCE_CHORD)
        .where(PROGRAM_SEQUENCE_CHORD.PROGRAM_ID.in(parentIds))
        .fetch());
  }

  @Override
  public void update(Access access, UUID id, ProgramSequenceChord entity) throws CoreException {
    entity.validate();
    requireTopLevel(access);
    executeUpdate(PROGRAM_SEQUENCE_CHORD, id, entity);
  }

  @Override
  public void destroy(Access access, UUID id) throws CoreException {
    requireLibrary(access);
    dbProvider.getDSL().deleteFrom(PROGRAM_SEQUENCE_CHORD)
      .where(PROGRAM_SEQUENCE_CHORD.ID.eq(id))
      .execute();
  }

  @Override
  public ProgramSequenceChord newInstance() {
    return new ProgramSequenceChord();
  }

}
