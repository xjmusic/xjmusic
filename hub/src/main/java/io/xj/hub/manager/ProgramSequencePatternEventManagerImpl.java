// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.manager;

import com.google.inject.Inject;
import io.xj.hub.access.HubAccess;
import io.xj.hub.persistence.HubDatabaseProvider;
import io.xj.hub.persistence.HubPersistenceServiceImpl;
import io.xj.hub.tables.pojos.ProgramSequencePatternEvent;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.entity.common.EventEntity;
import io.xj.lib.jsonapi.JsonapiException;
import io.xj.lib.util.ValueException;
import io.xj.lib.util.Values;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.UUID;

import static io.xj.hub.Tables.PROGRAM_SEQUENCE_PATTERN_EVENT;

public class ProgramSequencePatternEventManagerImpl extends HubPersistenceServiceImpl<ProgramSequencePatternEvent> implements ProgramSequencePatternEventManager {

  @Inject
  public ProgramSequencePatternEventManagerImpl(
    EntityFactory entityFactory,
    HubDatabaseProvider dbProvider
  ) {
    super(entityFactory, dbProvider);
  }

  @Override
  public ProgramSequencePatternEvent create(HubAccess access, ProgramSequencePatternEvent entity) throws ManagerException, JsonapiException, ValueException {
    var record = validate(entity);
    requireArtist(access);
    return modelFrom(ProgramSequencePatternEvent.class,
      executeCreate(dbProvider.getDSL(), PROGRAM_SEQUENCE_PATTERN_EVENT, record));

  }

  @Override
  @Nullable
  public ProgramSequencePatternEvent readOne(HubAccess access, UUID id) throws ManagerException {
    requireArtist(access);
    return modelFrom(ProgramSequencePatternEvent.class,
      dbProvider.getDSL().selectFrom(PROGRAM_SEQUENCE_PATTERN_EVENT)
        .where(PROGRAM_SEQUENCE_PATTERN_EVENT.ID.eq(id))
        .fetchOne());
  }

  @Override
  @Nullable
  public Collection<ProgramSequencePatternEvent> readMany(HubAccess access, Collection<UUID> parentIds) throws ManagerException {
    requireArtist(access);
    return modelsFrom(ProgramSequencePatternEvent.class,
      dbProvider.getDSL().selectFrom(PROGRAM_SEQUENCE_PATTERN_EVENT)
        .where(PROGRAM_SEQUENCE_PATTERN_EVENT.PROGRAM_SEQUENCE_PATTERN_ID.in(parentIds))
        .fetch());
  }

  @Override
  public ProgramSequencePatternEvent update(HubAccess access, UUID id, ProgramSequencePatternEvent entity) throws ManagerException, JsonapiException, ValueException {
    var record = validate(entity);
    requireArtist(access);
    executeUpdate(dbProvider.getDSL(), PROGRAM_SEQUENCE_PATTERN_EVENT, id, record);
    return record;
  }

  @Override
  public void destroy(HubAccess access, UUID id) throws ManagerException {
    requireArtist(access);
    dbProvider.getDSL().deleteFrom(PROGRAM_SEQUENCE_PATTERN_EVENT)
      .where(PROGRAM_SEQUENCE_PATTERN_EVENT.ID.eq(id))
      .execute();
  }

  @Override
  public ProgramSequencePatternEvent newInstance() {
    return new ProgramSequencePatternEvent();
  }

  /**
   Validate data

   @param builder to validate
   @throws ManagerException if invalid
   */
  public ProgramSequencePatternEvent validate(ProgramSequencePatternEvent builder) throws ManagerException {
    try {
      Values.require(builder.getProgramId(), "Program ID");
      Values.require(builder.getProgramSequencePatternId(), "Pattern ID");
      Values.require(builder.getProgramVoiceTrackId(), "Track ID");
      EventEntity.validate(builder);
      return builder;

    } catch (ValueException e) {
      throw new ManagerException(e);
    }
  }


}
