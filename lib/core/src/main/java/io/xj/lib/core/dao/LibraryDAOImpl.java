// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.core.dao;

import com.google.inject.Inject;
import io.xj.lib.core.access.Access;
import io.xj.lib.core.exception.CoreException;
import io.xj.lib.core.model.Library;
import io.xj.lib.core.persistence.SQLDatabaseProvider;
import org.jooq.DSLContext;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

import static io.xj.lib.core.Tables.LIBRARY;
import static io.xj.lib.core.Tables.PROGRAM;
import static io.xj.lib.core.tables.Account.ACCOUNT;
import static io.xj.lib.core.tables.Instrument.INSTRUMENT;

public class LibraryDAOImpl extends DAOImpl<Library> implements LibraryDAO {

  @Inject
  public LibraryDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public Library create(Access access, Library entity) throws CoreException {
    entity.validate();

    if (!access.isTopLevel())
      requireExists("Account",
        dbProvider.getDSL().selectCount().from(ACCOUNT)
          .where(ACCOUNT.ID.in(access.getAccountIds()))
          .fetchOne(0, int.class));

    return DAO.modelFrom(Library.class, executeCreate(dbProvider.getDSL(), LIBRARY, entity));
  }

  @Override
  @Nullable
  public Library readOne(Access access, UUID id) throws CoreException {
    if (access.isTopLevel())
      return DAO.modelFrom(Library.class, dbProvider.getDSL().selectFrom(LIBRARY)
        .where(LIBRARY.ID.eq(id))
        .fetchOne());
    else
      return DAO.modelFrom(Library.class, dbProvider.getDSL().select(LIBRARY.fields())
        .from(LIBRARY)
        .where(LIBRARY.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne());

  }

  @Override
  public Collection<Library> readMany(Access access, Collection<UUID> parentIds) throws CoreException {
    if (Objects.nonNull(parentIds) && !parentIds.isEmpty()) {
      if (access.isTopLevel())
        return DAO.modelsFrom(Library.class, dbProvider.getDSL().select(LIBRARY.fields())
          .from(LIBRARY)
          .where(LIBRARY.ACCOUNT_ID.in(parentIds))
          .fetch());
      else
        return DAO.modelsFrom(Library.class, dbProvider.getDSL().select(LIBRARY.fields())
          .from(LIBRARY)
          .where(LIBRARY.ACCOUNT_ID.in(parentIds))
          .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
          .fetch());
    } else {
      if (access.isTopLevel())
        return DAO.modelsFrom(Library.class, dbProvider.getDSL().select(LIBRARY.fields())
          .from(LIBRARY)
          .fetch());
      else
        return DAO.modelsFrom(Library.class, dbProvider.getDSL().select(LIBRARY.fields())
          .from(LIBRARY)
          .where(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
          .fetch());
    }

  }

  @Override
  public void update(Access access, UUID id, Library entity) throws CoreException {
    entity.validate();

    entity.setId(id); //prevent changing id

    if (!access.isTopLevel()) {
      requireExists("Library",
        dbProvider.getDSL().selectCount().from(LIBRARY)
          .where(LIBRARY.ID.eq(id))
          .fetchOne(0, int.class));
      requireExists("Account",
        dbProvider.getDSL().selectCount().from(ACCOUNT)
          .where(ACCOUNT.ID.in(access.getAccountIds()))
          .fetchOne(0, int.class));
    }

    executeUpdate(dbProvider.getDSL(), LIBRARY, id, entity);
  }

  @Override
  public void destroy(Access access, UUID id) throws CoreException {
    DSLContext db = dbProvider.getDSL();
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
  }

  @Override
  public Library newInstance() {
    return new Library();
  }
}
