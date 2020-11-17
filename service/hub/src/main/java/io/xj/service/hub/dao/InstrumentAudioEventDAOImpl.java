// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.dao;

import com.google.inject.Inject;
import io.xj.InstrumentAudioEvent;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.entity.common.EventEntity;
import io.xj.lib.jsonapi.JsonApiException;
import io.xj.lib.jsonapi.PayloadFactory;
import io.xj.lib.util.Text;
import io.xj.lib.util.Value;
import io.xj.lib.util.ValueException;
import io.xj.service.hub.access.HubAccess;
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
    InstrumentAudioEvent record = validate(entity.toBuilder()).build();
    requireArtist(hubAccess);
    return modelFrom(InstrumentAudioEvent.class,
      executeCreate(dbProvider.getDSL(), INSTRUMENT_AUDIO_EVENT, record));

  }

  @Override
  @Nullable
  public InstrumentAudioEvent readOne(HubAccess hubAccess, String id) throws DAOException {
    requireArtist(hubAccess);
    return modelFrom(InstrumentAudioEvent.class,
      dbProvider.getDSL().selectFrom(INSTRUMENT_AUDIO_EVENT)
        .where(INSTRUMENT_AUDIO_EVENT.ID.eq(UUID.fromString(id)))
        .fetchOne());
  }

  @Override
  @Nullable
  public Collection<InstrumentAudioEvent> readMany(HubAccess hubAccess, Collection<String> parentIds) throws DAOException {
    requireArtist(hubAccess);
    return modelsFrom(InstrumentAudioEvent.class,
      dbProvider.getDSL().selectFrom(INSTRUMENT_AUDIO_EVENT)
        .where(INSTRUMENT_AUDIO_EVENT.INSTRUMENT_AUDIO_ID.in(parentIds))
        .fetch());
  }

  @Override
  public void update(HubAccess hubAccess, String id, InstrumentAudioEvent entity) throws DAOException, JsonApiException, ValueException {
    InstrumentAudioEvent record = validate(entity.toBuilder()).build();
    requireArtist(hubAccess);
    executeUpdate(dbProvider.getDSL(), INSTRUMENT_AUDIO_EVENT, id, record);
  }

  @Override
  public void destroy(HubAccess hubAccess, String id) throws DAOException {
    requireArtist(hubAccess);
    dbProvider.getDSL().deleteFrom(INSTRUMENT_AUDIO_EVENT)
      .where(INSTRUMENT_AUDIO_EVENT.ID.eq(UUID.fromString(id)))
      .execute();
  }

  @Override
  public InstrumentAudioEvent newInstance() {
    return InstrumentAudioEvent.getDefaultInstance();
  }

  /**
   Validate data

   @param builder to validate
   @throws DAOException if invalid
   */
  public InstrumentAudioEvent.Builder validate(InstrumentAudioEvent.Builder builder) throws DAOException {
    try {
      Value.require(builder.getInstrumentId(), "Instrument ID");
      Value.require(builder.getInstrumentAudioId(), "Audio ID");
      builder.setName(Text.toUpperSlug(builder.getName()));
      EventEntity.validate(builder);
      return builder;

    } catch (ValueException e) {
      throw new DAOException(e);
    }
  }

}
