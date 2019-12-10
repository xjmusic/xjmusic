// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import com.google.inject.Inject;
import io.xj.core.access.Access;
import io.xj.core.exception.CoreException;
import io.xj.core.model.SegmentChoiceArrangementPick;
import io.xj.core.persistence.SQLDatabaseProvider;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.UUID;

import static io.xj.core.Tables.SEGMENT_CHOICE_ARRANGEMENT_PICK;

public class SegmentChoiceArrangementPickDAOImpl extends DAOImpl<SegmentChoiceArrangementPick> implements SegmentChoiceArrangementPickDAO {

  @Inject
  public SegmentChoiceArrangementPickDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public SegmentChoiceArrangementPick create(Access access, SegmentChoiceArrangementPick entity) throws CoreException {
    entity.validate();
    requireTopLevel(access);
    return DAO.modelFrom(SegmentChoiceArrangementPick.class,
      executeCreate(SEGMENT_CHOICE_ARRANGEMENT_PICK, entity));

  }

  @Override
  public void createMany(Access access, Collection<SegmentChoiceArrangementPick> entities) throws CoreException {
    for (SegmentChoiceArrangementPick entity : entities) entity.validate();
    requireTopLevel(access);

    executeCreateMany(SEGMENT_CHOICE_ARRANGEMENT_PICK, entities);

  }

  @Override
  @Nullable
  public SegmentChoiceArrangementPick readOne(Access access, UUID id) throws CoreException {
    requireUser(access);
    return DAO.modelFrom(SegmentChoiceArrangementPick.class,
      dbProvider.getDSL().selectFrom(SEGMENT_CHOICE_ARRANGEMENT_PICK)
        .where(SEGMENT_CHOICE_ARRANGEMENT_PICK.ID.eq(id))
        .fetchOne());
  }

  @Override
  @Nullable
  public Collection<SegmentChoiceArrangementPick> readMany(Access access, Collection<UUID> parentIds) throws CoreException {
    requireUser(access);
    return DAO.modelsFrom(SegmentChoiceArrangementPick.class,
      dbProvider.getDSL().selectFrom(SEGMENT_CHOICE_ARRANGEMENT_PICK)
        .where(SEGMENT_CHOICE_ARRANGEMENT_PICK.SEGMENT_ID.in(parentIds))
        .fetch());
  }

  @Override
  public void update(Access access, UUID id, SegmentChoiceArrangementPick entity) throws CoreException {
    entity.validate();
    requireTopLevel(access);
    executeUpdate(SEGMENT_CHOICE_ARRANGEMENT_PICK, id, entity);
  }

  @Override
  public void destroy(Access access, UUID id) throws CoreException {
    requireLibrary(access);
    dbProvider.getDSL().deleteFrom(SEGMENT_CHOICE_ARRANGEMENT_PICK)
      .where(SEGMENT_CHOICE_ARRANGEMENT_PICK.ID.eq(id))
      .execute();
  }

  @Override
  public SegmentChoiceArrangementPick newInstance() {
    return new SegmentChoiceArrangementPick();
  }

}
