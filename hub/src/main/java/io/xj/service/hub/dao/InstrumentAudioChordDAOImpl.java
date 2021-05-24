// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.dao;

import com.google.inject.Inject;
import io.xj.InstrumentAudioChord;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.entity.common.ChordEntity;
import io.xj.lib.jsonapi.JsonApiException;
import io.xj.lib.jsonapi.PayloadFactory;
import io.xj.lib.util.Value;
import io.xj.lib.util.ValueException;
import io.xj.service.hub.access.HubAccess;
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
    validate(entity);
    requireArtist(hubAccess);
    return modelFrom(InstrumentAudioChord.class,
      executeCreate(dbProvider.getDSL(), INSTRUMENT_AUDIO_CHORD, entity));

  }

  @Override
  @Nullable
  public InstrumentAudioChord readOne(HubAccess hubAccess, String id) throws DAOException {
    requireArtist(hubAccess);
    return modelFrom(InstrumentAudioChord.class,
      dbProvider.getDSL().selectFrom(INSTRUMENT_AUDIO_CHORD)
        .where(INSTRUMENT_AUDIO_CHORD.ID.eq(UUID.fromString(id)))
        .fetchOne());
  }

  @Override
  @Nullable
  public Collection<InstrumentAudioChord> readMany(HubAccess hubAccess, Collection<String> parentIds) throws DAOException {
    requireArtist(hubAccess);
    return modelsFrom(InstrumentAudioChord.class,
      dbProvider.getDSL().selectFrom(INSTRUMENT_AUDIO_CHORD)
        .where(INSTRUMENT_AUDIO_CHORD.INSTRUMENT_AUDIO_ID.in(parentIds))
        .fetch());
  }

  @Override
  public InstrumentAudioChord update(HubAccess hubAccess, String id, InstrumentAudioChord entity) throws DAOException, JsonApiException, ValueException {
    validate(entity);
    requireArtist(hubAccess);
    executeUpdate(dbProvider.getDSL(), INSTRUMENT_AUDIO_CHORD, id, entity);
    return entity;
  }

  @Override
  public void destroy(HubAccess hubAccess, String id) throws DAOException {
    requireArtist(hubAccess);
    dbProvider.getDSL().deleteFrom(INSTRUMENT_AUDIO_CHORD)
      .where(INSTRUMENT_AUDIO_CHORD.ID.eq(UUID.fromString(id)))
      .execute();
  }

  @Override
  public InstrumentAudioChord newInstance() {
    return InstrumentAudioChord.getDefaultInstance();
  }

  /**
   Validate data

   @param record to validate
   @throws DAOException if invalid
   */
  public void validate(InstrumentAudioChord record) throws DAOException {
    try {
      Value.require(record.getInstrumentId(), "Instrument ID");
      Value.require(record.getInstrumentAudioId(), "Instrument Audio ID");
      ChordEntity.validate(record);

    } catch (ValueException e) {
      throw new DAOException(e);
    }
  }
}
