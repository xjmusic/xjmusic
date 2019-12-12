// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import com.google.inject.Inject;
import io.xj.core.access.Access;
import io.xj.core.exception.CoreException;
import io.xj.core.model.InstrumentAudioChord;
import io.xj.core.persistence.SQLDatabaseProvider;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.UUID;

import static io.xj.core.Tables.INSTRUMENT_AUDIO_CHORD;

public class InstrumentAudioChordDAOImpl extends DAOImpl<InstrumentAudioChord> implements InstrumentAudioChordDAO {

  @Inject
  public InstrumentAudioChordDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public InstrumentAudioChord create(Access access, InstrumentAudioChord entity) throws CoreException {
    entity.validate();
    requireArtist(access);
    return DAO.modelFrom(InstrumentAudioChord.class,
      executeCreate(INSTRUMENT_AUDIO_CHORD, entity));

  }

  @Override
  @Nullable
  public InstrumentAudioChord readOne(Access access, UUID id) throws CoreException {
    requireArtist(access);
    return DAO.modelFrom(InstrumentAudioChord.class,
      dbProvider.getDSL().selectFrom(INSTRUMENT_AUDIO_CHORD)
        .where(INSTRUMENT_AUDIO_CHORD.ID.eq(id))
        .fetchOne());
  }

  @Override
  @Nullable
  public Collection<InstrumentAudioChord> readMany(Access access, Collection<UUID> parentIds) throws CoreException {
    requireArtist(access);
    return DAO.modelsFrom(InstrumentAudioChord.class,
      dbProvider.getDSL().selectFrom(INSTRUMENT_AUDIO_CHORD)
        .where(INSTRUMENT_AUDIO_CHORD.INSTRUMENT_ID.in(parentIds))
        .fetch());
  }

  @Override
  public void update(Access access, UUID id, InstrumentAudioChord entity) throws CoreException {
    entity.validate();
    requireArtist(access);
    executeUpdate(INSTRUMENT_AUDIO_CHORD, id, entity);
  }

  @Override
  public void destroy(Access access, UUID id) throws CoreException {
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
