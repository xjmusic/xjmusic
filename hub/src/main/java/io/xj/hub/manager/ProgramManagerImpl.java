// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.manager;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import io.xj.hub.ProgramConfig;
import io.xj.hub.access.HubAccess;
import io.xj.hub.enums.ProgramState;
import io.xj.hub.enums.UserRoleType;
import io.xj.hub.persistence.HubDatabaseProvider;
import io.xj.hub.persistence.HubPersistenceServiceImpl;
import io.xj.hub.tables.pojos.*;
import io.xj.lib.entity.Entities;
import io.xj.lib.entity.EntityException;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.JsonapiException;
import io.xj.lib.util.ValueException;
import io.xj.lib.util.Values;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import java.util.Collection;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static io.xj.hub.Tables.*;

public class ProgramManagerImpl extends HubPersistenceServiceImpl<Program> implements ProgramManager {
  @Inject
  public ProgramManagerImpl(
    EntityFactory entityFactory,
    HubDatabaseProvider dbProvider
  ) {
    super(entityFactory, dbProvider);
  }

  @Override
  public Program create(HubAccess access, Program rawProgram) throws ManagerException, JsonapiException, ValueException {
    var program = validate(rawProgram);
    requireArtist(access);
    return modelFrom(Program.class, executeCreate(dbProvider.getDSL(), PROGRAM, program));
  }

  @Override
  public ManagerCloner<Program> clone(HubAccess access, UUID cloneId, Program to) throws ManagerException {
    requireArtist(access);
    AtomicReference<ManagerCloner<Program>> result = new AtomicReference<>();
    dbProvider.getDSL().transaction(ctx -> result.set(clone(DSL.using(ctx), access, cloneId, to)));
    return result.get();
  }

  @Override
  public ManagerCloner<Program> clone(DSLContext db, HubAccess access, UUID cloneId, Program to) throws ManagerException {
    try {
      Program from = readOne(db, access, cloneId);
      if (Objects.isNull(from))
        throw new ManagerException("Can't clone nonexistent Program");

      // When not set, clone inherits attribute values from original record
      entityFactory.setAllEmptyAttributes(from, to);
      Program program = validate(to);
      requireParentExists(db, access, program);

      // Create main entity
      var result = modelFrom(Program.class, executeCreate(db, PROGRAM, program));
      UUID originalId = result.getId();

      // Prepare to clone sub-entities
      var cloner = new ManagerCloner<>(result, this);

      // Clone ProgramMeme
      cloner.clone(db, PROGRAM_MEME, PROGRAM_MEME.ID, ImmutableSet.of(), PROGRAM_MEME.PROGRAM_ID, cloneId, originalId);

      // Clone ProgramVoice
      cloner.clone(db, PROGRAM_VOICE, PROGRAM_VOICE.ID, ImmutableSet.of(), PROGRAM_VOICE.PROGRAM_ID, cloneId, originalId);

      // Clone ProgramVoiceTrack belongs to ProgramVoice
      cloner.clone(db, PROGRAM_VOICE_TRACK, PROGRAM_VOICE_TRACK.ID,
        ImmutableSet.of(PROGRAM_VOICE_TRACK.PROGRAM_VOICE_ID),
        PROGRAM_VOICE_TRACK.PROGRAM_ID, cloneId, originalId);

      // Clone ProgramSequence
      cloner.clone(db, PROGRAM_SEQUENCE, PROGRAM_SEQUENCE.ID, ImmutableSet.of(), PROGRAM_SEQUENCE.PROGRAM_ID, cloneId, originalId);

      // Clone ProgramSequenceChord belongs to ProgramSequence
      cloner.clone(db, PROGRAM_SEQUENCE_CHORD, PROGRAM_SEQUENCE_CHORD.ID,
        ImmutableSet.of(PROGRAM_SEQUENCE_CHORD.PROGRAM_SEQUENCE_ID),
        PROGRAM_SEQUENCE_CHORD.PROGRAM_ID, cloneId, originalId);

      // Clone ProgramSequenceChordVoiding belongs to ProgramSequenceChord
      cloner.clone(db, PROGRAM_SEQUENCE_CHORD_VOICING, PROGRAM_SEQUENCE_CHORD_VOICING.ID,
        ImmutableSet.of(PROGRAM_SEQUENCE_CHORD_VOICING.PROGRAM_SEQUENCE_CHORD_ID),
        PROGRAM_SEQUENCE_CHORD_VOICING.PROGRAM_ID, cloneId, originalId);

      // Clone ProgramSequenceBinding belongs to ProgramSequence
      cloner.clone(db, PROGRAM_SEQUENCE_BINDING, PROGRAM_SEQUENCE_BINDING.ID,
        ImmutableSet.of(PROGRAM_SEQUENCE_BINDING.PROGRAM_SEQUENCE_ID),
        PROGRAM_SEQUENCE_BINDING.PROGRAM_ID, cloneId, originalId);

      // Clone ProgramSequenceBindingMeme belongs to ProgramSequenceBinding
      cloner.clone(db, PROGRAM_SEQUENCE_BINDING_MEME, PROGRAM_SEQUENCE_BINDING_MEME.ID,
        ImmutableSet.of(PROGRAM_SEQUENCE_BINDING_MEME.PROGRAM_SEQUENCE_BINDING_ID),
        PROGRAM_SEQUENCE_BINDING_MEME.PROGRAM_ID, cloneId, originalId);

      // Clone ProgramSequencePattern belongs to ProgramSequence and ProgramVoice
      cloner.clone(db, PROGRAM_SEQUENCE_PATTERN, PROGRAM_SEQUENCE_PATTERN.ID,
        ImmutableSet.of(PROGRAM_SEQUENCE_PATTERN.PROGRAM_SEQUENCE_ID, PROGRAM_SEQUENCE_PATTERN.PROGRAM_VOICE_ID),
        PROGRAM_SEQUENCE_PATTERN.PROGRAM_ID, cloneId, originalId);

      // Clone ProgramSequencePatternEvent belongs to ProgramSequencePattern and ProgramVoiceTrack
      cloner.clone(db, PROGRAM_SEQUENCE_PATTERN_EVENT, PROGRAM_SEQUENCE_PATTERN_EVENT.ID,
        ImmutableSet.of(PROGRAM_SEQUENCE_PATTERN_EVENT.PROGRAM_SEQUENCE_PATTERN_ID, PROGRAM_SEQUENCE_PATTERN_EVENT.PROGRAM_VOICE_TRACK_ID),
        PROGRAM_SEQUENCE_PATTERN_EVENT.PROGRAM_ID, cloneId, originalId);

      return cloner;

    } catch (EntityException e) {
      throw new ManagerException("Failed to clone Program!", e);
    }
  }


  @Override
  public Program readOne(HubAccess access, UUID id) throws ManagerException {
    return readOne(dbProvider.getDSL(), access, id);
  }

  @Override
  public Collection<Program> readMany(HubAccess access, Collection<UUID> parentIds) throws ManagerException {
    if (access.isTopLevel())
      return modelsFrom(Program.class, dbProvider.getDSL().select(PROGRAM.fields()).from(PROGRAM)
        .where(PROGRAM.LIBRARY_ID.in(parentIds))
        .and(PROGRAM.IS_DELETED.eq(false))
        .orderBy(PROGRAM.TYPE, PROGRAM.NAME)
        .fetch());
    else
      return modelsFrom(Program.class, dbProvider.getDSL().select(PROGRAM.fields()).from(PROGRAM)
        .join(LIBRARY).on(LIBRARY.ID.eq(PROGRAM.LIBRARY_ID))
        .where(PROGRAM.LIBRARY_ID.in(parentIds))
        .and(PROGRAM.IS_DELETED.eq(false))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .orderBy(PROGRAM.TYPE, PROGRAM.NAME)
        .fetch());
  }

  @Override
  public <N> Collection<N> readManyWithChildEntities(HubAccess access, Collection<UUID> programIds) throws ManagerException {
    DSLContext db = dbProvider.getDSL();

    requireRead(db, access, programIds);

    Collection<Object> entities = Lists.newArrayList();
    entities.addAll(modelsFrom(Program.class, db.selectFrom(PROGRAM).where(PROGRAM.ID.in(programIds)).fetch()));
    entities.addAll(modelsFrom(ProgramSequencePatternEvent.class, db.selectFrom(PROGRAM_SEQUENCE_PATTERN_EVENT).where(PROGRAM_SEQUENCE_PATTERN_EVENT.PROGRAM_ID.in(programIds))));
    entities.addAll(modelsFrom(ProgramMeme.class, db.selectFrom(PROGRAM_MEME).where(PROGRAM_MEME.PROGRAM_ID.in(programIds))));
    entities.addAll(modelsFrom(ProgramSequencePattern.class, db.selectFrom(PROGRAM_SEQUENCE_PATTERN).where(PROGRAM_SEQUENCE_PATTERN.PROGRAM_ID.in(programIds))));
    entities.addAll(modelsFrom(ProgramSequenceBinding.class, db.selectFrom(PROGRAM_SEQUENCE_BINDING).where(PROGRAM_SEQUENCE_BINDING.PROGRAM_ID.in(programIds))));
    entities.addAll(modelsFrom(ProgramSequenceBindingMeme.class, db.selectFrom(PROGRAM_SEQUENCE_BINDING_MEME).where(PROGRAM_SEQUENCE_BINDING_MEME.PROGRAM_ID.in(programIds))));
    entities.addAll(modelsFrom(ProgramSequenceChord.class, db.selectFrom(PROGRAM_SEQUENCE_CHORD).where(PROGRAM_SEQUENCE_CHORD.PROGRAM_ID.in(programIds))));
    entities.addAll(modelsFrom(ProgramSequenceChordVoicing.class, db.selectFrom(PROGRAM_SEQUENCE_CHORD_VOICING).where(PROGRAM_SEQUENCE_CHORD_VOICING.PROGRAM_ID.in(programIds))));
    entities.addAll(modelsFrom(ProgramSequence.class, db.selectFrom(PROGRAM_SEQUENCE).where(PROGRAM_SEQUENCE.PROGRAM_ID.in(programIds))));
    entities.addAll(modelsFrom(ProgramVoiceTrack.class, db.selectFrom(PROGRAM_VOICE_TRACK).where(PROGRAM_VOICE_TRACK.PROGRAM_ID.in(programIds))));
    entities.addAll(modelsFrom(ProgramVoice.class, db.selectFrom(PROGRAM_VOICE).where(PROGRAM_VOICE.PROGRAM_ID.in(programIds))));
    entities.addAll(modelsFrom(FeedbackProgram.class, db.selectFrom(FEEDBACK_PROGRAM).where(FEEDBACK_PROGRAM.PROGRAM_ID.in(programIds))));
    //noinspection unchecked
    return (Collection<N>) entities;
  }

  @Override
  public Collection<Object> readChildEntities(HubAccess access, Collection<UUID> programIds, Collection<String> types) throws ManagerException {
    DSLContext db = dbProvider.getDSL();

    requireRead(db, access, programIds);

    Collection<Object> entities = Lists.newArrayList();

    // ProgramSequencePatternEvent
    if (types.contains(Entities.toResourceType(ProgramSequencePatternEvent.class)))
      entities.addAll(modelsFrom(ProgramSequencePatternEvent.class,
        db.selectFrom(PROGRAM_SEQUENCE_PATTERN_EVENT).where(PROGRAM_SEQUENCE_PATTERN_EVENT.PROGRAM_ID.in(programIds))));

    // ProgramMeme
    if (types.contains(Entities.toResourceType(ProgramMeme.class)))
      entities.addAll(modelsFrom(ProgramMeme.class,
        db.selectFrom(PROGRAM_MEME).where(PROGRAM_MEME.PROGRAM_ID.in(programIds))));

    // ProgramSequencePattern
    if (types.contains(Entities.toResourceType(ProgramSequencePattern.class)))
      entities.addAll(modelsFrom(ProgramSequencePattern.class,
        db.selectFrom(PROGRAM_SEQUENCE_PATTERN).where(PROGRAM_SEQUENCE_PATTERN.PROGRAM_ID.in(programIds))));

    // ProgramSequenceBinding
    if (types.contains(Entities.toResourceType(ProgramSequenceBinding.class)))
      entities.addAll(modelsFrom(ProgramSequenceBinding.class,
        db.selectFrom(PROGRAM_SEQUENCE_BINDING).where(PROGRAM_SEQUENCE_BINDING.PROGRAM_ID.in(programIds))));

    // ProgramSequenceBindingMeme
    if (types.contains(Entities.toResourceType(ProgramSequenceBindingMeme.class)))
      entities.addAll(modelsFrom(ProgramSequenceBindingMeme.class,
        db.selectFrom(PROGRAM_SEQUENCE_BINDING_MEME).where(PROGRAM_SEQUENCE_BINDING_MEME.PROGRAM_ID.in(programIds))));

    // ProgramSequenceChord
    if (types.contains(Entities.toResourceType(ProgramSequenceChord.class)))
      entities.addAll(modelsFrom(ProgramSequenceChord.class,
        db.selectFrom(PROGRAM_SEQUENCE_CHORD).where(PROGRAM_SEQUENCE_CHORD.PROGRAM_ID.in(programIds))));

    // ProgramSequenceChordVoicing
    if (types.contains(Entities.toResourceType(ProgramSequenceChordVoicing.class)))
      entities.addAll(modelsFrom(ProgramSequenceChordVoicing.class,
        db.selectFrom(PROGRAM_SEQUENCE_CHORD_VOICING).where(PROGRAM_SEQUENCE_CHORD_VOICING.PROGRAM_ID.in(programIds))));

    // ProgramSequence
    if (types.contains(Entities.toResourceType(ProgramSequence.class)))
      entities.addAll(modelsFrom(ProgramSequence.class,
        db.selectFrom(PROGRAM_SEQUENCE).where(PROGRAM_SEQUENCE.PROGRAM_ID.in(programIds))));

    // ProgramVoiceTrack
    if (types.contains(Entities.toResourceType(ProgramVoiceTrack.class)))
      entities.addAll(modelsFrom(ProgramVoiceTrack.class,
        db.selectFrom(PROGRAM_VOICE_TRACK).where(PROGRAM_VOICE_TRACK.PROGRAM_ID.in(programIds))));

    // ProgramVoice
    if (types.contains(Entities.toResourceType(ProgramVoice.class)))
      entities.addAll(modelsFrom(ProgramVoice.class,
        db.selectFrom(PROGRAM_VOICE).where(PROGRAM_VOICE.PROGRAM_ID.in(programIds))));

    // FeedbackProgram
    if (types.contains(Entities.toResourceType(FeedbackProgram.class)))
      entities.addAll(modelsFrom(FeedbackProgram.class,
        db.selectFrom(FEEDBACK_PROGRAM).where(FEEDBACK_PROGRAM.PROGRAM_ID.in(programIds))));

    return entities;
  }

  @Override
  public Collection<Program> readMany(HubAccess access) throws ManagerException {
    if (access.isTopLevel())
      return modelsFrom(Program.class, dbProvider.getDSL().select(PROGRAM.fields()).from(PROGRAM)
        .where(PROGRAM.IS_DELETED.eq(false))
        .orderBy(PROGRAM.TYPE, PROGRAM.NAME)
        .fetch());
    else
      return modelsFrom(Program.class, dbProvider.getDSL().select(PROGRAM.fields()).from(PROGRAM)
        .join(LIBRARY).on(LIBRARY.ID.eq(PROGRAM.LIBRARY_ID))
        .where(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .and(PROGRAM.IS_DELETED.eq(false))
        .orderBy(PROGRAM.TYPE, PROGRAM.NAME)
        .fetch());
  }

  @Override
  public Program update(HubAccess access, UUID id, Program rawProgram) throws ManagerException, JsonapiException, ValueException {
    var builder = validate(rawProgram);

    requireArtist(access);
    DSLContext db = dbProvider.getDSL();
    readOne(db, access, id);

    executeUpdate(db, PROGRAM, id, builder);
    return builder;
  }

  @Override
  public void destroy(HubAccess access, UUID id) throws ManagerException {
    DSLContext db = dbProvider.getDSL();

    if (!access.isTopLevel())
      requireExists("Program belonging to you", db.selectCount().from(PROGRAM)
        .join(LIBRARY).on(PROGRAM.LIBRARY_ID.eq(LIBRARY.ID))
        .where(PROGRAM.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .and(PROGRAM.IS_DELETED.eq(false))
        .fetchOne(0, int.class));

    db.update(PROGRAM)
      .set(PROGRAM.IS_DELETED, true)
      .where(PROGRAM.ID.eq(id))
      .execute();
  }

  @Override
  public Program newInstance() {
    return new Program();
  }

  @Override
  public Collection<Program> readManyInAccount(HubAccess access, String accountId) throws ManagerException {
    if (access.isTopLevel())
      return modelsFrom(Program.class, dbProvider.getDSL().select(PROGRAM.fields()).from(PROGRAM)
        .join(LIBRARY).on(PROGRAM.LIBRARY_ID.eq(LIBRARY.ID))
        .where(LIBRARY.ACCOUNT_ID.eq(UUID.fromString(accountId)))
        .and(PROGRAM.IS_DELETED.eq(false))
        .orderBy(PROGRAM.TYPE, PROGRAM.NAME)
        .fetch());
    else
      return modelsFrom(Program.class, dbProvider.getDSL().select(PROGRAM.fields()).from(PROGRAM)
        .join(LIBRARY).on(PROGRAM.LIBRARY_ID.eq(LIBRARY.ID))
        .where(LIBRARY.ACCOUNT_ID.in(UUID.fromString(accountId)))
        .and(PROGRAM.IS_DELETED.eq(false))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .orderBy(PROGRAM.TYPE, PROGRAM.NAME)
        .fetch());
  }

  @Override
  public Collection<Program> readManyInState(HubAccess access, ProgramState state) throws ManagerException {
    requireAny(access, UserRoleType.Admin, UserRoleType.Engineer);
    // FUTURE: engineer should only see programs in account?

    return modelsFrom(Program.class, dbProvider.getDSL().select(PROGRAM.fields())
      .from(PROGRAM)
      .where(PROGRAM.STATE.eq(state))
      .and(PROGRAM.IS_DELETED.eq(false))
      .fetch());
  }

  @Override
  public Collection<UUID> readIdsInLibraries(HubAccess access, Collection<UUID> parentIds) throws ManagerException {
    requireArtist(access);
    return Manager.idsFrom(dbProvider.getDSL().select(PROGRAM.ID)
      .from(PROGRAM)
      .where(PROGRAM.LIBRARY_ID.in(parentIds))
      .and(PROGRAM.STATE.equal(ProgramState.Published))
      .and(PROGRAM.IS_DELETED.eq(false))
      .fetch());
  }

  /**
   Require read access

   @param db         database context
   @param access  control
   @param programIds to require access to
   */
  private void requireRead(DSLContext db, HubAccess access, Collection<UUID> programIds) throws ManagerException {
    if (!access.isTopLevel())
      for (UUID programId : programIds)
        requireExists("Program", db.selectCount().from(PROGRAM)
          .join(LIBRARY).on(LIBRARY.ID.eq(PROGRAM.LIBRARY_ID))
          .where(PROGRAM.ID.eq(programId))
          .and(PROGRAM.IS_DELETED.eq(false))
          .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
          .fetchOne(0, int.class));
  }

  /**
   Read one record

   @param db        DSL context
   @param access control
   @param id        to read
   @return record
   @throws ManagerException on failure
   */
  private Program readOne(DSLContext db, HubAccess access, UUID id) throws ManagerException {
    if (access.isTopLevel())
      return modelFrom(Program.class,
        db.selectFrom(PROGRAM)
          .where(PROGRAM.ID.eq(id))
          .and(PROGRAM.IS_DELETED.eq(false))
          .fetchOne());
    else
      return modelFrom(Program.class,
        db.select(PROGRAM.fields())
          .from(PROGRAM)
          .join(LIBRARY).on(LIBRARY.ID.eq(PROGRAM.LIBRARY_ID))
          .where(PROGRAM.ID.eq(id))
          .and(PROGRAM.IS_DELETED.eq(false))
          .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
          .fetchOne());
  }

  /**
   Require parent program exists of a given possible entity in a DSL context

   @param db        DSL context
   @param access control
   @param entity    to validate
   @throws ManagerException if parent does not exist
   */
  private void requireParentExists(DSLContext db, HubAccess access, Program entity) throws ManagerException {
    if (access.isTopLevel())
      requireExists("Library", db.selectCount().from(LIBRARY)
        .where(LIBRARY.ID.eq(entity.getLibraryId()))
        .fetchOne(0, int.class));
    else
      requireExists("Library", db.selectCount().from(LIBRARY)
        .where(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .and(LIBRARY.ID.eq(entity.getLibraryId()))
        .fetchOne(0, int.class));
  }

  /**
   Validate data

   @param record to validate
   @return validated Program
   @throws ManagerException if invalid
   */
  public Program validate(Program record) throws ManagerException {
    try {
      Values.require(record.getLibraryId(), "Library ID");
      Values.require(record.getName(), "Name");
      Values.require(record.getKey(), "Key");
      Values.requireNonZero(record.getTempo(), "Tempo");
      Values.require(record.getType(), "Type");
      Values.require(record.getState(), "State");

      // [#175347578] validate TypeSafe chain config
      // [#177129498] Artist saves Program, Instrument, or Template config, validate & combine with defaults.
      if (Objects.isNull(record.getConfig()))
        record.setConfig(new ProgramConfig().toString());
      else
        record.setConfig(new ProgramConfig(record).toString());

      return record;

    } catch (ValueException e) {
      throw new ManagerException(e);
    }
  }
}

