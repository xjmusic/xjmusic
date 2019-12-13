// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import com.google.inject.Inject;
import io.xj.core.access.Access;
import io.xj.core.exception.CoreException;
import io.xj.core.model.InstrumentAudioEvent;
import io.xj.core.persistence.SQLDatabaseProvider;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.UUID;

import static io.xj.core.Tables.INSTRUMENT_AUDIO_EVENT;

public class InstrumentAudioEventDAOImpl extends DAOImpl<InstrumentAudioEvent> implements InstrumentAudioEventDAO {

  @Inject
  public InstrumentAudioEventDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public InstrumentAudioEvent create(Access access, InstrumentAudioEvent entity) throws CoreException {
    entity.validate();
    requireArtist(access);
    return DAO.modelFrom(InstrumentAudioEvent.class,
      executeCreate(dbProvider.getDSL(), INSTRUMENT_AUDIO_EVENT, entity));

  }

  @Override
  @Nullable
  public InstrumentAudioEvent readOne(Access access, UUID id) throws CoreException {
    requireArtist(access);
    return DAO.modelFrom(InstrumentAudioEvent.class,
      dbProvider.getDSL().selectFrom(INSTRUMENT_AUDIO_EVENT)
        .where(INSTRUMENT_AUDIO_EVENT.ID.eq(id))
        .fetchOne());
  }

  @Override
  @Nullable
  public Collection<InstrumentAudioEvent> readMany(Access access, Collection<UUID> parentIds) throws CoreException {
    requireArtist(access);
    return DAO.modelsFrom(InstrumentAudioEvent.class,
      dbProvider.getDSL().selectFrom(INSTRUMENT_AUDIO_EVENT)
        .where(INSTRUMENT_AUDIO_EVENT.INSTRUMENT_AUDIO_ID.in(parentIds))
        .fetch());
  }

  @Override
  public void update(Access access, UUID id, InstrumentAudioEvent entity) throws CoreException {
    entity.validate();
    requireArtist(access);
    executeUpdate(dbProvider.getDSL(), INSTRUMENT_AUDIO_EVENT, id, entity);
  }

  @Override
  public void destroy(Access access, UUID id) throws CoreException {
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
