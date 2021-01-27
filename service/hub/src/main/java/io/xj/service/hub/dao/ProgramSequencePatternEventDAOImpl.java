// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.dao;

import com.google.inject.Inject;
import datadog.trace.api.Trace;
import io.xj.ProgramSequencePatternEvent;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.entity.common.EventEntity;
import io.xj.lib.jsonapi.JsonApiException;
import io.xj.lib.jsonapi.PayloadFactory;
import io.xj.lib.util.Value;
import io.xj.lib.util.ValueException;
import io.xj.service.hub.access.HubAccess;
import io.xj.service.hub.persistence.HubDatabaseProvider;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.UUID;

import static io.xj.service.hub.Tables.PROGRAM_SEQUENCE_PATTERN_EVENT;

public class ProgramSequencePatternEventDAOImpl extends DAOImpl<ProgramSequencePatternEvent> implements ProgramSequencePatternEventDAO {

  @Inject
  public ProgramSequencePatternEventDAOImpl(
    PayloadFactory payloadFactory,
    EntityFactory entityFactory,
    HubDatabaseProvider dbProvider
  ) {
    super(payloadFactory, entityFactory);
    this.dbProvider = dbProvider;
  }

  @Override
  public ProgramSequencePatternEvent create(HubAccess hubAccess, ProgramSequencePatternEvent entity) throws DAOException, JsonApiException, ValueException {
    var record = validate(entity.toBuilder()).build();
    requireArtist(hubAccess);
    return modelFrom(ProgramSequencePatternEvent.class,
      executeCreate(dbProvider.getDSL(), PROGRAM_SEQUENCE_PATTERN_EVENT, record));

  }

  @Override
  @Nullable
  public ProgramSequencePatternEvent readOne(HubAccess hubAccess, String id) throws DAOException {
    requireArtist(hubAccess);
    return modelFrom(ProgramSequencePatternEvent.class,
      dbProvider.getDSL().selectFrom(PROGRAM_SEQUENCE_PATTERN_EVENT)
        .where(PROGRAM_SEQUENCE_PATTERN_EVENT.ID.eq(UUID.fromString(id)))
        .fetchOne());
  }

  @Override
  @Nullable
  public Collection<ProgramSequencePatternEvent> readMany(HubAccess hubAccess, Collection<String> parentIds) throws DAOException {
    requireArtist(hubAccess);
    return modelsFrom(ProgramSequencePatternEvent.class,
      dbProvider.getDSL().selectFrom(PROGRAM_SEQUENCE_PATTERN_EVENT)
        .where(PROGRAM_SEQUENCE_PATTERN_EVENT.PROGRAM_SEQUENCE_PATTERN_ID.in(parentIds))
        .fetch());
  }

  @Override
  public void update(HubAccess hubAccess, String id, ProgramSequencePatternEvent entity) throws DAOException, JsonApiException, ValueException {
    var record = validate(entity.toBuilder()).build();
    requireArtist(hubAccess);
    executeUpdate(dbProvider.getDSL(), PROGRAM_SEQUENCE_PATTERN_EVENT, id, record);
  }

  @Override
  public void destroy(HubAccess hubAccess, String id) throws DAOException {
    requireArtist(hubAccess);
    dbProvider.getDSL().deleteFrom(PROGRAM_SEQUENCE_PATTERN_EVENT)
      .where(PROGRAM_SEQUENCE_PATTERN_EVENT.ID.eq(UUID.fromString(id)))
      .execute();
  }

  @Override
  public ProgramSequencePatternEvent newInstance() {
    return ProgramSequencePatternEvent.getDefaultInstance();
  }

  /**
   Validate data

   @param builder to validate
   @throws DAOException if invalid
   */
  public ProgramSequencePatternEvent.Builder validate(ProgramSequencePatternEvent.Builder builder) throws DAOException {
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
