// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import com.google.inject.Inject;
import io.xj.core.access.Access;
import io.xj.core.exception.CoreException;
import io.xj.core.model.ProgramVoice;
import io.xj.core.persistence.SQLDatabaseProvider;
import org.jooq.DSLContext;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.UUID;

import static io.xj.core.Tables.*;

public class ProgramVoiceDAOImpl extends DAOImpl<ProgramVoice> implements ProgramVoiceDAO {

  @Inject
  public ProgramVoiceDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public ProgramVoice create(Access access, ProgramVoice entity) throws CoreException {
    entity.validate();
    DSLContext db = dbProvider.getDSL();
    requireProgramModification(db, access, entity.getProgramId());
    return DAO.modelFrom(ProgramVoice.class,
      executeCreate(db, PROGRAM_VOICE, entity));
  }

  @Override
  @Nullable
  public ProgramVoice readOne(Access access, UUID id) throws CoreException {
    requireArtist(access);
    return DAO.modelFrom(ProgramVoice.class,
      dbProvider.getDSL().selectFrom(PROGRAM_VOICE)
        .where(PROGRAM_VOICE.ID.eq(id))
        .fetchOne());
  }

  @Override
  @Nullable
  public Collection<ProgramVoice> readMany(Access access, Collection<UUID> parentIds) throws CoreException {
    requireArtist(access);
    return DAO.modelsFrom(ProgramVoice.class,
      dbProvider.getDSL().selectFrom(PROGRAM_VOICE)
        .where(PROGRAM_VOICE.PROGRAM_ID.in(parentIds))
        .fetch());
  }

  @Override
  public void update(Access access, UUID id, ProgramVoice entity) throws CoreException {
    entity.validate();
    DSLContext db = dbProvider.getDSL();

    requireModification(db, access, id);

    executeUpdate(db, PROGRAM_VOICE, id, entity);
  }

  @Override
  public void destroy(Access access, UUID id) throws CoreException {
    DSLContext db = dbProvider.getDSL();

    requireModification(db, access, id);

    requireNotExists("Pattern in Voice", db.selectCount().from(PROGRAM_SEQUENCE_PATTERN)
      .where(PROGRAM_SEQUENCE_PATTERN.PROGRAM_VOICE_ID.eq(id))
      .fetchOne(0, int.class));

    db.deleteFrom(PROGRAM_VOICE)
      .where(PROGRAM_VOICE.ID.eq(id))
      .execute();
  }

  @Override
  public ProgramVoice newInstance() {
    return new ProgramVoice();
  }

  /**
   Require permission to modify the specified program voice

   @param db     context
   @param access control
   @param id     of entity to require modification access to
   @throws CoreException on invalid permissions
   */
  private void requireModification(DSLContext db, Access access, UUID id) throws CoreException {
    requireArtist(access);

    if (access.isTopLevel())
      requireExists("Voice", db.selectCount().from(PROGRAM_VOICE)
        .where(PROGRAM_VOICE.ID.eq(id))
        .fetchOne(0, int.class));
    else
      requireExists("Voice in Program in Account you have access to", db.selectCount().from(PROGRAM_VOICE)
        .join(PROGRAM).on(PROGRAM_VOICE.PROGRAM_ID.eq(PROGRAM.ID))
        .join(LIBRARY).on(PROGRAM.LIBRARY_ID.eq(LIBRARY.ID))
        .where(PROGRAM_VOICE.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne(0, int.class));
  }

}
