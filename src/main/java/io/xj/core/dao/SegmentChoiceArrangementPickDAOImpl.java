// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import com.google.inject.Inject;
import io.xj.core.access.Access;
import io.xj.core.exception.CoreException;
import io.xj.core.model.SegmentChoiceArrangementPick;
import io.xj.core.persistence.sql.SQLDatabaseProvider;

import javax.annotation.Nullable;
import java.sql.Connection;
import java.sql.SQLException;
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
    try (Connection connection = dbProvider.getConnection()) {
      entity.validate();
      requireTopLevel(access);
      return DAORecord.modelFrom(SegmentChoiceArrangementPick.class,
        executeCreate(connection, SEGMENT_CHOICE_ARRANGEMENT_PICK, entity));

    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  public void createMany(Access access, Collection<SegmentChoiceArrangementPick> entities) throws CoreException {
    for (SegmentChoiceArrangementPick entity : entities) entity.validate();
    requireTopLevel(access);

    try (Connection connection = dbProvider.getConnection()) {
      executeCreateMany(connection, SEGMENT_CHOICE_ARRANGEMENT_PICK, entities);

    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  @Nullable
  public SegmentChoiceArrangementPick readOne(Access access, UUID id) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      requireUser(access);
      return DAORecord.modelFrom(SegmentChoiceArrangementPick.class,
        DAORecord.DSL(connection).selectFrom(SEGMENT_CHOICE_ARRANGEMENT_PICK)
          .where(SEGMENT_CHOICE_ARRANGEMENT_PICK.ID.eq(id))
          .fetchOne());
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  @Nullable
  public Collection<SegmentChoiceArrangementPick> readMany(Access access, Collection<UUID> parentIds) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      requireUser(access);
      return DAORecord.modelsFrom(SegmentChoiceArrangementPick.class,
        DAORecord.DSL(connection).selectFrom(SEGMENT_CHOICE_ARRANGEMENT_PICK)
          .where(SEGMENT_CHOICE_ARRANGEMENT_PICK.SEGMENT_ID.in(parentIds))
          .fetch());
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  public void update(Access access, UUID id, SegmentChoiceArrangementPick entity) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      entity.validate();
      requireTopLevel(access);
      executeUpdate(connection, SEGMENT_CHOICE_ARRANGEMENT_PICK, id, entity);
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  public void destroy(Access access, UUID id) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      requireLibrary(access);
      DAORecord.DSL(connection).deleteFrom(SEGMENT_CHOICE_ARRANGEMENT_PICK)
        .where(SEGMENT_CHOICE_ARRANGEMENT_PICK.ID.eq(id))
        .execute();
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  public SegmentChoiceArrangementPick newInstance() {
    return new SegmentChoiceArrangementPick();
  }

}
