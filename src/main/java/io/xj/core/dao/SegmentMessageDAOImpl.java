// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import com.google.inject.Inject;
import io.xj.core.access.Access;
import io.xj.core.exception.CoreException;
import io.xj.core.model.SegmentMessage;
import io.xj.core.persistence.SQLDatabaseProvider;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.UUID;

import static io.xj.core.Tables.SEGMENT_MESSAGE;

public class SegmentMessageDAOImpl extends DAOImpl<SegmentMessage> implements SegmentMessageDAO {

  @Inject
  public SegmentMessageDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public SegmentMessage create(Access access, SegmentMessage entity) throws CoreException {
    entity.validate();
    requireTopLevel(access);
    return DAO.modelFrom(SegmentMessage.class,
      executeCreate(SEGMENT_MESSAGE, entity));

  }

  @Override
  public void createMany(Access access, Collection<SegmentMessage> entities) throws CoreException {
    for (SegmentMessage entity : entities) entity.validate();
    requireTopLevel(access);

    executeCreateMany(SEGMENT_MESSAGE, entities);
  }

  @Override
  @Nullable
  public SegmentMessage readOne(Access access, UUID id) throws CoreException {
    requireUser(access);
    return DAO.modelFrom(SegmentMessage.class,
      dbProvider.getDSL().selectFrom(SEGMENT_MESSAGE)
        .where(SEGMENT_MESSAGE.ID.eq(id))
        .fetchOne());
  }

  @Override
  @Nullable
  public Collection<SegmentMessage> readMany(Access access, Collection<UUID> parentIds) throws CoreException {
    requireUser(access);
    return DAO.modelsFrom(SegmentMessage.class,
      dbProvider.getDSL().selectFrom(SEGMENT_MESSAGE)
        .where(SEGMENT_MESSAGE.SEGMENT_ID.in(parentIds))
        .fetch());
  }

  @Override
  public void update(Access access, UUID id, SegmentMessage entity) throws CoreException {
    entity.validate();
    requireTopLevel(access);
    executeUpdate(SEGMENT_MESSAGE, id, entity);
  }

  @Override
  public void destroy(Access access, UUID id) throws CoreException {
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
