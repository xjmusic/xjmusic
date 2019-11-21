// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao.impl;

import com.google.inject.Inject;
import io.xj.core.access.Access;
import io.xj.core.dao.DAORecord;
import io.xj.core.dao.SegmentChoiceDAO;
import io.xj.core.exception.CoreException;
import io.xj.core.model.ProgramType;
import io.xj.core.model.Segment;
import io.xj.core.model.SegmentChoice;
import io.xj.core.persistence.sql.SQLDatabaseProvider;

import javax.annotation.Nullable;
import java.sql.Connection;
import java.sql.SQLException;
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
    try (Connection connection = dbProvider.getConnection()) {
      entity.validate();
      requireTopLevel(access);
      return DAORecord.modelFrom(SegmentChoice.class,
        executeCreate(connection, SEGMENT_CHOICE, entity));

    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  public void createMany(Access access, Collection<SegmentChoice> entities) throws CoreException {
    for (SegmentChoice entity : entities) entity.validate();
    requireTopLevel(access);

    try (Connection connection = dbProvider.getConnection()) {
      executeCreateMany(connection, SEGMENT_CHOICE, entities);

    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  @Nullable
  public SegmentChoice readOne(Access access, UUID id) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      requireUser(access);
      return DAORecord.modelFrom(SegmentChoice.class,
        DAORecord.DSL(connection).selectFrom(SEGMENT_CHOICE)
          .where(SEGMENT_CHOICE.ID.eq(id))
          .fetchOne());
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  public SegmentChoice readOneOfTypeForSegment(Access access, Segment segment, ProgramType type) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      requireUser(access);
      return DAORecord.modelFrom(SegmentChoice.class,
        DAORecord.DSL(connection).selectFrom(SEGMENT_CHOICE)
          .where(SEGMENT_CHOICE.SEGMENT_ID.eq(segment.getId()))
          .and(SEGMENT_CHOICE.TYPE.eq(type.toString()))
          .fetchOne());
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  @Nullable
  public Collection<SegmentChoice> readMany(Access access, Collection<UUID> parentIds) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      requireUser(access);
      return DAORecord.modelsFrom(SegmentChoice.class,
        DAORecord.DSL(connection).selectFrom(SEGMENT_CHOICE)
          .where(SEGMENT_CHOICE.SEGMENT_ID.in(parentIds))
          .fetch());
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  public void update(Access access, UUID id, SegmentChoice entity) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      entity.validate();
      requireTopLevel(access);
      executeUpdate(connection, SEGMENT_CHOICE, id, entity);
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  public void destroy(Access access, UUID id) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      requireLibrary(access);
      DAORecord.DSL(connection).deleteFrom(SEGMENT_CHOICE)
        .where(SEGMENT_CHOICE.ID.eq(id))
        .execute();
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  public SegmentChoice newInstance() {
    return new SegmentChoice();
  }
}
