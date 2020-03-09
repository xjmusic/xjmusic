// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.core.dao;

import com.google.inject.Inject;
import io.xj.lib.core.access.Access;
import io.xj.lib.core.exception.CoreException;
import io.xj.lib.core.model.ProgramSequenceBindingMeme;
import io.xj.lib.core.persistence.SQLDatabaseProvider;
import org.jooq.DSLContext;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static io.xj.lib.core.Tables.LIBRARY;
import static io.xj.lib.core.Tables.PROGRAM;
import static io.xj.lib.core.Tables.PROGRAM_SEQUENCE_BINDING_MEME;

public class ProgramSequenceBindingMemeDAOImpl extends DAOImpl<ProgramSequenceBindingMeme> implements ProgramSequenceBindingMemeDAO {

  @Inject
  public ProgramSequenceBindingMemeDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public ProgramSequenceBindingMeme create(Access access, ProgramSequenceBindingMeme entity) throws CoreException {
    DSLContext db = dbProvider.getDSL();
    entity.validate();
    requireArtist(access);
    requireProgramModification(db, access, entity.getProgramId());

    return DAO.modelFrom(ProgramSequenceBindingMeme.class,
      executeCreate(db, PROGRAM_SEQUENCE_BINDING_MEME, entity));
  }

  @Override
  @Nullable
  public ProgramSequenceBindingMeme readOne(Access access, UUID id) throws CoreException {
    requireArtist(access);
    if (access.isTopLevel())
      return DAO.modelFrom(ProgramSequenceBindingMeme.class,
        dbProvider.getDSL().selectFrom(PROGRAM_SEQUENCE_BINDING_MEME)
          .where(PROGRAM_SEQUENCE_BINDING_MEME.ID.eq(id))
          .fetchOne());
    else
      return DAO.modelFrom(ProgramSequenceBindingMeme.class,
        dbProvider.getDSL().select(PROGRAM_SEQUENCE_BINDING_MEME.fields()).from(PROGRAM_SEQUENCE_BINDING_MEME)
          .join(PROGRAM).on(PROGRAM.ID.eq(PROGRAM_SEQUENCE_BINDING_MEME.PROGRAM_ID))
          .join(LIBRARY).on(LIBRARY.ID.eq(PROGRAM.LIBRARY_ID))
          .where(PROGRAM_SEQUENCE_BINDING_MEME.ID.eq(id))
          .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
          .fetchOne());
  }

  @Override
  @Nullable
  public Collection<ProgramSequenceBindingMeme> readMany(Access access, Collection<UUID> parentIds) throws CoreException {
    requireArtist(access);
    if (access.isTopLevel())
      return DAO.modelsFrom(ProgramSequenceBindingMeme.class,
        dbProvider.getDSL().selectFrom(PROGRAM_SEQUENCE_BINDING_MEME)
          .where(PROGRAM_SEQUENCE_BINDING_MEME.PROGRAM_SEQUENCE_BINDING_ID.in(parentIds))
          .fetch());
    else
      return DAO.modelsFrom(ProgramSequenceBindingMeme.class,
        dbProvider.getDSL().select(PROGRAM_SEQUENCE_BINDING_MEME.fields()).from(PROGRAM_SEQUENCE_BINDING_MEME)
          .join(PROGRAM).on(PROGRAM.ID.eq(PROGRAM_SEQUENCE_BINDING_MEME.PROGRAM_ID))
          .join(LIBRARY).on(LIBRARY.ID.eq(PROGRAM.LIBRARY_ID))
          .where(PROGRAM_SEQUENCE_BINDING_MEME.PROGRAM_SEQUENCE_BINDING_ID.in(parentIds))
          .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
          .fetch());
  }

  @Override
  public void update(Access access, UUID id, ProgramSequenceBindingMeme entity) throws CoreException {
    DSLContext db = dbProvider.getDSL();
    require("Same id", Objects.equals(id, entity.getId()));
    entity.validate();
    requireArtist(access);
    requireProgramModification(db, access, entity.getProgramId());

    executeUpdate(db, PROGRAM_SEQUENCE_BINDING_MEME, id, entity);
  }

  @Override
  public void destroy(Access access, UUID id) throws CoreException {
    DSLContext db = dbProvider.getDSL();
    requireArtist(access);

    if (!access.isTopLevel())
      requireExists("Meme belongs to Program in Account you have access to", db.selectCount()
        .from(PROGRAM_SEQUENCE_BINDING_MEME)
        .join(PROGRAM).on(PROGRAM.ID.eq(PROGRAM_SEQUENCE_BINDING_MEME.PROGRAM_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(PROGRAM.LIBRARY_ID))
        .where(PROGRAM_SEQUENCE_BINDING_MEME.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne(0, int.class));

    db.deleteFrom(PROGRAM_SEQUENCE_BINDING_MEME)
      .where(PROGRAM_SEQUENCE_BINDING_MEME.ID.eq(id))
      .execute();
  }

  @Override
  public ProgramSequenceBindingMeme newInstance() {
    return new ProgramSequenceBindingMeme();
  }

  @Override
  public Collection<ProgramSequenceBindingMeme> readAllForPrograms(Access access, Set<UUID> programIds) throws CoreException {
    requireArtist(access);
    if (access.isTopLevel())
      return DAO.modelsFrom(ProgramSequenceBindingMeme.class,
        dbProvider.getDSL().selectFrom(PROGRAM_SEQUENCE_BINDING_MEME)
          .where(PROGRAM_SEQUENCE_BINDING_MEME.PROGRAM_ID.in(programIds))
          .fetch());
    else
      return DAO.modelsFrom(ProgramSequenceBindingMeme.class,
        dbProvider.getDSL().select(PROGRAM_SEQUENCE_BINDING_MEME.fields()).from(PROGRAM_SEQUENCE_BINDING_MEME)
          .join(PROGRAM).on(PROGRAM.ID.eq(PROGRAM_SEQUENCE_BINDING_MEME.PROGRAM_ID))
          .join(LIBRARY).on(LIBRARY.ID.eq(PROGRAM.LIBRARY_ID))
          .where(PROGRAM_SEQUENCE_BINDING_MEME.PROGRAM_ID.in(programIds))
          .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
          .fetch());
  }

}
