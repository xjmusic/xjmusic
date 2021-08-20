// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.dao;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.typesafe.config.Config;
import io.xj.api.Program;
import io.xj.api.ProgramMeme;
import io.xj.api.ProgramSequence;
import io.xj.api.ProgramSequenceBinding;
import io.xj.api.ProgramSequenceBindingMeme;
import io.xj.api.ProgramSequenceChord;
import io.xj.api.ProgramSequenceChordVoicing;
import io.xj.api.ProgramSequencePattern;
import io.xj.api.ProgramSequencePatternEvent;
import io.xj.api.ProgramState;
import io.xj.api.ProgramVoice;
import io.xj.api.ProgramVoiceTrack;
import io.xj.api.UserRoleType;
import io.xj.hub.access.HubAccess;
import io.xj.hub.persistence.HubDatabaseProvider;
import io.xj.lib.entity.Entities;
import io.xj.lib.entity.EntityException;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.entity.common.ProgramConfig;
import io.xj.lib.jsonapi.JsonapiException;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.util.Value;
import io.xj.lib.util.ValueException;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import java.util.Collection;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static io.xj.hub.Tables.LIBRARY;
import static io.xj.hub.Tables.PROGRAM;
import static io.xj.hub.Tables.PROGRAM_MEME;
import static io.xj.hub.Tables.PROGRAM_SEQUENCE;
import static io.xj.hub.Tables.PROGRAM_SEQUENCE_BINDING;
import static io.xj.hub.Tables.PROGRAM_SEQUENCE_BINDING_MEME;
import static io.xj.hub.Tables.PROGRAM_SEQUENCE_CHORD;
import static io.xj.hub.Tables.PROGRAM_SEQUENCE_CHORD_VOICING;
import static io.xj.hub.Tables.PROGRAM_SEQUENCE_PATTERN;
import static io.xj.hub.Tables.PROGRAM_SEQUENCE_PATTERN_EVENT;
import static io.xj.hub.Tables.PROGRAM_VOICE;
import static io.xj.hub.Tables.PROGRAM_VOICE_TRACK;

public class ProgramDAOImpl extends DAOImpl<Program> implements ProgramDAO {
  private final Config config;

  @Inject
  public ProgramDAOImpl(
    Config config,
    JsonapiPayloadFactory payloadFactory,
    EntityFactory entityFactory,
    HubDatabaseProvider dbProvider
  ) {
    super(payloadFactory, entityFactory);
    this.config = config;
    this.dbProvider = dbProvider;
  }

  @Override
  public Program create(HubAccess hubAccess, Program rawProgram) throws DAOException, JsonapiException, ValueException {
    var program = validate(rawProgram);
    requireArtist(hubAccess);
    return modelFrom(Program.class, executeCreate(dbProvider.getDSL(), PROGRAM, program));
  }

  @Override
  public DAOCloner<Program> clone(HubAccess hubAccess, UUID rawCloneId, Program rawProgram) throws DAOException {
    requireArtist(hubAccess);
    AtomicReference<Program> result = new AtomicReference<>();
    AtomicReference<DAOCloner<Program>> cloner = new AtomicReference<>();
    dbProvider.getDSL().transaction(ctx -> {
      DSLContext db = DSL.using(ctx);

      Program from = readOne(db, hubAccess, rawCloneId);
      if (Objects.isNull(from))
        throw new DAOException("Can't clone nonexistent Program");

      // Inherits state, type if none specified
      setIfProvided(from, rawProgram, "libraryId");
      setIfProvided(from, rawProgram, "name");
      Program program = validate(from);
      requireParentExists(db, hubAccess, program);

      // Create main entity
      result.set(modelFrom(Program.class, executeCreate(db, PROGRAM, program)));
      UUID originalId = result.get().getId();

      // Prepare to clone sub-entities
      cloner.set(new DAOCloner<>(result.get(), this));

      // Clone ProgramMeme
      cloner.get().clone(db, PROGRAM_MEME, PROGRAM_MEME.ID, ImmutableSet.of(), PROGRAM_MEME.PROGRAM_ID, rawCloneId, originalId);

      // Clone ProgramVoice
      cloner.get().clone(db, PROGRAM_VOICE, PROGRAM_VOICE.ID, ImmutableSet.of(), PROGRAM_VOICE.PROGRAM_ID, rawCloneId, originalId);

      // Clone ProgramVoiceTrack belongs to ProgramVoice
      cloner.get().clone(db, PROGRAM_VOICE_TRACK, PROGRAM_VOICE_TRACK.ID,
        ImmutableSet.of(PROGRAM_VOICE_TRACK.PROGRAM_VOICE_ID),
        PROGRAM_VOICE_TRACK.PROGRAM_ID, rawCloneId, originalId);

      // Clone ProgramSequence
      cloner.get().clone(db, PROGRAM_SEQUENCE, PROGRAM_SEQUENCE.ID, ImmutableSet.of(), PROGRAM_SEQUENCE.PROGRAM_ID, rawCloneId, originalId);

      // Clone ProgramSequenceChord belongs to ProgramSequence
      cloner.get().clone(db, PROGRAM_SEQUENCE_CHORD, PROGRAM_SEQUENCE_CHORD.ID,
        ImmutableSet.of(PROGRAM_SEQUENCE_CHORD.PROGRAM_SEQUENCE_ID),
        PROGRAM_SEQUENCE_CHORD.PROGRAM_ID, rawCloneId, originalId);

      // Clone ProgramSequenceChordVoiding belongs to ProgramSequenceChord
      cloner.get().clone(db, PROGRAM_SEQUENCE_CHORD_VOICING, PROGRAM_SEQUENCE_CHORD_VOICING.ID,
        ImmutableSet.of(PROGRAM_SEQUENCE_CHORD_VOICING.PROGRAM_SEQUENCE_CHORD_ID),
        PROGRAM_SEQUENCE_CHORD_VOICING.PROGRAM_ID, rawCloneId, originalId);

      // Clone ProgramSequenceBinding belongs to ProgramSequence
      cloner.get().clone(db, PROGRAM_SEQUENCE_BINDING, PROGRAM_SEQUENCE_BINDING.ID,
        ImmutableSet.of(PROGRAM_SEQUENCE_BINDING.PROGRAM_SEQUENCE_ID),
        PROGRAM_SEQUENCE_BINDING.PROGRAM_ID, rawCloneId, originalId);

      // Clone ProgramSequenceBindingMeme belongs to ProgramSequenceBinding
      cloner.get().clone(db, PROGRAM_SEQUENCE_BINDING_MEME, PROGRAM_SEQUENCE_BINDING_MEME.ID,
        ImmutableSet.of(PROGRAM_SEQUENCE_BINDING_MEME.PROGRAM_SEQUENCE_BINDING_ID),
        PROGRAM_SEQUENCE_BINDING_MEME.PROGRAM_ID, rawCloneId, originalId);

      // Clone ProgramSequencePattern belongs to ProgramSequence and ProgramVoice
      cloner.get().clone(db, PROGRAM_SEQUENCE_PATTERN, PROGRAM_SEQUENCE_PATTERN.ID,
        ImmutableSet.of(PROGRAM_SEQUENCE_PATTERN.PROGRAM_SEQUENCE_ID, PROGRAM_SEQUENCE_PATTERN.PROGRAM_VOICE_ID),
        PROGRAM_SEQUENCE_PATTERN.PROGRAM_ID, rawCloneId, originalId);

      // Clone ProgramSequencePatternEvent belongs to ProgramSequencePattern and ProgramVoiceTrack
      cloner.get().clone(db, PROGRAM_SEQUENCE_PATTERN_EVENT, PROGRAM_SEQUENCE_PATTERN_EVENT.ID,
        ImmutableSet.of(PROGRAM_SEQUENCE_PATTERN_EVENT.PROGRAM_SEQUENCE_PATTERN_ID, PROGRAM_SEQUENCE_PATTERN_EVENT.PROGRAM_VOICE_TRACK_ID),
        PROGRAM_SEQUENCE_PATTERN_EVENT.PROGRAM_ID, rawCloneId, originalId);
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
  public <N> Collection<N> readManyWithChildEntities(HubAccess hubAccess, Collection<UUID> programIds) throws DAOException {
    DSLContext db = dbProvider.getDSL();

    requireRead(db, hubAccess, programIds);

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
    //noinspection unchecked
    return (Collection<N>) entities;
  }

  @Override
  public Collection<Object> readChildEntities(HubAccess hubAccess, Collection<UUID> programIds, Collection<String> types) throws DAOException {
    DSLContext db = dbProvider.getDSL();

    requireRead(db, hubAccess, programIds);

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
  public Program update(HubAccess hubAccess, UUID id, Program rawProgram) throws DAOException, JsonapiException, ValueException {
    var builder = validate(rawProgram);

    requireArtist(hubAccess);
    DSLContext db = dbProvider.getDSL();
    readOne(db, hubAccess, id);

    executeUpdate(db, PROGRAM, id, builder);
    return builder;
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

    db.deleteFrom(PROGRAM_SEQUENCE_CHORD_VOICING)
      .where(PROGRAM_SEQUENCE_CHORD_VOICING.PROGRAM_ID.eq(id))
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
  public Collection<Program> readManyInAccount(HubAccess hubAccess, String accountId) throws DAOException {
    if (hubAccess.isTopLevel())
      return modelsFrom(Program.class, dbProvider.getDSL().select(PROGRAM.fields()).from(PROGRAM)
        .join(LIBRARY).on(PROGRAM.LIBRARY_ID.eq(LIBRARY.ID))
        .where(LIBRARY.ACCOUNT_ID.eq(UUID.fromString(accountId)))
        .orderBy(PROGRAM.TYPE, PROGRAM.NAME)
        .fetch());
    else
      return modelsFrom(Program.class, dbProvider.getDSL().select(PROGRAM.fields()).from(PROGRAM)
        .join(LIBRARY).on(PROGRAM.LIBRARY_ID.eq(LIBRARY.ID))
        .where(LIBRARY.ACCOUNT_ID.in(UUID.fromString(accountId)))
        .and(LIBRARY.ACCOUNT_ID.in(hubAccess.getAccountIds()))
        .orderBy(PROGRAM.TYPE, PROGRAM.NAME)
        .fetch());
  }

  @Override
  public Collection<Program> readManyInState(HubAccess hubAccess, ProgramState state) throws DAOException {
    require(hubAccess, UserRoleType.ADMIN, UserRoleType.ENGINEER);
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
      .and(PROGRAM.STATE.equal(ProgramState.PUBLISHED.toString()))
      .fetch());
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

  /**
   Validate data

   @param record to validate
   @return validated Program
   @throws DAOException if invalid
   */
  public Program validate(Program record) throws DAOException {
    try {
      Value.require(record.getLibraryId(), "Library ID");
      Value.require(record.getName(), "Name");
      Value.require(record.getKey(), "Key");
      Value.requireNonZero(record.getTempo(), "Tempo");
      Value.require(record.getType(), "Type");
      Value.require(record.getState(), "State");

      // [#175347578] validate TypeSafe chain config
      // [#177129498] Artist saves Program, Instrument, or Template config, validate & combine with defaults.
      if (Objects.isNull(record.getConfig()))
        record.setConfig(new ProgramConfig(config).toString());
      else
        record.setConfig(new ProgramConfig(record, config).toString());

      return record;

    } catch (ValueException e) {
      throw new DAOException(e);
    }
  }

  /**
   Set a value on the builder if it's been provided in the source

   @param builder to set values on
   @param source  to check for source values
   @param attr    name of attribute
   @throws DAOException on failure
   */
  private void setIfProvided(Object builder, Object source, String attr) throws DAOException {
    try {
      Optional<Object> value = Entities.get(source, attr);
      if (value.isPresent() && Value.isSet(value.get()))
        Entities.set(builder, attr, value.get());

    } catch (EntityException e) {
      throw new DAOException(e);
    }
  }
}

