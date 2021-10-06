// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.dao;

import com.google.inject.Inject;
import io.xj.hub.access.HubAccess;
import io.xj.hub.persistence.HubDatabaseProvider;
import io.xj.hub.tables.pojos.InstrumentMessage;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.JsonapiException;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.util.ValueException;
import io.xj.lib.util.Values;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.UUID;

import static io.xj.hub.Tables.INSTRUMENT_MESSAGE;

public class InstrumentMessageDAOImpl extends DAOImpl<InstrumentMessage> implements InstrumentMessageDAO {

  @Inject
  public InstrumentMessageDAOImpl(
    JsonapiPayloadFactory payloadFactory,
    EntityFactory entityFactory,
    HubDatabaseProvider dbProvider
  ) {
    super(payloadFactory, entityFactory);
    this.dbProvider = dbProvider;
  }

  @Override
  public InstrumentMessage create(HubAccess hubAccess, InstrumentMessage rawMessage) throws DAOException, JsonapiException, ValueException {
    var message = validate(rawMessage);
    requireArtist(hubAccess);
    return modelFrom(InstrumentMessage.class,
      executeCreate(dbProvider.getDSL(), INSTRUMENT_MESSAGE, message));
  }

  @Override
  @Nullable
  public InstrumentMessage readOne(HubAccess hubAccess, UUID id) throws DAOException {
    requireArtist(hubAccess);
    return modelFrom(InstrumentMessage.class,
      dbProvider.getDSL().selectFrom(INSTRUMENT_MESSAGE)
        .where(INSTRUMENT_MESSAGE.ID.eq(id))
        .fetchOne());
  }

  @Override
  @Nullable
  public Collection<InstrumentMessage> readMany(HubAccess hubAccess, Collection<UUID> parentIds) throws DAOException {
    requireArtist(hubAccess);
    return modelsFrom(InstrumentMessage.class,
      dbProvider.getDSL().selectFrom(INSTRUMENT_MESSAGE)
        .where(INSTRUMENT_MESSAGE.INSTRUMENT_ID.in(parentIds))
        .fetch());
  }

  @Override
  public InstrumentMessage update(HubAccess hubAccess, UUID id, InstrumentMessage rawMessage) throws DAOException, JsonapiException, ValueException {
    throw new DAOException("Cannot update an instrument message!");
  }

  @Override
  public void destroy(HubAccess hubAccess, UUID id) throws DAOException {
    requireArtist(hubAccess);
    dbProvider.getDSL().deleteFrom(INSTRUMENT_MESSAGE)
      .where(INSTRUMENT_MESSAGE.ID.eq(id))
      .execute();
  }

  @Override
  public InstrumentMessage newInstance() {
    return new InstrumentMessage();
  }

  /**
   Validate data

   @param record to validate
   @throws DAOException if invalid
   */
  public InstrumentMessage validate(InstrumentMessage record) throws DAOException {
    try {
      Values.require(record.getInstrumentId(), "Instrument ID");
      Values.require(record.getInstrumentId(), "User ID");
      Values.require(record.getBody(), "Body");
      return record;

    } catch (ValueException e) {
      throw new DAOException(e);
    }
  }

}
