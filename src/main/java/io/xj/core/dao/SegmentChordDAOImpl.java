// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import com.google.inject.Inject;
import io.xj.core.access.Access;
import io.xj.core.exception.CoreException;
import io.xj.core.model.SegmentChord;
import io.xj.core.persistence.SQLDatabaseProvider;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.UUID;

import static io.xj.core.Tables.SEGMENT_CHORD;

public class SegmentChordDAOImpl extends DAOImpl<SegmentChord> implements SegmentChordDAO {

  @Inject
  public SegmentChordDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public SegmentChord create(Access access, SegmentChord entity) throws CoreException {
    entity.validate();
    requireTopLevel(access);
    return DAO.modelFrom(SegmentChord.class,
      executeCreate(dbProvider.getDSL(), SEGMENT_CHORD, entity));

  }

  @Override
  public void createMany(Access access, Collection<SegmentChord> entities) throws CoreException {
    for (SegmentChord entity : entities) entity.validate();
    requireTopLevel(access);

    executeCreateMany(dbProvider.getDSL(), SEGMENT_CHORD, entities);

  }

  @Override
  @Nullable
  public SegmentChord readOne(Access access, UUID id) throws CoreException {
    requireUser(access);
    return DAO.modelFrom(SegmentChord.class,
      dbProvider.getDSL().selectFrom(SEGMENT_CHORD)
        .where(SEGMENT_CHORD.ID.eq(id))
        .fetchOne());
  }

  @Override
  @Nullable
  public Collection<SegmentChord> readMany(Access access, Collection<UUID> parentIds) throws CoreException {
    requireUser(access);
    return DAO.modelsFrom(SegmentChord.class,
      dbProvider.getDSL().selectFrom(SEGMENT_CHORD)
        .where(SEGMENT_CHORD.SEGMENT_ID.in(parentIds))
        .fetch());
  }

  @Override
  public void update(Access access, UUID id, SegmentChord entity) throws CoreException {
    entity.validate();
    requireTopLevel(access);
    executeUpdate(dbProvider.getDSL(), SEGMENT_CHORD, id, entity);
  }

  @Override
  public void destroy(Access access, UUID id) throws CoreException {
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
