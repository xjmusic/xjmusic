// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.dao;

import com.google.inject.Inject;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.PayloadFactory;
import io.xj.lib.jsonapi.JsonApiException;
import io.xj.lib.util.ValueException;
import io.xj.service.hub.access.HubAccess;
import io.xj.service.hub.entity.InstrumentMeme;
import io.xj.service.hub.persistence.HubDatabaseProvider;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.UUID;

import static io.xj.service.hub.Tables.INSTRUMENT_MEME;

public class InstrumentMemeDAOImpl extends DAOImpl<InstrumentMeme> implements InstrumentMemeDAO {

  @Inject
  public InstrumentMemeDAOImpl(
    PayloadFactory payloadFactory,
    EntityFactory entityFactory,
    HubDatabaseProvider dbProvider
  ) {
    super(payloadFactory, entityFactory);
    this.dbProvider = dbProvider;
  }

  @Override
  public InstrumentMeme create(HubAccess hubAccess, InstrumentMeme entity) throws DAOException, JsonApiException, ValueException {
    entity.validate();
    requireArtist(hubAccess);
    return modelFrom(InstrumentMeme.class,
      executeCreate(dbProvider.getDSL(), INSTRUMENT_MEME, entity));
  }

  @Override
  @Nullable
  public InstrumentMeme readOne(HubAccess hubAccess, UUID id) throws DAOException {
    requireArtist(hubAccess);
    return modelFrom(InstrumentMeme.class,
      dbProvider.getDSL().selectFrom(INSTRUMENT_MEME)
        .where(INSTRUMENT_MEME.ID.eq(id))
        .fetchOne());
  }

  @Override
  @Nullable
  public Collection<InstrumentMeme> readMany(HubAccess hubAccess, Collection<UUID> parentIds) throws DAOException {
    requireArtist(hubAccess);
    return modelsFrom(InstrumentMeme.class,
      dbProvider.getDSL().selectFrom(INSTRUMENT_MEME)
        .where(INSTRUMENT_MEME.INSTRUMENT_ID.in(parentIds))
        .fetch());
  }

  @Override
  public void update(HubAccess hubAccess, UUID id, InstrumentMeme entity) throws DAOException, JsonApiException, ValueException {
    entity.validate();
    requireArtist(hubAccess);
    executeUpdate(dbProvider.getDSL(), INSTRUMENT_MEME, id, entity);
  }

  @Override
  public void destroy(HubAccess hubAccess, UUID id) throws DAOException {
    requireArtist(hubAccess);
    dbProvider.getDSL().deleteFrom(INSTRUMENT_MEME)
      .where(INSTRUMENT_MEME.ID.eq(id))
      .execute();
  }

  @Override
  public InstrumentMeme newInstance() {
    return new InstrumentMeme();
  }

}
