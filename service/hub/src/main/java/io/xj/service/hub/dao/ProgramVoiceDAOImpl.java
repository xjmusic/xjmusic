// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.dao;

import com.google.inject.Inject;
import io.xj.lib.rest_api.PayloadFactory;
import io.xj.lib.rest_api.RestApiException;
import io.xj.lib.util.ValueException;
import io.xj.service.hub.HubException;
import io.xj.service.hub.access.Access;
import io.xj.service.hub.model.ProgramVoice;
import io.xj.service.hub.persistence.SQLDatabaseProvider;
import org.jooq.DSLContext;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.UUID;

import static io.xj.service.hub.Tables.LIBRARY;
import static io.xj.service.hub.Tables.PROGRAM;
import static io.xj.service.hub.Tables.PROGRAM_SEQUENCE_PATTERN;
import static io.xj.service.hub.Tables.PROGRAM_VOICE;

public class ProgramVoiceDAOImpl extends DAOImpl<ProgramVoice> implements ProgramVoiceDAO {

  @Inject
  public ProgramVoiceDAOImpl(
    PayloadFactory payloadFactory,
    SQLDatabaseProvider dbProvider
  ) {
    super(payloadFactory);
    this.dbProvider = dbProvider;
  }

  @Override
  public ProgramVoice create(Access access, ProgramVoice entity) throws HubException, RestApiException, ValueException {
    entity.validate();
    DSLContext db = dbProvider.getDSL();
    requireProgramModification(db, access, entity.getProgramId());
    return modelFrom(ProgramVoice.class,
      executeCreate(db, PROGRAM_VOICE, entity));
  }

  @Override
  @Nullable
  public ProgramVoice readOne(Access access, UUID id) throws HubException {
    requireArtist(access);
    return modelFrom(ProgramVoice.class,
      dbProvider.getDSL().selectFrom(PROGRAM_VOICE)
        .where(PROGRAM_VOICE.ID.eq(id))
        .fetchOne());
  }

  @Override
  @Nullable
  public Collection<ProgramVoice> readMany(Access access, Collection<UUID> parentIds) throws HubException {
    requireArtist(access);
    return modelsFrom(ProgramVoice.class,
      dbProvider.getDSL().selectFrom(PROGRAM_VOICE)
        .where(PROGRAM_VOICE.PROGRAM_ID.in(parentIds))
        .fetch());
  }

  @Override
  public void update(Access access, UUID id, ProgramVoice entity) throws HubException, RestApiException, ValueException {
    entity.validate();
    DSLContext db = dbProvider.getDSL();

    requireModification(db, access, id);

    executeUpdate(db, PROGRAM_VOICE, id, entity);
  }

  @Override
  public void destroy(Access access, UUID id) throws HubException {
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
   @throws HubException on invalid permissions
   */
  private void requireModification(DSLContext db, Access access, UUID id) throws HubException {
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
