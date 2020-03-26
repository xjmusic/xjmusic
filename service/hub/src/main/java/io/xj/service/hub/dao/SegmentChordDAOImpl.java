// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.dao;

import com.google.inject.Inject;
import io.xj.lib.rest_api.PayloadFactory;
import io.xj.lib.rest_api.RestApiException;
import io.xj.lib.util.ValueException;
import io.xj.service.hub.HubException;
import io.xj.service.hub.access.Access;
import io.xj.service.hub.model.SegmentChord;
import io.xj.service.hub.persistence.SQLDatabaseProvider;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.UUID;

import static io.xj.service.hub.Tables.SEGMENT_CHORD;

public class SegmentChordDAOImpl extends DAOImpl<SegmentChord> implements SegmentChordDAO {

  @Inject
  public SegmentChordDAOImpl(
    PayloadFactory payloadFactory,
    SQLDatabaseProvider dbProvider
  ) {
    super(payloadFactory);
    this.dbProvider = dbProvider;
  }

  @Override
  public SegmentChord create(Access access, SegmentChord entity) throws HubException, RestApiException, ValueException {
    entity.validate();
    requireTopLevel(access);
    return modelFrom(SegmentChord.class,
      executeCreate(dbProvider.getDSL(), SEGMENT_CHORD, entity));

  }

  @Override
  public void createMany(Access access, Collection<SegmentChord> entities) throws HubException, RestApiException, ValueException {
    for (SegmentChord entity : entities)
      entity.validate();
    requireTopLevel(access);

    executeCreateMany(dbProvider.getDSL(), SEGMENT_CHORD, entities);

  }

  @Override
  @Nullable
  public SegmentChord readOne(Access access, UUID id) throws HubException {
    requireUser(access);
    return modelFrom(SegmentChord.class,
      dbProvider.getDSL().selectFrom(SEGMENT_CHORD)
        .where(SEGMENT_CHORD.ID.eq(id))
        .fetchOne());
  }

  @Override
  @Nullable
  public Collection<SegmentChord> readMany(Access access, Collection<UUID> parentIds) throws HubException {
    requireUser(access);
    return modelsFrom(SegmentChord.class,
      dbProvider.getDSL().selectFrom(SEGMENT_CHORD)
        .where(SEGMENT_CHORD.SEGMENT_ID.in(parentIds))
        .fetch());
  }

  @Override
  public void update(Access access, UUID id, SegmentChord entity) throws HubException, RestApiException, ValueException {
    entity.validate();
    requireTopLevel(access);
    executeUpdate(dbProvider.getDSL(), SEGMENT_CHORD, id, entity);
  }

  @Override
  public void destroy(Access access, UUID id) throws HubException {
    requireArtist(access);
    dbProvider.getDSL().deleteFrom(SEGMENT_CHORD)
      .where(SEGMENT_CHORD.ID.eq(id))
      .execute();
  }

  @Override
  public SegmentChord newInstance() {
    return new SegmentChord();
  }

}
