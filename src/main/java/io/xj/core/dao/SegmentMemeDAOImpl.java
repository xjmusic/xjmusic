// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import com.google.inject.Inject;
import io.xj.core.access.Access;
import io.xj.core.exception.CoreException;
import io.xj.core.model.SegmentMeme;
import io.xj.core.persistence.SQLDatabaseProvider;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.UUID;

import static io.xj.core.Tables.SEGMENT_MEME;

public class SegmentMemeDAOImpl extends DAOImpl<SegmentMeme> implements SegmentMemeDAO {

  @Inject
  public SegmentMemeDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public SegmentMeme create(Access access, SegmentMeme entity) throws CoreException {
    entity.validate();
    requireTopLevel(access);
    return DAO.modelFrom(SegmentMeme.class,
      executeCreate(SEGMENT_MEME, entity));

  }

  @Override
  public void createMany(Access access, Collection<SegmentMeme> entities) throws CoreException {
    for (SegmentMeme entity : entities) entity.validate();
    requireTopLevel(access);

    executeCreateMany(SEGMENT_MEME, entities);
  }

  @Override
  @Nullable
  public SegmentMeme readOne(Access access, UUID id) throws CoreException {
    requireUser(access);
    return DAO.modelFrom(SegmentMeme.class,
      dbProvider.getDSL().selectFrom(SEGMENT_MEME)
        .where(SEGMENT_MEME.ID.eq(id))
        .fetchOne());
  }

  @Override
  @Nullable
  public Collection<SegmentMeme> readMany(Access access, Collection<UUID> parentIds) throws CoreException {
    requireUser(access);
    return DAO.modelsFrom(SegmentMeme.class,
      dbProvider.getDSL().selectFrom(SEGMENT_MEME)
        .where(SEGMENT_MEME.SEGMENT_ID.in(parentIds))
        .fetch());
  }

  @Override
  public void update(Access access, UUID id, SegmentMeme entity) throws CoreException {
    entity.validate();
    requireTopLevel(access);
    executeUpdate(SEGMENT_MEME, id, entity);
  }

  @Override
  public void destroy(Access access, UUID id) throws CoreException {
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
