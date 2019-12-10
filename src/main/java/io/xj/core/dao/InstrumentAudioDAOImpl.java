// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import com.google.inject.Inject;
import io.xj.core.access.Access;
import io.xj.core.exception.CoreException;
import io.xj.core.model.InstrumentAudio;
import io.xj.core.persistence.SQLDatabaseProvider;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.UUID;

import static io.xj.core.Tables.INSTRUMENT_AUDIO;

public class InstrumentAudioDAOImpl extends DAOImpl<InstrumentAudio> implements InstrumentAudioDAO {

  @Inject
  public InstrumentAudioDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public InstrumentAudio create(Access access, InstrumentAudio entity) throws CoreException {
    entity.validate();
    requireTopLevel(access);
    return DAO.modelFrom(InstrumentAudio.class,
      executeCreate(INSTRUMENT_AUDIO, entity));

  }

  @Override
  @Nullable
  public InstrumentAudio readOne(Access access, UUID id) throws CoreException {
    requireUser(access);
    return DAO.modelFrom(InstrumentAudio.class,
      dbProvider.getDSL().selectFrom(INSTRUMENT_AUDIO)
        .where(INSTRUMENT_AUDIO.ID.eq(id))
        .fetchOne());
  }

  @Override
  @Nullable
  public Collection<InstrumentAudio> readMany(Access access, Collection<UUID> parentIds) throws CoreException {
    requireUser(access);
    return DAO.modelsFrom(InstrumentAudio.class,
      dbProvider.getDSL().selectFrom(INSTRUMENT_AUDIO)
        .where(INSTRUMENT_AUDIO.INSTRUMENT_ID.in(parentIds))
        .fetch());
  }

  @Override
  public void update(Access access, UUID id, InstrumentAudio entity) throws CoreException {
    entity.validate();
    requireTopLevel(access);
    executeUpdate(INSTRUMENT_AUDIO, id, entity);
  }

  @Override
  public void destroy(Access access, UUID id) throws CoreException {
    requireLibrary(access);
    dbProvider.getDSL().deleteFrom(INSTRUMENT_AUDIO)
      .where(INSTRUMENT_AUDIO.ID.eq(id))
      .execute();
  }

  @Override
  public InstrumentAudio newInstance() {
    return new InstrumentAudio();
  }

}
