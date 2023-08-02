// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.manager;

import java.util.Set;
import io.xj.hub.access.HubAccess;
import io.xj.hub.persistence.HubPersistenceServiceImpl;
import io.xj.hub.persistence.HubSqlStoreProvider;
import io.xj.hub.tables.pojos.ProgramSequence;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.JsonapiException;
import io.xj.lib.util.ValueException;
import io.xj.lib.util.ValueUtils;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static io.xj.hub.Tables.LIBRARY;
import static io.xj.hub.Tables.PROGRAM;
import static io.xj.hub.Tables.PROGRAM_SEQUENCE;
import static io.xj.hub.Tables.PROGRAM_SEQUENCE_BINDING;
import static io.xj.hub.Tables.PROGRAM_SEQUENCE_BINDING_MEME;
import static io.xj.hub.Tables.PROGRAM_SEQUENCE_CHORD;
import static io.xj.hub.Tables.PROGRAM_SEQUENCE_PATTERN;
import static io.xj.hub.Tables.PROGRAM_SEQUENCE_PATTERN_EVENT;
import static io.xj.hub.tables.ProgramSequenceChordVoicing.PROGRAM_SEQUENCE_CHORD_VOICING;

@Service
public class ProgramSequenceManagerImpl extends HubPersistenceServiceImpl implements ProgramSequenceManager {

  public ProgramSequenceManagerImpl(
    EntityFactory entityFactory,
    HubSqlStoreProvider sqlStoreProvider
  ) {
    super(entityFactory, sqlStoreProvider);
  }

  @Override
  public ProgramSequence create(HubAccess access, ProgramSequence entity) throws ManagerException, JsonapiException, ValueException {
    ProgramSequence builder = validate(entity);
    DSLContext db = sqlStoreProvider.getDSL();
    requireProgramModification(db, access, builder.getProgramId());
    return modelFrom(ProgramSequence.class,
      executeCreate(sqlStoreProvider.getDSL(), PROGRAM_SEQUENCE, builder));
  }

  @Override
  public ManagerCloner<ProgramSequence> clone(HubAccess access, UUID cloneId, ProgramSequence to) throws ManagerException {
    requireArtist(access);
    AtomicReference<ProgramSequence> result = new AtomicReference<>();
    AtomicReference<ManagerCloner<ProgramSequence>> cloner = new AtomicReference<>();
    sqlStoreProvider.getDSL().transaction(ctx -> {
      DSLContext db = DSL.using(ctx);
      requireModification(db, access, cloneId);

      var from = readOne(db, access, cloneId);
      if (Objects.isNull(from))
        throw new ManagerException("Can't clone nonexistent ProgramSequence");

      // Inherits these attributes if none specified
      // When not set, clone inherits attribute values from original record
      entityFactory.setAllEmptyAttributes(from, to);
      var record = validate(to);
      requireParentExists(db, access, record);

      // Create main entity
      result.set(modelFrom(ProgramSequence.class, executeCreate(db, PROGRAM_SEQUENCE, record)));

      // Prepare to clone sub-entities
      cloner.set(new ManagerCloner<>(result.get(), this));

      // Clone ProgramSequenceChord belongs to ProgramSequence
      Map<UUID, UUID> clonedProgramSequenceChords = cloner.get().clone(db, PROGRAM_SEQUENCE_CHORD, PROGRAM_SEQUENCE_CHORD.ID,
        Set.of(PROGRAM_SEQUENCE_CHORD.PROGRAM_SEQUENCE_ID),
        PROGRAM_SEQUENCE_CHORD.PROGRAM_SEQUENCE_ID, cloneId, result.get().getId());

      // Clone ProgramSequenceChordMeme belongs to newly cloned ProgramSequenceChords
      for (UUID originalId : clonedProgramSequenceChords.keySet())
        cloner.get().clone(db, PROGRAM_SEQUENCE_CHORD_VOICING, PROGRAM_SEQUENCE_CHORD_VOICING.ID,
          Set.of(PROGRAM_SEQUENCE_CHORD_VOICING.PROGRAM_SEQUENCE_CHORD_ID),
          PROGRAM_SEQUENCE_CHORD_VOICING.PROGRAM_SEQUENCE_CHORD_ID,
          originalId, clonedProgramSequenceChords.get(originalId));

      // Clone ProgramSequenceBinding belongs to ProgramSequence
      Map<UUID, UUID> clonedProgramSequenceBindings = cloner.get().clone(db, PROGRAM_SEQUENCE_BINDING, PROGRAM_SEQUENCE_BINDING.ID,
        Set.of(PROGRAM_SEQUENCE_BINDING.PROGRAM_SEQUENCE_ID),
        PROGRAM_SEQUENCE_BINDING.PROGRAM_SEQUENCE_ID, cloneId, result.get().getId());

      // Clone ProgramSequenceBindingMeme belongs to newly cloned ProgramSequenceBindings
      for (UUID originalId : clonedProgramSequenceBindings.keySet())
        cloner.get().clone(db, PROGRAM_SEQUENCE_BINDING_MEME, PROGRAM_SEQUENCE_BINDING_MEME.ID,
          Set.of(PROGRAM_SEQUENCE_BINDING_MEME.PROGRAM_SEQUENCE_BINDING_ID),
          PROGRAM_SEQUENCE_BINDING_MEME.PROGRAM_SEQUENCE_BINDING_ID,
          originalId, clonedProgramSequenceBindings.get(originalId));

      // Clone ProgramSequencePattern belongs to ProgramSequence and ProgramVoice
      Map<UUID, UUID> clonedProgramSequencePatterns = cloner.get().clone(db, PROGRAM_SEQUENCE_PATTERN, PROGRAM_SEQUENCE_PATTERN.ID,
        Set.of(PROGRAM_SEQUENCE_PATTERN.PROGRAM_SEQUENCE_ID, PROGRAM_SEQUENCE_PATTERN.PROGRAM_VOICE_ID),
        PROGRAM_SEQUENCE_PATTERN.PROGRAM_SEQUENCE_ID, cloneId, result.get().getId());

      // Clone ProgramSequencePatternEvent belongs to newly cloned ProgramSequencePattern and ProgramVoiceTrack
      for (UUID originalId : clonedProgramSequencePatterns.keySet())
        cloner.get().clone(db, PROGRAM_SEQUENCE_PATTERN_EVENT, PROGRAM_SEQUENCE_PATTERN_EVENT.ID,
          Set.of(PROGRAM_SEQUENCE_PATTERN_EVENT.PROGRAM_SEQUENCE_PATTERN_ID, PROGRAM_SEQUENCE_PATTERN_EVENT.PROGRAM_VOICE_TRACK_ID),
          PROGRAM_SEQUENCE_PATTERN_EVENT.PROGRAM_SEQUENCE_PATTERN_ID,
          originalId, clonedProgramSequencePatterns.get(originalId));

    });
    return cloner.get();
  }

  @Override
  @Nullable
  public ProgramSequence readOne(HubAccess access, UUID id) throws ManagerException {
    requireArtist(access);
    return readOne(sqlStoreProvider.getDSL(), access, id);
  }

  @Override
  @Nullable
  public Collection<ProgramSequence> readMany(HubAccess access, Collection<UUID> parentIds) throws ManagerException {
    requireArtist(access);
    if (access.isTopLevel())
      return modelsFrom(ProgramSequence.class,
        sqlStoreProvider.getDSL().selectFrom(PROGRAM_SEQUENCE)
          .where(PROGRAM_SEQUENCE.PROGRAM_ID.in(parentIds))
          .fetch());
    else
      return modelsFrom(ProgramSequence.class,
        sqlStoreProvider.getDSL().select(PROGRAM_SEQUENCE.fields()).from(PROGRAM_SEQUENCE)
          .join(PROGRAM).on(PROGRAM.ID.eq(PROGRAM_SEQUENCE.PROGRAM_ID))
          .join(LIBRARY).on(LIBRARY.ID.eq(PROGRAM.LIBRARY_ID))
          .where(PROGRAM_SEQUENCE.PROGRAM_ID.in(parentIds))
          .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
          .fetch());
  }

  @Override
  public ProgramSequence update(HubAccess access, UUID id, ProgramSequence ramProgramSequence) throws ManagerException, JsonapiException, ValueException {
    ProgramSequence builder = validate(ramProgramSequence);
    DSLContext db = sqlStoreProvider.getDSL();
    requireModification(db, access, id);
    executeUpdate(db, PROGRAM_SEQUENCE, id, builder);
    return builder;
  }

  @Override
  public void destroy(HubAccess access, UUID id) throws ManagerException {
    DSLContext db = sqlStoreProvider.getDSL();
    requireModification(db, access, id);

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
   * Require permission to modify the specified program sequence
   *
   * @param db     context
   * @param access control
   * @param id     of entity to require modification access to
   * @throws ManagerException on invalid permissions
   */
  void requireModification(DSLContext db, HubAccess access, UUID id) throws ManagerException {
    requireArtist(access);

    if (access.isTopLevel())
      requireExists("Sequence", db.selectCount().from(PROGRAM_SEQUENCE)
        .where(PROGRAM_SEQUENCE.ID.eq(id))
        .fetchOne(0, int.class));
    else
      requireExists("Sequence in Program in Account you have access to", db.selectCount().from(PROGRAM_SEQUENCE)
        .join(PROGRAM).on(PROGRAM_SEQUENCE.PROGRAM_ID.eq(PROGRAM.ID))
        .join(LIBRARY).on(PROGRAM.LIBRARY_ID.eq(LIBRARY.ID))
        .where(PROGRAM_SEQUENCE.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne(0, int.class));
  }

  /**
   * Require parent ProgramSequence exists of a given possible entity in a DSL context
   *
   * @param db     DSL context
   * @param access control
   * @param entity to validate
   * @throws ManagerException if parent does not exist
   */
  void requireParentExists(DSLContext db, HubAccess access, ProgramSequence entity) throws ManagerException {
    if (access.isTopLevel())
      requireExists("Program", db.selectCount().from(PROGRAM)
        .where(PROGRAM.ID.eq(entity.getProgramId()))
        .fetchOne(0, int.class));
    else
      requireExists("Program", db.selectCount().from(PROGRAM)
        .join(LIBRARY).on(LIBRARY.ID.eq(PROGRAM.LIBRARY_ID))
        .where(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .and(PROGRAM.ID.eq(entity.getProgramId()))
        .fetchOne(0, int.class));
  }

  /**
   * Read one Program Sequence
   *
   * @param db     context
   * @param access control
   * @param id     of entity to read
   * @return program sequence
   */
  ProgramSequence readOne(DSLContext db, HubAccess access, UUID id) throws ManagerException {
    if (access.isTopLevel())
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
          .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
          .fetchOne());
  }

  /**
   * validate data
   *
   * @param record to validate
   * @throws ManagerException if invalid
   */
  public ProgramSequence validate(ProgramSequence record) throws ManagerException {
    try {
      ValueUtils.require(record.getProgramId(), "Program ID");
      ValueUtils.require(record.getName(), "Name");
      ValueUtils.require(record.getKey(), "Key");
      ValueUtils.require(record.getDensity(), "Density");
      if (ValueUtils.isEmpty(record.getTotal())) record.setTotal((short) 0);
      return record;

    } catch (ValueException e) {
      throw new ManagerException(e);
    }
  }

}
