// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.core.dao;

import com.google.inject.Inject;
import io.xj.lib.core.access.Access;
import io.xj.lib.core.exception.CoreException;
import io.xj.lib.core.model.ProgramVoiceTrack;
import io.xj.lib.core.persistence.SQLDatabaseProvider;
import org.jooq.DSLContext;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.UUID;

import static io.xj.lib.core.Tables.LIBRARY;
import static io.xj.lib.core.Tables.PROGRAM;
import static io.xj.lib.core.Tables.PROGRAM_SEQUENCE_PATTERN_EVENT;
import static io.xj.lib.core.Tables.PROGRAM_VOICE;
import static io.xj.lib.core.Tables.PROGRAM_VOICE_TRACK;

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
    DSLContext db = dbProvider.getDSL();
    requireProgramModification(db, access, entity.getProgramId());
    return DAO.modelFrom(ProgramVoiceTrack.class,
      executeCreate(db, PROGRAM_VOICE_TRACK, entity));
  }

  @Override
  @Nullable
  public ProgramVoiceTrack readOne(Access access, UUID id) throws CoreException {
    requireArtist(access);
    if (access.isTopLevel())
      return DAO.modelFrom(ProgramVoiceTrack.class,
        dbProvider.getDSL().selectFrom(PROGRAM_VOICE_TRACK)
          .where(PROGRAM_VOICE_TRACK.ID.eq(id))
          .fetchOne());
    else
      return DAO.modelFrom(ProgramVoiceTrack.class,
        dbProvider.getDSL().select(PROGRAM_VOICE_TRACK.fields()).from(PROGRAM_VOICE_TRACK)
          .join(PROGRAM_VOICE).on(PROGRAM_VOICE.ID.eq(PROGRAM_VOICE_TRACK.PROGRAM_VOICE_ID))
          .join(PROGRAM).on(PROGRAM.ID.eq(PROGRAM_VOICE.PROGRAM_ID))
          .join(LIBRARY).on(LIBRARY.ID.eq(PROGRAM.LIBRARY_ID))
          .where(PROGRAM_VOICE_TRACK.ID.eq(id))
          .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
          .fetchOne());

  }

  @Override
  @Nullable
  public Collection<ProgramVoiceTrack> readMany(Access access, Collection<UUID> parentIds) throws CoreException {
    requireArtist(access);
    if (access.isTopLevel())
      return DAO.modelsFrom(ProgramVoiceTrack.class,
        dbProvider.getDSL().selectFrom(PROGRAM_VOICE_TRACK)
          .where(PROGRAM_VOICE_TRACK.PROGRAM_VOICE_ID.in(parentIds))
          .fetch());
    else
      return DAO.modelsFrom(ProgramVoiceTrack.class,
        dbProvider.getDSL().select(PROGRAM_VOICE_TRACK.fields()).from(PROGRAM_VOICE_TRACK)
          .join(PROGRAM_VOICE).on(PROGRAM_VOICE.ID.eq(PROGRAM_VOICE_TRACK.PROGRAM_VOICE_ID))
          .join(PROGRAM).on(PROGRAM.ID.eq(PROGRAM_VOICE.PROGRAM_ID))
          .join(LIBRARY).on(LIBRARY.ID.eq(PROGRAM.LIBRARY_ID))
          .where(PROGRAM_VOICE_TRACK.PROGRAM_VOICE_ID.in(parentIds))
          .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
          .fetch());
  }

  @Override
  public void update(Access access, UUID id, ProgramVoiceTrack entity) throws CoreException {
    entity.validate();
    requireArtist(access);
    DSLContext db = dbProvider.getDSL();
    requireModification(db, access, id);
    executeUpdate(db, PROGRAM_VOICE_TRACK, id, entity);
  }

  @Override
  public void destroy(Access access, UUID id) throws CoreException {
    requireArtist(access);
    DSLContext db = dbProvider.getDSL();
    requireModification(db, access, id);
    requireNotExists("Events in Track", db.selectCount().from(PROGRAM_SEQUENCE_PATTERN_EVENT)
      .where(PROGRAM_SEQUENCE_PATTERN_EVENT.PROGRAM_VOICE_TRACK_ID.eq(id))
      .fetchOne(0, int.class));
    db.deleteFrom(PROGRAM_VOICE_TRACK)
      .where(PROGRAM_VOICE_TRACK.ID.eq(id))
      .execute();
  }

  @Override
  public ProgramVoiceTrack newInstance() {
    return new ProgramVoiceTrack();
  }

  /**
   Require modification access to an entity

   @param db     context
   @param access control
   @param id     of entity to read
   @throws CoreException if none exists or no access
   */
  private void requireModification(DSLContext db, Access access, UUID id) throws CoreException {
    if (access.isTopLevel())
      requireExists("Track",
        db.selectCount().from(PROGRAM_VOICE_TRACK)
          .where(PROGRAM_VOICE_TRACK.ID.eq(id))
          .fetchOne(0, int.class));
    else
      requireExists("Track in Voice in Program you have access to",
        db.selectCount().from(PROGRAM_VOICE_TRACK)
          .join(PROGRAM_VOICE).on(PROGRAM_VOICE.ID.eq(PROGRAM_VOICE_TRACK.PROGRAM_VOICE_ID))
          .join(PROGRAM).on(PROGRAM.ID.eq(PROGRAM_VOICE.PROGRAM_ID))
          .join(LIBRARY).on(LIBRARY.ID.eq(PROGRAM.LIBRARY_ID))
          .where(PROGRAM_VOICE_TRACK.ID.eq(id))
          .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
          .fetchOne(0, int.class));
  }
}
