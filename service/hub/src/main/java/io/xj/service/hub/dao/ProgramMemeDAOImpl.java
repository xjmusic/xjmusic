// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.dao;

import com.google.inject.Inject;
import io.xj.lib.rest_api.PayloadFactory;
import io.xj.lib.rest_api.RestApiException;
import io.xj.lib.util.ValueException;
import io.xj.service.hub.HubException;
import io.xj.service.hub.access.Access;
import io.xj.service.hub.model.ProgramMeme;
import io.xj.service.hub.persistence.SQLDatabaseProvider;
import org.jooq.DSLContext;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.UUID;

import static io.xj.service.hub.Tables.LIBRARY;
import static io.xj.service.hub.Tables.PROGRAM;
import static io.xj.service.hub.Tables.PROGRAM_MEME;

public class ProgramMemeDAOImpl extends DAOImpl<ProgramMeme> implements ProgramMemeDAO {

  @Inject
  public ProgramMemeDAOImpl(
    PayloadFactory payloadFactory,
    SQLDatabaseProvider dbProvider
  ) {
    super(payloadFactory);
    this.dbProvider = dbProvider;
  }

  @Override
  public ProgramMeme create(Access access, ProgramMeme entity) throws HubException, RestApiException, ValueException {
    entity.validate();
    requireArtist(access);
    DSLContext db = dbProvider.getDSL();
    requireProgramModification(db, access, entity.getProgramId());
    return modelFrom(ProgramMeme.class,
      executeCreate(db, PROGRAM_MEME, entity));

  }

  @Override
  @Nullable
  public ProgramMeme readOne(Access access, UUID id) throws HubException {
    requireArtist(access);
    DSLContext db = dbProvider.getDSL();
    return readOne(db, access, id);
  }

  @Override
  @Nullable
  public Collection<ProgramMeme> readMany(Access access, Collection<UUID> parentIds) throws HubException {
    requireArtist(access);
    if (access.isTopLevel())
      return modelsFrom(ProgramMeme.class,
        dbProvider.getDSL().selectFrom(PROGRAM_MEME)
          .where(PROGRAM_MEME.PROGRAM_ID.in(parentIds))
          .fetch());
    else
      return modelsFrom(ProgramMeme.class,
        dbProvider.getDSL().select(PROGRAM_MEME.fields()).from(PROGRAM_MEME)
          .join(PROGRAM).on(PROGRAM.ID.eq(PROGRAM_MEME.PROGRAM_ID))
          .join(LIBRARY).on(LIBRARY.ID.eq(PROGRAM.LIBRARY_ID))
          .where(PROGRAM_MEME.PROGRAM_ID.in(parentIds))
          .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
          .fetch());
  }

  @Override
  public void update(Access access, UUID id, ProgramMeme entity) throws HubException, RestApiException, ValueException {
    entity.validate();
    requireArtist(access);
    DSLContext db = dbProvider.getDSL();
    ProgramMeme original = readOne(db, access, id);
    entity.setProgramId(original.getProgramId());
    executeUpdate(db, PROGRAM_MEME, id, entity);
  }

  @Override
  public void destroy(Access access, UUID id) throws HubException {
    requireArtist(access);
    DSLContext db = dbProvider.getDSL();
    requireModification(db, access, id);
    db.deleteFrom(PROGRAM_MEME)
      .where(PROGRAM_MEME.ID.eq(id))
      .execute();
  }

  @Override
  public ProgramMeme newInstance() {
    return new ProgramMeme();
  }

  /**
   Read one Program Meme that have permissions to

   @param db     context
   @param access control
   @param id     of entity to read
   @return Program Meme
   @throws HubException on failure
   */
  private ProgramMeme readOne(DSLContext db, Access access, UUID id) throws HubException {
    if (access.isTopLevel())
      return modelFrom(ProgramMeme.class,
        db.selectFrom(PROGRAM_MEME)
          .where(PROGRAM_MEME.ID.eq(id))
          .fetchOne());
    else
      return modelFrom(ProgramMeme.class,
        db.select(PROGRAM_MEME.fields()).from(PROGRAM_MEME)
          .join(PROGRAM).on(PROGRAM.ID.eq(PROGRAM_MEME.PROGRAM_ID))
          .join(LIBRARY).on(LIBRARY.ID.eq(PROGRAM.LIBRARY_ID))
          .where(PROGRAM_MEME.ID.eq(id))
          .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
          .fetchOne());
  }

  /**
   Require access to modification of a Program Meme

   @param db     context
   @param access control
   @param id     to validate access to
   @throws HubException if no access
   */
  private void requireModification(DSLContext db, Access access, UUID id) throws HubException {
    requireArtist(access);
    if (access.isTopLevel())
      requireExists("Program Meme", db.selectCount().from(PROGRAM_MEME)
        .where(PROGRAM_MEME.ID.eq(id))
        .fetchOne(0, int.class));
    else
      requireExists("Meme in Program in Account you have access to", db.selectCount().from(PROGRAM_MEME)
        .join(PROGRAM).on(PROGRAM.ID.eq(PROGRAM_MEME.PROGRAM_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(PROGRAM.LIBRARY_ID))
        .where(PROGRAM_MEME.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne(0, int.class));
  }

}
