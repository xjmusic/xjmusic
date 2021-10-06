// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.dao;

import com.google.inject.Inject;
import io.xj.hub.access.HubAccess;
import io.xj.hub.persistence.HubDatabaseProvider;
import io.xj.hub.tables.pojos.ProgramAuthorship;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.JsonapiException;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.util.ValueException;
import io.xj.lib.util.Values;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.UUID;

import static io.xj.hub.Tables.PROGRAM_AUTHORSHIP;

public class ProgramAuthorshipDAOImpl extends DAOImpl<ProgramAuthorship> implements ProgramAuthorshipDAO {

  @Inject
  public ProgramAuthorshipDAOImpl(
    JsonapiPayloadFactory payloadFactory,
    EntityFactory entityFactory,
    HubDatabaseProvider dbProvider
  ) {
    super(payloadFactory, entityFactory);
    this.dbProvider = dbProvider;
  }

  @Override
  public ProgramAuthorship create(HubAccess hubAccess, ProgramAuthorship rawAuthorship) throws DAOException, JsonapiException, ValueException {
    var authorship = validate(rawAuthorship);
    requireArtist(hubAccess);
    return modelFrom(ProgramAuthorship.class,
      executeCreate(dbProvider.getDSL(), PROGRAM_AUTHORSHIP, authorship));
  }

  @Override
  @Nullable
  public ProgramAuthorship readOne(HubAccess hubAccess, UUID id) throws DAOException {
    requireArtist(hubAccess);
    return modelFrom(ProgramAuthorship.class,
      dbProvider.getDSL().selectFrom(PROGRAM_AUTHORSHIP)
        .where(PROGRAM_AUTHORSHIP.ID.eq(id))
        .fetchOne());
  }

  @Override
  @Nullable
  public Collection<ProgramAuthorship> readMany(HubAccess hubAccess, Collection<UUID> parentIds) throws DAOException {
    requireArtist(hubAccess);
    return modelsFrom(ProgramAuthorship.class,
      dbProvider.getDSL().selectFrom(PROGRAM_AUTHORSHIP)
        .where(PROGRAM_AUTHORSHIP.PROGRAM_ID.in(parentIds))
        .fetch());
  }

  @Override
  public ProgramAuthorship update(HubAccess hubAccess, UUID id, ProgramAuthorship rawAuthorship) throws DAOException, JsonapiException, ValueException {
    throw new DAOException("Cannot update a program authorship!");
  }

  @Override
  public void destroy(HubAccess hubAccess, UUID id) throws DAOException {
    requireArtist(hubAccess);
    dbProvider.getDSL().deleteFrom(PROGRAM_AUTHORSHIP)
      .where(PROGRAM_AUTHORSHIP.ID.eq(id))
      .execute();
  }

  @Override
  public ProgramAuthorship newInstance() {
    return new ProgramAuthorship();
  }

  /**
   Validate data

   @param record to validate
   @throws DAOException if invalid
   */
  public ProgramAuthorship validate(ProgramAuthorship record) throws DAOException {
    try {
      Values.require(record.getProgramId(), "Program ID");
      Values.require(record.getUserId(), "User ID");
      Values.require(record.getHours(), "Hours");
      Values.require(record.getDescription(), "Description");
      return record;

    } catch (ValueException e) {
      throw new DAOException(e);
    }
  }

}
