// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.dao;

import com.google.inject.Inject;
import io.xj.lib.rest_api.PayloadFactory;
import io.xj.lib.rest_api.RestApiException;
import io.xj.lib.util.ValueException;
import io.xj.service.hub.HubException;
import io.xj.service.hub.access.Access;
import io.xj.service.hub.model.SegmentMeme;
import io.xj.service.hub.persistence.SQLDatabaseProvider;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.UUID;

import static io.xj.service.hub.Tables.SEGMENT_MEME;

public class SegmentMemeDAOImpl extends DAOImpl<SegmentMeme> implements SegmentMemeDAO {

  @Inject
  public SegmentMemeDAOImpl(
    PayloadFactory payloadFactory,
    SQLDatabaseProvider dbProvider
  ) {
    super(payloadFactory);
    this.dbProvider = dbProvider;
  }

  @Override
  public SegmentMeme create(Access access, SegmentMeme entity) throws HubException, RestApiException, ValueException {
    entity.validate();
    requireTopLevel(access);
    return modelFrom(SegmentMeme.class,
      executeCreate(dbProvider.getDSL(), SEGMENT_MEME, entity));

  }

  @Override
  public void createMany(Access access, Collection<SegmentMeme> entities) throws HubException, RestApiException, ValueException {
    for (SegmentMeme entity : entities)
      entity.validate();
    requireTopLevel(access);

    executeCreateMany(dbProvider.getDSL(), SEGMENT_MEME, entities);
  }

  @Override
  @Nullable
  public SegmentMeme readOne(Access access, UUID id) throws HubException {
    requireUser(access);
    return modelFrom(SegmentMeme.class,
      dbProvider.getDSL().selectFrom(SEGMENT_MEME)
        .where(SEGMENT_MEME.ID.eq(id))
        .fetchOne());
  }

  @Override
  @Nullable
  public Collection<SegmentMeme> readMany(Access access, Collection<UUID> parentIds) throws HubException {
    requireUser(access);
    return modelsFrom(SegmentMeme.class,
      dbProvider.getDSL().selectFrom(SEGMENT_MEME)
        .where(SEGMENT_MEME.SEGMENT_ID.in(parentIds))
        .fetch());
  }

  @Override
  public void update(Access access, UUID id, SegmentMeme entity) throws HubException, RestApiException, ValueException {
    entity.validate();
    requireTopLevel(access);
    executeUpdate(dbProvider.getDSL(), SEGMENT_MEME, id, entity);
  }

  @Override
  public void destroy(Access access, UUID id) throws HubException {
    requireArtist(access);
    dbProvider.getDSL().deleteFrom(SEGMENT_MEME)
      .where(SEGMENT_MEME.ID.eq(id))
      .execute();
  }

  @Override
  public SegmentMeme newInstance() {
    return new SegmentMeme();
  }

}
