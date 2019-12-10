// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import com.google.inject.Inject;
import io.xj.core.access.Access;
import io.xj.core.exception.CoreException;
import io.xj.core.model.ProgramMeme;
import io.xj.core.persistence.SQLDatabaseProvider;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.UUID;

import static io.xj.core.Tables.PROGRAM_MEME;

public class ProgramMemeDAOImpl extends DAOImpl<ProgramMeme> implements ProgramMemeDAO {

  @Inject
  public ProgramMemeDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public ProgramMeme create(Access access, ProgramMeme entity) throws CoreException {
    entity.validate();
    requireTopLevel(access);
    return DAO.modelFrom(ProgramMeme.class,
      executeCreate(PROGRAM_MEME, entity));

  }

  @Override
  @Nullable
  public ProgramMeme readOne(Access access, UUID id) throws CoreException {
    requireUser(access);
    return DAO.modelFrom(ProgramMeme.class,
      dbProvider.getDSL().selectFrom(PROGRAM_MEME)
        .where(PROGRAM_MEME.ID.eq(id))
        .fetchOne());
  }

  @Override
  @Nullable
  public Collection<ProgramMeme> readMany(Access access, Collection<UUID> parentIds) throws CoreException {
    requireUser(access);
    return DAO.modelsFrom(ProgramMeme.class,
      dbProvider.getDSL().selectFrom(PROGRAM_MEME)
        .where(PROGRAM_MEME.PROGRAM_ID.in(parentIds))
        .fetch());
  }

  @Override
  public void update(Access access, UUID id, ProgramMeme entity) throws CoreException {
    entity.validate();
    requireTopLevel(access);
    executeUpdate(PROGRAM_MEME, id, entity);
  }

  @Override
  public void destroy(Access access, UUID id) throws CoreException {
    requireLibrary(access);
    dbProvider.getDSL().deleteFrom(PROGRAM_MEME)
      .where(PROGRAM_MEME.ID.eq(id))
      .execute();
  }

  @Override
  public ProgramMeme newInstance() {
    return new ProgramMeme();
  }

}
