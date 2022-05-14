// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.manager;

import com.google.inject.Inject;
import io.xj.hub.access.HubAccess;
import io.xj.hub.enums.InstrumentType;
import io.xj.hub.persistence.HubDatabaseProvider;
import io.xj.hub.persistence.HubPersistenceServiceImpl;
import io.xj.hub.tables.pojos.ProgramVoice;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.JsonapiException;
import io.xj.lib.util.ValueException;
import io.xj.lib.util.Values;
import org.jooq.DSLContext;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.UUID;

import static io.xj.hub.Tables.*;

public class ProgramVoiceManagerImpl extends HubPersistenceServiceImpl<ProgramVoice> implements ProgramVoiceManager {
  private static final Float DEFAULT_ORDER_VALUE = 1000.0f;

  @Inject
  public ProgramVoiceManagerImpl(
    EntityFactory entityFactory,
    HubDatabaseProvider dbProvider
  ) {
    super(entityFactory, dbProvider);
  }

  @Override
  public ProgramVoice create(HubAccess access, ProgramVoice entity) throws ManagerException, JsonapiException, ValueException {
    var record = validate(entity);
    DSLContext db = dbProvider.getDSL();
    requireProgramModification(db, access, record.getProgramId());
    return modelFrom(ProgramVoice.class, executeCreate(db, PROGRAM_VOICE, record));
  }

  @Override
  public ProgramVoice add(DSLContext db, UUID programId, InstrumentType type) throws ManagerException, JsonapiException, ValueException {
    ProgramVoice entity = new ProgramVoice();
    entity.setProgramId(programId);
    entity.setType(type);
    entity.setName(type.toString());
    return modelFrom(ProgramVoice.class, executeCreate(db, PROGRAM_VOICE, entity));
  }

  @Override
  @Nullable
  public ProgramVoice readOne(HubAccess access, UUID id) throws ManagerException {
    requireArtist(access);
    if (access.isTopLevel())
      return modelFrom(ProgramVoice.class,
        dbProvider.getDSL().selectFrom(PROGRAM_VOICE)
          .where(PROGRAM_VOICE.ID.eq(id))
          .fetchOne());
    else
      return modelFrom(ProgramVoice.class,
        dbProvider.getDSL().select(PROGRAM_VOICE.fields()).from(PROGRAM_VOICE)
          .join(PROGRAM).on(PROGRAM.ID.eq(PROGRAM_VOICE.PROGRAM_ID))
          .join(LIBRARY).on(LIBRARY.ID.eq(PROGRAM.LIBRARY_ID))
          .where(PROGRAM_VOICE.ID.eq(id))
          .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
          .fetchOne());
  }

  @Override
  @Nullable
  public Collection<ProgramVoice> readMany(HubAccess access, Collection<UUID> programIds) throws ManagerException {
    requireArtist(access);
    if (access.isTopLevel())
      return modelsFrom(ProgramVoice.class,
        dbProvider.getDSL().selectFrom(PROGRAM_VOICE)
          .where(PROGRAM_VOICE.PROGRAM_ID.in(programIds))
          .orderBy(PROGRAM_VOICE.ORDER.asc())
          .fetch());
    else
      return modelsFrom(ProgramVoice.class,
        dbProvider.getDSL().select(PROGRAM_VOICE.fields()).from(PROGRAM_VOICE)
          .join(PROGRAM).on(PROGRAM.ID.eq(PROGRAM_VOICE.PROGRAM_ID))
          .join(LIBRARY).on(LIBRARY.ID.eq(PROGRAM.LIBRARY_ID))
          .where(PROGRAM_VOICE.PROGRAM_ID.in(programIds))
          .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
          .orderBy(PROGRAM_VOICE.ORDER.asc())
          .fetch());
  }

  @Override
  public ProgramVoice update(HubAccess access, UUID id, ProgramVoice entity) throws ManagerException, JsonapiException, ValueException {
    var record = validate(entity);
    DSLContext db = dbProvider.getDSL();

    requireModification(db, access, id);

    executeUpdate(db, PROGRAM_VOICE, id, record);
    return record;
  }

  @Override
  public void destroy(HubAccess access, UUID id) throws ManagerException {
    DSLContext db = dbProvider.getDSL();

    requireModification(db, access, id);

    db.deleteFrom(PROGRAM_SEQUENCE_CHORD_VOICING)
      .where(PROGRAM_SEQUENCE_CHORD_VOICING.PROGRAM_VOICE_ID.eq(id))
      .execute();

    db.deleteFrom(PROGRAM_SEQUENCE_PATTERN_EVENT)
      .where(PROGRAM_SEQUENCE_PATTERN_EVENT.PROGRAM_SEQUENCE_PATTERN_ID.in(
        db.select(PROGRAM_SEQUENCE_PATTERN.ID)
          .from(PROGRAM_SEQUENCE_PATTERN)
          .where(PROGRAM_SEQUENCE_PATTERN.PROGRAM_VOICE_ID.eq(id))))
      .execute();

    db.deleteFrom(PROGRAM_SEQUENCE_PATTERN)
      .where(PROGRAM_SEQUENCE_PATTERN.PROGRAM_VOICE_ID.eq(id))
      .execute();

    db.deleteFrom(PROGRAM_VOICE_TRACK)
      .where(PROGRAM_VOICE_TRACK.PROGRAM_VOICE_ID.eq(id))
      .execute();

    db.deleteFrom(PROGRAM_VOICE)
      .where(PROGRAM_VOICE.ID.eq(id))
      .execute();
  }

  @Override
  public ProgramVoice newInstance() {
    return new ProgramVoice();
  }

  /**
   Require permission to modify the specified program voice

   @param db        context
   @param access control
   @param id        of entity to require modification access to
   @throws ManagerException on invalid permissions
   */
  private void requireModification(DSLContext db, HubAccess access, UUID id) throws ManagerException {
    requireArtist(access);

    if (access.isTopLevel())
      requireExists("Voice", db.selectCount().from(PROGRAM_VOICE)
        .where(PROGRAM_VOICE.ID.eq(id))
        .fetchOne(0, int.class));
    else
      requireExists("Voice in Program in Account you have access to", db.selectCount().from(PROGRAM_VOICE)
        .join(PROGRAM).on(PROGRAM_VOICE.PROGRAM_ID.eq(PROGRAM.ID))
        .join(LIBRARY).on(PROGRAM.LIBRARY_ID.eq(LIBRARY.ID))
        .where(PROGRAM_VOICE.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne(0, int.class));
  }

  /**
   Validate data

   @param record to validate
   @throws ManagerException if invalid
   */
  public ProgramVoice validate(ProgramVoice record) throws ManagerException {
    try {
      if (Values.isEmpty(record.getOrder())) record.setOrder(DEFAULT_ORDER_VALUE);
      Values.require(record.getProgramId(), "Program ID");
      Values.require(record.getName(), "Name");
      Values.require(record.getType(), "Type");
      return record;

    } catch (ValueException e) {
      throw new ManagerException(e);
    }
  }

}
