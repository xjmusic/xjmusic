// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.dao;

import com.google.inject.Inject;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.PayloadFactory;
import io.xj.lib.jsonapi.JsonApiException;
import io.xj.lib.util.ValueException;
import io.xj.service.hub.access.HubAccess;
import io.xj.service.hub.entity.InstrumentAudioEvent;
import io.xj.service.hub.persistence.HubDatabaseProvider;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.UUID;

import static io.xj.service.hub.Tables.INSTRUMENT_AUDIO_EVENT;

public class InstrumentAudioEventDAOImpl extends DAOImpl<InstrumentAudioEvent> implements InstrumentAudioEventDAO {

  @Inject
  public InstrumentAudioEventDAOImpl(
    PayloadFactory payloadFactory,
    EntityFactory entityFactory,
    HubDatabaseProvider dbProvider
  ) {
    super(payloadFactory, entityFactory);
    this.dbProvider = dbProvider;
  }

  @Override
  public InstrumentAudioEvent create(HubAccess hubAccess, InstrumentAudioEvent entity) throws DAOException, JsonApiException, ValueException {
    entity.validate();
    requireArtist(hubAccess);
    return modelFrom(InstrumentAudioEvent.class,
      executeCreate(dbProvider.getDSL(), INSTRUMENT_AUDIO_EVENT, entity));

  }

  @Override
  @Nullable
  public InstrumentAudioEvent readOne(HubAccess hubAccess, UUID id) throws DAOException {
    requireArtist(hubAccess);
    return modelFrom(InstrumentAudioEvent.class,
      dbProvider.getDSL().selectFrom(INSTRUMENT_AUDIO_EVENT)
        .where(INSTRUMENT_AUDIO_EVENT.ID.eq(id))
        .fetchOne());
  }

  @Override
  @Nullable
  public Collection<InstrumentAudioEvent> readMany(HubAccess hubAccess, Collection<UUID> parentIds) throws DAOException {
    requireArtist(hubAccess);
    return modelsFrom(InstrumentAudioEvent.class,
      dbProvider.getDSL().selectFrom(INSTRUMENT_AUDIO_EVENT)
        .where(INSTRUMENT_AUDIO_EVENT.INSTRUMENT_AUDIO_ID.in(parentIds))
        .fetch());
  }

  @Override
  public void update(HubAccess hubAccess, UUID id, InstrumentAudioEvent entity) throws DAOException, JsonApiException, ValueException {
    entity.validate();
    requireArtist(hubAccess);
    executeUpdate(dbProvider.getDSL(), INSTRUMENT_AUDIO_EVENT, id, entity);
  }

  @Override
  public void destroy(HubAccess hubAccess, UUID id) throws DAOException {
    requireArtist(hubAccess);
    dbProvider.getDSL().deleteFrom(INSTRUMENT_AUDIO_EVENT)
      .where(INSTRUMENT_AUDIO_EVENT.ID.eq(id))
      .execute();
  }

  @Override
  public InstrumentAudioEvent newInstance() {
    return new InstrumentAudioEvent();
  }

}
