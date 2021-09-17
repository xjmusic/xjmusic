// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.dao;

import com.google.inject.Inject;
import io.xj.hub.tables.pojos.ProgramVoice;
import io.xj.hub.access.HubAccess;
import io.xj.hub.persistence.HubDatabaseProvider;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.JsonapiException;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.util.Value;
import io.xj.lib.util.ValueException;
import org.jooq.DSLContext;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.UUID;

import static io.xj.hub.Tables.LIBRARY;
import static io.xj.hub.Tables.PROGRAM;
import static io.xj.hub.Tables.PROGRAM_SEQUENCE_PATTERN;
import static io.xj.hub.Tables.PROGRAM_VOICE;

public class ProgramVoiceDAOImpl extends DAOImpl<ProgramVoice> implements ProgramVoiceDAO {
  private static final Float DEFAULT_ORDER_VALUE = 1000.0f;

  @Inject
  public ProgramVoiceDAOImpl(
    JsonapiPayloadFactory payloadFactory,
    EntityFactory entityFactory,
    HubDatabaseProvider dbProvider
  ) {
    super(payloadFactory, entityFactory);
    this.dbProvider = dbProvider;
  }

  @Override
  public ProgramVoice create(HubAccess hubAccess, ProgramVoice entity) throws DAOException, JsonapiException, ValueException {
    var record = validate(entity);
    DSLContext db = dbProvider.getDSL();
    requireProgramModification(db, hubAccess, record.getProgramId());
    return modelFrom(ProgramVoice.class,
      executeCreate(db, PROGRAM_VOICE, record));
  }

  @Override
  @Nullable
  public ProgramVoice readOne(HubAccess hubAccess, UUID id) throws DAOException {
    requireArtist(hubAccess);
    if (hubAccess.isTopLevel())
      return modelFrom(ProgramVoice.class,
        dbProvider.getDSL().selectFrom(PROGRAM_VOICE)
          .where(PROGRAM_VOICE.ID.eq(id))
          .fetchOne());
    else
      return modelFrom(ProgramVoice.class,
        dbProvider.getDSL().select(PROGRAM_VOICE.fields()).from(PROGRAM_VOICE)
          .join(PROGRAM).on(PROGRAM.ID.eq(PROGRAM_VOICE.PROGRAM_ID))
          .join(LIBRARY).on(LIBRARY.ID.eq(PROGRAM.LIBRARY_ID))
          .where(PROGRAM_VOICE.ID.eq(id))
          .and(LIBRARY.ACCOUNT_ID.in(hubAccess.getAccountIds()))
          .fetchOne());
  }

  @Override
  @Nullable
  public Collection<ProgramVoice> readMany(HubAccess hubAccess, Collection<UUID> parentIds) throws DAOException {
    requireArtist(hubAccess);
    if (hubAccess.isTopLevel())
      return modelsFrom(ProgramVoice.class,
        dbProvider.getDSL().selectFrom(PROGRAM_VOICE)
          .where(PROGRAM_VOICE.PROGRAM_ID.in(parentIds))
          .orderBy(PROGRAM_VOICE.ORDER.asc())
          .fetch());
    else
      return modelsFrom(ProgramVoice.class,
        dbProvider.getDSL().select(PROGRAM_VOICE.fields()).from(PROGRAM_VOICE)
          .join(PROGRAM).on(PROGRAM.ID.eq(PROGRAM_VOICE.PROGRAM_ID))
          .join(LIBRARY).on(LIBRARY.ID.eq(PROGRAM.LIBRARY_ID))
          .where(PROGRAM_VOICE.PROGRAM_ID.in(parentIds))
          .and(LIBRARY.ACCOUNT_ID.in(hubAccess.getAccountIds()))
          .orderBy(PROGRAM_VOICE.ORDER.asc())
          .fetch());
  }

  @Override
  public ProgramVoice update(HubAccess hubAccess, UUID id, ProgramVoice entity) throws DAOException, JsonapiException, ValueException {
    var record = validate(entity);
    DSLContext db = dbProvider.getDSL();

    requireModification(db, hubAccess, id);

    executeUpdate(db, PROGRAM_VOICE, id, record);
    return record;
  }

  @Override
  public void destroy(HubAccess hubAccess, UUID id) throws DAOException {
    DSLContext db = dbProvider.getDSL();

    requireModification(db, hubAccess, id);

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

   @param db        context
   @param hubAccess control
   @param id        of entity to require modification hubAccess to
   @throws DAOException on invalid permissions
   */
  private void requireModification(DSLContext db, HubAccess hubAccess, UUID id) throws DAOException {
    requireArtist(hubAccess);

    if (hubAccess.isTopLevel())
      requireExists("Voice", db.selectCount().from(PROGRAM_VOICE)
        .where(PROGRAM_VOICE.ID.eq(id))
        .fetchOne(0, int.class));
    else
      requireExists("Voice in Program in Account you have hubAccess to", db.selectCount().from(PROGRAM_VOICE)
        .join(PROGRAM).on(PROGRAM_VOICE.PROGRAM_ID.eq(PROGRAM.ID))
        .join(LIBRARY).on(PROGRAM.LIBRARY_ID.eq(LIBRARY.ID))
        .where(PROGRAM_VOICE.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(hubAccess.getAccountIds()))
        .fetchOne(0, int.class));
  }

  /**
   Validate data

   @param record to validate
   @throws DAOException if invalid
   */
  public ProgramVoice validate(ProgramVoice record) throws DAOException {
    try {
      if (Value.isEmpty(record.getOrder())) record.setOrder(DEFAULT_ORDER_VALUE);
      Value.require(record.getProgramId(), "Program ID");
      Value.require(record.getName(), "Name");
      Value.require(record.getType(), "Type");
      return record;

    } catch (ValueException e) {
      throw new DAOException(e);
    }
  }

}
