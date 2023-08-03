// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.manager;

import java.util.Set;
import io.xj.hub.access.HubAccess;
import io.xj.hub.persistence.HubPersistenceServiceImpl;
import io.xj.hub.persistence.HubSqlStoreProvider;
import io.xj.hub.tables.pojos.ProgramSequencePattern;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.JsonapiException;
import io.xj.lib.util.ValueException;
import io.xj.lib.util.ValueUtils;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;

import org.jetbrains.annotations.Nullable;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static io.xj.hub.Tables.LIBRARY;
import static io.xj.hub.Tables.PROGRAM;
import static io.xj.hub.Tables.PROGRAM_SEQUENCE;
import static io.xj.hub.Tables.PROGRAM_SEQUENCE_PATTERN;
import static io.xj.hub.Tables.PROGRAM_SEQUENCE_PATTERN_EVENT;

@Service
public class ProgramSequencePatternManagerImpl extends HubPersistenceServiceImpl implements ProgramSequencePatternManager {

  public ProgramSequencePatternManagerImpl(
    EntityFactory entityFactory,
    HubSqlStoreProvider sqlStoreProvider
  ) {
    super(entityFactory, sqlStoreProvider);
  }

  @Override
  public ProgramSequencePattern create(HubAccess access, ProgramSequencePattern entity) throws ManagerException, JsonapiException, ValueException {
    ProgramSequencePattern builder = validate(entity);
    DSLContext db = sqlStoreProvider.getDSL();
    requireProgramModification(db, access, builder.getProgramId());
    return modelFrom(ProgramSequencePattern.class,
      executeCreate(db, PROGRAM_SEQUENCE_PATTERN, builder));
  }

  @Override
  public ManagerCloner<ProgramSequencePattern> clone(HubAccess access, UUID cloneId, ProgramSequencePattern to) throws ManagerException {
    requireArtist(access);
    AtomicReference<ProgramSequencePattern> result = new AtomicReference<>();
    AtomicReference<ManagerCloner<ProgramSequencePattern>> cloner = new AtomicReference<>();
    sqlStoreProvider.getDSL().transaction(ctx -> {
      DSLContext db = DSL.using(ctx);
      requireModification(db, access, cloneId);

      var from = readOne(db, access, cloneId);
      if (Objects.isNull(from))
        throw new ManagerException("Can't clone nonexistent ProgramSequencePattern");

      // When not set, clone inherits attribute values from original record
      entityFactory.setAllEmptyAttributes(from, to);
      to.setTotal(from.getTotal()); // total cannot be modified while cloning
      var record = validate(to);
      requireParentExists(db, access, record);

      // Create main entity
      result.set(modelFrom(ProgramSequencePattern.class, executeCreate(db, PROGRAM_SEQUENCE_PATTERN, record)));

      // Prepare to clone sub-entities
      cloner.set(new ManagerCloner<>(result.get(), this));

      // Clone ProgramSequencePatternEvent belongs to newly cloned ProgramSequencePattern and ProgramVoiceTrack
      cloner.get().clone(db, PROGRAM_SEQUENCE_PATTERN_EVENT, PROGRAM_SEQUENCE_PATTERN_EVENT.ID,
        Set.of(PROGRAM_SEQUENCE_PATTERN_EVENT.PROGRAM_SEQUENCE_PATTERN_ID, PROGRAM_SEQUENCE_PATTERN_EVENT.PROGRAM_VOICE_TRACK_ID),
        PROGRAM_SEQUENCE_PATTERN_EVENT.PROGRAM_SEQUENCE_PATTERN_ID,
        cloneId, result.get().getId());

    });
    return cloner.get();
  }

  @Override
  @Nullable
  public ProgramSequencePattern readOne(HubAccess access, UUID id) throws ManagerException {
    return readOne(sqlStoreProvider.getDSL(), access, id);
  }

  @Override
  @Nullable
  public Collection<ProgramSequencePattern> readMany(HubAccess access, Collection<UUID> parentIds) throws ManagerException {
    requireArtist(access);
    if (access.isTopLevel())
      return modelsFrom(ProgramSequencePattern.class,
        sqlStoreProvider.getDSL().selectFrom(PROGRAM_SEQUENCE_PATTERN)
          .where(PROGRAM_SEQUENCE_PATTERN.PROGRAM_SEQUENCE_ID.in(parentIds))
          .fetch());
    else
      return modelsFrom(ProgramSequencePattern.class,
        sqlStoreProvider.getDSL().select(PROGRAM_SEQUENCE_PATTERN.fields()).from(PROGRAM_SEQUENCE_PATTERN)
          .join(PROGRAM).on(PROGRAM.ID.eq(PROGRAM_SEQUENCE_PATTERN.PROGRAM_ID))
          .join(LIBRARY).on(LIBRARY.ID.eq(PROGRAM.LIBRARY_ID))
          .where(PROGRAM_SEQUENCE_PATTERN.PROGRAM_SEQUENCE_ID.in(parentIds))
          .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
          .fetch());

  }

  @Override
  public ProgramSequencePattern update(HubAccess access, UUID id, ProgramSequencePattern rawProgramSequencePattern) throws ManagerException, JsonapiException, ValueException {
    ProgramSequencePattern builder = validate(rawProgramSequencePattern);
    requireArtist(access);
    DSLContext db = sqlStoreProvider.getDSL();
    requireModification(db, access, id);
    executeUpdate(db, PROGRAM_SEQUENCE_PATTERN, id, builder);
    return builder;
  }

  @Override
  public void destroy(HubAccess access, UUID id) throws ManagerException {
    requireArtist(access);
    DSLContext db = sqlStoreProvider.getDSL();
    requireModification(db, access, id);

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
   * Read one Program Sequence Pattern
   *
   * @param db     context
   * @param access control
   * @param id     of entity to read
   * @return program sequence pattern
   */
  ProgramSequencePattern readOne(DSLContext db, HubAccess access, UUID id) throws ManagerException {
    requireArtist(access);
    if (access.isTopLevel())
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
          .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
          .fetchOne());
  }

  /**
   * Require parent ProgramSequence exists of a given possible entity in a DSL context
   *
   * @param db     DSL context
   * @param access control
   * @param entity to validate
   * @throws ManagerException if parent does not exist
   */
  void requireParentExists(DSLContext db, HubAccess access, ProgramSequencePattern entity) throws ManagerException {
    if (access.isTopLevel())
      requireExists("Program Sequence", db.selectCount().from(PROGRAM_SEQUENCE)
        .where(PROGRAM_SEQUENCE.ID.eq(entity.getProgramSequenceId()))
        .fetchOne(0, int.class));
    else
      requireExists("Program Sequence", db.selectCount().from(PROGRAM_SEQUENCE)
        .join(PROGRAM).on(PROGRAM.ID.eq(PROGRAM_SEQUENCE.PROGRAM_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(PROGRAM.LIBRARY_ID))
        .where(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .and(PROGRAM_SEQUENCE.ID.eq(entity.getProgramSequenceId()))
        .fetchOne(0, int.class));
  }

  /**
   * Require access to modification of a Program Sequence Pattern
   *
   * @param db     context
   * @param access control
   * @param id     to validate access to
   * @throws ManagerException if no access
   */
  void requireModification(DSLContext db, HubAccess access, UUID id) throws ManagerException {
    requireArtist(access);
    if (access.isTopLevel())
      requireExists("Program Sequence Pattern", db.selectCount().from(PROGRAM_SEQUENCE_PATTERN)
        .where(PROGRAM_SEQUENCE_PATTERN.ID.eq(id))
        .fetchOne(0, int.class));
    else
      requireExists("Sequence Pattern in Program in Account you have access to", db.selectCount().from(PROGRAM_SEQUENCE_PATTERN)
        .join(PROGRAM).on(PROGRAM.ID.eq(PROGRAM_SEQUENCE_PATTERN.PROGRAM_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(PROGRAM.LIBRARY_ID))
        .where(PROGRAM_SEQUENCE_PATTERN.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne(0, int.class));
  }

  /**
   * Validate data
   *
   * @param record to validate
   * @throws ManagerException if invalid
   */
  public ProgramSequencePattern validate(ProgramSequencePattern record) throws ManagerException {
    try {
      ValueUtils.require(record.getProgramId(), "Program ID");
      ValueUtils.require(record.getProgramVoiceId(), "Voice ID");
      ValueUtils.require(record.getProgramSequenceId(), "Sequence ID");
      ValueUtils.require(record.getName(), "Name");
      ValueUtils.require(record.getTotal(), "Total");
      return record;

    } catch (ValueException e) {
      throw new ManagerException(e);
    }
  }


}
