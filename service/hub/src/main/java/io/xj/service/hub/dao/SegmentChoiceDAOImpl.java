// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.dao;

import com.google.inject.Inject;
import io.xj.lib.rest_api.PayloadFactory;
import io.xj.lib.rest_api.RestApiException;
import io.xj.lib.util.ValueException;
import io.xj.service.hub.HubException;
import io.xj.service.hub.access.Access;
import io.xj.service.hub.model.ProgramType;
import io.xj.service.hub.model.Segment;
import io.xj.service.hub.model.SegmentChoice;
import io.xj.service.hub.persistence.SQLDatabaseProvider;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.UUID;

import static io.xj.service.hub.Tables.SEGMENT_CHOICE;

public class SegmentChoiceDAOImpl extends DAOImpl<SegmentChoice> implements SegmentChoiceDAO {

  @Inject
  public SegmentChoiceDAOImpl(
    PayloadFactory payloadFactory,
    SQLDatabaseProvider dbProvider
  ) {
    super(payloadFactory);
    this.dbProvider = dbProvider;
  }

  @Override
  public SegmentChoice create(Access access, SegmentChoice entity) throws HubException, RestApiException, ValueException {
    entity.validate();
    requireTopLevel(access);
    return modelFrom(SegmentChoice.class,
      executeCreate(dbProvider.getDSL(), SEGMENT_CHOICE, entity));

  }

  @Override
  public void createMany(Access access, Collection<SegmentChoice> entities) throws HubException, RestApiException, ValueException {
    for (SegmentChoice entity : entities)
      entity.validate();
    requireTopLevel(access);

    executeCreateMany(dbProvider.getDSL(), SEGMENT_CHOICE, entities);

  }

  @Override
  @Nullable
  public SegmentChoice readOne(Access access, UUID id) throws HubException {
    requireUser(access);
    return modelFrom(SegmentChoice.class,
      dbProvider.getDSL().selectFrom(SEGMENT_CHOICE)
        .where(SEGMENT_CHOICE.ID.eq(id))
        .fetchOne());
  }

  @Override
  public SegmentChoice readOneOfTypeForSegment(Access access, Segment segment, ProgramType type) throws HubException {
    requireUser(access);
    return modelFrom(SegmentChoice.class,
      dbProvider.getDSL().selectFrom(SEGMENT_CHOICE)
        .where(SEGMENT_CHOICE.SEGMENT_ID.eq(segment.getId()))
        .and(SEGMENT_CHOICE.TYPE.eq(type.toString()))
        .fetchOne());
  }

  @Override
  @Nullable
  public Collection<SegmentChoice> readMany(Access access, Collection<UUID> parentIds) throws HubException {
    requireUser(access);
    return modelsFrom(SegmentChoice.class,
      dbProvider.getDSL().selectFrom(SEGMENT_CHOICE)
        .where(SEGMENT_CHOICE.SEGMENT_ID.in(parentIds))
        .fetch());
  }

  @Override
  public void update(Access access, UUID id, SegmentChoice entity) throws HubException, RestApiException, ValueException {
    entity.validate();
    requireTopLevel(access);
    executeUpdate(dbProvider.getDSL(), SEGMENT_CHOICE, id, entity);
  }

  @Override
  public void destroy(Access access, UUID id) throws HubException {
    requireArtist(access);
    dbProvider.getDSL().deleteFrom(SEGMENT_CHOICE)
      .where(SEGMENT_CHOICE.ID.eq(id))
      .execute();
  }

  @Override
  public SegmentChoice newInstance() {
    return new SegmentChoice();
  }
}
