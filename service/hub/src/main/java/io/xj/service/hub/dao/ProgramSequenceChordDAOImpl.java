// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.dao;

import com.google.inject.Inject;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.PayloadFactory;
import io.xj.lib.jsonapi.JsonApiException;
import io.xj.lib.util.ValueException;
import io.xj.service.hub.access.HubAccess;
import io.xj.service.hub.entity.ProgramSequenceChord;
import io.xj.service.hub.persistence.HubDatabaseProvider;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.UUID;

import static io.xj.service.hub.Tables.PROGRAM_SEQUENCE_CHORD;

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
    entity.validate();
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
        .where(PROGRAM_SEQUENCE_CHORD.PROGRAM_SEQUENCE_ID.in(parentIds))
        .fetch());
  }

  @Override
  public void update(HubAccess hubAccess, UUID id, ProgramSequenceChord entity) throws DAOException, JsonApiException, ValueException {
    entity.validate();
    requireArtist(hubAccess);
    executeUpdate(dbProvider.getDSL(), PROGRAM_SEQUENCE_CHORD, id, entity);
  }

  @Override
  public void destroy(HubAccess hubAccess, UUID id) throws DAOException {
    requireArtist(hubAccess);
    dbProvider.getDSL().deleteFrom(PROGRAM_SEQUENCE_CHORD)
      .where(PROGRAM_SEQUENCE_CHORD.ID.eq(id))
      .execute();
  }

  @Override
  public ProgramSequenceChord newInstance() {
    return new ProgramSequenceChord();
  }

}
