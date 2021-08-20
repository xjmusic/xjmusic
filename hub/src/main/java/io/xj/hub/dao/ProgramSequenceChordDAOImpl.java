// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.dao;

import com.google.inject.Inject;
import io.xj.api.ProgramSequenceChord;
import io.xj.hub.access.HubAccess;
import io.xj.hub.persistence.HubDatabaseProvider;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.entity.common.ChordEntity;
import io.xj.lib.jsonapi.JsonapiException;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.util.Value;
import io.xj.lib.util.ValueException;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.UUID;

import static io.xj.hub.Tables.PROGRAM_SEQUENCE_CHORD;
import static io.xj.hub.tables.ProgramSequenceChordVoicing.PROGRAM_SEQUENCE_CHORD_VOICING;

public class ProgramSequenceChordDAOImpl extends DAOImpl<ProgramSequenceChord> implements ProgramSequenceChordDAO {

  @Inject
  public ProgramSequenceChordDAOImpl(
    JsonapiPayloadFactory payloadFactory,
    EntityFactory entityFactory,
    HubDatabaseProvider dbProvider
  ) {
    super(payloadFactory, entityFactory);
    this.dbProvider = dbProvider;
  }

  @Override
  public ProgramSequenceChord create(HubAccess hubAccess, ProgramSequenceChord entity) throws DAOException, JsonapiException, ValueException {
    validate(entity);
    requireArtist(hubAccess);
    return modelFrom(ProgramSequenceChord.class,
      executeCreate(dbProvider.getDSL(), PROGRAM_SEQUENCE_CHORD, entity));

  }

  @Override
  @Nullable
  public ProgramSequenceChord readOne(HubAccess hubAccess, UUID id) throws DAOException {
    requireArtist(hubAccess);
    return modelFrom(ProgramSequenceChord.class,
      dbProvider.getDSL().selectFrom(PROGRAM_SEQUENCE_CHORD)
        .where(PROGRAM_SEQUENCE_CHORD.ID.eq(id))
        .fetchOne());
  }

  @Override
  @Nullable
  public Collection<ProgramSequenceChord> readMany(HubAccess hubAccess, Collection<UUID> parentIds) throws DAOException {
    requireArtist(hubAccess);
    return modelsFrom(ProgramSequenceChord.class,
      dbProvider.getDSL().selectFrom(PROGRAM_SEQUENCE_CHORD)
        .where(PROGRAM_SEQUENCE_CHORD.PROGRAM_ID.in(parentIds))
        .fetch());
  }

  @Override
  public ProgramSequenceChord update(HubAccess hubAccess, UUID id, ProgramSequenceChord entity) throws DAOException, JsonapiException, ValueException {
    validate(entity);
    requireArtist(hubAccess);
    executeUpdate(dbProvider.getDSL(), PROGRAM_SEQUENCE_CHORD, id, entity);
    return entity;
  }

  @Override
  public void destroy(HubAccess hubAccess, UUID id) throws DAOException {
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
   @throws DAOException if invalid
   */
  public void validate(ProgramSequenceChord record) throws DAOException {
    try {
      Value.require(record.getProgramId(), "Program ID");
      Value.require(record.getProgramSequenceId(), "Sequence ID");
      ChordEntity.validate(record);

    } catch (ValueException e) {
      throw new DAOException(e);
    }
  }

}
