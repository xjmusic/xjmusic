// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.dao;

import com.google.inject.Inject;
import io.xj.lib.rest_api.PayloadFactory;
import io.xj.lib.rest_api.RestApiException;
import io.xj.lib.util.ValueException;
import io.xj.service.hub.HubException;
import io.xj.service.hub.access.Access;
import io.xj.service.hub.model.InstrumentMeme;
import io.xj.service.hub.persistence.SQLDatabaseProvider;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.UUID;

import static io.xj.service.hub.Tables.INSTRUMENT_MEME;

public class InstrumentMemeDAOImpl extends DAOImpl<InstrumentMeme> implements InstrumentMemeDAO {

  @Inject
  public InstrumentMemeDAOImpl(
    PayloadFactory payloadFactory,
    SQLDatabaseProvider dbProvider
  ) {
    super(payloadFactory);
    this.dbProvider = dbProvider;
  }

  @Override
  public InstrumentMeme create(Access access, InstrumentMeme entity) throws HubException, RestApiException, ValueException {
    entity.validate();
    requireArtist(access);
    return modelFrom(InstrumentMeme.class,
      executeCreate(dbProvider.getDSL(), INSTRUMENT_MEME, entity));
  }

  @Override
  @Nullable
  public InstrumentMeme readOne(Access access, UUID id) throws HubException {
    requireArtist(access);
    return modelFrom(InstrumentMeme.class,
      dbProvider.getDSL().selectFrom(INSTRUMENT_MEME)
        .where(INSTRUMENT_MEME.ID.eq(id))
        .fetchOne());
  }

  @Override
  @Nullable
  public Collection<InstrumentMeme> readMany(Access access, Collection<UUID> parentIds) throws HubException {
    requireArtist(access);
    return modelsFrom(InstrumentMeme.class,
      dbProvider.getDSL().selectFrom(INSTRUMENT_MEME)
        .where(INSTRUMENT_MEME.INSTRUMENT_ID.in(parentIds))
        .fetch());
  }

  @Override
  public void update(Access access, UUID id, InstrumentMeme entity) throws HubException, RestApiException, ValueException {
    entity.validate();
    requireArtist(access);
    executeUpdate(dbProvider.getDSL(), INSTRUMENT_MEME, id, entity);
  }

  @Override
  public void destroy(Access access, UUID id) throws HubException {
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
