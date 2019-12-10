// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import com.google.inject.Inject;
import io.xj.core.access.Access;
import io.xj.core.exception.CoreException;
import io.xj.core.model.ProgramSequence;
import io.xj.core.persistence.SQLDatabaseProvider;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.UUID;

import static io.xj.core.Tables.PROGRAM_SEQUENCE;

public class ProgramSequenceDAOImpl extends DAOImpl<ProgramSequence> implements ProgramSequenceDAO {

  @Inject
  public ProgramSequenceDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public ProgramSequence create(Access access, ProgramSequence entity) throws CoreException {
    entity.validate();
    requireTopLevel(access);
    return DAO.modelFrom(ProgramSequence.class,
      executeCreate(PROGRAM_SEQUENCE, entity));

  }

  @Override
  @Nullable
  public ProgramSequence readOne(Access access, UUID id) throws CoreException {
    requireUser(access);
    return DAO.modelFrom(ProgramSequence.class,
      dbProvider.getDSL().selectFrom(PROGRAM_SEQUENCE)
        .where(PROGRAM_SEQUENCE.ID.eq(id))
        .fetchOne());
  }

  @Override
  @Nullable
  public Collection<ProgramSequence> readMany(Access access, Collection<UUID> parentIds) throws CoreException {
    requireUser(access);
    return DAO.modelsFrom(ProgramSequence.class,
      dbProvider.getDSL().selectFrom(PROGRAM_SEQUENCE)
        .where(PROGRAM_SEQUENCE.PROGRAM_ID.in(parentIds))
        .fetch());
  }

  @Override
  public void update(Access access, UUID id, ProgramSequence entity) throws CoreException {
    entity.validate();
    requireTopLevel(access);
    executeUpdate(PROGRAM_SEQUENCE, id, entity);
  }

  @Override
  public void destroy(Access access, UUID id) throws CoreException {
    requireLibrary(access);
    dbProvider.getDSL().deleteFrom(PROGRAM_SEQUENCE)
      .where(PROGRAM_SEQUENCE.ID.eq(id))
      .execute();
  }

  @Override
  public ProgramSequence newInstance() {
    return new ProgramSequence();
  }

}
