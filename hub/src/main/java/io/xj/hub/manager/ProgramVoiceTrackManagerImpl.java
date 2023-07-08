// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.manager;

import io.xj.hub.access.HubAccess;
import io.xj.hub.persistence.HubSqlStoreProvider;
import io.xj.hub.persistence.HubPersistenceServiceImpl;
import io.xj.hub.tables.pojos.ProgramVoiceTrack;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.JsonapiException;
import io.xj.lib.util.Text;
import io.xj.lib.util.ValueException;
import io.xj.lib.util.Values;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.UUID;

import static io.xj.hub.Tables.*;

@Service
public class ProgramVoiceTrackManagerImpl extends HubPersistenceServiceImpl implements ProgramVoiceTrackManager {
  static final float DEFAULT_ORDER_VALUE = 1000.0f;

  public ProgramVoiceTrackManagerImpl(
    EntityFactory entityFactory,
    HubSqlStoreProvider sqlStoreProvider
  ) {
    super(entityFactory, sqlStoreProvider);
  }

  @Override
  public ProgramVoiceTrack create(HubAccess access, ProgramVoiceTrack entity) throws ManagerException, JsonapiException, ValueException {
    var record = validate(entity);
    requireArtist(access);
    DSLContext db = sqlStoreProvider.getDSL();
    requireProgramModification(db, access, record.getProgramId());
    return modelFrom(ProgramVoiceTrack.class,
      executeCreate(db, PROGRAM_VOICE_TRACK, record));
  }

  @Override
  @Nullable
  public ProgramVoiceTrack readOne(HubAccess access, UUID id) throws ManagerException {
    requireArtist(access);
    if (access.isTopLevel())
      return modelFrom(ProgramVoiceTrack.class,
        sqlStoreProvider.getDSL().selectFrom(PROGRAM_VOICE_TRACK)
          .where(PROGRAM_VOICE_TRACK.ID.eq(id))
          .fetchOne());
    else
      return modelFrom(ProgramVoiceTrack.class,
        sqlStoreProvider.getDSL().select(PROGRAM_VOICE_TRACK.fields()).from(PROGRAM_VOICE_TRACK)
          .join(PROGRAM_VOICE).on(PROGRAM_VOICE.ID.eq(PROGRAM_VOICE_TRACK.PROGRAM_VOICE_ID))
          .join(PROGRAM).on(PROGRAM.ID.eq(PROGRAM_VOICE.PROGRAM_ID))
          .join(LIBRARY).on(LIBRARY.ID.eq(PROGRAM.LIBRARY_ID))
          .where(PROGRAM_VOICE_TRACK.ID.eq(id))
          .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
          .fetchOne());
  }

  @Override
  @Nullable
  public Collection<ProgramVoiceTrack> readMany(HubAccess access, Collection<UUID> parentIds) throws ManagerException {
    requireArtist(access);
    if (access.isTopLevel())
      return modelsFrom(ProgramVoiceTrack.class,
        sqlStoreProvider.getDSL().selectFrom(PROGRAM_VOICE_TRACK)
          .where(PROGRAM_VOICE_TRACK.PROGRAM_VOICE_ID.in(parentIds))
          .orderBy(PROGRAM_VOICE_TRACK.ORDER.asc())
          .fetch());
    else
      return modelsFrom(ProgramVoiceTrack.class,
        sqlStoreProvider.getDSL().select(PROGRAM_VOICE_TRACK.fields()).from(PROGRAM_VOICE_TRACK)
          .join(PROGRAM_VOICE).on(PROGRAM_VOICE.ID.eq(PROGRAM_VOICE_TRACK.PROGRAM_VOICE_ID))
          .join(PROGRAM).on(PROGRAM.ID.eq(PROGRAM_VOICE.PROGRAM_ID))
          .join(LIBRARY).on(LIBRARY.ID.eq(PROGRAM.LIBRARY_ID))
          .where(PROGRAM_VOICE_TRACK.PROGRAM_VOICE_ID.in(parentIds))
          .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
          .orderBy(PROGRAM_VOICE_TRACK.ORDER.asc())
          .fetch());
  }

  @Override
  public ProgramVoiceTrack update(HubAccess access, UUID id, ProgramVoiceTrack entity) throws ManagerException, JsonapiException, ValueException {
    var record = validate(entity);
    requireArtist(access);
    DSLContext db = sqlStoreProvider.getDSL();
    requireModification(db, access, id);
    executeUpdate(db, PROGRAM_VOICE_TRACK, id, record);
    return record;
  }

  @Override
  public void destroy(HubAccess access, UUID id) throws ManagerException {
    requireArtist(access);
    DSLContext db = sqlStoreProvider.getDSL();
    requireModification(db, access, id);

    db.deleteFrom(PROGRAM_SEQUENCE_PATTERN_EVENT)
      .where(PROGRAM_SEQUENCE_PATTERN_EVENT.PROGRAM_VOICE_TRACK_ID.eq(id))
      .execute();

    db.deleteFrom(PROGRAM_VOICE_TRACK)
      .where(PROGRAM_VOICE_TRACK.ID.eq(id))
      .execute();
  }

  @Override
  public ProgramVoiceTrack newInstance() {
    return new ProgramVoiceTrack();
  }

  /**
   * Require modification access to an entity
   *
   * @param db     context
   * @param access control
   * @param id     of entity to read
   * @throws ManagerException if none exists or no access
   */
  void requireModification(DSLContext db, HubAccess access, UUID id) throws ManagerException {
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

  /**
   * Validate data
   *
   * @param track to validate
   * @throws ManagerException if invalid
   */
  public ProgramVoiceTrack validate(ProgramVoiceTrack track) throws ManagerException {
    try {
      if (Values.isEmpty(track.getOrder())) track.setOrder(DEFAULT_ORDER_VALUE);
      Values.require(track.getProgramId(), "Program ID");
      Values.require(track.getProgramVoiceId(), "Voice ID");
      Values.require(track.getName(), "Name");
      track.setName(Text.toEvent(track.getName()));
      return track;

    } catch (ValueException e) {
      throw new ManagerException(e);
    }
  }

}
