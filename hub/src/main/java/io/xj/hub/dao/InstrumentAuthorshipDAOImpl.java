// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.dao;

import com.google.inject.Inject;
import io.xj.hub.access.HubAccess;
import io.xj.hub.persistence.HubDatabaseProvider;
import io.xj.hub.tables.pojos.InstrumentAuthorship;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.JsonapiException;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.util.Value;
import io.xj.lib.util.ValueException;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.UUID;

import static io.xj.hub.Tables.INSTRUMENT_AUTHORSHIP;

public class InstrumentAuthorshipDAOImpl extends DAOImpl<InstrumentAuthorship> implements InstrumentAuthorshipDAO {

  @Inject
  public InstrumentAuthorshipDAOImpl(
    JsonapiPayloadFactory payloadFactory,
    EntityFactory entityFactory,
    HubDatabaseProvider dbProvider
  ) {
    super(payloadFactory, entityFactory);
    this.dbProvider = dbProvider;
  }

  @Override
  public InstrumentAuthorship create(HubAccess hubAccess, InstrumentAuthorship rawAuthorship) throws DAOException, JsonapiException, ValueException {
    var authorship = validate(rawAuthorship);
    requireArtist(hubAccess);
    return modelFrom(InstrumentAuthorship.class,
      executeCreate(dbProvider.getDSL(), INSTRUMENT_AUTHORSHIP, authorship));
  }

  @Override
  @Nullable
  public InstrumentAuthorship readOne(HubAccess hubAccess, UUID id) throws DAOException {
    requireArtist(hubAccess);
    return modelFrom(InstrumentAuthorship.class,
      dbProvider.getDSL().selectFrom(INSTRUMENT_AUTHORSHIP)
        .where(INSTRUMENT_AUTHORSHIP.ID.eq(id))
        .fetchOne());
  }

  @Override
  @Nullable
  public Collection<InstrumentAuthorship> readMany(HubAccess hubAccess, Collection<UUID> parentIds) throws DAOException {
    requireArtist(hubAccess);
    return modelsFrom(InstrumentAuthorship.class,
      dbProvider.getDSL().selectFrom(INSTRUMENT_AUTHORSHIP)
        .where(INSTRUMENT_AUTHORSHIP.INSTRUMENT_ID.in(parentIds))
        .fetch());
  }

  @Override
  public InstrumentAuthorship update(HubAccess hubAccess, UUID id, InstrumentAuthorship rawAuthorship) throws DAOException, JsonapiException, ValueException {
    throw new DAOException("Cannot update an instrument authorship!");
  }

  @Override
  public void destroy(HubAccess hubAccess, UUID id) throws DAOException {
    requireArtist(hubAccess);
    dbProvider.getDSL().deleteFrom(INSTRUMENT_AUTHORSHIP)
      .where(INSTRUMENT_AUTHORSHIP.ID.eq(id))
      .execute();
  }

  @Override
  public InstrumentAuthorship newInstance() {
    return new InstrumentAuthorship();
  }

  /**
   Validate data

   @param record to validate
   @throws DAOException if invalid
   */
  public InstrumentAuthorship validate(InstrumentAuthorship record) throws DAOException {
    try {
      Value.require(record.getInstrumentId(), "Instrument ID");
      Value.require(record.getUserId(), "User ID");
      Value.require(record.getHours(), "Hours");
      Value.require(record.getDescription(), "Description");
      return record;

    } catch (ValueException e) {
      throw new DAOException(e);
    }
  }

}
