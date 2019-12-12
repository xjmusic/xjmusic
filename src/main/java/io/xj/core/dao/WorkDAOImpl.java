// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import com.google.inject.Inject;
import io.xj.core.access.Access;
import io.xj.core.exception.CoreException;
import io.xj.core.model.Work;
import io.xj.core.persistence.SQLDatabaseProvider;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.UUID;

import static io.xj.core.Tables.WORK;

public class WorkDAOImpl extends DAOImpl<Work> implements WorkDAO {

  @Inject
  public WorkDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public Work create(Access access, Work entity) throws CoreException {
    entity.validate();
    requireTopLevel(access);
    return DAO.modelFrom(Work.class,
      executeCreate(WORK, entity));

  }

  @Override
  @Nullable
  public Work readOne(Access access, UUID id) throws CoreException {
    requireUser(access);
    return DAO.modelFrom(Work.class,
      dbProvider.getDSL().selectFrom(WORK)
        .where(WORK.ID.eq(id))
        .fetchOne());
  }

  @Override
  @Nullable
  public Collection<Work> readMany(Access access, Collection<UUID> parentIds) throws CoreException {
    requireUser(access);
    return DAO.modelsFrom(Work.class,
      dbProvider.getDSL().selectFrom(WORK)
        .fetch());
  }

  @Override
  public void update(Access access, UUID id, Work entity) throws CoreException {
    entity.validate();
    requireTopLevel(access);
    executeUpdate(WORK, id, entity);
  }

  @Override
  public void destroy(Access access, UUID id) throws CoreException {
    requireArtist(access);
    dbProvider.getDSL().deleteFrom(WORK)
      .where(WORK.ID.eq(id))
      .execute();
  }

  @Override
  public Work newInstance() {
    return new Work();
  }

}
