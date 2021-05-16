// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.dao;

import com.google.inject.Inject;
import io.xj.ProgramVoiceTrack;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.JsonApiException;
import io.xj.lib.jsonapi.PayloadFactory;
import io.xj.lib.util.Text;
import io.xj.lib.util.Value;
import io.xj.lib.util.ValueException;
import io.xj.service.hub.access.HubAccess;
import io.xj.service.hub.persistence.HubDatabaseProvider;
import org.jooq.DSLContext;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.UUID;

import static io.xj.service.hub.Tables.LIBRARY;
import static io.xj.service.hub.Tables.PROGRAM;
import static io.xj.service.hub.Tables.PROGRAM_SEQUENCE_PATTERN_EVENT;
import static io.xj.service.hub.Tables.PROGRAM_VOICE;
import static io.xj.service.hub.Tables.PROGRAM_VOICE_TRACK;

public class ProgramVoiceTrackDAOImpl extends DAOImpl<ProgramVoiceTrack> implements ProgramVoiceTrackDAO {
  private static final Float DEFAULT_ORDER_VALUE = 1000.0f;

  @Inject
  public ProgramVoiceTrackDAOImpl(
    PayloadFactory payloadFactory,
    EntityFactory entityFactory,
    HubDatabaseProvider dbProvider
  ) {
    super(payloadFactory, entityFactory);
    this.dbProvider = dbProvider;
  }

  @Override
  public ProgramVoiceTrack create(HubAccess hubAccess, ProgramVoiceTrack entity) throws DAOException, JsonApiException, ValueException {
    var record = validate(entity.toBuilder()).build();
    requireArtist(hubAccess);
    DSLContext db = dbProvider.getDSL();
    requireProgramModification(db, hubAccess, record.getProgramId());
    return modelFrom(ProgramVoiceTrack.class,
      executeCreate(db, PROGRAM_VOICE_TRACK, record));
  }

  @Override
  @Nullable
  public ProgramVoiceTrack readOne(HubAccess hubAccess, String id) throws DAOException {
    requireArtist(hubAccess);
    if (hubAccess.isTopLevel())
      return modelFrom(ProgramVoiceTrack.class,
        dbProvider.getDSL().selectFrom(PROGRAM_VOICE_TRACK)
          .where(PROGRAM_VOICE_TRACK.ID.eq(UUID.fromString(id)))
          .fetchOne());
    else
      return modelFrom(ProgramVoiceTrack.class,
        dbProvider.getDSL().select(PROGRAM_VOICE_TRACK.fields()).from(PROGRAM_VOICE_TRACK)
          .join(PROGRAM_VOICE).on(PROGRAM_VOICE.ID.eq(PROGRAM_VOICE_TRACK.PROGRAM_VOICE_ID))
          .join(PROGRAM).on(PROGRAM.ID.eq(PROGRAM_VOICE.PROGRAM_ID))
          .join(LIBRARY).on(LIBRARY.ID.eq(PROGRAM.LIBRARY_ID))
          .where(PROGRAM_VOICE_TRACK.ID.eq(UUID.fromString(id)))
          .and(LIBRARY.ACCOUNT_ID.in(hubAccess.getAccountIds()))
          .fetchOne());
  }

  @Override
  @Nullable
  public Collection<ProgramVoiceTrack> readMany(HubAccess hubAccess, Collection<String> parentIds) throws DAOException {
    requireArtist(hubAccess);
    if (hubAccess.isTopLevel())
      return modelsFrom(ProgramVoiceTrack.class,
        dbProvider.getDSL().selectFrom(PROGRAM_VOICE_TRACK)
          .where(PROGRAM_VOICE_TRACK.PROGRAM_VOICE_ID.in(parentIds))
          .orderBy(PROGRAM_VOICE_TRACK.ORDER.asc())
          .fetch());
    else
      return modelsFrom(ProgramVoiceTrack.class,
        dbProvider.getDSL().select(PROGRAM_VOICE_TRACK.fields()).from(PROGRAM_VOICE_TRACK)
          .join(PROGRAM_VOICE).on(PROGRAM_VOICE.ID.eq(PROGRAM_VOICE_TRACK.PROGRAM_VOICE_ID))
          .join(PROGRAM).on(PROGRAM.ID.eq(PROGRAM_VOICE.PROGRAM_ID))
          .join(LIBRARY).on(LIBRARY.ID.eq(PROGRAM.LIBRARY_ID))
          .where(PROGRAM_VOICE_TRACK.PROGRAM_VOICE_ID.in(parentIds))
          .and(LIBRARY.ACCOUNT_ID.in(hubAccess.getAccountIds()))
          .orderBy(PROGRAM_VOICE_TRACK.ORDER.asc())
          .fetch());
  }

  @Override
  public void update(HubAccess hubAccess, String id, ProgramVoiceTrack entity) throws DAOException, JsonApiException, ValueException {
    var record = validate(entity.toBuilder()).build();
    requireArtist(hubAccess);
    DSLContext db = dbProvider.getDSL();
    requireModification(db, hubAccess, id);
    executeUpdate(db, PROGRAM_VOICE_TRACK, id, record);
  }

  @Override
  public void destroy(HubAccess hubAccess, String id) throws DAOException {
    requireArtist(hubAccess);
    DSLContext db = dbProvider.getDSL();
    requireModification(db, hubAccess, id);
    requireNotExists("Events in Track", db.selectCount().from(PROGRAM_SEQUENCE_PATTERN_EVENT)
      .where(PROGRAM_SEQUENCE_PATTERN_EVENT.PROGRAM_VOICE_TRACK_ID.eq(UUID.fromString(id)))
      .fetchOne(0, int.class));
    db.deleteFrom(PROGRAM_VOICE_TRACK)
      .where(PROGRAM_VOICE_TRACK.ID.eq(UUID.fromString(id)))
      .execute();
  }

  @Override
  public ProgramVoiceTrack newInstance() {
    return ProgramVoiceTrack.getDefaultInstance();
  }

  /**
   Require modification hubAccess to an entity

   @param db        context
   @param hubAccess control
   @param id        of entity to read
   @throws DAOException if none exists or no hubAccess
   */
  private void requireModification(DSLContext db, HubAccess hubAccess, String id) throws DAOException {
    if (hubAccess.isTopLevel())
      requireExists("Track",
        db.selectCount().from(PROGRAM_VOICE_TRACK)
          .where(PROGRAM_VOICE_TRACK.ID.eq(UUID.fromString(id)))
          .fetchOne(0, int.class));
    else
      requireExists("Track in Voice in Program you have hubAccess to",
        db.selectCount().from(PROGRAM_VOICE_TRACK)
          .join(PROGRAM_VOICE).on(PROGRAM_VOICE.ID.eq(PROGRAM_VOICE_TRACK.PROGRAM_VOICE_ID))
          .join(PROGRAM).on(PROGRAM.ID.eq(PROGRAM_VOICE.PROGRAM_ID))
          .join(LIBRARY).on(LIBRARY.ID.eq(PROGRAM.LIBRARY_ID))
          .where(PROGRAM_VOICE_TRACK.ID.eq(UUID.fromString(id)))
          .and(LIBRARY.ACCOUNT_ID.in(hubAccess.getAccountIds()))
          .fetchOne(0, int.class));
  }

  /**
   Validate data

   @param builder to validate
   @throws DAOException if invalid
   */
  public ProgramVoiceTrack.Builder validate(ProgramVoiceTrack.Builder builder) throws DAOException {
    try {
      if (Value.isEmpty(builder.getOrder())) builder.setOrder(DEFAULT_ORDER_VALUE);
      Value.require(builder.getProgramId(), "Program ID");
      Value.require(builder.getProgramVoiceId(), "Voice ID");
      Value.require(builder.getName(), "Name");
      builder.setName(Text.toMeme(builder.getName()));
      return builder;

    } catch (ValueException e) {
      throw new DAOException(e);
    }
  }

}
