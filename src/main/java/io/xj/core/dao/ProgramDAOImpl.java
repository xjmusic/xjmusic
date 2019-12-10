// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import io.xj.core.access.Access;
import io.xj.core.entity.Entity;
import io.xj.core.exception.CoreException;
import io.xj.core.model.Program;
import io.xj.core.model.ProgramMeme;
import io.xj.core.model.ProgramSequence;
import io.xj.core.model.ProgramSequenceBinding;
import io.xj.core.model.ProgramSequenceBindingMeme;
import io.xj.core.model.ProgramSequenceChord;
import io.xj.core.model.ProgramSequencePattern;
import io.xj.core.model.ProgramSequencePatternEvent;
import io.xj.core.model.ProgramState;
import io.xj.core.model.ProgramVoice;
import io.xj.core.model.ProgramVoiceTrack;
import io.xj.core.model.UserRoleType;
import io.xj.core.persistence.SQLDatabaseProvider;
import io.xj.core.tables.Library;
import org.jooq.DSLContext;
import org.jooq.Record;

import java.util.Collection;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import static io.xj.core.Tables.LIBRARY;
import static io.xj.core.Tables.PROGRAM;
import static io.xj.core.Tables.PROGRAM_MEME;
import static io.xj.core.Tables.PROGRAM_SEQUENCE;
import static io.xj.core.Tables.PROGRAM_SEQUENCE_BINDING;
import static io.xj.core.Tables.PROGRAM_SEQUENCE_BINDING_MEME;
import static io.xj.core.Tables.PROGRAM_SEQUENCE_CHORD;
import static io.xj.core.Tables.PROGRAM_SEQUENCE_PATTERN;
import static io.xj.core.Tables.PROGRAM_SEQUENCE_PATTERN_EVENT;
import static io.xj.core.Tables.PROGRAM_VOICE;
import static io.xj.core.Tables.PROGRAM_VOICE_TRACK;

public class ProgramDAOImpl extends DAOImpl<Program> implements ProgramDAO {

  @Inject
  public ProgramDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public Program create(Access access, Program entity) throws CoreException {
    entity.validate();
    requireLibrary(access);
    return DAO.modelFrom(Program.class, executeCreate(PROGRAM, entity));
  }

  @Override
  public Program clone(Access access, UUID cloneId, Program entity) throws CoreException {
    requireLibrary(access);
// TODO figure out how to make this all a rollback-able transaction in the new getDataSource() context:  dataSource.setAutoCommit(false);
    Program from = readOne(access, cloneId);
    if (Objects.isNull(from))
      throw new CoreException("Can't clone nonexistent Program");

    // When null, inherits, type, state, key, and tempo
    if (Objects.isNull(entity.getType())) entity.setTypeEnum(from.getType());
    if (Objects.isNull(entity.getState())) entity.setStateEnum(from.getState());
    if (Objects.isNull(entity.getKey())) entity.setKey(from.getKey());
    if (Objects.isNull(entity.getTempo())) entity.setTempo(from.getTempo());
    entity.validate();

    // TODO clone all sub-entities of program into whole new set of entities:

// TODO figure out how to make this all a rollback-able transaction in the new getDataSource() context:     dataSource.commit();
    return create(access, entity);
  }

  @Override
  public Program readOne(Access access, UUID id) throws CoreException {
    if (access.isTopLevel())
      return DAO.modelFrom(Program.class, dbProvider.getDSL().selectFrom(PROGRAM)
        .where(PROGRAM.ID.eq(id))
        .fetchOne());
    else {
      Record programRecord = dbProvider.getDSL().select(PROGRAM.fields())
        .from(PROGRAM)
        .join(LIBRARY).on(LIBRARY.ID.eq(PROGRAM.LIBRARY_ID))
        .where(PROGRAM.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne();
      return DAO.modelFrom(Program.class, programRecord);
    }
  }

  @Override
  public Collection<Program> readMany(Access access, Collection<UUID> parentIds) throws CoreException {
    if (access.isTopLevel())
      return DAO.modelsFrom(Program.class, dbProvider.getDSL().select(PROGRAM.fields()).from(PROGRAM)
        .where(PROGRAM.LIBRARY_ID.in(parentIds))
        .and(PROGRAM.STATE.notEqual(String.valueOf(ProgramState.Erase)))
        .orderBy(PROGRAM.TYPE, PROGRAM.NAME)
        .fetch());
    else
      return DAO.modelsFrom(Program.class, dbProvider.getDSL().select(PROGRAM.fields()).from(PROGRAM)
        .join(LIBRARY).on(LIBRARY.ID.eq(PROGRAM.LIBRARY_ID))
        .where(PROGRAM.LIBRARY_ID.in(parentIds))
        .and(PROGRAM.STATE.notEqual(String.valueOf(ProgramState.Erase)))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .orderBy(PROGRAM.TYPE, PROGRAM.NAME)
        .fetch());
  }

  @Override
  public Collection<Entity> readManyWithChildEntities(Access access, Collection<UUID> programIds) throws CoreException {
    DSLContext db = dbProvider.getDSL();

    if (!access.isTopLevel())
      for (UUID programId : programIds)
        requireExists("access via account", db.selectCount().from(PROGRAM)
          .join(LIBRARY).on(LIBRARY.ID.eq(PROGRAM.LIBRARY_ID))
          .where(PROGRAM.ID.eq(programId))
          .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
          .fetchOne(0, int.class));

    Collection<Entity> entities = Lists.newArrayList();
    entities.addAll(DAO.modelsFrom(Program.class, db.selectFrom(PROGRAM).where(PROGRAM.ID.in(programIds)).fetch()));
    entities.addAll(DAO.modelsFrom(ProgramSequencePatternEvent.class, db.selectFrom(PROGRAM_SEQUENCE_PATTERN_EVENT).where(PROGRAM_SEQUENCE_PATTERN_EVENT.PROGRAM_ID.in(programIds))));
    entities.addAll(DAO.modelsFrom(ProgramMeme.class, db.selectFrom(PROGRAM_MEME).where(PROGRAM_MEME.PROGRAM_ID.in(programIds))));
    entities.addAll(DAO.modelsFrom(ProgramSequencePattern.class, db.selectFrom(PROGRAM_SEQUENCE_PATTERN).where(PROGRAM_SEQUENCE_PATTERN.PROGRAM_ID.in(programIds))));
    entities.addAll(DAO.modelsFrom(ProgramSequenceBinding.class, db.selectFrom(PROGRAM_SEQUENCE_BINDING).where(PROGRAM_SEQUENCE_BINDING.PROGRAM_ID.in(programIds))));
    entities.addAll(DAO.modelsFrom(ProgramSequenceBindingMeme.class, db.selectFrom(PROGRAM_SEQUENCE_BINDING_MEME).where(PROGRAM_SEQUENCE_BINDING_MEME.PROGRAM_ID.in(programIds))));
    entities.addAll(DAO.modelsFrom(ProgramSequenceChord.class, db.selectFrom(PROGRAM_SEQUENCE_CHORD).where(PROGRAM_SEQUENCE_CHORD.PROGRAM_ID.in(programIds))));
    entities.addAll(DAO.modelsFrom(ProgramSequence.class, db.selectFrom(PROGRAM_SEQUENCE).where(PROGRAM_SEQUENCE.PROGRAM_ID.in(programIds))));
    entities.addAll(DAO.modelsFrom(ProgramVoiceTrack.class, db.selectFrom(PROGRAM_VOICE_TRACK).where(PROGRAM_VOICE_TRACK.PROGRAM_ID.in(programIds))));
    entities.addAll(DAO.modelsFrom(ProgramVoice.class, db.selectFrom(PROGRAM_VOICE).where(PROGRAM_VOICE.PROGRAM_ID.in(programIds))));
    return entities;

  }

  @Override
  public Collection<Program> readAll(Access access) throws CoreException {
    if (access.isTopLevel())
      return DAO.modelsFrom(Program.class, dbProvider.getDSL().select(PROGRAM.fields()).from(PROGRAM)
        .where(PROGRAM.STATE.notEqual(String.valueOf(ProgramState.Erase)))
        .orderBy(PROGRAM.TYPE, PROGRAM.NAME)
        .fetch());
    else
      return DAO.modelsFrom(Program.class, dbProvider.getDSL().select(PROGRAM.fields()).from(PROGRAM)
        .join(LIBRARY).on(LIBRARY.ID.eq(PROGRAM.LIBRARY_ID))
        .where(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .and(PROGRAM.STATE.notEqual(String.valueOf(ProgramState.Erase)))
        .orderBy(PROGRAM.TYPE, PROGRAM.NAME)
        .fetch());
  }

  @Override
  public void update(Access access, UUID id, Program entity) throws CoreException {
    entity.validate();
    requireLibrary(access);
    executeUpdate(PROGRAM, id, entity);

  }

  @Override
  public void destroy(Access access, UUID id) throws CoreException {
    DSLContext db = dbProvider.getDSL();

    if (!access.isTopLevel())
      requireExists("Program belonging to you", db.selectCount().from(PROGRAM)
        .join(LIBRARY).on(PROGRAM.LIBRARY_ID.eq(LIBRARY.ID))
        .where(PROGRAM.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne(0, int.class));

    db.deleteFrom(PROGRAM)
      .where(PROGRAM.ID.eq(id))
      .execute();
  }

  @Override
  public Program newInstance() {
    return new Program();
  }

  @Override
  public Collection<Program> readAllInAccount(Access access, UUID accountId) throws CoreException {
    if (access.isTopLevel())
      return DAO.modelsFrom(Program.class, dbProvider.getDSL().select(PROGRAM.fields()).from(PROGRAM)
        .join(LIBRARY).on(PROGRAM.LIBRARY_ID.eq(LIBRARY.ID))
        .where(LIBRARY.ACCOUNT_ID.eq(accountId))
        .and(PROGRAM.STATE.notEqual(String.valueOf(ProgramState.Erase)))
        .orderBy(PROGRAM.TYPE, PROGRAM.NAME)
        .fetch());
    else
      return DAO.modelsFrom(Program.class, dbProvider.getDSL().select(PROGRAM.fields()).from(PROGRAM)
        .join(LIBRARY).on(PROGRAM.LIBRARY_ID.eq(LIBRARY.ID))
        .where(LIBRARY.ACCOUNT_ID.in(accountId))
        .and(PROGRAM.STATE.notEqual(String.valueOf(ProgramState.Erase)))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .orderBy(PROGRAM.TYPE, PROGRAM.NAME)
        .fetch());
  }

  @Override
  public Collection<Program> readAllInState(Access access, ProgramState state) throws CoreException {
    requireRole("platform access", access, UserRoleType.Admin, UserRoleType.Engineer);
    // FUTURE: engineer should only see programs in account?

    return DAO.modelsFrom(Program.class, dbProvider.getDSL().select(PROGRAM.fields())
      .from(PROGRAM)
      .where(PROGRAM.STATE.eq(state.toString()))
      .or(PROGRAM.STATE.eq(state.toString().toLowerCase(Locale.ENGLISH)))
      .fetch());
  }

  @Override
  public void erase(Access access, UUID id) throws CoreException {
    if (access.isTopLevel())
      requireExists("Program", dbProvider.getDSL().selectCount().from(PROGRAM)
        .where(PROGRAM.ID.eq(id))
        .fetchOne(0, int.class));
    else requireExists("Program", dbProvider.getDSL().selectCount().from(PROGRAM)
      .join(Library.LIBRARY).on(Library.LIBRARY.ID.eq(PROGRAM.LIBRARY_ID))
      .where(PROGRAM.ID.eq(id))
      .and(Library.LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
      .fetchOne(0, int.class));

    // Update program state to Erase
    Program program = readOne(access, id);
    program.setStateEnum(ProgramState.Erase);

    executeUpdate(PROGRAM, id, program);
  }

  @Override
  public Collection<UUID> readIdsInLibraries(Access access, Collection<UUID> parentIds) throws CoreException {
    requireLibrary(access);
    return DAO.idsFrom(dbProvider.getDSL().select(PROGRAM.ID)
      .from(PROGRAM)
      .where(PROGRAM.LIBRARY_ID.in(parentIds))
      .and(PROGRAM.STATE.equal(ProgramState.Published.toString()))
      .fetch());
  }

  @Override
  public void destroyChildEntities(Access access, Collection<UUID> programIds) throws CoreException {
    requireTopLevel(access);
    DSLContext db = dbProvider.getDSL();
    db.deleteFrom(PROGRAM_SEQUENCE_PATTERN_EVENT).where(PROGRAM_SEQUENCE_PATTERN_EVENT.PROGRAM_ID.in(programIds)).execute();
    db.deleteFrom(PROGRAM_SEQUENCE_PATTERN).where(PROGRAM_SEQUENCE_PATTERN.PROGRAM_ID.in(programIds)).execute();
    db.deleteFrom(PROGRAM_SEQUENCE_BINDING_MEME).where(PROGRAM_SEQUENCE_BINDING_MEME.PROGRAM_ID.in(programIds)).execute();
    db.deleteFrom(PROGRAM_SEQUENCE_BINDING).where(PROGRAM_SEQUENCE_BINDING.PROGRAM_ID.in(programIds)).execute();
    db.deleteFrom(PROGRAM_SEQUENCE_CHORD).where(PROGRAM_SEQUENCE_CHORD.PROGRAM_ID.in(programIds)).execute();
    db.deleteFrom(PROGRAM_SEQUENCE).where(PROGRAM_SEQUENCE.PROGRAM_ID.in(programIds)).execute();
    db.deleteFrom(PROGRAM_VOICE_TRACK).where(PROGRAM_VOICE_TRACK.PROGRAM_ID.in(programIds)).execute();
    db.deleteFrom(PROGRAM_VOICE).where(PROGRAM_VOICE.PROGRAM_ID.in(programIds)).execute();
    db.deleteFrom(PROGRAM_MEME).where(PROGRAM_MEME.PROGRAM_ID.in(programIds)).execute();
  }


}

