// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import com.google.inject.Inject;
import io.xj.core.access.Access;
import io.xj.core.exception.CoreException;
import io.xj.core.model.ProgramType;
import io.xj.core.model.Segment;
import io.xj.core.model.SegmentChoice;
import io.xj.core.persistence.SQLDatabaseProvider;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.UUID;

import static io.xj.core.Tables.SEGMENT_CHOICE;

public class SegmentChoiceDAOImpl extends DAOImpl<SegmentChoice> implements SegmentChoiceDAO {

  @Inject
  public SegmentChoiceDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public SegmentChoice create(Access access, SegmentChoice entity) throws CoreException {
    entity.validate();
    requireTopLevel(access);
    return DAO.modelFrom(SegmentChoice.class,
      executeCreate(SEGMENT_CHOICE, entity));

  }

  @Override
  public void createMany(Access access, Collection<SegmentChoice> entities) throws CoreException {
    for (SegmentChoice entity : entities) entity.validate();
    requireTopLevel(access);

    executeCreateMany(SEGMENT_CHOICE, entities);

  }

  @Override
  @Nullable
  public SegmentChoice readOne(Access access, UUID id) throws CoreException {
    requireUser(access);
    return DAO.modelFrom(SegmentChoice.class,
      dbProvider.getDSL().selectFrom(SEGMENT_CHOICE)
        .where(SEGMENT_CHOICE.ID.eq(id))
        .fetchOne());
  }

  @Override
  public SegmentChoice readOneOfTypeForSegment(Access access, Segment segment, ProgramType type) throws CoreException {
    requireUser(access);
    return DAO.modelFrom(SegmentChoice.class,
      dbProvider.getDSL().selectFrom(SEGMENT_CHOICE)
        .where(SEGMENT_CHOICE.SEGMENT_ID.eq(segment.getId()))
        .and(SEGMENT_CHOICE.TYPE.eq(type.toString()))
        .fetchOne());
  }

  @Override
  @Nullable
  public Collection<SegmentChoice> readMany(Access access, Collection<UUID> parentIds) throws CoreException {
    requireUser(access);
    return DAO.modelsFrom(SegmentChoice.class,
      dbProvider.getDSL().selectFrom(SEGMENT_CHOICE)
        .where(SEGMENT_CHOICE.SEGMENT_ID.in(parentIds))
        .fetch());
  }

  @Override
  public void update(Access access, UUID id, SegmentChoice entity) throws CoreException {
    entity.validate();
    requireTopLevel(access);
    executeUpdate(SEGMENT_CHOICE, id, entity);
  }

  @Override
  public void destroy(Access access, UUID id) throws CoreException {
    requireLibrary(access);
    dbProvider.getDSL().deleteFrom(SEGMENT_CHOICE)
      .where(SEGMENT_CHOICE.ID.eq(id))
      .execute();
  }

  @Override
  public SegmentChoice newInstance() {
    return new SegmentChoice();
  }
}
