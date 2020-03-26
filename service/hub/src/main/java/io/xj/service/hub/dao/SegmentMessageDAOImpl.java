// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.dao;

import com.google.inject.Inject;
import io.xj.lib.rest_api.PayloadFactory;
import io.xj.lib.rest_api.RestApiException;
import io.xj.lib.util.ValueException;
import io.xj.service.hub.HubException;
import io.xj.service.hub.access.Access;
import io.xj.service.hub.model.SegmentMessage;
import io.xj.service.hub.persistence.SQLDatabaseProvider;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.UUID;

import static io.xj.service.hub.Tables.SEGMENT_MESSAGE;

public class SegmentMessageDAOImpl extends DAOImpl<SegmentMessage> implements SegmentMessageDAO {

  @Inject
  public SegmentMessageDAOImpl(
    PayloadFactory payloadFactory,
    SQLDatabaseProvider dbProvider
  ) {
    super(payloadFactory);
    this.dbProvider = dbProvider;
  }

  @Override
  public SegmentMessage create(Access access, SegmentMessage entity) throws HubException, RestApiException, ValueException {
    entity.validate();
    requireTopLevel(access);
    return modelFrom(SegmentMessage.class,
      executeCreate(dbProvider.getDSL(), SEGMENT_MESSAGE, entity));

  }

  @Override
  public void createMany(Access access, Collection<SegmentMessage> entities) throws HubException, RestApiException, ValueException {
    for (SegmentMessage entity : entities)
      entity.validate();
    requireTopLevel(access);

    executeCreateMany(dbProvider.getDSL(), SEGMENT_MESSAGE, entities);
  }

  @Override
  @Nullable
  public SegmentMessage readOne(Access access, UUID id) throws HubException {
    requireUser(access);
    return modelFrom(SegmentMessage.class,
      dbProvider.getDSL().selectFrom(SEGMENT_MESSAGE)
        .where(SEGMENT_MESSAGE.ID.eq(id))
        .fetchOne());
  }

  @Override
  @Nullable
  public Collection<SegmentMessage> readMany(Access access, Collection<UUID> parentIds) throws HubException {
    requireUser(access);
    return modelsFrom(SegmentMessage.class,
      dbProvider.getDSL().selectFrom(SEGMENT_MESSAGE)
        .where(SEGMENT_MESSAGE.SEGMENT_ID.in(parentIds))
        .fetch());
  }

  @Override
  public void update(Access access, UUID id, SegmentMessage entity) throws HubException, RestApiException, ValueException {
    entity.validate();
    requireTopLevel(access);
    executeUpdate(dbProvider.getDSL(), SEGMENT_MESSAGE, id, entity);
  }

  @Override
  public void destroy(Access access, UUID id) throws HubException {
    requireArtist(access);
    dbProvider.getDSL().deleteFrom(SEGMENT_MESSAGE)
      .where(SEGMENT_MESSAGE.ID.eq(id))
      .execute();
  }

  @Override
  public SegmentMessage newInstance() {
    return new SegmentMessage();
  }

}
