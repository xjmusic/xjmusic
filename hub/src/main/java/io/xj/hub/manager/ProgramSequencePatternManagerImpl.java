// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.manager;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import io.xj.hub.access.HubAccess;
import io.xj.hub.persistence.HubDatabaseProvider;
import io.xj.hub.persistence.HubPersistenceServiceImpl;
import io.xj.hub.tables.pojos.ProgramSequencePattern;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.JsonapiException;
import io.xj.lib.util.ValueException;
import io.xj.lib.util.Values;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static io.xj.hub.Tables.*;

public class ProgramSequencePatternManagerImpl extends HubPersistenceServiceImpl<ProgramSequencePattern> implements ProgramSequencePatternManager {

  @Inject
  public ProgramSequencePatternManagerImpl(
    EntityFactory entityFactory,
    HubDatabaseProvider dbProvider
  ) {
    super(entityFactory, dbProvider);
  }

  @Override
  public ProgramSequencePattern create(HubAccess hubAccess, ProgramSequencePattern entity) throws ManagerException, JsonapiException, ValueException {
    ProgramSequencePattern builder = validate(entity);
    DSLContext db = dbProvider.getDSL();
    requireProgramModification(db, hubAccess, builder.getProgramId());
    return modelFrom(ProgramSequencePattern.class,
      executeCreate(db, PROGRAM_SEQUENCE_PATTERN, builder));

  }

  @Override
  public ManagerCloner<ProgramSequencePattern> clone(HubAccess hubAccess, UUID cloneId, ProgramSequencePattern to) throws ManagerException {
    requireArtist(hubAccess);
    AtomicReference<ProgramSequencePattern> result = new AtomicReference<>();
    AtomicReference<ManagerCloner<ProgramSequencePattern>> cloner = new AtomicReference<>();
    dbProvider.getDSL().transaction(ctx -> {
      DSLContext db = DSL.using(ctx);
      requireModification(db, hubAccess, cloneId);

      var from = readOne(db, hubAccess, cloneId);
      if (Objects.isNull(from))
        throw new ManagerException("Can't clone nonexistent ProgramSequencePattern");

      // When not set, clone inherits attribute values from original record
      entityFactory.setAllEmptyAttributes(from, to);
      to.setTotal(from.getTotal()); // total cannot be modified while cloning
      var record = validate(to);
      requireParentExists(db, hubAccess, record);

      // Create main entity
      result.set(modelFrom(ProgramSequencePattern.class, executeCreate(db, PROGRAM_SEQUENCE_PATTERN, record)));

      // Prepare to clone sub-entities
      cloner.set(new ManagerCloner<>(result.get(), this));

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
  public ProgramSequencePattern readOne(HubAccess hubAccess, UUID id) throws ManagerException {
    return readOne(dbProvider.getDSL(), hubAccess, id);
  }

  @Override
  @Nullable
  public Collection<ProgramSequencePattern> readMany(HubAccess hubAccess, Collection<UUID> parentIds) throws ManagerException {
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
  public ProgramSequencePattern update(HubAccess hubAccess, UUID id, ProgramSequencePattern rawProgramSequencePattern) throws ManagerException, JsonapiException, ValueException {
    ProgramSequencePattern builder = validate(rawProgramSequencePattern);
    requireArtist(hubAccess);
    DSLContext db = dbProvider.getDSL();
    requireModification(db, hubAccess, id);
    executeUpdate(db, PROGRAM_SEQUENCE_PATTERN, id, builder);
    return builder;
  }

  @Override
  public void destroy(HubAccess hubAccess, UUID id) throws ManagerException {
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
  private ProgramSequencePattern readOne(DSLContext db, HubAccess hubAccess, UUID id) throws ManagerException {
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
   @throws ManagerException if parent does not exist
   */
  private void requireParentExists(DSLContext db, HubAccess hubAccess, ProgramSequencePattern entity) throws ManagerException {
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
   @throws ManagerException if no hubAccess
   */
  private void requireModification(DSLContext db, HubAccess hubAccess, UUID id) throws ManagerException {
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
   @throws ManagerException if invalid
   */
  public ProgramSequencePattern validate(ProgramSequencePattern record) throws ManagerException {
    try {
      Values.require(record.getProgramId(), "Program ID");
      Values.require(record.getProgramVoiceId(), "Voice ID");
      Values.require(record.getProgramSequenceId(), "Sequence ID");
      Values.require(record.getName(), "Name");
      Values.require(record.getTotal(), "Total");
      return record;

    } catch (ValueException e) {
      throw new ManagerException(e);
    }
  }


}
