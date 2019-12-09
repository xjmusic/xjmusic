// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import com.google.inject.Inject;
import io.xj.core.access.Access;
import io.xj.core.exception.CoreException;
import io.xj.core.model.SegmentChoiceArrangement;
import io.xj.core.persistence.sql.SQLDatabaseProvider;

import javax.annotation.Nullable;
import java.sql.Connection;
import java.sql.SQLException;
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
    try (Connection connection = dbProvider.getConnection()) {
      entity.validate();
      requireTopLevel(access);
      return DAORecord.modelFrom(SegmentChoiceArrangement.class,
        executeCreate(connection, SEGMENT_CHOICE_ARRANGEMENT, entity));

    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  public void createMany(Access access, Collection<SegmentChoiceArrangement> entities) throws CoreException {
    for (SegmentChoiceArrangement entity : entities) entity.validate();
    requireTopLevel(access);

    try (Connection connection = dbProvider.getConnection()) {
      executeCreateMany(connection, SEGMENT_CHOICE_ARRANGEMENT, entities);

    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  @Nullable
  public SegmentChoiceArrangement readOne(Access access, UUID id) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      requireUser(access);
      return DAORecord.modelFrom(SegmentChoiceArrangement.class,
        DAORecord.DSL(connection).selectFrom(SEGMENT_CHOICE_ARRANGEMENT)
          .where(SEGMENT_CHOICE_ARRANGEMENT.ID.eq(id))
          .fetchOne());
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  @Nullable
  public Collection<SegmentChoiceArrangement> readMany(Access access, Collection<UUID> parentIds) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      requireUser(access);
      return DAORecord.modelsFrom(SegmentChoiceArrangement.class,
        DAORecord.DSL(connection).selectFrom(SEGMENT_CHOICE_ARRANGEMENT)
          .where(SEGMENT_CHOICE_ARRANGEMENT.SEGMENT_ID.in(parentIds))
          .fetch());
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  public void update(Access access, UUID id, SegmentChoiceArrangement entity) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      entity.validate();
      requireTopLevel(access);
      executeUpdate(connection, SEGMENT_CHOICE_ARRANGEMENT, id, entity);
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  public void destroy(Access access, UUID id) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      requireLibrary(access);
      DAORecord.DSL(connection).deleteFrom(SEGMENT_CHOICE_ARRANGEMENT)
        .where(SEGMENT_CHOICE_ARRANGEMENT.ID.eq(id))
        .execute();
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  public SegmentChoiceArrangement newInstance() {
    return new SegmentChoiceArrangement();
  }

}
