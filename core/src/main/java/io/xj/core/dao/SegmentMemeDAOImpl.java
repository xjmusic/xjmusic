// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import com.google.inject.Inject;
import io.xj.core.access.Access;
import io.xj.core.exception.CoreException;
import io.xj.core.model.SegmentMeme;
import io.xj.core.persistence.sql.SQLDatabaseProvider;

import javax.annotation.Nullable;
import java.sql.Connection;
import java.sql.SQLException;
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
    try (Connection connection = dbProvider.getConnection()) {
      entity.validate();
      requireTopLevel(access);
      return DAORecord.modelFrom(SegmentMeme.class,
        executeCreate(connection, SEGMENT_MEME, entity));

    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  public void createMany(Access access, Collection<SegmentMeme> entities) throws CoreException {
    for (SegmentMeme entity : entities) entity.validate();
    requireTopLevel(access);

    try (Connection connection = dbProvider.getConnection()) {
      executeCreateMany(connection, SEGMENT_MEME, entities);

    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  @Nullable
  public SegmentMeme readOne(Access access, UUID id) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      requireUser(access);
      return DAORecord.modelFrom(SegmentMeme.class,
        DAORecord.DSL(connection).selectFrom(SEGMENT_MEME)
          .where(SEGMENT_MEME.ID.eq(id))
          .fetchOne());
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  @Nullable
  public Collection<SegmentMeme> readMany(Access access, Collection<UUID> parentIds) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      requireUser(access);
      return DAORecord.modelsFrom(SegmentMeme.class,
        DAORecord.DSL(connection).selectFrom(SEGMENT_MEME)
          .where(SEGMENT_MEME.SEGMENT_ID.in(parentIds))
          .fetch());
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  public void update(Access access, UUID id, SegmentMeme entity) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      entity.validate();
      requireTopLevel(access);
      executeUpdate(connection, SEGMENT_MEME, id, entity);
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  public void destroy(Access access, UUID id) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      requireLibrary(access);
      DAORecord.DSL(connection).deleteFrom(SEGMENT_MEME)
        .where(SEGMENT_MEME.ID.eq(id))
        .execute();
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  public SegmentMeme newInstance() {
    return new SegmentMeme();
  }

}
