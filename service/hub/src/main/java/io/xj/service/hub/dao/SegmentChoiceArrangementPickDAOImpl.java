// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.dao;

import com.google.inject.Inject;
import io.xj.lib.rest_api.PayloadFactory;
import io.xj.lib.rest_api.RestApiException;
import io.xj.lib.util.ValueException;
import io.xj.service.hub.HubException;
import io.xj.service.hub.access.Access;
import io.xj.service.hub.model.SegmentChoiceArrangementPick;
import io.xj.service.hub.persistence.SQLDatabaseProvider;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.UUID;

import static io.xj.service.hub.Tables.SEGMENT_CHOICE_ARRANGEMENT_PICK;

public class SegmentChoiceArrangementPickDAOImpl extends DAOImpl<SegmentChoiceArrangementPick> implements SegmentChoiceArrangementPickDAO {

  @Inject
  public SegmentChoiceArrangementPickDAOImpl(
    PayloadFactory payloadFactory,
    SQLDatabaseProvider dbProvider
  ) {
    super(payloadFactory);
    this.dbProvider = dbProvider;
  }

  @Override
  public SegmentChoiceArrangementPick create(Access access, SegmentChoiceArrangementPick entity) throws HubException, RestApiException, ValueException {
    entity.validate();
    requireTopLevel(access);
    return modelFrom(SegmentChoiceArrangementPick.class,
      executeCreate(dbProvider.getDSL(), SEGMENT_CHOICE_ARRANGEMENT_PICK, entity));

  }

  @Override
  public void createMany(Access access, Collection<SegmentChoiceArrangementPick> entities) throws HubException, RestApiException, ValueException {
    for (SegmentChoiceArrangementPick entity : entities)
      entity.validate();
    requireTopLevel(access);

    executeCreateMany(dbProvider.getDSL(), SEGMENT_CHOICE_ARRANGEMENT_PICK, entities);

  }

  @Override
  @Nullable
  public SegmentChoiceArrangementPick readOne(Access access, UUID id) throws HubException {
    requireUser(access);
    return modelFrom(SegmentChoiceArrangementPick.class,
      dbProvider.getDSL().selectFrom(SEGMENT_CHOICE_ARRANGEMENT_PICK)
        .where(SEGMENT_CHOICE_ARRANGEMENT_PICK.ID.eq(id))
        .fetchOne());
  }

  @Override
  @Nullable
  public Collection<SegmentChoiceArrangementPick> readMany(Access access, Collection<UUID> parentIds) throws HubException {
    requireUser(access);
    return modelsFrom(SegmentChoiceArrangementPick.class,
      dbProvider.getDSL().selectFrom(SEGMENT_CHOICE_ARRANGEMENT_PICK)
        .where(SEGMENT_CHOICE_ARRANGEMENT_PICK.SEGMENT_CHOICE_ARRANGEMENT_ID.in(parentIds))
        .fetch());
  }

  @Override
  public void update(Access access, UUID id, SegmentChoiceArrangementPick entity) throws HubException, RestApiException, ValueException {
    entity.validate();
    requireTopLevel(access);
    executeUpdate(dbProvider.getDSL(), SEGMENT_CHOICE_ARRANGEMENT_PICK, id, entity);
  }

  @Override
  public void destroy(Access access, UUID id) throws HubException {
    requireArtist(access);
    dbProvider.getDSL().deleteFrom(SEGMENT_CHOICE_ARRANGEMENT_PICK)
      .where(SEGMENT_CHOICE_ARRANGEMENT_PICK.ID.eq(id))
      .execute();
  }

  @Override
  public SegmentChoiceArrangementPick newInstance() {
    return new SegmentChoiceArrangementPick();
  }

}
