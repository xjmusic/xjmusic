// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.manager;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import io.xj.hub.access.HubAccess;
import io.xj.hub.persistence.HubDatabaseProvider;
import io.xj.hub.persistence.HubPersistenceServiceImpl;
import io.xj.hub.tables.pojos.ProgramSequence;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.JsonapiException;
import io.xj.lib.util.ValueException;
import io.xj.lib.util.Values;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static io.xj.hub.Tables.*;
import static io.xj.hub.tables.ProgramSequenceChordVoicing.PROGRAM_SEQUENCE_CHORD_VOICING;

public class ProgramSequenceManagerImpl extends HubPersistenceServiceImpl<ProgramSequence> implements ProgramSequenceManager {

  @Inject
  public ProgramSequenceManagerImpl(
    EntityFactory entityFactory,
    HubDatabaseProvider dbProvider
  ) {
    super(entityFactory, dbProvider);
  }

  @Override
  public ProgramSequence create(HubAccess hubAccess, ProgramSequence entity) throws ManagerException, JsonapiException, ValueException {
    ProgramSequence builder = validate(entity);
    DSLContext db = dbProvider.getDSL();
    requireProgramModification(db, hubAccess, builder.getProgramId());
    return modelFrom(ProgramSequence.class,
      executeCreate(dbProvider.getDSL(), PROGRAM_SEQUENCE, builder));
  }

  @Override
  public ManagerCloner<ProgramSequence> clone(HubAccess hubAccess, UUID cloneId, ProgramSequence to) throws ManagerException {
    requireArtist(hubAccess);
    AtomicReference<ProgramSequence> result = new AtomicReference<>();
    AtomicReference<ManagerCloner<ProgramSequence>> cloner = new AtomicReference<>();
    dbProvider.getDSL().transaction(ctx -> {
      DSLContext db = DSL.using(ctx);
      requireModification(db, hubAccess, cloneId);

      var from = readOne(db, hubAccess, cloneId);
      if (Objects.isNull(from))
        throw new ManagerException("Can't clone nonexistent ProgramSequence");

      // Inherits these attributes if none specified
      // When not set, clone inherits attribute values from original record
      entityFactory.setAllEmptyAttributes(from, to);
      var record = validate(to);
      requireParentExists(db, hubAccess, record);

      // Create main entity
      result.set(modelFrom(ProgramSequence.class, executeCreate(db, PROGRAM_SEQUENCE, record)));

      // Prepare to clone sub-entities
      cloner.set(new ManagerCloner<>(result.get(), this));

      // Clone ProgramSequenceChord belongs to ProgramSequence
      cloner.get().clone(db, PROGRAM_SEQUENCE_CHORD, PROGRAM_SEQUENCE_CHORD.ID,
        ImmutableSet.of(PROGRAM_SEQUENCE_CHORD.PROGRAM_SEQUENCE_ID),
        PROGRAM_SEQUENCE_CHORD.PROGRAM_SEQUENCE_ID, cloneId, result.get().getId());

      // Clone ProgramSequenceBinding belongs to ProgramSequence
      Map<UUID, UUID> clonedProgramSequenceBindings = cloner.get().clone(db, PROGRAM_SEQUENCE_BINDING, PROGRAM_SEQUENCE_BINDING.ID,
        ImmutableSet.of(PROGRAM_SEQUENCE_BINDING.PROGRAM_SEQUENCE_ID),
        PROGRAM_SEQUENCE_BINDING.PROGRAM_SEQUENCE_ID, cloneId, result.get().getId());

      // Clone ProgramSequenceBindingMeme belongs to newly cloned ProgramSequenceBindings
      for (UUID originalId : clonedProgramSequenceBindings.keySet())
        cloner.get().clone(db, PROGRAM_SEQUENCE_BINDING_MEME, PROGRAM_SEQUENCE_BINDING_MEME.ID,
          ImmutableSet.of(PROGRAM_SEQUENCE_BINDING_MEME.PROGRAM_SEQUENCE_BINDING_ID),
          PROGRAM_SEQUENCE_BINDING_MEME.PROGRAM_SEQUENCE_BINDING_ID,
          originalId, clonedProgramSequenceBindings.get(originalId));

      // Clone ProgramSequencePattern belongs to ProgramSequence and ProgramVoice
      Map<UUID, UUID> clonedProgramSequencePatterns = cloner.get().clone(db, PROGRAM_SEQUENCE_PATTERN, PROGRAM_SEQUENCE_PATTERN.ID,
        ImmutableSet.of(PROGRAM_SEQUENCE_PATTERN.PROGRAM_SEQUENCE_ID, PROGRAM_SEQUENCE_PATTERN.PROGRAM_VOICE_ID),
        PROGRAM_SEQUENCE_PATTERN.PROGRAM_SEQUENCE_ID, cloneId, result.get().getId());

      // Clone ProgramSequencePatternEvent belongs to newly cloned ProgramSequencePattern and ProgramVoiceTrack
      for (UUID originalId : clonedProgramSequencePatterns.keySet())
        cloner.get().clone(db, PROGRAM_SEQUENCE_PATTERN_EVENT, PROGRAM_SEQUENCE_PATTERN_EVENT.ID,
          ImmutableSet.of(PROGRAM_SEQUENCE_PATTERN_EVENT.PROGRAM_SEQUENCE_PATTERN_ID, PROGRAM_SEQUENCE_PATTERN_EVENT.PROGRAM_VOICE_TRACK_ID),
          PROGRAM_SEQUENCE_PATTERN_EVENT.PROGRAM_SEQUENCE_PATTERN_ID,
          originalId, clonedProgramSequencePatterns.get(originalId));

    });
    return cloner.get();
  }

  @Override
  @Nullable
  public ProgramSequence readOne(HubAccess hubAccess, UUID id) throws ManagerException {
    requireArtist(hubAccess);
    return readOne(dbProvider.getDSL(), hubAccess, id);
  }

  @Override
  @Nullable
  public Collection<ProgramSequence> readMany(HubAccess hubAccess, Collection<UUID> parentIds) throws ManagerException {
    requireArtist(hubAccess);
    if (hubAccess.isTopLevel())
      return modelsFrom(ProgramSequence.class,
        dbProvider.getDSL().selectFrom(PROGRAM_SEQUENCE)
          .where(PROGRAM_SEQUENCE.PROGRAM_ID.in(parentIds))
          .fetch());
    else
      return modelsFrom(ProgramSequence.class,
        dbProvider.getDSL().select(PROGRAM_SEQUENCE.fields()).from(PROGRAM_SEQUENCE)
          .join(PROGRAM).on(PROGRAM.ID.eq(PROGRAM_SEQUENCE.PROGRAM_ID))
          .join(LIBRARY).on(LIBRARY.ID.eq(PROGRAM.LIBRARY_ID))
          .where(PROGRAM_SEQUENCE.PROGRAM_ID.in(parentIds))
          .and(LIBRARY.ACCOUNT_ID.in(hubAccess.getAccountIds()))
          .fetch());
  }

  @Override
  public ProgramSequence update(HubAccess hubAccess, UUID id, ProgramSequence ramProgramSequence) throws ManagerException, JsonapiException, ValueException {
    ProgramSequence builder = validate(ramProgramSequence);
    DSLContext db = dbProvider.getDSL();
    requireModification(db, hubAccess, id);
    executeUpdate(db, PROGRAM_SEQUENCE, id, builder);
    return builder;
  }

  @Override
  public void destroy(HubAccess hubAccess, UUID id) throws ManagerException {
    DSLContext db = dbProvider.getDSL();
    requireModification(db, hubAccess, id);

    // Delete all ProgramSequenceBindingMeme
    db.deleteFrom(PROGRAM_SEQUENCE_BINDING_MEME)
      .where(PROGRAM_SEQUENCE_BINDING_MEME.PROGRAM_SEQUENCE_BINDING_ID.in(
        db.select(PROGRAM_SEQUENCE_BINDING.ID).from(PROGRAM_SEQUENCE_BINDING)
          .where(PROGRAM_SEQUENCE_BINDING.PROGRAM_SEQUENCE_ID.eq(id))))
      .execute();

    // Delete all ProgramSequenceBinding
    db.deleteFrom(PROGRAM_SEQUENCE_BINDING)
      .where(PROGRAM_SEQUENCE_BINDING.PROGRAM_SEQUENCE_ID.eq(id))
      .execute();

    // Delete all ProgramSequenceChordVoicing
    db.deleteFrom(PROGRAM_SEQUENCE_CHORD_VOICING)
      .where(PROGRAM_SEQUENCE_CHORD_VOICING.PROGRAM_SEQUENCE_CHORD_ID.in(
        db.select(PROGRAM_SEQUENCE_CHORD.ID).from(PROGRAM_SEQUENCE_CHORD)
          .where(PROGRAM_SEQUENCE_CHORD.PROGRAM_SEQUENCE_ID.eq(id))))
      .execute();

    // Delete all ProgramSequenceChord
    db.deleteFrom(PROGRAM_SEQUENCE_CHORD)
      .where(PROGRAM_SEQUENCE_CHORD.PROGRAM_SEQUENCE_ID.eq(id))
      .execute();

    // Delete all ProgramSequencePatternEvent
    db.deleteFrom(PROGRAM_SEQUENCE_PATTERN_EVENT)
      .where(PROGRAM_SEQUENCE_PATTERN_EVENT.PROGRAM_SEQUENCE_PATTERN_ID.in(
        db.select(PROGRAM_SEQUENCE_PATTERN.ID).from(PROGRAM_SEQUENCE_PATTERN)
          .where(PROGRAM_SEQUENCE_PATTERN.PROGRAM_SEQUENCE_ID.eq(id))))
      .execute();

    // Delete all ProgramSequencePattern
    db.deleteFrom(PROGRAM_SEQUENCE_PATTERN)
      .where(PROGRAM_SEQUENCE_PATTERN.PROGRAM_SEQUENCE_ID.eq(id))
      .execute();

    // Delete all ProgramSequence
    db.deleteFrom(PROGRAM_SEQUENCE)
      .where(PROGRAM_SEQUENCE.ID.eq(id))
      .execute();
  }

  @Override
  public ProgramSequence newInstance() {
    return new ProgramSequence();
  }

  /**
   Require permission to modify the specified program sequence

   @param db        context
   @param hubAccess control
   @param id        of entity to require modification access to
   @throws ManagerException on invalid permissions
   */
  private void requireModification(DSLContext db, HubAccess hubAccess, UUID id) throws ManagerException {
    requireArtist(hubAccess);

    if (hubAccess.isTopLevel())
      requireExists("Sequence", db.selectCount().from(PROGRAM_SEQUENCE)
        .where(PROGRAM_SEQUENCE.ID.eq(id))
        .fetchOne(0, int.class));
    else
      requireExists("Sequence in Program in Account you have access to", db.selectCount().from(PROGRAM_SEQUENCE)
        .join(PROGRAM).on(PROGRAM_SEQUENCE.PROGRAM_ID.eq(PROGRAM.ID))
        .join(LIBRARY).on(PROGRAM.LIBRARY_ID.eq(LIBRARY.ID))
        .where(PROGRAM_SEQUENCE.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(hubAccess.getAccountIds()))
        .fetchOne(0, int.class));
  }

  /**
   Require parent ProgramSequence exists of a given possible entity in a DSL context

   @param db        DSL context
   @param hubAccess control
   @param entity    to validate
   @throws ManagerException if parent does not exist
   */
  private void requireParentExists(DSLContext db, HubAccess hubAccess, ProgramSequence entity) throws ManagerException {
    if (hubAccess.isTopLevel())
      requireExists("Program", db.selectCount().from(PROGRAM)
        .where(PROGRAM.ID.eq(entity.getProgramId()))
        .fetchOne(0, int.class));
    else
      requireExists("Program", db.selectCount().from(PROGRAM)
        .join(LIBRARY).on(LIBRARY.ID.eq(PROGRAM.LIBRARY_ID))
        .where(LIBRARY.ACCOUNT_ID.in(hubAccess.getAccountIds()))
        .and(PROGRAM.ID.eq(entity.getProgramId()))
        .fetchOne(0, int.class));
  }

  /**
   Read one Program Sequence

   @param db        context
   @param hubAccess control
   @param id        of entity to read
   @return program sequence
   */
  private ProgramSequence readOne(DSLContext db, HubAccess hubAccess, UUID id) throws ManagerException {
    if (hubAccess.isTopLevel())
      return modelFrom(ProgramSequence.class,
        db.selectFrom(PROGRAM_SEQUENCE)
          .where(PROGRAM_SEQUENCE.ID.eq(id))
          .fetchOne());
    else
      return modelFrom(ProgramSequence.class,
        db.select(PROGRAM_SEQUENCE.fields()).from(PROGRAM_SEQUENCE)
          .join(PROGRAM).on(PROGRAM.ID.eq(PROGRAM_SEQUENCE.PROGRAM_ID))
          .join(LIBRARY).on(LIBRARY.ID.eq(PROGRAM.LIBRARY_ID))
          .where(PROGRAM_SEQUENCE.ID.eq(id))
          .and(LIBRARY.ACCOUNT_ID.in(hubAccess.getAccountIds()))
          .fetchOne());
  }

  /**
   validate data

   @param record to validate
   @throws ManagerException if invalid
   */
  public ProgramSequence validate(ProgramSequence record) throws ManagerException {
    try {
      Values.require(record.getProgramId(), "Program ID");
      Values.require(record.getName(), "Name");
      Values.require(record.getKey(), "Key");
      Values.require(record.getDensity(), "Density");
      if (Values.isEmpty(record.getTotal())) record.setTotal((short) 0);
      return record;

    } catch (ValueException e) {
      throw new ManagerException(e);
    }
  }

}
