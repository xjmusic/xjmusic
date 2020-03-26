// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.dao;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import io.xj.lib.rest_api.PayloadFactory;
import io.xj.lib.rest_api.RestApiException;
import io.xj.lib.util.Text;
import io.xj.lib.util.ValueException;
import io.xj.service.hub.HubException;
import io.xj.service.hub.access.Access;
import io.xj.service.hub.entity.Entity;
import io.xj.service.hub.model.Program;
import io.xj.service.hub.model.ProgramMeme;
import io.xj.service.hub.model.ProgramSequence;
import io.xj.service.hub.model.ProgramSequenceBinding;
import io.xj.service.hub.model.ProgramSequenceBindingMeme;
import io.xj.service.hub.model.ProgramSequenceChord;
import io.xj.service.hub.model.ProgramSequencePattern;
import io.xj.service.hub.model.ProgramSequencePatternEvent;
import io.xj.service.hub.model.ProgramState;
import io.xj.service.hub.model.ProgramType;
import io.xj.service.hub.model.ProgramVoice;
import io.xj.service.hub.model.ProgramVoiceTrack;
import io.xj.service.hub.model.UserRoleType;
import io.xj.service.hub.persistence.SQLDatabaseProvider;
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
import static io.xj.service.hub.Tables.PROGRAM_SEQUENCE_PATTERN;
import static io.xj.service.hub.Tables.PROGRAM_SEQUENCE_PATTERN_EVENT;
import static io.xj.service.hub.Tables.PROGRAM_VOICE;
import static io.xj.service.hub.Tables.PROGRAM_VOICE_TRACK;

public class ProgramDAOImpl extends DAOImpl<Program> implements ProgramDAO {

  @Inject
  public ProgramDAOImpl(
    PayloadFactory payloadFactory,
    SQLDatabaseProvider dbProvider
  ) {
    super(payloadFactory);
    this.dbProvider = dbProvider;
  }

  @Override
  public Program create(Access access, Program entity) throws HubException, RestApiException, ValueException {
    entity.validate();
    requireArtist(access);
    return modelFrom(Program.class, executeCreate(dbProvider.getDSL(), PROGRAM, entity));
  }

  @Override
  public DAOCloner<Program> clone(Access access, UUID cloneId, Program entity) throws HubException {
    requireArtist(access);
    AtomicReference<Program> result = new AtomicReference<>();
    AtomicReference<DAOCloner<Program>> cloner = new AtomicReference<>();
    dbProvider.getDSL().transaction(ctx -> {
      DSLContext db = DSL.using(ctx);

      Program from = readOne(db, access, cloneId);
      if (Objects.isNull(from))
        throw new HubException("Can't clone nonexistent Program");

      // Inherits state, type if none specified
      if (Objects.isNull(entity.getState())) entity.setStateEnum(from.getState());
      if (Objects.isNull(entity.getDensity())) entity.setDensity(from.getDensity());
      if (Objects.isNull(entity.getName())) entity.setName(from.getName());
      if (Objects.isNull(entity.getType())) entity.setTypeEnum(from.getType());
      if (Objects.isNull(entity.getTempo())) entity.setTempo(from.getTempo());
      if (Objects.isNull(entity.getKey())) entity.setKey(from.getKey());
      entity.setUserId(from.getUserId());
      entity.validate();
      requireParentExists(db, access, entity);

      // Create main entity
      result.set(modelFrom(Program.class, executeCreate(db, PROGRAM, entity)));

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
  public Program readOne(Access access, UUID id) throws HubException {
    return readOne(dbProvider.getDSL(), access, id);
  }

  @Override
  public Collection<Program> readMany(Access access, Collection<UUID> parentIds) throws HubException {
    if (access.isTopLevel())
      return modelsFrom(Program.class, dbProvider.getDSL().select(PROGRAM.fields()).from(PROGRAM)
        .where(PROGRAM.LIBRARY_ID.in(parentIds))
        .orderBy(PROGRAM.TYPE, PROGRAM.NAME)
        .fetch());
    else
      return modelsFrom(Program.class, dbProvider.getDSL().select(PROGRAM.fields()).from(PROGRAM)
        .join(LIBRARY).on(LIBRARY.ID.eq(PROGRAM.LIBRARY_ID))
        .where(PROGRAM.LIBRARY_ID.in(parentIds))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .orderBy(PROGRAM.TYPE, PROGRAM.NAME)
        .fetch());
  }

  @Override
  public Collection<Entity> readManyWithChildEntities(Access access, Collection<UUID> programIds) throws HubException {
    DSLContext db = dbProvider.getDSL();

    requireRead(db, access, programIds);

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
  public Collection<Entity> readChildEntities(Access access, Collection<UUID> programIds, Collection<String> types) throws HubException {
    DSLContext db = dbProvider.getDSL();

    requireRead(db, access, programIds);

    Collection<Entity> entities = Lists.newArrayList();

    // ProgramSequencePatternEvent
    if (types.contains(Text.toResourceType(ProgramSequencePatternEvent.class)))
      entities.addAll(modelsFrom(ProgramSequencePatternEvent.class,
        db.selectFrom(PROGRAM_SEQUENCE_PATTERN_EVENT).where(PROGRAM_SEQUENCE_PATTERN_EVENT.PROGRAM_ID.in(programIds))));

    // ProgramMeme
    if (types.contains(Text.toResourceType(ProgramMeme.class)))
      entities.addAll(modelsFrom(ProgramMeme.class,
        db.selectFrom(PROGRAM_MEME).where(PROGRAM_MEME.PROGRAM_ID.in(programIds))));

    // ProgramSequencePattern
    if (types.contains(Text.toResourceType(ProgramSequencePattern.class)))
      entities.addAll(modelsFrom(ProgramSequencePattern.class,
        db.selectFrom(PROGRAM_SEQUENCE_PATTERN).where(PROGRAM_SEQUENCE_PATTERN.PROGRAM_ID.in(programIds))));

    // ProgramSequenceBinding
    if (types.contains(Text.toResourceType(ProgramSequenceBinding.class)))
      entities.addAll(modelsFrom(ProgramSequenceBinding.class,
        db.selectFrom(PROGRAM_SEQUENCE_BINDING).where(PROGRAM_SEQUENCE_BINDING.PROGRAM_ID.in(programIds))));

    // ProgramSequenceBindingMeme
    if (types.contains(Text.toResourceType(ProgramSequenceBindingMeme.class)))
      entities.addAll(modelsFrom(ProgramSequenceBindingMeme.class,
        db.selectFrom(PROGRAM_SEQUENCE_BINDING_MEME).where(PROGRAM_SEQUENCE_BINDING_MEME.PROGRAM_ID.in(programIds))));

    // ProgramSequenceChord
    if (types.contains(Text.toResourceType(ProgramSequenceChord.class)))
      entities.addAll(modelsFrom(ProgramSequenceChord.class,
        db.selectFrom(PROGRAM_SEQUENCE_CHORD).where(PROGRAM_SEQUENCE_CHORD.PROGRAM_ID.in(programIds))));

    // ProgramSequence
    if (types.contains(Text.toResourceType(ProgramSequence.class)))
      entities.addAll(modelsFrom(ProgramSequence.class,
        db.selectFrom(PROGRAM_SEQUENCE).where(PROGRAM_SEQUENCE.PROGRAM_ID.in(programIds))));

    // ProgramVoiceTrack
    if (types.contains(Text.toResourceType(ProgramVoiceTrack.class)))
      entities.addAll(modelsFrom(ProgramVoiceTrack.class,
        db.selectFrom(PROGRAM_VOICE_TRACK).where(PROGRAM_VOICE_TRACK.PROGRAM_ID.in(programIds))));

    // ProgramVoice
    if (types.contains(Text.toResourceType(ProgramVoice.class)))
      entities.addAll(modelsFrom(ProgramVoice.class,
        db.selectFrom(PROGRAM_VOICE).where(PROGRAM_VOICE.PROGRAM_ID.in(programIds))));

    return entities;
  }

  @Override
  public Collection<Program> readAll(Access access) throws HubException {
    if (access.isTopLevel())
      return modelsFrom(Program.class, dbProvider.getDSL().select(PROGRAM.fields()).from(PROGRAM)
        .orderBy(PROGRAM.TYPE, PROGRAM.NAME)
        .fetch());
    else
      return modelsFrom(Program.class, dbProvider.getDSL().select(PROGRAM.fields()).from(PROGRAM)
        .join(LIBRARY).on(LIBRARY.ID.eq(PROGRAM.LIBRARY_ID))
        .where(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .orderBy(PROGRAM.TYPE, PROGRAM.NAME)
        .fetch());
  }

  @Override
  public void update(Access access, UUID id, Program entity) throws HubException, RestApiException, ValueException {
    entity.validate();
    requireArtist(access);
    DSLContext db = dbProvider.getDSL();
    Program existing = readOne(db, access, id);

    // [#170390872] prevent user from changing program type of a Rhythm program, when it has any Tracks and/or Voices.
    if (ProgramType.Rhythm.equals(existing.getType()) && !ProgramType.Rhythm.equals(entity.getType()))
      requireNotExists("Voice in Program; Can't change type away from Rhythm", db.selectCount().from(PROGRAM_VOICE)
        .where(PROGRAM_VOICE.PROGRAM_ID.eq(id))
        .fetchOne(0, int.class));

    executeUpdate(db, PROGRAM, id, entity);
  }

  @Override
  public void destroy(Access access, UUID id) throws HubException {
    DSLContext db = dbProvider.getDSL();

    if (!access.isTopLevel())
      requireExists("Program belonging to you", db.selectCount().from(PROGRAM)
        .join(LIBRARY).on(PROGRAM.LIBRARY_ID.eq(LIBRARY.ID))
        .where(PROGRAM.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
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
  public Collection<Program> readAllInAccount(Access access, UUID accountId) throws HubException {
    if (access.isTopLevel())
      return modelsFrom(Program.class, dbProvider.getDSL().select(PROGRAM.fields()).from(PROGRAM)
        .join(LIBRARY).on(PROGRAM.LIBRARY_ID.eq(LIBRARY.ID))
        .where(LIBRARY.ACCOUNT_ID.eq(accountId))
        .orderBy(PROGRAM.TYPE, PROGRAM.NAME)
        .fetch());
    else
      return modelsFrom(Program.class, dbProvider.getDSL().select(PROGRAM.fields()).from(PROGRAM)
        .join(LIBRARY).on(PROGRAM.LIBRARY_ID.eq(LIBRARY.ID))
        .where(LIBRARY.ACCOUNT_ID.in(accountId))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .orderBy(PROGRAM.TYPE, PROGRAM.NAME)
        .fetch());
  }

  @Override
  public Collection<Program> readAllInState(Access access, ProgramState state) throws HubException {
    requireRole("platform access", access, UserRoleType.Admin, UserRoleType.Engineer);
    // FUTURE: engineer should only see programs in account?

    return modelsFrom(Program.class, dbProvider.getDSL().select(PROGRAM.fields())
      .from(PROGRAM)
      .where(PROGRAM.STATE.eq(state.toString()))
      .or(PROGRAM.STATE.eq(state.toString().toLowerCase(Locale.ENGLISH)))
      .fetch());
  }

  @Override
  public Collection<UUID> readIdsInLibraries(Access access, Collection<UUID> parentIds) throws HubException {
    requireArtist(access);
    return DAO.idsFrom(dbProvider.getDSL().select(PROGRAM.ID)
      .from(PROGRAM)
      .where(PROGRAM.LIBRARY_ID.in(parentIds))
      .and(PROGRAM.STATE.equal(ProgramState.Published.toString()))
      .fetch());
  }

  /**
   Require read access

   @param db         database context
   @param access     control
   @param programIds to require access to
   */
  private void requireRead(DSLContext db, Access access, Collection<UUID> programIds) throws HubException {
    if (!access.isTopLevel())
      for (UUID programId : programIds)
        requireExists("access via account", db.selectCount().from(PROGRAM)
          .join(LIBRARY).on(LIBRARY.ID.eq(PROGRAM.LIBRARY_ID))
          .where(PROGRAM.ID.eq(programId))
          .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
          .fetchOne(0, int.class));
  }

  /**
   Read one record

   @param db     DSL context
   @param access control
   @param id     to read
   @return record
   @throws HubException on failure
   */
  private Program readOne(DSLContext db, Access access, UUID id) throws HubException {
    if (access.isTopLevel())
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
          .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
          .fetchOne());
  }

  /**
   Require parent program exists of a given possible entity in a DSL context

   @param db     DSL context
   @param access control
   @param entity to validate
   @throws HubException if parent does not exist
   */
  private void requireParentExists(DSLContext db, Access access, Program entity) throws HubException {
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

}

