// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.dao;

import com.google.inject.Inject;
import io.xj.hub.access.HubAccess;
import io.xj.hub.persistence.HubDatabaseProvider;
import io.xj.hub.tables.pojos.ProgramMessage;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.JsonapiException;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.util.ValueException;
import io.xj.lib.util.Values;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.UUID;

import static io.xj.hub.Tables.PROGRAM_MESSAGE;

public class ProgramMessageDAOImpl extends DAOImpl<ProgramMessage> implements ProgramMessageDAO {

  @Inject
  public ProgramMessageDAOImpl(
    JsonapiPayloadFactory payloadFactory,
    EntityFactory entityFactory,
    HubDatabaseProvider dbProvider
  ) {
    super(payloadFactory, entityFactory);
    this.dbProvider = dbProvider;
  }

  @Override
  public ProgramMessage create(HubAccess hubAccess, ProgramMessage rawMessage) throws DAOException, JsonapiException, ValueException {
    var message = validate(rawMessage);
    requireArtist(hubAccess);
    return modelFrom(ProgramMessage.class,
      executeCreate(dbProvider.getDSL(), PROGRAM_MESSAGE, message));
  }

  @Override
  @Nullable
  public ProgramMessage readOne(HubAccess hubAccess, UUID id) throws DAOException {
    requireArtist(hubAccess);
    return modelFrom(ProgramMessage.class,
      dbProvider.getDSL().selectFrom(PROGRAM_MESSAGE)
        .where(PROGRAM_MESSAGE.ID.eq(id))
        .fetchOne());
  }

  @Override
  @Nullable
  public Collection<ProgramMessage> readMany(HubAccess hubAccess, Collection<UUID> parentIds) throws DAOException {
    requireArtist(hubAccess);
    return modelsFrom(ProgramMessage.class,
      dbProvider.getDSL().selectFrom(PROGRAM_MESSAGE)
        .where(PROGRAM_MESSAGE.PROGRAM_ID.in(parentIds))
        .fetch());
  }

  @Override
  public ProgramMessage update(HubAccess hubAccess, UUID id, ProgramMessage rawMessage) throws DAOException, JsonapiException, ValueException {
    throw new DAOException("Cannot update a program message!");
  }

  @Override
  public void destroy(HubAccess hubAccess, UUID id) throws DAOException {
    requireArtist(hubAccess);
    dbProvider.getDSL().deleteFrom(PROGRAM_MESSAGE)
      .where(PROGRAM_MESSAGE.ID.eq(id))
      .execute();
  }

  @Override
  public ProgramMessage newInstance() {
    return new ProgramMessage();
  }

  /**
   Validate data

   @param record to validate
   @throws DAOException if invalid
   */
  public ProgramMessage validate(ProgramMessage record) throws DAOException {
    try {
      Values.require(record.getProgramId(), "Program ID");
      Values.require(record.getProgramId(), "User ID");
      Values.require(record.getBody(), "Body");
      return record;

    } catch (ValueException e) {
      throw new DAOException(e);
    }
  }

}
