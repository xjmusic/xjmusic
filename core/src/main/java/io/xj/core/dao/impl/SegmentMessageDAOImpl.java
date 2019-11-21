// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao.impl;

import com.google.inject.Inject;
import io.xj.core.access.Access;
import io.xj.core.dao.DAORecord;
import io.xj.core.dao.SegmentMessageDAO;
import io.xj.core.exception.CoreException;
import io.xj.core.model.SegmentMessage;
import io.xj.core.persistence.sql.SQLDatabaseProvider;

import javax.annotation.Nullable;
import java.sql.Connection;
import java.sql.SQLException;
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
    try (Connection connection = dbProvider.getConnection()) {
      entity.validate();
      requireTopLevel(access);
      return DAORecord.modelFrom(SegmentMessage.class,
        executeCreate(connection, SEGMENT_MESSAGE, entity));

    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  public void createMany(Access access, Collection<SegmentMessage> entities) throws CoreException {
    for (SegmentMessage entity : entities) entity.validate();
    requireTopLevel(access);

    try (Connection connection = dbProvider.getConnection()) {
      executeCreateMany(connection, SEGMENT_MESSAGE, entities);

    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  @Nullable
  public SegmentMessage readOne(Access access, UUID id) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      requireUser(access);
      return DAORecord.modelFrom(SegmentMessage.class,
        DAORecord.DSL(connection).selectFrom(SEGMENT_MESSAGE)
          .where(SEGMENT_MESSAGE.ID.eq(id))
          .fetchOne());
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  @Nullable
  public Collection<SegmentMessage> readMany(Access access, Collection<UUID> parentIds) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      requireUser(access);
      return DAORecord.modelsFrom(SegmentMessage.class,
        DAORecord.DSL(connection).selectFrom(SEGMENT_MESSAGE)
          .where(SEGMENT_MESSAGE.SEGMENT_ID.in(parentIds))
          .fetch());
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  public void update(Access access, UUID id, SegmentMessage entity) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      entity.validate();
      requireTopLevel(access);
      executeUpdate(connection, SEGMENT_MESSAGE, id, entity);
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  public void destroy(Access access, UUID id) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      requireLibrary(access);
      DAORecord.DSL(connection).deleteFrom(SEGMENT_MESSAGE)
        .where(SEGMENT_MESSAGE.ID.eq(id))
        .execute();
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  public SegmentMessage newInstance() {
    return new SegmentMessage();
  }

}
