// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import com.google.inject.Inject;
import io.xj.core.access.Access;
import io.xj.core.exception.CoreException;
import io.xj.core.model.ProgramVoiceTrack;
import io.xj.core.persistence.SQLDatabaseProvider;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.UUID;

import static io.xj.core.Tables.PROGRAM_VOICE_TRACK;

public class ProgramVoiceTrackDAOImpl extends DAOImpl<ProgramVoiceTrack> implements ProgramVoiceTrackDAO {

  @Inject
  public ProgramVoiceTrackDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public ProgramVoiceTrack create(Access access, ProgramVoiceTrack entity) throws CoreException {
    entity.validate();
    requireArtist(access);
    return DAO.modelFrom(ProgramVoiceTrack.class,
      executeCreate(dbProvider.getDSL(), PROGRAM_VOICE_TRACK, entity));
  }

  @Override
  @Nullable
  public ProgramVoiceTrack readOne(Access access, UUID id) throws CoreException {
    requireArtist(access);
    return DAO.modelFrom(ProgramVoiceTrack.class,
      dbProvider.getDSL().selectFrom(PROGRAM_VOICE_TRACK)
        .where(PROGRAM_VOICE_TRACK.ID.eq(id))
        .fetchOne());
  }

  @Override
  @Nullable
  public Collection<ProgramVoiceTrack> readMany(Access access, Collection<UUID> parentIds) throws CoreException {
    requireArtist(access);
    return DAO.modelsFrom(ProgramVoiceTrack.class,
      dbProvider.getDSL().selectFrom(PROGRAM_VOICE_TRACK)
        .where(PROGRAM_VOICE_TRACK.PROGRAM_VOICE_ID.in(parentIds))
        .fetch());
  }

  @Override
  public void update(Access access, UUID id, ProgramVoiceTrack entity) throws CoreException {
    entity.validate();
    requireArtist(access);
    executeUpdate(dbProvider.getDSL(), PROGRAM_VOICE_TRACK, id, entity);
  }

  @Override
  public void destroy(Access access, UUID id) throws CoreException {
    requireArtist(access);
    dbProvider.getDSL().deleteFrom(PROGRAM_VOICE_TRACK)
      .where(PROGRAM_VOICE_TRACK.ID.eq(id))
      .execute();
  }

  @Override
  public ProgramVoiceTrack newInstance() {
    return new ProgramVoiceTrack();
  }

}
