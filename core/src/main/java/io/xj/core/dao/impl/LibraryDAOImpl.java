// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao.impl;

import com.google.inject.Inject;
import io.xj.core.access.Access;
import io.xj.core.dao.DAORecord;
import io.xj.core.dao.LibraryDAO;
import io.xj.core.exception.CoreException;
import io.xj.core.model.Library;
import io.xj.core.persistence.sql.SQLDatabaseProvider;
import org.jooq.DSLContext;

import javax.annotation.Nullable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

import static io.xj.core.Tables.LIBRARY;
import static io.xj.core.Tables.PROGRAM;
import static io.xj.core.tables.Account.ACCOUNT;
import static io.xj.core.tables.Instrument.INSTRUMENT;

public class LibraryDAOImpl extends DAOImpl<Library> implements LibraryDAO {

  @Inject
  public LibraryDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public Library create(Access access, Library entity) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      entity.validate();


      if (!access.isTopLevel())
        requireExists("Account",
          DAORecord.DSL(connection).selectCount().from(ACCOUNT)
            .where(ACCOUNT.ID.in(access.getAccountIds()))
            .fetchOne(0, int.class));

      return DAORecord.modelFrom(Library.class, executeCreate(connection, LIBRARY, entity));
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  @Nullable
  public Library readOne(Access access, UUID id) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      if (access.isTopLevel())
        return DAORecord.modelFrom(Library.class, DAORecord.DSL(connection).selectFrom(LIBRARY)
          .where(LIBRARY.ID.eq(id))
          .fetchOne());
      else
        return DAORecord.modelFrom(Library.class, DAORecord.DSL(connection).select(LIBRARY.fields())
          .from(LIBRARY)
          .where(LIBRARY.ID.eq(id))
          .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
          .fetchOne());

    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  public Collection<Library> readMany(Access access, Collection<UUID> parentIds) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      if (Objects.nonNull(parentIds) && !parentIds.isEmpty()) {
        if (access.isTopLevel())
          return DAORecord.modelsFrom(Library.class, DAORecord.DSL(connection).select(LIBRARY.fields())
            .from(LIBRARY)
            .where(LIBRARY.ACCOUNT_ID.in(parentIds))
            .fetch());
        else
          return DAORecord.modelsFrom(Library.class, DAORecord.DSL(connection).select(LIBRARY.fields())
            .from(LIBRARY)
            .where(LIBRARY.ACCOUNT_ID.in(parentIds))
            .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
            .fetch());
      } else {
        if (access.isTopLevel())
          return DAORecord.modelsFrom(Library.class, DAORecord.DSL(connection).select(LIBRARY.fields())
            .from(LIBRARY)
            .fetch());
        else
          return DAORecord.modelsFrom(Library.class, DAORecord.DSL(connection).select(LIBRARY.fields())
            .from(LIBRARY)
            .where(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
            .fetch());
      }

    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  public void update(Access access, UUID id, Library entity) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      entity.validate();

      entity.setId(id); //prevent changing id

      if (!access.isTopLevel()) {
        requireExists("Library",
          DAORecord.DSL(connection).selectCount().from(LIBRARY)
            .where(LIBRARY.ID.eq(id))
            .fetchOne(0, int.class));
        requireExists("Account",
          DAORecord.DSL(connection).selectCount().from(ACCOUNT)
            .where(ACCOUNT.ID.in(access.getAccountIds()))
            .fetchOne(0, int.class));
      }

      executeUpdate(connection, LIBRARY, id, entity);
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  public void destroy(Access access, UUID id) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      DSLContext db = DAORecord.DSL(connection);
      requireTopLevel(access);

      requireNotExists("Program in Library", db.select(PROGRAM.ID)
        .from(PROGRAM)
        .where(PROGRAM.LIBRARY_ID.eq(id))
        .fetch().into(PROGRAM));

      requireNotExists("Instrument in Library", db.select(INSTRUMENT.ID)
        .from(INSTRUMENT)
        .where(INSTRUMENT.LIBRARY_ID.eq(id))
        .fetch().into(INSTRUMENT));

      db.deleteFrom(LIBRARY)
        .where(LIBRARY.ID.eq(id))
        .andNotExists(
          db.select(PROGRAM.ID)
            .from(PROGRAM)
            .where(PROGRAM.LIBRARY_ID.eq(id))
        )
        .andNotExists(
          db.select(INSTRUMENT.ID)
            .from(INSTRUMENT)
            .where(INSTRUMENT.LIBRARY_ID.eq(id))
        )
        .execute();
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  public Library newInstance() {
    return new Library();
  }
}
