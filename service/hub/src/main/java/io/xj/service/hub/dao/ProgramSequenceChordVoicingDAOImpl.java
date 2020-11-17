// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.dao;

import com.google.inject.Inject;
import io.xj.ProgramSequenceChordVoicing;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.JsonApiException;
import io.xj.lib.jsonapi.PayloadFactory;
import io.xj.lib.util.Value;
import io.xj.lib.util.ValueException;
import io.xj.service.hub.access.HubAccess;
import io.xj.service.hub.persistence.HubDatabaseProvider;
import org.jooq.DSLContext;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

import static io.xj.service.hub.Tables.LIBRARY;
import static io.xj.service.hub.Tables.PROGRAM;
import static io.xj.service.hub.Tables.PROGRAM_SEQUENCE_CHORD_VOICING;

public class ProgramSequenceChordVoicingDAOImpl extends DAOImpl<ProgramSequenceChordVoicing> implements ProgramSequenceChordVoicingDAO {

  @Inject
  public ProgramSequenceChordVoicingDAOImpl(
    PayloadFactory payloadFactory,
    EntityFactory entityFactory,
    HubDatabaseProvider dbProvider
  ) {
    super(payloadFactory, entityFactory);
    this.dbProvider = dbProvider;
  }

  @Override
  public ProgramSequenceChordVoicing create(HubAccess hubAccess, ProgramSequenceChordVoicing entity) throws DAOException, JsonApiException, ValueException {
    DSLContext db = dbProvider.getDSL();
    validate(entity);
    requireArtist(hubAccess);
    requireProgramModification(db, hubAccess, entity.getProgramId());

    return modelFrom(ProgramSequenceChordVoicing.class,
      executeCreate(db, PROGRAM_SEQUENCE_CHORD_VOICING, entity));
  }

  @Override
  @Nullable
  public ProgramSequenceChordVoicing readOne(HubAccess hubAccess, String id) throws DAOException {
    requireArtist(hubAccess);
    if (hubAccess.isTopLevel())
      return modelFrom(ProgramSequenceChordVoicing.class,
        dbProvider.getDSL().selectFrom(PROGRAM_SEQUENCE_CHORD_VOICING)
          .where(PROGRAM_SEQUENCE_CHORD_VOICING.ID.eq(UUID.fromString(id)))
          .fetchOne());
    else
      return modelFrom(ProgramSequenceChordVoicing.class,
        dbProvider.getDSL().select(PROGRAM_SEQUENCE_CHORD_VOICING.fields()).from(PROGRAM_SEQUENCE_CHORD_VOICING)
          .join(PROGRAM).on(PROGRAM.ID.eq(PROGRAM_SEQUENCE_CHORD_VOICING.PROGRAM_ID))
          .join(LIBRARY).on(LIBRARY.ID.eq(PROGRAM.LIBRARY_ID))
          .where(PROGRAM_SEQUENCE_CHORD_VOICING.ID.eq(UUID.fromString(id)))
          .and(LIBRARY.ACCOUNT_ID.in(hubAccess.getAccountIds()))
          .fetchOne());
  }

  @Override
  @Nullable
  public Collection<ProgramSequenceChordVoicing> readMany(HubAccess hubAccess, Collection<String> programIds) throws DAOException {
    requireArtist(hubAccess);
    if (hubAccess.isTopLevel())
      return modelsFrom(ProgramSequenceChordVoicing.class,
        dbProvider.getDSL().selectFrom(PROGRAM_SEQUENCE_CHORD_VOICING)
          .where(PROGRAM_SEQUENCE_CHORD_VOICING.PROGRAM_ID.in(programIds))
          .fetch());
    else
      return modelsFrom(ProgramSequenceChordVoicing.class,
        dbProvider.getDSL().select(PROGRAM_SEQUENCE_CHORD_VOICING.fields()).from(PROGRAM_SEQUENCE_CHORD_VOICING)
          .join(PROGRAM).on(PROGRAM.ID.eq(PROGRAM_SEQUENCE_CHORD_VOICING.PROGRAM_ID))
          .join(LIBRARY).on(LIBRARY.ID.eq(PROGRAM.LIBRARY_ID))
          .where(PROGRAM_SEQUENCE_CHORD_VOICING.PROGRAM_ID.in(programIds))
          .and(LIBRARY.ACCOUNT_ID.in(hubAccess.getAccountIds()))
          .fetch());
  }

  @Override
  public void update(HubAccess hubAccess, String id, ProgramSequenceChordVoicing entity) throws DAOException, JsonApiException, ValueException {
    DSLContext db = dbProvider.getDSL();
    require("Same id", Objects.equals(id, entity.getId()));
    validate(entity);
    requireArtist(hubAccess);
    requireProgramModification(db, hubAccess, entity.getProgramId());

    executeUpdate(db, PROGRAM_SEQUENCE_CHORD_VOICING, id, entity);
  }

  @Override
  public void destroy(HubAccess hubAccess, String id) throws DAOException {
    DSLContext db = dbProvider.getDSL();
    requireArtist(hubAccess);

    if (!hubAccess.isTopLevel())
      requireExists("Voicing belongs to Program in Account you have hubAccess to", db.selectCount()
        .from(PROGRAM_SEQUENCE_CHORD_VOICING)
        .join(PROGRAM).on(PROGRAM.ID.eq(PROGRAM_SEQUENCE_CHORD_VOICING.PROGRAM_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(PROGRAM.LIBRARY_ID))
        .where(PROGRAM_SEQUENCE_CHORD_VOICING.ID.eq(UUID.fromString(id)))
        .and(LIBRARY.ACCOUNT_ID.in(hubAccess.getAccountIds()))
        .fetchOne(0, int.class));

    db.deleteFrom(PROGRAM_SEQUENCE_CHORD_VOICING)
      .where(PROGRAM_SEQUENCE_CHORD_VOICING.ID.eq(UUID.fromString(id)))
      .execute();
  }

  @Override
  public ProgramSequenceChordVoicing newInstance() {
    return ProgramSequenceChordVoicing.getDefaultInstance();
  }

  /**
   Validate data

   @param record to validate
   @throws DAOException if invalid
   */
  public void validate(ProgramSequenceChordVoicing record) throws DAOException {
    try {
      Value.require(record.getProgramId(), "Program ID");
      Value.require(record.getProgramSequenceChordId(), "Sequence Chord ID");
      Value.require(record.getType(), "Voice type");
      Value.require(record.getNotes(), "Notes are required");

    } catch (ValueException e) {
      throw new DAOException(e);
    }
  }


}
