// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.dao;

import com.google.inject.Inject;
import io.xj.lib.rest_api.PayloadFactory;
import io.xj.lib.rest_api.RestApiException;
import io.xj.lib.util.ValueException;
import io.xj.service.hub.HubException;
import io.xj.service.hub.access.Access;
import io.xj.service.hub.model.InstrumentAudioEvent;
import io.xj.service.hub.persistence.SQLDatabaseProvider;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.UUID;

import static io.xj.service.hub.Tables.INSTRUMENT_AUDIO_EVENT;

public class InstrumentAudioEventDAOImpl extends DAOImpl<InstrumentAudioEvent> implements InstrumentAudioEventDAO {

  @Inject
  public InstrumentAudioEventDAOImpl(
    PayloadFactory payloadFactory,
    SQLDatabaseProvider dbProvider
  ) {
    super(payloadFactory);
    this.dbProvider = dbProvider;
  }

  @Override
  public InstrumentAudioEvent create(Access access, InstrumentAudioEvent entity) throws HubException, RestApiException, ValueException {
    entity.validate();
    requireArtist(access);
    return modelFrom(InstrumentAudioEvent.class,
      executeCreate(dbProvider.getDSL(), INSTRUMENT_AUDIO_EVENT, entity));

  }

  @Override
  @Nullable
  public InstrumentAudioEvent readOne(Access access, UUID id) throws HubException {
    requireArtist(access);
    return modelFrom(InstrumentAudioEvent.class,
      dbProvider.getDSL().selectFrom(INSTRUMENT_AUDIO_EVENT)
        .where(INSTRUMENT_AUDIO_EVENT.ID.eq(id))
        .fetchOne());
  }

  @Override
  @Nullable
  public Collection<InstrumentAudioEvent> readMany(Access access, Collection<UUID> parentIds) throws HubException {
    requireArtist(access);
    return modelsFrom(InstrumentAudioEvent.class,
      dbProvider.getDSL().selectFrom(INSTRUMENT_AUDIO_EVENT)
        .where(INSTRUMENT_AUDIO_EVENT.INSTRUMENT_AUDIO_ID.in(parentIds))
        .fetch());
  }

  @Override
  public void update(Access access, UUID id, InstrumentAudioEvent entity) throws HubException, RestApiException, ValueException {
    entity.validate();
    requireArtist(access);
    executeUpdate(dbProvider.getDSL(), INSTRUMENT_AUDIO_EVENT, id, entity);
  }

  @Override
  public void destroy(Access access, UUID id) throws HubException {
    requireArtist(access);
    dbProvider.getDSL().deleteFrom(INSTRUMENT_AUDIO_EVENT)
      .where(INSTRUMENT_AUDIO_EVENT.ID.eq(id))
      .execute();
  }

  @Override
  public InstrumentAudioEvent newInstance() {
    return new InstrumentAudioEvent();
  }

}
