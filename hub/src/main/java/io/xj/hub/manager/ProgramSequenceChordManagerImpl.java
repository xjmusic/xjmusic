// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.manager;

import com.google.inject.Inject;
import io.xj.hub.access.HubAccess;
import io.xj.hub.persistence.HubDatabaseProvider;
import io.xj.hub.persistence.HubPersistenceServiceImpl;
import io.xj.hub.tables.pojos.ProgramSequenceChord;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.entity.common.ChordEntity;
import io.xj.lib.jsonapi.JsonapiException;
import io.xj.lib.util.ValueException;
import io.xj.lib.util.Values;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.UUID;

import static io.xj.hub.Tables.PROGRAM_SEQUENCE_CHORD;
import static io.xj.hub.tables.ProgramSequenceChordVoicing.PROGRAM_SEQUENCE_CHORD_VOICING;

public class ProgramSequenceChordManagerImpl extends HubPersistenceServiceImpl<ProgramSequenceChord> implements ProgramSequenceChordManager {

  @Inject
  public ProgramSequenceChordManagerImpl(
    EntityFactory entityFactory,
    HubDatabaseProvider dbProvider
  ) {
    super(entityFactory, dbProvider);
  }

  @Override
  public ProgramSequenceChord create(HubAccess hubAccess, ProgramSequenceChord entity) throws ManagerException, JsonapiException, ValueException {
    validate(entity);
    requireArtist(hubAccess);
    return modelFrom(ProgramSequenceChord.class,
      executeCreate(dbProvider.getDSL(), PROGRAM_SEQUENCE_CHORD, entity));

  }

  @Override
  @Nullable
  public ProgramSequenceChord readOne(HubAccess hubAccess, UUID id) throws ManagerException {
    requireArtist(hubAccess);
    return modelFrom(ProgramSequenceChord.class,
      dbProvider.getDSL().selectFrom(PROGRAM_SEQUENCE_CHORD)
        .where(PROGRAM_SEQUENCE_CHORD.ID.eq(id))
        .fetchOne());
  }

  @Override
  @Nullable
  public Collection<ProgramSequenceChord> readMany(HubAccess hubAccess, Collection<UUID> parentIds) throws ManagerException {
    requireArtist(hubAccess);
    return modelsFrom(ProgramSequenceChord.class,
      dbProvider.getDSL().selectFrom(PROGRAM_SEQUENCE_CHORD)
        .where(PROGRAM_SEQUENCE_CHORD.PROGRAM_ID.in(parentIds))
        .fetch());
  }

  @Override
  public ProgramSequenceChord update(HubAccess hubAccess, UUID id, ProgramSequenceChord entity) throws ManagerException, JsonapiException, ValueException {
    validate(entity);
    requireArtist(hubAccess);
    executeUpdate(dbProvider.getDSL(), PROGRAM_SEQUENCE_CHORD, id, entity);
    return entity;
  }

  @Override
  public void destroy(HubAccess hubAccess, UUID id) throws ManagerException {
    requireArtist(hubAccess);
    dbProvider.getDSL().deleteFrom(PROGRAM_SEQUENCE_CHORD_VOICING)
      .where(PROGRAM_SEQUENCE_CHORD_VOICING.PROGRAM_SEQUENCE_CHORD_ID.eq(id))
      .execute();
    dbProvider.getDSL().deleteFrom(PROGRAM_SEQUENCE_CHORD)
      .where(PROGRAM_SEQUENCE_CHORD.ID.eq(id))
      .execute();
  }

  @Override
  public ProgramSequenceChord newInstance() {
    return new ProgramSequenceChord();
  }

  /**
   Validate data

   @param record to validate
   @throws ManagerException if invalid
   */
  public void validate(ProgramSequenceChord record) throws ManagerException {
    try {
      Values.require(record.getProgramId(), "Program ID");
      Values.require(record.getProgramSequenceId(), "Sequence ID");
      ChordEntity.validate(record);

    } catch (ValueException e) {
      throw new ManagerException(e);
    }
  }

}
