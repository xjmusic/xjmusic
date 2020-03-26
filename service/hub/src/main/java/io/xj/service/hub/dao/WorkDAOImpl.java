// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.dao;

import com.google.inject.Inject;
import io.xj.lib.rest_api.PayloadFactory;
import io.xj.lib.rest_api.RestApiException;
import io.xj.lib.util.ValueException;
import io.xj.service.hub.HubException;
import io.xj.service.hub.access.Access;
import io.xj.service.hub.model.Work;
import io.xj.service.hub.persistence.SQLDatabaseProvider;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.UUID;

import static io.xj.service.hub.Tables.WORK;

public class WorkDAOImpl extends DAOImpl<Work> implements WorkDAO {

  @Inject
  public WorkDAOImpl(
    PayloadFactory payloadFactory,
    SQLDatabaseProvider dbProvider
  ) {
    super(payloadFactory);
    this.dbProvider = dbProvider;
  }

  @Override
  public Work create(Access access, Work entity) throws HubException, RestApiException, ValueException {
    entity.validate();
    requireTopLevel(access);
    return modelFrom(Work.class,
      executeCreate(dbProvider.getDSL(), WORK, entity));

  }

  @Override
  @Nullable
  public Work readOne(Access access, UUID id) throws HubException {
    requireUser(access);
    return modelFrom(Work.class,
      dbProvider.getDSL().selectFrom(WORK)
        .where(WORK.ID.eq(id))
        .fetchOne());
  }

  @Override
  @Nullable
  public Collection<Work> readMany(Access access, Collection<UUID> parentIds) throws HubException {
    requireUser(access);
    return modelsFrom(Work.class,
      dbProvider.getDSL().selectFrom(WORK)
        .fetch());
  }

  @Override
  public void update(Access access, UUID id, Work entity) throws HubException, RestApiException, ValueException {
    entity.validate();
    requireTopLevel(access);
    executeUpdate(dbProvider.getDSL(), WORK, id, entity);
  }

  @Override
  public void destroy(Access access, UUID id) throws HubException {
    requireArtist(access);
    dbProvider.getDSL().deleteFrom(WORK)
      .where(WORK.ID.eq(id))
      .execute();
  }

  @Override
  public Work newInstance() {
    return new Work();
  }

}
