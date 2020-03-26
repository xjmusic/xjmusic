// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.dao;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import io.xj.lib.rest_api.PayloadFactory;
import io.xj.lib.rest_api.RestApiException;
import io.xj.lib.util.ValueException;
import io.xj.service.hub.HubException;
import io.xj.service.hub.access.Access;
import io.xj.service.hub.model.ProgramSequence;
import io.xj.service.hub.persistence.SQLDatabaseProvider;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static io.xj.service.hub.Tables.LIBRARY;
import static io.xj.service.hub.Tables.PROGRAM;
import static io.xj.service.hub.Tables.PROGRAM_SEQUENCE;
import static io.xj.service.hub.Tables.PROGRAM_SEQUENCE_BINDING;
import static io.xj.service.hub.Tables.PROGRAM_SEQUENCE_BINDING_MEME;
import static io.xj.service.hub.Tables.PROGRAM_SEQUENCE_CHORD;
import static io.xj.service.hub.Tables.PROGRAM_SEQUENCE_PATTERN;
import static io.xj.service.hub.Tables.PROGRAM_SEQUENCE_PATTERN_EVENT;

public class ProgramSequenceDAOImpl extends DAOImpl<ProgramSequence> implements ProgramSequenceDAO {

  @Inject
  public ProgramSequenceDAOImpl(
    PayloadFactory payloadFactory,
    SQLDatabaseProvider dbProvider
  ) {
    super(payloadFactory);
    this.dbProvider = dbProvider;
  }

  @Override
  public ProgramSequence create(Access access, ProgramSequence entity) throws HubException, RestApiException, ValueException {
    entity.validate();
    DSLContext db = dbProvider.getDSL();
    requireProgramModification(db, access, entity.getProgramId());
    return modelFrom(ProgramSequence.class,
      executeCreate(dbProvider.getDSL(), PROGRAM_SEQUENCE, entity));
  }

  @Override
  public DAOCloner<ProgramSequence> clone(Access access, UUID cloneId, ProgramSequence entity) throws HubException {
    requireArtist(access);
    AtomicReference<ProgramSequence> result = new AtomicReference<>();
    AtomicReference<DAOCloner<ProgramSequence>> cloner = new AtomicReference<>();
    dbProvider.getDSL().transaction(ctx -> {
      DSLContext db = DSL.using(ctx);
      requireModification(db, access, cloneId);

      ProgramSequence from = readOne(db, access, cloneId);
      if (Objects.isNull(from))
        throw new HubException("Can't clone nonexistent ProgramSequence");

      // Inherits state, type if none specified
      if (Objects.isNull(entity.getTotal())) entity.setTotal(from.getTotal());
      if (Objects.isNull(entity.getName())) entity.setName(from.getName());
      if (Objects.isNull(entity.getTempo())) entity.setTempo(from.getTempo());
      if (Objects.isNull(entity.getDensity())) entity.setDensity(from.getDensity());
      if (Objects.isNull(entity.getKey())) entity.setKey(from.getKey());
      entity.validate();
      requireParentExists(db, access, entity);

      // Create main entity
      result.set(modelFrom(ProgramSequence.class, executeCreate(db, PROGRAM_SEQUENCE, entity)));

      // Prepare to clone sub-entities
      cloner.set(new DAOCloner<>(result.get(), this));

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
  public ProgramSequence readOne(Access access, UUID id) throws HubException {
    requireArtist(access);
    return readOne(dbProvider.getDSL(), access, id);
  }

  @Override
  @Nullable
  public Collection<ProgramSequence> readMany(Access access, Collection<UUID> parentIds) throws HubException {
    requireArtist(access);
    if (access.isTopLevel())
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
          .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
          .fetch());
  }

  @Override
  public void update(Access access, UUID id, ProgramSequence entity) throws HubException, RestApiException, ValueException {
    entity.validate();
    DSLContext db = dbProvider.getDSL();
    requireModification(db, access, id);
    executeUpdate(db, PROGRAM_SEQUENCE, id, entity);
  }

  @Override
  public void destroy(Access access, UUID id) throws HubException {
    DSLContext db = dbProvider.getDSL();
    requireModification(db, access, id);

    requireNotExists("binding of Sequence to Program", db.selectCount().from(PROGRAM_SEQUENCE_BINDING)
      .where(PROGRAM_SEQUENCE_BINDING.PROGRAM_SEQUENCE_ID.eq(id))
      .fetchOne(0, int.class));

    db.deleteFrom(PROGRAM_SEQUENCE_CHORD)
      .where(PROGRAM_SEQUENCE_CHORD.PROGRAM_SEQUENCE_ID.eq(id))
      .execute();

    db.deleteFrom(PROGRAM_SEQUENCE_PATTERN_EVENT)
      .where(PROGRAM_SEQUENCE_PATTERN_EVENT.PROGRAM_SEQUENCE_PATTERN_ID.in(
        db.select(PROGRAM_SEQUENCE_PATTERN.ID).from(PROGRAM_SEQUENCE_PATTERN)
          .where(PROGRAM_SEQUENCE_PATTERN.PROGRAM_SEQUENCE_ID.eq(id))))
      .execute();

    db.deleteFrom(PROGRAM_SEQUENCE_PATTERN)
      .where(PROGRAM_SEQUENCE_PATTERN.PROGRAM_SEQUENCE_ID.eq(id))
      .execute();

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

   @param db     context
   @param access control
   @param id     of entity to require modification access to
   @throws HubException on invalid permissions
   */
  private void requireModification(DSLContext db, Access access, UUID id) throws HubException {
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
   Require parent ProgramSequence exists of a given possible entity in a DSL context

   @param db     DSL context
   @param access control
   @param entity to validate
   @throws HubException if parent does not exist
   */
  private void requireParentExists(DSLContext db, Access access, ProgramSequence entity) throws HubException {
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
   Read one Program Sequence

   @param db     context
   @param access control
   @param id     of entity to read
   @return program sequence
   */
  private ProgramSequence readOne(DSLContext db, Access access, UUID id) throws HubException {
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
}
