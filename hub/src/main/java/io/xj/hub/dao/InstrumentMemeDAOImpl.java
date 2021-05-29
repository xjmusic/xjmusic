// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.dao;

import com.google.inject.Inject;


import io.xj.InstrumentMeme;
import io.xj.hub.access.HubAccess;
import io.xj.hub.persistence.HubDatabaseProvider;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.JsonApiException;
import io.xj.lib.jsonapi.PayloadFactory;
import io.xj.lib.util.Text;
import io.xj.lib.util.Value;
import io.xj.lib.util.ValueException;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.UUID;

import static io.xj.hub.Tables.INSTRUMENT_MEME;

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
  public InstrumentMeme create(HubAccess hubAccess, InstrumentMeme rawMeme) throws DAOException, JsonApiException, ValueException {
    var meme = validate(rawMeme.toBuilder()).build();
    requireArtist(hubAccess);
    return modelFrom(InstrumentMeme.class,
      executeCreate(dbProvider.getDSL(), INSTRUMENT_MEME, meme));
  }

  @Override
  @Nullable
  public InstrumentMeme readOne(HubAccess hubAccess, String id) throws DAOException {
    requireArtist(hubAccess);
    return modelFrom(InstrumentMeme.class,
      dbProvider.getDSL().selectFrom(INSTRUMENT_MEME)
        .where(INSTRUMENT_MEME.ID.eq(UUID.fromString(id)))
        .fetchOne());
  }

  @Override
  @Nullable
  public Collection<InstrumentMeme> readMany(HubAccess hubAccess, Collection<String> parentIds) throws DAOException {
    requireArtist(hubAccess);
    return modelsFrom(InstrumentMeme.class,
      dbProvider.getDSL().selectFrom(INSTRUMENT_MEME)
        .where(INSTRUMENT_MEME.INSTRUMENT_ID.in(parentIds))
        .fetch());
  }

  @Override
  public InstrumentMeme update(HubAccess hubAccess, String id, InstrumentMeme rawMeme) throws DAOException, JsonApiException, ValueException {
    var meme = validate(rawMeme.toBuilder()).build();
    requireArtist(hubAccess);
    executeUpdate(dbProvider.getDSL(), INSTRUMENT_MEME, id, meme);
    return meme;
  }

  @Override
  public void destroy(HubAccess hubAccess, String id) throws DAOException {
    requireArtist(hubAccess);
    dbProvider.getDSL().deleteFrom(INSTRUMENT_MEME)
      .where(INSTRUMENT_MEME.ID.eq(UUID.fromString(id)))
      .execute();
  }

  @Override
  public InstrumentMeme newInstance() {
    return InstrumentMeme.getDefaultInstance();
  }

  /**
   Validate data

   @param record to validate
   @throws DAOException if invalid
   */
  public InstrumentMeme.Builder validate(InstrumentMeme.Builder record) throws DAOException {
    try {
      Value.require(record.getInstrumentId(), "Instrument ID");
      Value.require(record.getName(), "Name");
      record.setName(Text.toMeme(record.getName()));
      return record;

    } catch (ValueException e) {
      throw new DAOException(e);
    }
  }

}
