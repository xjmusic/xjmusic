// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.dao;

import com.google.inject.Inject;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.PayloadFactory;
import io.xj.lib.jsonapi.JsonApiException;
import io.xj.lib.util.ValueException;
import io.xj.service.hub.access.HubAccess;
import io.xj.service.hub.entity.InstrumentAudioChord;
import io.xj.service.hub.persistence.HubDatabaseProvider;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.UUID;

import static io.xj.service.hub.Tables.INSTRUMENT_AUDIO_CHORD;

public class InstrumentAudioChordDAOImpl extends DAOImpl<InstrumentAudioChord> implements InstrumentAudioChordDAO {

  @Inject
  public InstrumentAudioChordDAOImpl(
    PayloadFactory payloadFactory,
    EntityFactory entityFactory,
    HubDatabaseProvider dbProvider
  ) {
    super(payloadFactory, entityFactory);
    this.dbProvider = dbProvider;
  }

  @Override
  public InstrumentAudioChord create(HubAccess hubAccess, InstrumentAudioChord entity) throws DAOException, JsonApiException, ValueException {
    entity.validate();
    requireArtist(hubAccess);
    return modelFrom(InstrumentAudioChord.class,
      executeCreate(dbProvider.getDSL(), INSTRUMENT_AUDIO_CHORD, entity));

  }

  @Override
  @Nullable
  public InstrumentAudioChord readOne(HubAccess hubAccess, UUID id) throws DAOException {
    requireArtist(hubAccess);
    return modelFrom(InstrumentAudioChord.class,
      dbProvider.getDSL().selectFrom(INSTRUMENT_AUDIO_CHORD)
        .where(INSTRUMENT_AUDIO_CHORD.ID.eq(id))
        .fetchOne());
  }

  @Override
  @Nullable
  public Collection<InstrumentAudioChord> readMany(HubAccess hubAccess, Collection<UUID> parentIds) throws DAOException {
    requireArtist(hubAccess);
    return modelsFrom(InstrumentAudioChord.class,
      dbProvider.getDSL().selectFrom(INSTRUMENT_AUDIO_CHORD)
        .where(INSTRUMENT_AUDIO_CHORD.INSTRUMENT_AUDIO_ID.in(parentIds))
        .fetch());
  }

  @Override
  public void update(HubAccess hubAccess, UUID id, InstrumentAudioChord entity) throws DAOException, JsonApiException, ValueException {
    entity.validate();
    requireArtist(hubAccess);
    executeUpdate(dbProvider.getDSL(), INSTRUMENT_AUDIO_CHORD, id, entity);
  }

  @Override
  public void destroy(HubAccess hubAccess, UUID id) throws DAOException {
    requireArtist(hubAccess);
    dbProvider.getDSL().deleteFrom(INSTRUMENT_AUDIO_CHORD)
      .where(INSTRUMENT_AUDIO_CHORD.ID.eq(id))
      .execute();
  }

  @Override
  public InstrumentAudioChord newInstance() {
    return new InstrumentAudioChord();
  }

}
