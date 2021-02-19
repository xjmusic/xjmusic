// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.dao;

import com.google.inject.Inject;
import datadog.trace.api.Trace;
import io.xj.ProgramSequenceChord;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.entity.common.ChordEntity;
import io.xj.lib.jsonapi.JsonApiException;
import io.xj.lib.jsonapi.PayloadFactory;
import io.xj.lib.util.Value;
import io.xj.lib.util.ValueException;
import io.xj.service.hub.access.HubAccess;
import io.xj.service.hub.persistence.HubDatabaseProvider;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.UUID;

import static io.xj.service.hub.Tables.PROGRAM_SEQUENCE_CHORD;
import static io.xj.service.hub.tables.ProgramSequenceChordVoicing.PROGRAM_SEQUENCE_CHORD_VOICING;

public class ProgramSequenceChordDAOImpl extends DAOImpl<ProgramSequenceChord> implements ProgramSequenceChordDAO {

  @Inject
  public ProgramSequenceChordDAOImpl(
    PayloadFactory payloadFactory,
    EntityFactory entityFactory,
    HubDatabaseProvider dbProvider
  ) {
    super(payloadFactory, entityFactory);
    this.dbProvider = dbProvider;
  }

  @Override
  public ProgramSequenceChord create(HubAccess hubAccess, ProgramSequenceChord entity) throws DAOException, JsonApiException, ValueException {
    validate(entity);
    requireArtist(hubAccess);
    return modelFrom(ProgramSequenceChord.class,
      executeCreate(dbProvider.getDSL(), PROGRAM_SEQUENCE_CHORD, entity));

  }

  @Override
  @Nullable
  public ProgramSequenceChord readOne(HubAccess hubAccess, String id) throws DAOException {
    requireArtist(hubAccess);
    return modelFrom(ProgramSequenceChord.class,
      dbProvider.getDSL().selectFrom(PROGRAM_SEQUENCE_CHORD)
        .where(PROGRAM_SEQUENCE_CHORD.ID.eq(UUID.fromString(id)))
        .fetchOne());
  }

  @Override
  @Nullable
  public Collection<ProgramSequenceChord> readMany(HubAccess hubAccess, Collection<String> parentIds) throws DAOException {
    requireArtist(hubAccess);
    return modelsFrom(ProgramSequenceChord.class,
      dbProvider.getDSL().selectFrom(PROGRAM_SEQUENCE_CHORD)
        .where(PROGRAM_SEQUENCE_CHORD.PROGRAM_ID.in(parentIds))
        .fetch());
  }

  @Override
  public void update(HubAccess hubAccess, String id, ProgramSequenceChord entity) throws DAOException, JsonApiException, ValueException {
    validate(entity);
    requireArtist(hubAccess);
    executeUpdate(dbProvider.getDSL(), PROGRAM_SEQUENCE_CHORD, id, entity);
  }

  @Override
  public void destroy(HubAccess hubAccess, String id) throws DAOException {
    requireArtist(hubAccess);
    dbProvider.getDSL().deleteFrom(PROGRAM_SEQUENCE_CHORD_VOICING)
      .where(PROGRAM_SEQUENCE_CHORD_VOICING.PROGRAM_SEQUENCE_CHORD_ID.eq(UUID.fromString(id)))
      .execute();
    dbProvider.getDSL().deleteFrom(PROGRAM_SEQUENCE_CHORD)
      .where(PROGRAM_SEQUENCE_CHORD.ID.eq(UUID.fromString(id)))
      .execute();
  }

  @Override
  public ProgramSequenceChord newInstance() {
    return ProgramSequenceChord.getDefaultInstance();
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
