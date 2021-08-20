// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.dao;

import com.google.inject.Inject;
import io.xj.api.ProgramSequencePatternEvent;
import io.xj.hub.access.HubAccess;
import io.xj.hub.persistence.HubDatabaseProvider;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.entity.common.EventEntity;
import io.xj.lib.jsonapi.JsonapiException;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.util.Value;
import io.xj.lib.util.ValueException;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.UUID;

import static io.xj.hub.Tables.PROGRAM_SEQUENCE_PATTERN_EVENT;

public class ProgramSequencePatternEventDAOImpl extends DAOImpl<ProgramSequencePatternEvent> implements ProgramSequencePatternEventDAO {

  @Inject
  public ProgramSequencePatternEventDAOImpl(
    JsonapiPayloadFactory payloadFactory,
    EntityFactory entityFactory,
    HubDatabaseProvider dbProvider
  ) {
    super(payloadFactory, entityFactory);
    this.dbProvider = dbProvider;
  }

  @Override
  public ProgramSequencePatternEvent create(HubAccess hubAccess, ProgramSequencePatternEvent entity) throws DAOException, JsonapiException, ValueException {
    var record = validate(entity);
    requireArtist(hubAccess);
    return modelFrom(ProgramSequencePatternEvent.class,
      executeCreate(dbProvider.getDSL(), PROGRAM_SEQUENCE_PATTERN_EVENT, record));

  }

  @Override
  @Nullable
  public ProgramSequencePatternEvent readOne(HubAccess hubAccess, UUID id) throws DAOException {
    requireArtist(hubAccess);
    return modelFrom(ProgramSequencePatternEvent.class,
      dbProvider.getDSL().selectFrom(PROGRAM_SEQUENCE_PATTERN_EVENT)
        .where(PROGRAM_SEQUENCE_PATTERN_EVENT.ID.eq(id))
        .fetchOne());
  }

  @Override
  @Nullable
  public Collection<ProgramSequencePatternEvent> readMany(HubAccess hubAccess, Collection<UUID> parentIds) throws DAOException {
    requireArtist(hubAccess);
    return modelsFrom(ProgramSequencePatternEvent.class,
      dbProvider.getDSL().selectFrom(PROGRAM_SEQUENCE_PATTERN_EVENT)
        .where(PROGRAM_SEQUENCE_PATTERN_EVENT.PROGRAM_SEQUENCE_PATTERN_ID.in(parentIds))
        .fetch());
  }

  @Override
  public ProgramSequencePatternEvent update(HubAccess hubAccess, UUID id, ProgramSequencePatternEvent entity) throws DAOException, JsonapiException, ValueException {
    var record = validate(entity);
    requireArtist(hubAccess);
    executeUpdate(dbProvider.getDSL(), PROGRAM_SEQUENCE_PATTERN_EVENT, id, record);
    return record;
  }

  @Override
  public void destroy(HubAccess hubAccess, UUID id) throws DAOException {
    requireArtist(hubAccess);
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
   @throws DAOException if invalid
   */
  public ProgramSequencePatternEvent validate(ProgramSequencePatternEvent builder) throws DAOException {
    try {
      Value.require(builder.getProgramId(), "Program ID");
      Value.require(builder.getProgramSequencePatternId(), "Pattern ID");
      Value.require(builder.getProgramVoiceTrackId(), "Track ID");
      EventEntity.validate(builder);
      return builder;

    } catch (ValueException e) {
      throw new DAOException(e);
    }
  }


}
