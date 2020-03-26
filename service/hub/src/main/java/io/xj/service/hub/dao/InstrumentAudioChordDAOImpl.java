// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.dao;

import com.google.inject.Inject;
import io.xj.lib.rest_api.PayloadFactory;
import io.xj.lib.rest_api.RestApiException;
import io.xj.lib.util.ValueException;
import io.xj.service.hub.HubException;
import io.xj.service.hub.access.Access;
import io.xj.service.hub.model.InstrumentAudioChord;
import io.xj.service.hub.persistence.SQLDatabaseProvider;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.UUID;

import static io.xj.service.hub.Tables.INSTRUMENT_AUDIO_CHORD;

public class InstrumentAudioChordDAOImpl extends DAOImpl<InstrumentAudioChord> implements InstrumentAudioChordDAO {

  @Inject
  public InstrumentAudioChordDAOImpl(
    PayloadFactory payloadFactory,
    SQLDatabaseProvider dbProvider
  ) {
    super(payloadFactory);
    this.dbProvider = dbProvider;
  }

  @Override
  public InstrumentAudioChord create(Access access, InstrumentAudioChord entity) throws HubException, RestApiException, ValueException {
    entity.validate();
    requireArtist(access);
    return modelFrom(InstrumentAudioChord.class,
      executeCreate(dbProvider.getDSL(), INSTRUMENT_AUDIO_CHORD, entity));

  }

  @Override
  @Nullable
  public InstrumentAudioChord readOne(Access access, UUID id) throws HubException {
    requireArtist(access);
    return modelFrom(InstrumentAudioChord.class,
      dbProvider.getDSL().selectFrom(INSTRUMENT_AUDIO_CHORD)
        .where(INSTRUMENT_AUDIO_CHORD.ID.eq(id))
        .fetchOne());
  }

  @Override
  @Nullable
  public Collection<InstrumentAudioChord> readMany(Access access, Collection<UUID> parentIds) throws HubException {
    requireArtist(access);
    return modelsFrom(InstrumentAudioChord.class,
      dbProvider.getDSL().selectFrom(INSTRUMENT_AUDIO_CHORD)
        .where(INSTRUMENT_AUDIO_CHORD.INSTRUMENT_AUDIO_ID.in(parentIds))
        .fetch());
  }

  @Override
  public void update(Access access, UUID id, InstrumentAudioChord entity) throws HubException, RestApiException, ValueException {
    entity.validate();
    requireArtist(access);
    executeUpdate(dbProvider.getDSL(), INSTRUMENT_AUDIO_CHORD, id, entity);
  }

  @Override
  public void destroy(Access access, UUID id) throws HubException {
    requireArtist(access);
    dbProvider.getDSL().deleteFrom(INSTRUMENT_AUDIO_CHORD)
      .where(INSTRUMENT_AUDIO_CHORD.ID.eq(id))
      .execute();
  }

  @Override
  public InstrumentAudioChord newInstance() {
    return new InstrumentAudioChord();
  }

}
