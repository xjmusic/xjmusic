// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import com.google.inject.Inject;
import io.xj.core.access.Access;
import io.xj.core.exception.CoreException;
import io.xj.core.model.ProgramSequenceBindingMeme;
import io.xj.core.persistence.SQLDatabaseProvider;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.UUID;

import static io.xj.core.Tables.PROGRAM_SEQUENCE_BINDING_MEME;

public class ProgramSequenceBindingMemeDAOImpl extends DAOImpl<ProgramSequenceBindingMeme> implements ProgramSequenceBindingMemeDAO {

  @Inject
  public ProgramSequenceBindingMemeDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public ProgramSequenceBindingMeme create(Access access, ProgramSequenceBindingMeme entity) throws CoreException {
    entity.validate();
    requireArtist(access);
    return DAO.modelFrom(ProgramSequenceBindingMeme.class,
      executeCreate(dbProvider.getDSL(), PROGRAM_SEQUENCE_BINDING_MEME, entity));

  }

  @Override
  @Nullable
  public ProgramSequenceBindingMeme readOne(Access access, UUID id) throws CoreException {
    requireArtist(access);
    return DAO.modelFrom(ProgramSequenceBindingMeme.class,
      dbProvider.getDSL().selectFrom(PROGRAM_SEQUENCE_BINDING_MEME)
        .where(PROGRAM_SEQUENCE_BINDING_MEME.ID.eq(id))
        .fetchOne());
  }

  @Override
  @Nullable
  public Collection<ProgramSequenceBindingMeme> readMany(Access access, Collection<UUID> parentIds) throws CoreException {
    requireArtist(access);
    return DAO.modelsFrom(ProgramSequenceBindingMeme.class,
      dbProvider.getDSL().selectFrom(PROGRAM_SEQUENCE_BINDING_MEME)
        .where(PROGRAM_SEQUENCE_BINDING_MEME.PROGRAM_SEQUENCE_BINDING_ID.in(parentIds))
        .fetch());
  }

  @Override
  public void update(Access access, UUID id, ProgramSequenceBindingMeme entity) throws CoreException {
    entity.validate();
    requireArtist(access);
    executeUpdate(dbProvider.getDSL(), PROGRAM_SEQUENCE_BINDING_MEME, id, entity);
  }

  @Override
  public void destroy(Access access, UUID id) throws CoreException {
    requireArtist(access);
    dbProvider.getDSL().deleteFrom(PROGRAM_SEQUENCE_BINDING_MEME)
      .where(PROGRAM_SEQUENCE_BINDING_MEME.ID.eq(id))
      .execute();
  }

  @Override
  public ProgramSequenceBindingMeme newInstance() {
    return new ProgramSequenceBindingMeme();
  }

}
