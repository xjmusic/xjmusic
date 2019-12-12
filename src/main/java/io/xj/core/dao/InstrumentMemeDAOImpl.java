// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import com.google.inject.Inject;
import io.xj.core.access.Access;
import io.xj.core.exception.CoreException;
import io.xj.core.model.InstrumentMeme;
import io.xj.core.persistence.SQLDatabaseProvider;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.UUID;

import static io.xj.core.Tables.INSTRUMENT_MEME;

public class InstrumentMemeDAOImpl extends DAOImpl<InstrumentMeme> implements InstrumentMemeDAO {

  @Inject
  public InstrumentMemeDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public InstrumentMeme create(Access access, InstrumentMeme entity) throws CoreException {
    entity.validate();
    requireArtist(access);
    return DAO.modelFrom(InstrumentMeme.class,
      executeCreate(INSTRUMENT_MEME, entity));

  }

  @Override
  @Nullable
  public InstrumentMeme readOne(Access access, UUID id) throws CoreException {
    requireArtist(access);
    return DAO.modelFrom(InstrumentMeme.class,
      dbProvider.getDSL().selectFrom(INSTRUMENT_MEME)
        .where(INSTRUMENT_MEME.ID.eq(id))
        .fetchOne());
  }

  @Override
  @Nullable
  public Collection<InstrumentMeme> readMany(Access access, Collection<UUID> parentIds) throws CoreException {
    requireArtist(access);
    return DAO.modelsFrom(InstrumentMeme.class,
      dbProvider.getDSL().selectFrom(INSTRUMENT_MEME)
        .where(INSTRUMENT_MEME.INSTRUMENT_ID.in(parentIds))
        .fetch());
  }

  @Override
  public void update(Access access, UUID id, InstrumentMeme entity) throws CoreException {
    entity.validate();
    requireArtist(access);
    executeUpdate(INSTRUMENT_MEME, id, entity);
  }

  @Override
  public void destroy(Access access, UUID id) throws CoreException {
    requireArtist(access);
    dbProvider.getDSL().deleteFrom(INSTRUMENT_MEME)
      .where(INSTRUMENT_MEME.ID.eq(id))
      .execute();
  }

  @Override
  public InstrumentMeme newInstance() {
    return new InstrumentMeme();
  }

}
