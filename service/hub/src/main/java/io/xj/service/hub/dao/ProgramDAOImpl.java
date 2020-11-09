// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.dao;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.typesafe.config.Config;
import io.xj.lib.entity.Entities;
import io.xj.lib.entity.Entity;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.JsonApiException;
import io.xj.lib.jsonapi.PayloadFactory;
import io.xj.lib.util.ValueException;
import io.xj.service.hub.access.HubAccess;
import io.xj.service.hub.entity.Program;
import io.xj.service.hub.entity.ProgramConfig;
import io.xj.service.hub.entity.ProgramMeme;
import io.xj.service.hub.entity.ProgramSequence;
import io.xj.service.hub.entity.ProgramSequenceBinding;
import io.xj.service.hub.entity.ProgramSequenceBindingMeme;
import io.xj.service.hub.entity.ProgramSequenceChord;
import io.xj.service.hub.entity.ProgramSequenceChordVoicing;
import io.xj.service.hub.entity.ProgramSequencePattern;
import io.xj.service.hub.entity.ProgramSequencePatternEvent;
import io.xj.service.hub.entity.ProgramState;
import io.xj.service.hub.entity.ProgramType;
import io.xj.service.hub.entity.ProgramVoice;
import io.xj.service.hub.entity.ProgramVoiceTrack;
import io.xj.service.hub.entity.UserRoleType;
import io.xj.service.hub.persistence.HubDatabaseProvider;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import java.util.Collection;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static io.xj.service.hub.Tables.LIBRARY;
import static io.xj.service.hub.Tables.PROGRAM;
import static io.xj.service.hub.Tables.PROGRAM_MEME;
import static io.xj.service.hub.Tables.PROGRAM_SEQUENCE;
import static io.xj.service.hub.Tables.PROGRAM_SEQUENCE_BINDING;
import static io.xj.service.hub.Tables.PROGRAM_SEQUENCE_BINDING_MEME;
import static io.xj.service.hub.Tables.PROGRAM_SEQUENCE_CHORD;
import static io.xj.service.hub.Tables.PROGRAM_SEQUENCE_CHORD_VOICING;
import static io.xj.service.hub.Tables.PROGRAM_SEQUENCE_PATTERN;
import static io.xj.service.hub.Tables.PROGRAM_SEQUENCE_PATTERN_EVENT;
import static io.xj.service.hub.Tables.PROGRAM_VOICE;
import static io.xj.service.hub.Tables.PROGRAM_VOICE_TRACK;

public class ProgramDAOImpl extends DAOImpl<Program> implements ProgramDAO {

  private final Config config;

  @Inject
  public ProgramDAOImpl(
    Config config,
    PayloadFactory payloadFactory,
    EntityFactory entityFactory,
    HubDatabaseProvider dbProvider
  ) {
    super(payloadFactory, entityFactory);
    this.config = config;
    this.dbProvider = dbProvider;
  }

  @Override
  public Program create(HubAccess hubAccess, Program program) throws DAOException, JsonApiException, ValueException {
    program.validate();
    requireArtist(hubAccess);
    validateConfig(program);
    return modelFrom(Program.class, executeCreate(dbProvider.getDSL(), PROGRAM, program));
  }

  @Override
  public DAOCloner<Program> clone(HubAccess hubAccess, UUID cloneId, Program program) throws DAOException {
    requireArtist(hubAccess);
    AtomicReference<Program> result = new AtomicReference<>();
    AtomicReference<DAOCloner<Program>> cloner = new AtomicReference<>();
    dbProvider.getDSL().transaction(ctx -> {
      DSLContext db = DSL.using(ctx);

      Program from = readOne(db, hubAccess, cloneId);
      if (Objects.isNull(from))
        throw new DAOException("Can't clone nonexistent Program");

      // Inherits state, type if none specified
      if (Objects.isNull(program.getState())) program.setStateEnum(from.getState());
      if (Objects.isNull(program.getDensity())) program.setDensity(from.getDensity());
      if (Objects.isNull(program.getName())) program.setName(from.getName());
      if (Objects.isNull(program.getType())) program.setTypeEnum(from.getType());
      if (Objects.isNull(program.getTempo())) program.setTempo(from.getTempo());
      if (Objects.isNull(program.getKey())) program.setKey(from.getKey());
      program.setUserId(from.getUserId());
      program.validate();
      requireParentExists(db, hubAccess, program);
      validateConfig(program);

      // Create main entity
      result.set(modelFrom(Program.class, executeCreate(db, PROGRAM, program)));

      // Prepare to clone sub-entities
      cloner.set(new DAOCloner<>(result.get(), this));

      // Clone ProgramMeme
      cloner.get().clone(db, PROGRAM_MEME, PROGRAM_MEME.ID, ImmutableSet.of(), PROGRAM_MEME.PROGRAM_ID, cloneId, result.get().getId());

      // Clone ProgramVoice
      cloner.get().clone(db, PROGRAM_VOICE, PROGRAM_VOICE.ID, ImmutableSet.of(), PROGRAM_VOICE.PROGRAM_ID, cloneId, result.get().getId());

      // Clone ProgramVoiceTrack belongs to ProgramVoice
      cloner.get().clone(db, PROGRAM_VOICE_TRACK, PROGRAM_VOICE_TRACK.ID,
        ImmutableSet.of(PROGRAM_VOICE_TRACK.PROGRAM_VOICE_ID),
        PROGRAM_VOICE_TRACK.PROGRAM_ID, cloneId, result.get().getId());

      // Clone ProgramSequence
      cloner.get().clone(db, PROGRAM_SEQUENCE, PROGRAM_SEQUENCE.ID, ImmutableSet.of(), PROGRAM_SEQUENCE.PROGRAM_ID, cloneId, result.get().getId());

      // Clone ProgramSequenceChord belongs to ProgramSequence
      cloner.get().clone(db, PROGRAM_SEQUENCE_CHORD, PROGRAM_SEQUENCE_CHORD.ID,
        ImmutableSet.of(PROGRAM_SEQUENCE_CHORD.PROGRAM_SEQUENCE_ID),
        PROGRAM_SEQUENCE_CHORD.PROGRAM_ID, cloneId, result.get().getId());

      // Clone ProgramSequenceBinding belongs to ProgramSequence
      cloner.get().clone(db, PROGRAM_SEQUENCE_BINDING, PROGRAM_SEQUENCE_BINDING.ID,
        ImmutableSet.of(PROGRAM_SEQUENCE_BINDING.PROGRAM_SEQUENCE_ID),
        PROGRAM_SEQUENCE_BINDING.PROGRAM_ID, cloneId, result.get().getId());

      // Clone ProgramSequenceBindingMeme belongs to ProgramSequenceBinding
      cloner.get().clone(db, PROGRAM_SEQUENCE_BINDING_MEME, PROGRAM_SEQUENCE_BINDING_MEME.ID,
        ImmutableSet.of(PROGRAM_SEQUENCE_BINDING_MEME.PROGRAM_SEQUENCE_BINDING_ID),
        PROGRAM_SEQUENCE_BINDING_MEME.PROGRAM_ID, cloneId, result.get().getId());

      // Clone ProgramSequencePattern belongs to ProgramSequence and ProgramVoice
      cloner.get().clone(db, PROGRAM_SEQUENCE_PATTERN, PROGRAM_SEQUENCE_PATTERN.ID,
        ImmutableSet.of(PROGRAM_SEQUENCE_PATTERN.PROGRAM_SEQUENCE_ID, PROGRAM_SEQUENCE_PATTERN.PROGRAM_VOICE_ID),
        PROGRAM_SEQUENCE_PATTERN.PROGRAM_ID, cloneId, result.get().getId());

      // Clone ProgramSequencePatternEvent belongs to ProgramSequencePattern and ProgramVoiceTrack
      cloner.get().clone(db, PROGRAM_SEQUENCE_PATTERN_EVENT, PROGRAM_SEQUENCE_PATTERN_EVENT.ID,
        ImmutableSet.of(PROGRAM_SEQUENCE_PATTERN_EVENT.PROGRAM_SEQUENCE_PATTERN_ID, PROGRAM_SEQUENCE_PATTERN_EVENT.PROGRAM_VOICE_TRACK_ID),
        PROGRAM_SEQUENCE_PATTERN_EVENT.PROGRAM_ID, cloneId, result.get().getId());
    });
    return cloner.get();
  }

  @Override
  public Program readOne(HubAccess hubAccess, UUID id) throws DAOException {
    return readOne(dbProvider.getDSL(), hubAccess, id);
  }

  @Override
  public Collection<Program> readMany(HubAccess hubAccess, Collection<UUID> parentIds) throws DAOException {
    if (hubAccess.isTopLevel())
      return modelsFrom(Program.class, dbProvider.getDSL().select(PROGRAM.fields()).from(PROGRAM)
        .where(PROGRAM.LIBRARY_ID.in(parentIds))
        .orderBy(PROGRAM.TYPE, PROGRAM.NAME)
        .fetch());
    else
      return modelsFrom(Program.class, dbProvider.getDSL().select(PROGRAM.fields()).from(PROGRAM)
        .join(LIBRARY).on(LIBRARY.ID.eq(PROGRAM.LIBRARY_ID))
        .where(PROGRAM.LIBRARY_ID.in(parentIds))
        .and(LIBRARY.ACCOUNT_ID.in(hubAccess.getAccountIds()))
        .orderBy(PROGRAM.TYPE, PROGRAM.NAME)
        .fetch());
  }

  @Override
  public Collection<Entity> readManyWithChildEntities(HubAccess hubAccess, Collection<UUID> programIds) throws DAOException {
    DSLContext db = dbProvider.getDSL();

    requireRead(db, hubAccess, programIds);

    Collection<Entity> entities = Lists.newArrayList();
    entities.addAll(modelsFrom(Program.class, db.selectFrom(PROGRAM).where(PROGRAM.ID.in(programIds)).fetch()));
    entities.addAll(modelsFrom(ProgramSequencePatternEvent.class, db.selectFrom(PROGRAM_SEQUENCE_PATTERN_EVENT).where(PROGRAM_SEQUENCE_PATTERN_EVENT.PROGRAM_ID.in(programIds))));
    entities.addAll(modelsFrom(ProgramMeme.class, db.selectFrom(PROGRAM_MEME).where(PROGRAM_MEME.PROGRAM_ID.in(programIds))));
    entities.addAll(modelsFrom(ProgramSequencePattern.class, db.selectFrom(PROGRAM_SEQUENCE_PATTERN).where(PROGRAM_SEQUENCE_PATTERN.PROGRAM_ID.in(programIds))));
    entities.addAll(modelsFrom(ProgramSequenceBinding.class, db.selectFrom(PROGRAM_SEQUENCE_BINDING).where(PROGRAM_SEQUENCE_BINDING.PROGRAM_ID.in(programIds))));
    entities.addAll(modelsFrom(ProgramSequenceBindingMeme.class, db.selectFrom(PROGRAM_SEQUENCE_BINDING_MEME).where(PROGRAM_SEQUENCE_BINDING_MEME.PROGRAM_ID.in(programIds))));
    entities.addAll(modelsFrom(ProgramSequenceChord.class, db.selectFrom(PROGRAM_SEQUENCE_CHORD).where(PROGRAM_SEQUENCE_CHORD.PROGRAM_ID.in(programIds))));
    entities.addAll(modelsFrom(ProgramSequence.class, db.selectFrom(PROGRAM_SEQUENCE).where(PROGRAM_SEQUENCE.PROGRAM_ID.in(programIds))));
    entities.addAll(modelsFrom(ProgramVoiceTrack.class, db.selectFrom(PROGRAM_VOICE_TRACK).where(PROGRAM_VOICE_TRACK.PROGRAM_ID.in(programIds))));
    entities.addAll(modelsFrom(ProgramVoice.class, db.selectFrom(PROGRAM_VOICE).where(PROGRAM_VOICE.PROGRAM_ID.in(programIds))));
    return entities;

  }

  @Override
  public Collection<Entity> readChildEntities(HubAccess hubAccess, Collection<UUID> programIds, Collection<String> types) throws DAOException {
    DSLContext db = dbProvider.getDSL();

    requireRead(db, hubAccess, programIds);

    Collection<Entity> entities = Lists.newArrayList();

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

    return entities;
  }

  @Override
  public Collection<Program> readMany(HubAccess hubAccess) throws DAOException {
    if (hubAccess.isTopLevel())
      return modelsFrom(Program.class, dbProvider.getDSL().select(PROGRAM.fields()).from(PROGRAM)
        .orderBy(PROGRAM.TYPE, PROGRAM.NAME)
        .fetch());
    else
      return modelsFrom(Program.class, dbProvider.getDSL().select(PROGRAM.fields()).from(PROGRAM)
        .join(LIBRARY).on(LIBRARY.ID.eq(PROGRAM.LIBRARY_ID))
        .where(LIBRARY.ACCOUNT_ID.in(hubAccess.getAccountIds()))
        .orderBy(PROGRAM.TYPE, PROGRAM.NAME)
        .fetch());
  }

  @Override
  public void update(HubAccess hubAccess, UUID id, Program program) throws DAOException, JsonApiException, ValueException {
    program.validate();
    requireArtist(hubAccess);
    DSLContext db = dbProvider.getDSL();
    Program existing = readOne(db, hubAccess, id);
    validateConfig(program);

    // [#170390872] prevent user from changing program type of a Rhythm program, when it has any Tracks and/or Voices.
    if (ProgramType.Rhythm.equals(existing.getType()) && !ProgramType.Rhythm.equals(program.getType()))
      requireNotExists("Voice in Program; Can't change type away from Rhythm", db.selectCount().from(PROGRAM_VOICE)
        .where(PROGRAM_VOICE.PROGRAM_ID.eq(id))
        .fetchOne(0, int.class));

    executeUpdate(db, PROGRAM, id, program);
  }

  @Override
  public void destroy(HubAccess hubAccess, UUID id) throws DAOException {
    DSLContext db = dbProvider.getDSL();

    if (!hubAccess.isTopLevel())
      requireExists("Program belonging to you", db.selectCount().from(PROGRAM)
        .join(LIBRARY).on(PROGRAM.LIBRARY_ID.eq(LIBRARY.ID))
        .where(PROGRAM.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(hubAccess.getAccountIds()))
        .fetchOne(0, int.class));

    //
    // [#170299297] Cannot delete Programs that have a Meme-- otherwise, destroy all inner entities
    //

    requireNotExists("Program Memes", db.selectCount().from(PROGRAM_MEME)
      .where(PROGRAM_MEME.PROGRAM_ID.eq(id))
      .fetchOne(0, int.class));


    db.deleteFrom(PROGRAM_SEQUENCE_PATTERN_EVENT)
      .where(PROGRAM_SEQUENCE_PATTERN_EVENT.PROGRAM_ID.eq(id))
      .execute();

    db.deleteFrom(PROGRAM_SEQUENCE_PATTERN)
      .where(PROGRAM_SEQUENCE_PATTERN.PROGRAM_ID.eq(id))
      .execute();

    db.deleteFrom(PROGRAM_SEQUENCE_BINDING_MEME)
      .where(PROGRAM_SEQUENCE_BINDING_MEME.PROGRAM_ID.eq(id))
      .execute();

    db.deleteFrom(PROGRAM_SEQUENCE_BINDING)
      .where(PROGRAM_SEQUENCE_BINDING.PROGRAM_ID.eq(id))
      .execute();

    db.deleteFrom(PROGRAM_SEQUENCE_CHORD)
      .where(PROGRAM_SEQUENCE_CHORD.PROGRAM_ID.eq(id))
      .execute();

    db.deleteFrom(PROGRAM_SEQUENCE)
      .where(PROGRAM_SEQUENCE.PROGRAM_ID.eq(id))
      .execute();

    db.deleteFrom(PROGRAM_VOICE_TRACK)
      .where(PROGRAM_VOICE_TRACK.PROGRAM_ID.eq(id))
      .execute();

    db.deleteFrom(PROGRAM_VOICE)
      .where(PROGRAM_VOICE.PROGRAM_ID.eq(id))
      .execute();

    db.deleteFrom(PROGRAM)
      .where(PROGRAM.ID.eq(id))
      .execute();
  }

  @Override
  public Program newInstance() {
    return new Program();
  }

  @Override
  public Collection<Program> readManyInAccount(HubAccess hubAccess, UUID accountId) throws DAOException {
    if (hubAccess.isTopLevel())
      return modelsFrom(Program.class, dbProvider.getDSL().select(PROGRAM.fields()).from(PROGRAM)
        .join(LIBRARY).on(PROGRAM.LIBRARY_ID.eq(LIBRARY.ID))
        .where(LIBRARY.ACCOUNT_ID.eq(accountId))
        .orderBy(PROGRAM.TYPE, PROGRAM.NAME)
        .fetch());
    else
      return modelsFrom(Program.class, dbProvider.getDSL().select(PROGRAM.fields()).from(PROGRAM)
        .join(LIBRARY).on(PROGRAM.LIBRARY_ID.eq(LIBRARY.ID))
        .where(LIBRARY.ACCOUNT_ID.in(accountId))
        .and(LIBRARY.ACCOUNT_ID.in(hubAccess.getAccountIds()))
        .orderBy(PROGRAM.TYPE, PROGRAM.NAME)
        .fetch());
  }

  @Override
  public Collection<Program> readManyInState(HubAccess hubAccess, ProgramState state) throws DAOException {
    require(hubAccess, UserRoleType.Admin, UserRoleType.Engineer);
    // FUTURE: engineer should only see programs in account?

    return modelsFrom(Program.class, dbProvider.getDSL().select(PROGRAM.fields())
      .from(PROGRAM)
      .where(PROGRAM.STATE.eq(state.toString()))
      .or(PROGRAM.STATE.eq(state.toString().toLowerCase(Locale.ENGLISH)))
      .fetch());
  }

  @Override
  public Collection<UUID> readIdsInLibraries(HubAccess hubAccess, Collection<UUID> parentIds) throws DAOException {
    requireArtist(hubAccess);
    return DAO.idsFrom(dbProvider.getDSL().select(PROGRAM.ID)
      .from(PROGRAM)
      .where(PROGRAM.LIBRARY_ID.in(parentIds))
      .and(PROGRAM.STATE.equal(ProgramState.Published.toString()))
      .fetch());
  }

  /**
   [#175347578] validate TypeSafe program config

   @param program config to validate
   */
  private void validateConfig(Program program) throws ValueException {
    new ProgramConfig(program, config);
  }

  /**
   Require read hubAccess

   @param db         database context
   @param hubAccess  control
   @param programIds to require hubAccess to
   */
  private void requireRead(DSLContext db, HubAccess hubAccess, Collection<UUID> programIds) throws DAOException {
    if (!hubAccess.isTopLevel())
      for (UUID programId : programIds)
        requireExists("hubAccess via account", db.selectCount().from(PROGRAM)
          .join(LIBRARY).on(LIBRARY.ID.eq(PROGRAM.LIBRARY_ID))
          .where(PROGRAM.ID.eq(programId))
          .and(LIBRARY.ACCOUNT_ID.in(hubAccess.getAccountIds()))
          .fetchOne(0, int.class));
  }

  /**
   Read one record

   @param db        DSL context
   @param hubAccess control
   @param id        to read
   @return record
   @throws DAOException on failure
   */
  private Program readOne(DSLContext db, HubAccess hubAccess, UUID id) throws DAOException {
    if (hubAccess.isTopLevel())
      return modelFrom(Program.class,
        db.selectFrom(PROGRAM)
          .where(PROGRAM.ID.eq(id))
          .fetchOne());
    else
      return modelFrom(Program.class,
        db.select(PROGRAM.fields())
          .from(PROGRAM)
          .join(LIBRARY).on(LIBRARY.ID.eq(PROGRAM.LIBRARY_ID))
          .where(PROGRAM.ID.eq(id))
          .and(LIBRARY.ACCOUNT_ID.in(hubAccess.getAccountIds()))
          .fetchOne());
  }

  /**
   Require parent program exists of a given possible entity in a DSL context

   @param db        DSL context
   @param hubAccess control
   @param entity    to validate
   @throws DAOException if parent does not exist
   */
  private void requireParentExists(DSLContext db, HubAccess hubAccess, Program entity) throws DAOException {
    if (hubAccess.isTopLevel())
      requireExists("Library", db.selectCount().from(LIBRARY)
        .where(LIBRARY.ID.eq(entity.getLibraryId()))
        .fetchOne(0, int.class));
    else
      requireExists("Library", db.selectCount().from(LIBRARY)
        .where(LIBRARY.ACCOUNT_ID.in(hubAccess.getAccountIds()))
        .and(LIBRARY.ID.eq(entity.getLibraryId()))
        .fetchOne(0, int.class));
  }

}

