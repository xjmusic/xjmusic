// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.manager;

import io.xj.hub.access.HubAccess;
import io.xj.hub.enums.InstrumentType;
import io.xj.hub.persistence.HubPersistenceServiceImpl;
import io.xj.hub.persistence.HubSqlStoreProvider;
import io.xj.hub.tables.pojos.ProgramVoice;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.JsonapiException;
import io.xj.lib.util.ValueException;
import io.xj.lib.util.ValueUtils;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;

import org.jetbrains.annotations.Nullable;
import java.util.Collection;
import java.util.UUID;

import static io.xj.hub.Tables.LIBRARY;
import static io.xj.hub.Tables.PROGRAM;
import static io.xj.hub.Tables.PROGRAM_SEQUENCE_CHORD_VOICING;
import static io.xj.hub.Tables.PROGRAM_SEQUENCE_PATTERN;
import static io.xj.hub.Tables.PROGRAM_SEQUENCE_PATTERN_EVENT;
import static io.xj.hub.Tables.PROGRAM_VOICE;
import static io.xj.hub.Tables.PROGRAM_VOICE_TRACK;

@Service
public class ProgramVoiceManagerImpl extends HubPersistenceServiceImpl implements ProgramVoiceManager {
  static final Float DEFAULT_ORDER_VALUE = 1000.0f;

  public ProgramVoiceManagerImpl(
    EntityFactory entityFactory,
    HubSqlStoreProvider sqlStoreProvider
  ) {
    super(entityFactory, sqlStoreProvider);
  }

  @Override
  public ProgramVoice create(HubAccess access, ProgramVoice entity) throws ManagerException, JsonapiException, ValueException {
    var record = validate(entity);
    DSLContext db = sqlStoreProvider.getDSL();
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
      try (
        var selectVoice = sqlStoreProvider.getDSL().selectFrom(PROGRAM_VOICE)
      ) {
        return modelFrom(ProgramVoice.class,
          selectVoice
            .where(PROGRAM_VOICE.ID.eq(id))
            .fetchOne());
      }
    else
      try (
        var selectVoice = sqlStoreProvider.getDSL().select(PROGRAM_VOICE.fields());
        var joinProgram = selectVoice.from(PROGRAM_VOICE).join(PROGRAM).on(PROGRAM.ID.eq(PROGRAM_VOICE.PROGRAM_ID));
        var joinLibrary = joinProgram.join(LIBRARY).on(LIBRARY.ID.eq(PROGRAM.LIBRARY_ID))
      ) {
        return modelFrom(ProgramVoice.class,
          joinLibrary
            .where(PROGRAM_VOICE.ID.eq(id))
            .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
            .fetchOne());
      }
  }

  @Override
  @Nullable
  public Collection<ProgramVoice> readMany(HubAccess access, Collection<UUID> programIds) throws ManagerException {
    requireArtist(access);
    if (access.isTopLevel())
      try (
        var selectVoice = sqlStoreProvider.getDSL().selectFrom(PROGRAM_VOICE)
      ) {
        return modelsFrom(ProgramVoice.class,
          selectVoice
            .where(PROGRAM_VOICE.PROGRAM_ID.in(programIds))
            .orderBy(PROGRAM_VOICE.ORDER.asc())
            .fetch());
      }
    else
      try (
        var selectVoice = sqlStoreProvider.getDSL().select(PROGRAM_VOICE.fields());
        var joinProgram = selectVoice.from(PROGRAM_VOICE).join(PROGRAM).on(PROGRAM.ID.eq(PROGRAM_VOICE.PROGRAM_ID));
        var joinLibrary = joinProgram.join(LIBRARY).on(LIBRARY.ID.eq(PROGRAM.LIBRARY_ID))
      ) {
        return modelsFrom(ProgramVoice.class,
          joinLibrary
            .where(PROGRAM_VOICE.PROGRAM_ID.in(programIds))
            .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
            .orderBy(PROGRAM_VOICE.ORDER.asc())
            .fetch());
      }
  }

  @Override
  public ProgramVoice update(HubAccess access, UUID id, ProgramVoice entity) throws ManagerException, JsonapiException, ValueException {
    var record = validate(entity);
    DSLContext db = sqlStoreProvider.getDSL();

    requireModification(db, access, id);

    executeUpdate(db, PROGRAM_VOICE, id, record);
    return record;
  }

  @Override
  public void destroy(HubAccess access, UUID id) throws ManagerException {
    DSLContext db = sqlStoreProvider.getDSL();

    requireModification(db, access, id);

    try (
      var deleteVoicing = db.deleteFrom(PROGRAM_SEQUENCE_CHORD_VOICING)
    ) {
      deleteVoicing
        .where(PROGRAM_SEQUENCE_CHORD_VOICING.PROGRAM_VOICE_ID.eq(id))
        .execute();
    }

    try (
      var deleteEvent = db.deleteFrom(PROGRAM_SEQUENCE_PATTERN_EVENT);
      var selectPattern = db.select(PROGRAM_SEQUENCE_PATTERN.ID)
    ) {
      deleteEvent
        .where(PROGRAM_SEQUENCE_PATTERN_EVENT.PROGRAM_SEQUENCE_PATTERN_ID.in(
          selectPattern
            .from(PROGRAM_SEQUENCE_PATTERN)
            .where(PROGRAM_SEQUENCE_PATTERN.PROGRAM_VOICE_ID.eq(id))))
        .execute();
    }

    try (
      var deletePattern = db.deleteFrom(PROGRAM_SEQUENCE_PATTERN)
    ) {
      deletePattern
        .where(PROGRAM_SEQUENCE_PATTERN.PROGRAM_VOICE_ID.eq(id))
        .execute();
    }

    try (
      var deleteTrack = db.deleteFrom(PROGRAM_VOICE_TRACK)
    ) {
      deleteTrack
        .where(PROGRAM_VOICE_TRACK.PROGRAM_VOICE_ID.eq(id))
        .execute();
    }

    try (
      var deleteVoice = db.deleteFrom(PROGRAM_VOICE)
    ) {
      deleteVoice
        .where(PROGRAM_VOICE.ID.eq(id))
        .execute();
    }
  }

  @Override
  public ProgramVoice newInstance() {
    return new ProgramVoice();
  }

  /**
   * Require permission to modify the specified program voice
   *
   * @param db     context
   * @param access control
   * @param id     of entity to require modification access to
   * @throws ManagerException on invalid permissions
   */
  void requireModification(DSLContext db, HubAccess access, UUID id) throws ManagerException {
    requireArtist(access);

    if (access.isTopLevel())
      try (var selectCount = db.selectCount()) {
        requireExists("Voice", selectCount.from(PROGRAM_VOICE)
          .where(PROGRAM_VOICE.ID.eq(id))
          .fetchOne(0, int.class));
      }
    else
      try (
        var selectCount = db.selectCount();
        var joinProgram = selectCount.from(PROGRAM_VOICE).join(PROGRAM).on(PROGRAM_VOICE.PROGRAM_ID.eq(PROGRAM.ID));
        var joinLibrary = joinProgram.join(LIBRARY).on(PROGRAM.LIBRARY_ID.eq(LIBRARY.ID))
      ) {
        requireExists("Voice in Program in Account you have access to",
          joinLibrary
            .where(PROGRAM_VOICE.ID.eq(id))
            .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
            .fetchOne(0, int.class));
      }
  }

  /**
   * Validate data
   *
   * @param record to validate
   * @throws ManagerException if invalid
   */
  public ProgramVoice validate(ProgramVoice record) throws ManagerException {
    try {
      if (ValueUtils.isEmpty(record.getOrder())) record.setOrder(DEFAULT_ORDER_VALUE);
      ValueUtils.require(record.getProgramId(), "Program ID");
      ValueUtils.require(record.getName(), "Name");
      ValueUtils.require(record.getType(), "Type");
      return record;

    } catch (ValueException e) {
      throw new ManagerException(e);
    }
  }

}
