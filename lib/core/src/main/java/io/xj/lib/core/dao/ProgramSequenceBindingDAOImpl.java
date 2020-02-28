// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.core.dao;

import com.google.inject.Inject;
import io.xj.lib.core.access.Access;
import io.xj.lib.core.exception.CoreException;
import io.xj.lib.core.model.ProgramSequenceBinding;
import io.xj.lib.core.persistence.SQLDatabaseProvider;
import org.jooq.DSLContext;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.UUID;

import static io.xj.lib.core.Tables.LIBRARY;
import static io.xj.lib.core.Tables.PROGRAM;
import static io.xj.lib.core.Tables.PROGRAM_SEQUENCE_BINDING;
import static io.xj.lib.core.Tables.PROGRAM_SEQUENCE_BINDING_MEME;

public class ProgramSequenceBindingDAOImpl extends DAOImpl<ProgramSequenceBinding> implements ProgramSequenceBindingDAO {

  @Inject
  public ProgramSequenceBindingDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public ProgramSequenceBinding create(Access access, ProgramSequenceBinding entity) throws CoreException {
    entity.validate();
    requireArtist(access);
    DSLContext db = dbProvider.getDSL();
    requireProgramModification(db, access, entity.getProgramId());
    return DAO.modelFrom(ProgramSequenceBinding.class,
      executeCreate(db, PROGRAM_SEQUENCE_BINDING, entity));

  }

  @Override
  @Nullable
  public ProgramSequenceBinding readOne(Access access, UUID id) throws CoreException {
    requireArtist(access);
    if (access.isTopLevel())
      return DAO.modelFrom(ProgramSequenceBinding.class,
        dbProvider.getDSL().selectFrom(PROGRAM_SEQUENCE_BINDING)
          .where(PROGRAM_SEQUENCE_BINDING.ID.eq(id))
          .fetchOne());
    else
      return DAO.modelFrom(ProgramSequenceBinding.class,
        dbProvider.getDSL().select(PROGRAM_SEQUENCE_BINDING.fields()).from(PROGRAM_SEQUENCE_BINDING)
          .join(PROGRAM).on(PROGRAM.ID.eq(PROGRAM_SEQUENCE_BINDING.PROGRAM_ID))
          .join(LIBRARY).on(LIBRARY.ID.eq(PROGRAM.LIBRARY_ID))
          .where(PROGRAM_SEQUENCE_BINDING.ID.eq(id))
          .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
          .fetchOne());
  }

  @Override
  @Nullable
  public Collection<ProgramSequenceBinding> readMany(Access access, Collection<UUID> parentIds) throws CoreException {
    requireArtist(access);
    if (access.isTopLevel())
      return DAO.modelsFrom(ProgramSequenceBinding.class,
        dbProvider.getDSL().selectFrom(PROGRAM_SEQUENCE_BINDING)
          .where(PROGRAM_SEQUENCE_BINDING.PROGRAM_SEQUENCE_ID.in(parentIds))
          .fetch());
    else
      return DAO.modelsFrom(ProgramSequenceBinding.class,
        dbProvider.getDSL().select(PROGRAM_SEQUENCE_BINDING.fields()).from(PROGRAM_SEQUENCE_BINDING)
          .join(PROGRAM).on(PROGRAM.ID.eq(PROGRAM_SEQUENCE_BINDING.PROGRAM_ID))
          .join(LIBRARY).on(LIBRARY.ID.eq(PROGRAM.LIBRARY_ID))
          .where(PROGRAM_SEQUENCE_BINDING.PROGRAM_SEQUENCE_ID.in(parentIds))
          .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
          .fetch());
  }

  @Override
  public void update(Access access, UUID id, ProgramSequenceBinding entity) throws CoreException {
    entity.validate();
    DSLContext db = dbProvider.getDSL();
    requireModification(db, access, id);
    executeUpdate(db, PROGRAM_SEQUENCE_BINDING, id, entity);
  }

  @Override
  public void destroy(Access access, UUID id) throws CoreException {
    requireArtist(access);
    DSLContext db = dbProvider.getDSL();
    requireModification(db, access, id);

    requireNotExists("Meme on Sequence Binding", db.selectCount().from(PROGRAM_SEQUENCE_BINDING_MEME)
      .where(PROGRAM_SEQUENCE_BINDING_MEME.PROGRAM_SEQUENCE_BINDING_ID.eq(id))
      .fetchOne(0, int.class));

    db.deleteFrom(PROGRAM_SEQUENCE_BINDING)
      .where(PROGRAM_SEQUENCE_BINDING.ID.eq(id))
      .execute();
  }

  @Override
  public ProgramSequenceBinding newInstance() {
    return new ProgramSequenceBinding();
  }

  /**
   Require access to modification of a Program Sequence Binding

   @param db     context
   @param access control
   @param id     to validate access to
   @throws CoreException if no access
   */
  private void requireModification(DSLContext db, Access access, UUID id) throws CoreException {
    requireArtist(access);
    if (access.isTopLevel())
      requireExists("Program Sequence Binding", db.selectCount().from(PROGRAM_SEQUENCE_BINDING)
        .where(PROGRAM_SEQUENCE_BINDING.ID.eq(id))
        .fetchOne(0, int.class));
    else
      requireExists("Sequence Binding in Program in Account you have access to", db.selectCount().from(PROGRAM_SEQUENCE_BINDING)
        .join(PROGRAM).on(PROGRAM.ID.eq(PROGRAM_SEQUENCE_BINDING.PROGRAM_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(PROGRAM.LIBRARY_ID))
        .where(PROGRAM_SEQUENCE_BINDING.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne(0, int.class));
  }

}
