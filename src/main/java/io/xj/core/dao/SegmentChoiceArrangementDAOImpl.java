// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import com.google.inject.Inject;
import io.xj.core.access.Access;
import io.xj.core.exception.CoreException;
import io.xj.core.model.SegmentChoiceArrangement;
import io.xj.core.persistence.SQLDatabaseProvider;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.UUID;

import static io.xj.core.Tables.SEGMENT_CHOICE_ARRANGEMENT;

public class SegmentChoiceArrangementDAOImpl extends DAOImpl<SegmentChoiceArrangement> implements SegmentChoiceArrangementDAO {
  @Inject
  public SegmentChoiceArrangementDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public SegmentChoiceArrangement create(Access access, SegmentChoiceArrangement entity) throws CoreException {
    entity.validate();
    requireTopLevel(access);
    return DAO.modelFrom(SegmentChoiceArrangement.class,
      executeCreate(SEGMENT_CHOICE_ARRANGEMENT, entity));

  }

  @Override
  public void createMany(Access access, Collection<SegmentChoiceArrangement> entities) throws CoreException {
    for (SegmentChoiceArrangement entity : entities) entity.validate();
    requireTopLevel(access);

    executeCreateMany(SEGMENT_CHOICE_ARRANGEMENT, entities);
  }

  @Override
  @Nullable
  public SegmentChoiceArrangement readOne(Access access, UUID id) throws CoreException {
    requireUser(access);
    return DAO.modelFrom(SegmentChoiceArrangement.class,
      dbProvider.getDSL().selectFrom(SEGMENT_CHOICE_ARRANGEMENT)
        .where(SEGMENT_CHOICE_ARRANGEMENT.ID.eq(id))
        .fetchOne());
  }

  @Override
  @Nullable
  public Collection<SegmentChoiceArrangement> readMany(Access access, Collection<UUID> parentIds) throws CoreException {
    requireUser(access);
    return DAO.modelsFrom(SegmentChoiceArrangement.class,
      dbProvider.getDSL().selectFrom(SEGMENT_CHOICE_ARRANGEMENT)
        .where(SEGMENT_CHOICE_ARRANGEMENT.SEGMENT_ID.in(parentIds))
        .fetch());
  }

  @Override
  public void update(Access access, UUID id, SegmentChoiceArrangement entity) throws CoreException {
    entity.validate();
    requireTopLevel(access);
    executeUpdate(SEGMENT_CHOICE_ARRANGEMENT, id, entity);
  }

  @Override
  public void destroy(Access access, UUID id) throws CoreException {
    requireLibrary(access);
    dbProvider.getDSL().deleteFrom(SEGMENT_CHOICE_ARRANGEMENT)
      .where(SEGMENT_CHOICE_ARRANGEMENT.ID.eq(id))
      .execute();
  }

  @Override
  public SegmentChoiceArrangement newInstance() {
    return new SegmentChoiceArrangement();
  }

}
