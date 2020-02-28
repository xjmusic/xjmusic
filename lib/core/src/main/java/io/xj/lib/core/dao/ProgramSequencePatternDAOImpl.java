// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.core.dao;

import com.google.inject.Inject;
import io.xj.lib.core.access.Access;
import io.xj.lib.core.exception.CoreException;
import io.xj.lib.core.model.ProgramSequencePattern;
import io.xj.lib.core.persistence.SQLDatabaseProvider;
import org.jooq.DSLContext;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.UUID;

import static io.xj.lib.core.Tables.LIBRARY;
import static io.xj.lib.core.Tables.PROGRAM;
import static io.xj.lib.core.Tables.PROGRAM_SEQUENCE_PATTERN;
import static io.xj.lib.core.Tables.PROGRAM_SEQUENCE_PATTERN_EVENT;

public class ProgramSequencePatternDAOImpl extends DAOImpl<ProgramSequencePattern> implements ProgramSequencePatternDAO {

  @Inject
  public ProgramSequencePatternDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public ProgramSequencePattern create(Access access, ProgramSequencePattern entity) throws CoreException {
    entity.validate();
    DSLContext db = dbProvider.getDSL();
    requireProgramModification(db, access, entity.getProgramId());
    return DAO.modelFrom(ProgramSequencePattern.class,
      executeCreate(db, PROGRAM_SEQUENCE_PATTERN, entity));

  }

  @Override
  @Nullable
  public ProgramSequencePattern readOne(Access access, UUID id) throws CoreException {
    requireArtist(access);
    if (access.isTopLevel())
      return DAO.modelFrom(ProgramSequencePattern.class,
        dbProvider.getDSL().selectFrom(PROGRAM_SEQUENCE_PATTERN)
          .where(PROGRAM_SEQUENCE_PATTERN.ID.eq(id))
          .fetchOne());
    else
      return DAO.modelFrom(ProgramSequencePattern.class,
        dbProvider.getDSL().select(PROGRAM_SEQUENCE_PATTERN.fields()).from(PROGRAM_SEQUENCE_PATTERN)
          .join(PROGRAM).on(PROGRAM.ID.eq(PROGRAM_SEQUENCE_PATTERN.PROGRAM_ID))
          .join(LIBRARY).on(LIBRARY.ID.eq(PROGRAM.LIBRARY_ID))
          .where(PROGRAM_SEQUENCE_PATTERN.ID.eq(id))
          .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
          .fetchOne());
  }

  @Override
  @Nullable
  public Collection<ProgramSequencePattern> readMany(Access access, Collection<UUID> parentIds) throws CoreException {
    requireArtist(access);
    if (access.isTopLevel())
      return DAO.modelsFrom(ProgramSequencePattern.class,
        dbProvider.getDSL().selectFrom(PROGRAM_SEQUENCE_PATTERN)
          .where(PROGRAM_SEQUENCE_PATTERN.PROGRAM_SEQUENCE_ID.in(parentIds))
          .fetch());
    else
      return DAO.modelsFrom(ProgramSequencePattern.class,
        dbProvider.getDSL().select(PROGRAM_SEQUENCE_PATTERN.fields()).from(PROGRAM_SEQUENCE_PATTERN)
          .join(PROGRAM).on(PROGRAM.ID.eq(PROGRAM_SEQUENCE_PATTERN.PROGRAM_ID))
          .join(LIBRARY).on(LIBRARY.ID.eq(PROGRAM.LIBRARY_ID))
          .where(PROGRAM_SEQUENCE_PATTERN.PROGRAM_SEQUENCE_ID.in(parentIds))
          .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
          .fetch());

  }

  @Override
  public void update(Access access, UUID id, ProgramSequencePattern entity) throws CoreException {
    entity.validate();
    requireArtist(access);
    DSLContext db = dbProvider.getDSL();
    requireModification(db, access, id);
    executeUpdate(db, PROGRAM_SEQUENCE_PATTERN, id, entity);
  }

  @Override
  public void destroy(Access access, UUID id) throws CoreException {
    requireArtist(access);
    DSLContext db = dbProvider.getDSL();
    requireModification(db, access, id);

    db.deleteFrom(PROGRAM_SEQUENCE_PATTERN_EVENT)
      .where(PROGRAM_SEQUENCE_PATTERN_EVENT.PROGRAM_SEQUENCE_PATTERN_ID.eq(id))
      .execute();

    db.deleteFrom(PROGRAM_SEQUENCE_PATTERN)
      .where(PROGRAM_SEQUENCE_PATTERN.ID.eq(id))
      .execute();
  }

  @Override
  public ProgramSequencePattern newInstance() {
    return new ProgramSequencePattern();
  }

  /**
   Require access to modification of a Program Sequence Pattern

   @param db     context
   @param access control
   @param id     to validate access to
   @throws CoreException if no access
   */
  private void requireModification(DSLContext db, Access access, UUID id) throws CoreException {
    requireArtist(access);
    if (access.isTopLevel())
      requireExists("Program Sequence Pattern", db.selectCount().from(PROGRAM_SEQUENCE_PATTERN)
        .where(PROGRAM_SEQUENCE_PATTERN.ID.eq(id))
        .fetchOne(0, int.class));
    else
      requireExists("Sequence Pattern in Program in Account you have access to", db.selectCount().from(PROGRAM_SEQUENCE_PATTERN)
        .join(PROGRAM).on(PROGRAM.ID.eq(PROGRAM_SEQUENCE_PATTERN.PROGRAM_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(PROGRAM.LIBRARY_ID))
        .where(PROGRAM_SEQUENCE_PATTERN.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne(0, int.class));
  }

}
