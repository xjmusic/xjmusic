// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import com.google.inject.Inject;
import io.xj.core.access.Access;
import io.xj.core.exception.CoreException;
import io.xj.core.model.ProgramVoice;
import io.xj.core.persistence.SQLDatabaseProvider;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.UUID;

import static io.xj.core.Tables.PROGRAM_VOICE;

public class ProgramVoiceDAOImpl extends DAOImpl<ProgramVoice> implements ProgramVoiceDAO {

  @Inject
  public ProgramVoiceDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public ProgramVoice create(Access access, ProgramVoice entity) throws CoreException {
    entity.validate();
    requireTopLevel(access);
    return DAO.modelFrom(ProgramVoice.class,
      executeCreate(PROGRAM_VOICE, entity));
  }

  @Override
  @Nullable
  public ProgramVoice readOne(Access access, UUID id) throws CoreException {
    requireUser(access);
    return DAO.modelFrom(ProgramVoice.class,
      dbProvider.getDSL().selectFrom(PROGRAM_VOICE)
        .where(PROGRAM_VOICE.ID.eq(id))
        .fetchOne());
  }

  @Override
  @Nullable
  public Collection<ProgramVoice> readMany(Access access, Collection<UUID> parentIds) throws CoreException {
    requireUser(access);
    return DAO.modelsFrom(ProgramVoice.class,
      dbProvider.getDSL().selectFrom(PROGRAM_VOICE)
        .where(PROGRAM_VOICE.PROGRAM_ID.in(parentIds))
        .fetch());
  }

  @Override
  public void update(Access access, UUID id, ProgramVoice entity) throws CoreException {
    entity.validate();
    requireTopLevel(access);
    executeUpdate(PROGRAM_VOICE, id, entity);
  }

  @Override
  public void destroy(Access access, UUID id) throws CoreException {
    requireLibrary(access);
    dbProvider.getDSL().deleteFrom(PROGRAM_VOICE)
      .where(PROGRAM_VOICE.ID.eq(id))
      .execute();
  }

  @Override
  public ProgramVoice newInstance() {
    return new ProgramVoice();
  }

}
