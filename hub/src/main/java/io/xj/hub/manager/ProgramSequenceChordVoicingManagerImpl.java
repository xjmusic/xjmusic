// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.manager;

import com.google.inject.Inject;
import io.xj.hub.access.HubAccess;
import io.xj.hub.persistence.HubDatabaseProvider;
import io.xj.hub.persistence.HubPersistenceServiceImpl;
import io.xj.hub.tables.pojos.ProgramSequenceChordVoicing;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.JsonapiException;
import io.xj.lib.util.ValueException;
import io.xj.lib.util.Values;
import org.jooq.DSLContext;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

import static io.xj.hub.Tables.*;

public class ProgramSequenceChordVoicingManagerImpl extends HubPersistenceServiceImpl<ProgramSequenceChordVoicing> implements ProgramSequenceChordVoicingManager {

  @Inject
  public ProgramSequenceChordVoicingManagerImpl(
    EntityFactory entityFactory,
    HubDatabaseProvider dbProvider
  ) {
    super(entityFactory, dbProvider);
  }

  @Override
  public ProgramSequenceChordVoicing create(HubAccess hubAccess, ProgramSequenceChordVoicing entity) throws ManagerException, JsonapiException, ValueException {
    DSLContext db = dbProvider.getDSL();
    validate(entity);
    requireArtist(hubAccess);
    requireProgramModification(db, hubAccess, entity.getProgramId());
    requireNotExists(String.format("Can't create another %s-type voicing for this chord!", entity.getType()),
      db.select(PROGRAM_SEQUENCE_CHORD_VOICING.ID)
        .from(PROGRAM_SEQUENCE_CHORD_VOICING)
        .where(PROGRAM_SEQUENCE_CHORD_VOICING.PROGRAM_SEQUENCE_CHORD_ID.eq(entity.getProgramSequenceChordId()))
        .and(PROGRAM_SEQUENCE_CHORD_VOICING.TYPE.eq(entity.getType()))
        .fetch());

    return modelFrom(ProgramSequenceChordVoicing.class,
      executeCreate(db, PROGRAM_SEQUENCE_CHORD_VOICING, entity));
  }

  @Override
  @Nullable
  public ProgramSequenceChordVoicing readOne(HubAccess hubAccess, UUID id) throws ManagerException {
    requireArtist(hubAccess);
    if (hubAccess.isTopLevel())
      return modelFrom(ProgramSequenceChordVoicing.class,
        dbProvider.getDSL().selectFrom(PROGRAM_SEQUENCE_CHORD_VOICING)
          .where(PROGRAM_SEQUENCE_CHORD_VOICING.ID.eq(id))
          .fetchOne());
    else
      return modelFrom(ProgramSequenceChordVoicing.class,
        dbProvider.getDSL().select(PROGRAM_SEQUENCE_CHORD_VOICING.fields()).from(PROGRAM_SEQUENCE_CHORD_VOICING)
          .join(PROGRAM).on(PROGRAM.ID.eq(PROGRAM_SEQUENCE_CHORD_VOICING.PROGRAM_ID))
          .join(LIBRARY).on(LIBRARY.ID.eq(PROGRAM.LIBRARY_ID))
          .where(PROGRAM_SEQUENCE_CHORD_VOICING.ID.eq(id))
          .and(LIBRARY.ACCOUNT_ID.in(hubAccess.getAccountIds()))
          .fetchOne());
  }

  @Override
  @Nullable
  public Collection<ProgramSequenceChordVoicing> readMany(HubAccess hubAccess, Collection<UUID> programIds) throws ManagerException {
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
  public ProgramSequenceChordVoicing update(HubAccess hubAccess, UUID id, ProgramSequenceChordVoicing entity) throws ManagerException, JsonapiException, ValueException {
    DSLContext db = dbProvider.getDSL();
    requireAny("Same id", Objects.equals(id, entity.getId()));
    validate(entity);
    requireArtist(hubAccess);
    requireProgramModification(db, hubAccess, entity.getProgramId());
    requireNotExists(String.format("Can't change to %s-type voicing for this chord because it already exists!", entity.getType()),
      db.select(PROGRAM_SEQUENCE_CHORD_VOICING.ID)
        .from(PROGRAM_SEQUENCE_CHORD_VOICING)
        .where(PROGRAM_SEQUENCE_CHORD_VOICING.PROGRAM_SEQUENCE_CHORD_ID.eq(entity.getProgramSequenceChordId()))
        .and(PROGRAM_SEQUENCE_CHORD_VOICING.TYPE.eq(entity.getType()))
        .and(PROGRAM_SEQUENCE_CHORD_VOICING.ID.notEqual(id))
        .fetch());
    executeUpdate(db, PROGRAM_SEQUENCE_CHORD_VOICING, id, entity);
    return entity;
  }

  @Override
  public void destroy(HubAccess hubAccess, UUID id) throws ManagerException {
    DSLContext db = dbProvider.getDSL();
    requireArtist(hubAccess);

    if (!hubAccess.isTopLevel())
      requireExists("Voicing belongs to Program in Account you have access to", db.selectCount()
        .from(PROGRAM_SEQUENCE_CHORD_VOICING)
        .join(PROGRAM).on(PROGRAM.ID.eq(PROGRAM_SEQUENCE_CHORD_VOICING.PROGRAM_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(PROGRAM.LIBRARY_ID))
        .where(PROGRAM_SEQUENCE_CHORD_VOICING.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(hubAccess.getAccountIds()))
        .fetchOne(0, int.class));

    db.deleteFrom(PROGRAM_SEQUENCE_CHORD_VOICING)
      .where(PROGRAM_SEQUENCE_CHORD_VOICING.ID.eq(id))
      .execute();
  }

  @Override
  public ProgramSequenceChordVoicing newInstance() {
    return new ProgramSequenceChordVoicing();
  }

  /**
   Validate data

   @param record to validate
   @throws ManagerException if invalid
   */
  public void validate(ProgramSequenceChordVoicing record) throws ManagerException {
    try {
      Values.require(record.getProgramId(), "Program ID");
      Values.require(record.getProgramSequenceChordId(), "Sequence Chord ID");
      Values.require(record.getType(), "Voice type");
      Values.require(record.getNotes(), "Notes are required");

    } catch (ValueException e) {
      throw new ManagerException(e);
    }
  }
}
