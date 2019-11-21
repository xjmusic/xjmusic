// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao.impl;

import com.google.inject.Inject;
import io.xj.core.access.Access;
import io.xj.core.dao.DAORecord;
import io.xj.core.dao.PlatformMessageDAO;
import io.xj.core.exception.CoreException;
import io.xj.core.model.PlatformMessage;
import io.xj.core.model.UserRoleType;
import io.xj.core.persistence.sql.SQLDatabaseProvider;
import org.jooq.DSLContext;

import javax.annotation.Nullable;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collection;
import java.util.UUID;

import static io.xj.core.Tables.PLATFORM_MESSAGE;

public class PlatformMessageDAOImpl extends DAOImpl<PlatformMessage> implements PlatformMessageDAO {
  private static final Long secondsPerDay = 86400L;

  @Inject
  public PlatformMessageDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public PlatformMessage create(Access access, PlatformMessage entity) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      requireRole("platform access", access, UserRoleType.Admin, UserRoleType.Engineer);

      entity.validate();

      return DAORecord.modelFrom(PlatformMessage.class, executeCreate(connection, PLATFORM_MESSAGE, entity));
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  public PlatformMessage readOne(Access access, UUID id) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      requireRole("platform access", access, UserRoleType.Admin, UserRoleType.Engineer);

      return DAORecord.modelFrom(PlatformMessage.class, DAORecord.DSL(connection).selectFrom(PLATFORM_MESSAGE)
        .where(PLATFORM_MESSAGE.ID.eq(id))
        .fetchOne());
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  public Collection<PlatformMessage> readMany(Access access, Collection<UUID> parentIds) throws CoreException {
    throw new CoreException("Not allowed to read all Platform Messages (must specify # previous days).");

  }

  @Override
  public void update(Access access, UUID id, PlatformMessage entity) throws CoreException {
    throw new CoreException("Not allowed to update PlatformMessage record.");
  }

  @Override
  @Nullable
  public Collection<PlatformMessage> readAllPreviousDays(Access access, Integer previousDays) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      requireRole("platform access", access, UserRoleType.Admin, UserRoleType.Engineer);

      return DAORecord.modelsFrom(PlatformMessage.class, DAORecord.DSL(connection).select(PLATFORM_MESSAGE.fields())
        .from(PLATFORM_MESSAGE)
        .where(PLATFORM_MESSAGE.CREATED_AT.ge(Timestamp.from(Instant.now().minusSeconds(previousDays * secondsPerDay))))
        .orderBy(PLATFORM_MESSAGE.CREATED_AT.desc())
        .fetch());
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  public void destroy(Access access, UUID id) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      DSLContext db = DAORecord.DSL(connection);
      requireTopLevel(access);

      requireExists("PlatformMessage", db.selectCount().from(PLATFORM_MESSAGE)
        .where(PLATFORM_MESSAGE.ID.eq(id))
        .fetchOne(0, int.class));

      db.deleteFrom(PLATFORM_MESSAGE)
        .where(PLATFORM_MESSAGE.ID.eq(id))
        .execute();
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  public PlatformMessage newInstance() {
    return new PlatformMessage();
  }


}
