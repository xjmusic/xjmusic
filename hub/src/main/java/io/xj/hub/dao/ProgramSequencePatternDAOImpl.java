// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.dao;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import io.xj.hub.tables.pojos.ProgramSequencePattern;
import io.xj.hub.access.HubAccess;
import io.xj.hub.persistence.HubDatabaseProvider;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.JsonapiException;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.util.Value;
import io.xj.lib.util.ValueException;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static io.xj.hub.Tables.LIBRARY;
import static io.xj.hub.Tables.PROGRAM;
import static io.xj.hub.Tables.PROGRAM_SEQUENCE;
import static io.xj.hub.Tables.PROGRAM_SEQUENCE_PATTERN;
import static io.xj.hub.Tables.PROGRAM_SEQUENCE_PATTERN_EVENT;

public class ProgramSequencePatternDAOImpl extends DAOImpl<ProgramSequencePattern> implements ProgramSequencePatternDAO {

  @Inject
  public ProgramSequencePatternDAOImpl(
    JsonapiPayloadFactory payloadFactory,
    EntityFactory entityFactory,
    HubDatabaseProvider dbProvider
  ) {
    super(payloadFactory, entityFactory);
    this.dbProvider = dbProvider;
  }

  @Override
  public ProgramSequencePattern create(HubAccess hubAccess, ProgramSequencePattern entity) throws DAOException, JsonapiException, ValueException {
    ProgramSequencePattern builder = validate(entity);
    DSLContext db = dbProvider.getDSL();
    requireProgramModification(db, hubAccess, builder.getProgramId());
    return modelFrom(ProgramSequencePattern.class,
      executeCreate(db, PROGRAM_SEQUENCE_PATTERN, builder));

  }

  @Override
  public DAOCloner<ProgramSequencePattern> clone(HubAccess hubAccess, UUID cloneId, ProgramSequencePattern entity) throws DAOException {
    requireArtist(hubAccess);
    AtomicReference<ProgramSequencePattern> result = new AtomicReference<>();
    AtomicReference<DAOCloner<ProgramSequencePattern>> cloner = new AtomicReference<>();
    dbProvider.getDSL().transaction(ctx -> {
      DSLContext db = DSL.using(ctx);
      requireModification(db, hubAccess, cloneId);

      var from = readOne(db, hubAccess, cloneId);
      if (Objects.isNull(from))
        throw new DAOException("Can't clone nonexistent ProgramSequencePattern");

      // Inherits parent's attributes if none specified
      entity.setTotal(from.getTotal());
      entity.setType(from.getType());
      if (Value.isUnset(entity.getName())) entity.setName(from.getName());
      if (Value.isUnset(entity.getProgramId())) entity.setProgramId(from.getProgramId());
      if (Value.isUnset(entity.getProgramSequenceId())) entity.setProgramSequenceId(from.getProgramSequenceId());
      if (Value.isUnset(entity.getProgramVoiceId())) entity.setProgramVoiceId(from.getProgramVoiceId());
      var record = validate(entity);
      requireParentExists(db, hubAccess, record);

      // Create main entity
      result.set(modelFrom(ProgramSequencePattern.class, executeCreate(db, PROGRAM_SEQUENCE_PATTERN, record)));

      // Prepare to clone sub-entities
      cloner.set(new DAOCloner<>(result.get(), this));

      // Clone ProgramSequencePatternEvent belongs to newly cloned ProgramSequencePattern and ProgramVoiceTrack
      cloner.get().clone(db, PROGRAM_SEQUENCE_PATTERN_EVENT, PROGRAM_SEQUENCE_PATTERN_EVENT.ID,
        ImmutableSet.of(PROGRAM_SEQUENCE_PATTERN_EVENT.PROGRAM_SEQUENCE_PATTERN_ID, PROGRAM_SEQUENCE_PATTERN_EVENT.PROGRAM_VOICE_TRACK_ID),
        PROGRAM_SEQUENCE_PATTERN_EVENT.PROGRAM_SEQUENCE_PATTERN_ID,
        cloneId, result.get().getId());

    });
    return cloner.get();
  }

  @Override
  @Nullable
  public ProgramSequencePattern readOne(HubAccess hubAccess, UUID id) throws DAOException {
    return readOne(dbProvider.getDSL(), hubAccess, id);
  }

  @Override
  @Nullable
  public Collection<ProgramSequencePattern> readMany(HubAccess hubAccess, Collection<UUID> parentIds) throws DAOException {
    requireArtist(hubAccess);
    if (hubAccess.isTopLevel())
      return modelsFrom(ProgramSequencePattern.class,
        dbProvider.getDSL().selectFrom(PROGRAM_SEQUENCE_PATTERN)
          .where(PROGRAM_SEQUENCE_PATTERN.PROGRAM_SEQUENCE_ID.in(parentIds))
          .fetch());
    else
      return modelsFrom(ProgramSequencePattern.class,
        dbProvider.getDSL().select(PROGRAM_SEQUENCE_PATTERN.fields()).from(PROGRAM_SEQUENCE_PATTERN)
          .join(PROGRAM).on(PROGRAM.ID.eq(PROGRAM_SEQUENCE_PATTERN.PROGRAM_ID))
          .join(LIBRARY).on(LIBRARY.ID.eq(PROGRAM.LIBRARY_ID))
          .where(PROGRAM_SEQUENCE_PATTERN.PROGRAM_SEQUENCE_ID.in(parentIds))
          .and(LIBRARY.ACCOUNT_ID.in(hubAccess.getAccountIds()))
          .fetch());

  }

  @Override
  public ProgramSequencePattern update(HubAccess hubAccess, UUID id, ProgramSequencePattern rawProgramSequencePattern) throws DAOException, JsonapiException, ValueException {
    ProgramSequencePattern builder = validate(rawProgramSequencePattern);
    requireArtist(hubAccess);
    DSLContext db = dbProvider.getDSL();
    requireModification(db, hubAccess, id);
    executeUpdate(db, PROGRAM_SEQUENCE_PATTERN, id, builder);
    return builder;
  }

  @Override
  public void destroy(HubAccess hubAccess, UUID id) throws DAOException {
    requireArtist(hubAccess);
    DSLContext db = dbProvider.getDSL();
    requireModification(db, hubAccess, id);

    db.deleteFrom(PROGRAM_SEQUENCE_PATTERN_EVENT)
      .where(PROGRAM_SEQUENCE_PATTERN_EVENT.PROGRAM_SEQUENCE_PATTERN_ID.eq(id))
      .execute();

    db.deleteFrom(PROGRAM_SEQUENCE_PATTERN)
      .where(PROGRAM_SEQUENCE_PATTERN.ID.eq(id))
      .execute();
  }

  @Override
  public ProgramSequencePattern newInstance() {
    return new ProgramSequencePattern();
  }

  /**
   Read one Program Sequence Pattern

   @param db        context
   @param hubAccess control
   @param id        of entity to read
   @return program sequence pattern
   */
  private ProgramSequencePattern readOne(DSLContext db, HubAccess hubAccess, UUID id) throws DAOException {
    requireArtist(hubAccess);
    if (hubAccess.isTopLevel())
      return modelFrom(ProgramSequencePattern.class,
        db.selectFrom(PROGRAM_SEQUENCE_PATTERN)
          .where(PROGRAM_SEQUENCE_PATTERN.ID.eq(id))
          .fetchOne());
    else
      return modelFrom(ProgramSequencePattern.class,
        db.select(PROGRAM_SEQUENCE_PATTERN.fields()).from(PROGRAM_SEQUENCE_PATTERN)
          .join(PROGRAM).on(PROGRAM.ID.eq(PROGRAM_SEQUENCE_PATTERN.PROGRAM_ID))
          .join(LIBRARY).on(LIBRARY.ID.eq(PROGRAM.LIBRARY_ID))
          .where(PROGRAM_SEQUENCE_PATTERN.ID.eq(id))
          .and(LIBRARY.ACCOUNT_ID.in(hubAccess.getAccountIds()))
          .fetchOne());
  }

  /**
   Require parent ProgramSequence exists of a given possible entity in a DSL context

   @param db        DSL context
   @param hubAccess control
   @param entity    to validate
   @throws DAOException if parent does not exist
   */
  private void requireParentExists(DSLContext db, HubAccess hubAccess, ProgramSequencePattern entity) throws DAOException {
    if (hubAccess.isTopLevel())
      requireExists("Program Sequence", db.selectCount().from(PROGRAM_SEQUENCE)
        .where(PROGRAM_SEQUENCE.ID.eq(entity.getProgramSequenceId()))
        .fetchOne(0, int.class));
    else
      requireExists("Program Sequence", db.selectCount().from(PROGRAM_SEQUENCE)
        .join(PROGRAM).on(PROGRAM.ID.eq(PROGRAM_SEQUENCE.PROGRAM_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(PROGRAM.LIBRARY_ID))
        .where(LIBRARY.ACCOUNT_ID.in(hubAccess.getAccountIds()))
        .and(PROGRAM_SEQUENCE.ID.eq(entity.getProgramSequenceId()))
        .fetchOne(0, int.class));
  }

  /**
   Require hubAccess to modification of a Program Sequence Pattern

   @param db        context
   @param hubAccess control
   @param id        to validate hubAccess to
   @throws DAOException if no hubAccess
   */
  private void requireModification(DSLContext db, HubAccess hubAccess, UUID id) throws DAOException {
    requireArtist(hubAccess);
    if (hubAccess.isTopLevel())
      requireExists("Program Sequence Pattern", db.selectCount().from(PROGRAM_SEQUENCE_PATTERN)
        .where(PROGRAM_SEQUENCE_PATTERN.ID.eq(id))
        .fetchOne(0, int.class));
    else
      requireExists("Sequence Pattern in Program in Account you have hubAccess to", db.selectCount().from(PROGRAM_SEQUENCE_PATTERN)
        .join(PROGRAM).on(PROGRAM.ID.eq(PROGRAM_SEQUENCE_PATTERN.PROGRAM_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(PROGRAM.LIBRARY_ID))
        .where(PROGRAM_SEQUENCE_PATTERN.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(hubAccess.getAccountIds()))
        .fetchOne(0, int.class));
  }

  /**
   Validate data

   @param record to validate
   @throws DAOException if invalid
   */
  public ProgramSequencePattern validate(ProgramSequencePattern record) throws DAOException {
    try {
      Value.require(record.getProgramId(), "Program ID");
      Value.require(record.getProgramVoiceId(), "Voice ID");
      Value.require(record.getProgramSequenceId(), "Sequence ID");
      Value.require(record.getName(), "Name");
      Value.require(record.getTotal(), "Total");
      Value.require(record.getType(), "Type");
      return record;

    } catch (ValueException e) {
      throw new DAOException(e);
    }
  }


}
