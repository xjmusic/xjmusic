// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.dao;

import com.google.inject.Inject;
import io.xj.lib.rest_api.PayloadFactory;
import io.xj.lib.rest_api.RestApiException;
import io.xj.lib.util.ValueException;
import io.xj.service.hub.HubException;
import io.xj.service.hub.access.Access;
import io.xj.service.hub.model.SegmentChoiceArrangement;
import io.xj.service.hub.persistence.SQLDatabaseProvider;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.UUID;

import static io.xj.service.hub.Tables.SEGMENT_CHOICE_ARRANGEMENT;

public class SegmentChoiceArrangementDAOImpl extends DAOImpl<SegmentChoiceArrangement> implements SegmentChoiceArrangementDAO {
  @Inject
  public SegmentChoiceArrangementDAOImpl(
    PayloadFactory payloadFactory,
    SQLDatabaseProvider dbProvider
  ) {
    super(payloadFactory);
    this.dbProvider = dbProvider;
  }

  @Override
  public SegmentChoiceArrangement create(Access access, SegmentChoiceArrangement entity) throws HubException, RestApiException, ValueException {
    entity.validate();
    requireTopLevel(access);
    return modelFrom(SegmentChoiceArrangement.class,
      executeCreate(dbProvider.getDSL(), SEGMENT_CHOICE_ARRANGEMENT, entity));

  }

  @Override
  public void createMany(Access access, Collection<SegmentChoiceArrangement> entities) throws HubException, RestApiException, ValueException {
    for (SegmentChoiceArrangement entity : entities)
      entity.validate();
    requireTopLevel(access);

    executeCreateMany(dbProvider.getDSL(), SEGMENT_CHOICE_ARRANGEMENT, entities);
  }

  @Override
  @Nullable
  public SegmentChoiceArrangement readOne(Access access, UUID id) throws HubException {
    requireUser(access);
    return modelFrom(SegmentChoiceArrangement.class,
      dbProvider.getDSL().selectFrom(SEGMENT_CHOICE_ARRANGEMENT)
        .where(SEGMENT_CHOICE_ARRANGEMENT.ID.eq(id))
        .fetchOne());
  }

  @Override
  @Nullable
  public Collection<SegmentChoiceArrangement> readMany(Access access, Collection<UUID> parentIds) throws HubException {
    requireUser(access);
    return modelsFrom(SegmentChoiceArrangement.class,
      dbProvider.getDSL().selectFrom(SEGMENT_CHOICE_ARRANGEMENT)
        .where(SEGMENT_CHOICE_ARRANGEMENT.SEGMENT_CHOICE_ID.in(parentIds))
        .fetch());
  }

  @Override
  public void update(Access access, UUID id, SegmentChoiceArrangement entity) throws HubException, RestApiException, ValueException {
    entity.validate();
    requireTopLevel(access);
    executeUpdate(dbProvider.getDSL(), SEGMENT_CHOICE_ARRANGEMENT, id, entity);
  }

  @Override
  public void destroy(Access access, UUID id) throws HubException {
    requireArtist(access);
    dbProvider.getDSL().deleteFrom(SEGMENT_CHOICE_ARRANGEMENT)
      .where(SEGMENT_CHOICE_ARRANGEMENT.ID.eq(id))
      .execute();
  }

  @Override
  public SegmentChoiceArrangement newInstance() {
    return new SegmentChoiceArrangement();
  }

}
