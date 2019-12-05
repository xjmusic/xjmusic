// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import io.xj.core.access.Access;
import io.xj.core.entity.Entity;
import io.xj.core.exception.CoreException;
import io.xj.core.model.Program;
import io.xj.core.model.ProgramSequencePatternEvent;
import io.xj.core.model.ProgramMeme;
import io.xj.core.model.ProgramSequencePattern;
import io.xj.core.model.ProgramSequence;
import io.xj.core.model.ProgramSequenceBinding;
import io.xj.core.model.ProgramSequenceBindingMeme;
import io.xj.core.model.ProgramSequenceChord;
import io.xj.core.model.ProgramState;
import io.xj.core.model.ProgramVoiceTrack;
import io.xj.core.model.ProgramVoice;
import io.xj.core.model.UserRoleType;
import io.xj.core.persistence.sql.SQLDatabaseProvider;
import io.xj.core.tables.Library;
import org.jooq.DSLContext;
import org.jooq.Record;

import javax.annotation.Nullable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import static io.xj.core.Tables.LIBRARY;
import static io.xj.core.Tables.PROGRAM;
import static io.xj.core.Tables.PROGRAM_SEQUENCE_PATTERN_EVENT;
import static io.xj.core.Tables.PROGRAM_MEME;
import static io.xj.core.Tables.PROGRAM_SEQUENCE_PATTERN;
import static io.xj.core.Tables.PROGRAM_SEQUENCE;
import static io.xj.core.Tables.PROGRAM_SEQUENCE_BINDING;
import static io.xj.core.Tables.PROGRAM_SEQUENCE_BINDING_MEME;
import static io.xj.core.Tables.PROGRAM_SEQUENCE_CHORD;
import static io.xj.core.Tables.PROGRAM_VOICE_TRACK;
import static io.xj.core.Tables.PROGRAM_VOICE;

public class ProgramDAOImpl extends DAOImpl<Program> implements ProgramDAO {

  @Inject
  public ProgramDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  /**
   Read one record

   @param connection to database
   @param access     control
   @param id         of record
   @return record
   */
  private Program readOne(Connection connection, Access access, UUID id) throws CoreException {
    if (access.isTopLevel())
      return DAORecord.modelFrom(Program.class, DAORecord.DSL(connection).selectFrom(PROGRAM)
        .where(PROGRAM.ID.eq(id))
        .fetchOne());
    else {
      Record programRecord = DAORecord.DSL(connection).select(PROGRAM.fields())
        .from(PROGRAM)
        .join(LIBRARY).on(LIBRARY.ID.eq(PROGRAM.LIBRARY_ID))
        .where(PROGRAM.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne();
      return DAORecord.modelFrom(Program.class, programRecord);
    }
  }

  /**
   Create a record

   @param connection to database
   @param access     control
   @param entity     for new record
   @return newly readMany record
   @throws CoreException on failure
   */
  private Program create(Connection connection, Access access, Program entity) throws CoreException {
    entity.validate();
    requireLibrary(access);
    return DAORecord.modelFrom(Program.class, executeCreate(connection, PROGRAM, entity));
  }

  @Override
  public Program create(Access access, Program entity) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      return create(connection, access, entity);
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  public Program clone(Access access, UUID cloneId, Program entity) throws CoreException {
    requireLibrary(access);
    try (Connection connection = dbProvider.getConnection()) {
      connection.setAutoCommit(false);
      Program from = readOne(connection, access, cloneId);
      if (Objects.isNull(from))
        throw new CoreException("Can't clone nonexistent Program");

      // When null, inherits, type, state, key, and tempo
      if (Objects.isNull(entity.getType())) entity.setTypeEnum(from.getType());
      if (Objects.isNull(entity.getState())) entity.setStateEnum(from.getState());
      if (Objects.isNull(entity.getKey())) entity.setKey(from.getKey());
      if (Objects.isNull(entity.getTempo())) entity.setTempo(from.getTempo());
      entity.validate();

      Program program = create(connection, access, entity);
      // TODO clone all sub-entities of program into whole new set of entities:

      connection.commit();
      return program;
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  @Nullable
  public Program readOne(Access access, UUID id) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      return readOne(connection, access, id);
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  public Collection<Program> readMany(Access access, Collection<UUID> parentIds) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      if (access.isTopLevel())
        return DAORecord.modelsFrom(Program.class, DAORecord.DSL(connection).select(PROGRAM.fields()).from(PROGRAM)
          .where(PROGRAM.LIBRARY_ID.in(parentIds))
          .and(PROGRAM.STATE.notEqual(String.valueOf(ProgramState.Erase)))
          .orderBy(PROGRAM.TYPE, PROGRAM.NAME)
          .fetch());
      else
        return DAORecord.modelsFrom(Program.class, DAORecord.DSL(connection).select(PROGRAM.fields()).from(PROGRAM)
          .join(LIBRARY).on(LIBRARY.ID.eq(PROGRAM.LIBRARY_ID))
          .where(PROGRAM.LIBRARY_ID.in(parentIds))
          .and(PROGRAM.STATE.notEqual(String.valueOf(ProgramState.Erase)))
          .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
          .orderBy(PROGRAM.TYPE, PROGRAM.NAME)
          .fetch());
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  public Collection<Entity> readManyWithChildEntities(Access access, Collection<UUID> programIds) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      DSLContext db = DAORecord.DSL(connection);

      if (!access.isTopLevel())
        for (UUID programId : programIds)
          requireExists("access via account", db.selectCount().from(PROGRAM)
            .join(LIBRARY).on(LIBRARY.ID.eq(PROGRAM.LIBRARY_ID))
            .where(PROGRAM.ID.eq(programId))
            .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
            .fetchOne(0, int.class));

      Collection<Entity> entities = Lists.newArrayList();
      entities.addAll(DAORecord.modelsFrom(Program.class, db.selectFrom(PROGRAM).where(PROGRAM.ID.in(programIds)).fetch()));
      entities.addAll(DAORecord.modelsFrom(ProgramSequencePatternEvent.class, db.selectFrom(PROGRAM_SEQUENCE_PATTERN_EVENT).where(PROGRAM_SEQUENCE_PATTERN_EVENT.PROGRAM_ID.in(programIds))));
      entities.addAll(DAORecord.modelsFrom(ProgramMeme.class, db.selectFrom(PROGRAM_MEME).where(PROGRAM_MEME.PROGRAM_ID.in(programIds))));
      entities.addAll(DAORecord.modelsFrom(ProgramSequencePattern.class, db.selectFrom(PROGRAM_SEQUENCE_PATTERN).where(PROGRAM_SEQUENCE_PATTERN.PROGRAM_ID.in(programIds))));
      entities.addAll(DAORecord.modelsFrom(ProgramSequenceBinding.class, db.selectFrom(PROGRAM_SEQUENCE_BINDING).where(PROGRAM_SEQUENCE_BINDING.PROGRAM_ID.in(programIds))));
      entities.addAll(DAORecord.modelsFrom(ProgramSequenceBindingMeme.class, db.selectFrom(PROGRAM_SEQUENCE_BINDING_MEME).where(PROGRAM_SEQUENCE_BINDING_MEME.PROGRAM_ID.in(programIds))));
      entities.addAll(DAORecord.modelsFrom(ProgramSequenceChord.class, db.selectFrom(PROGRAM_SEQUENCE_CHORD).where(PROGRAM_SEQUENCE_CHORD.PROGRAM_ID.in(programIds))));
      entities.addAll(DAORecord.modelsFrom(ProgramSequence.class, db.selectFrom(PROGRAM_SEQUENCE).where(PROGRAM_SEQUENCE.PROGRAM_ID.in(programIds))));
      entities.addAll(DAORecord.modelsFrom(ProgramVoiceTrack.class, db.selectFrom(PROGRAM_VOICE_TRACK).where(PROGRAM_VOICE_TRACK.PROGRAM_ID.in(programIds))));
      entities.addAll(DAORecord.modelsFrom(ProgramVoice.class, db.selectFrom(PROGRAM_VOICE).where(PROGRAM_VOICE.PROGRAM_ID.in(programIds))));
      return entities;

    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  public Collection<Program> readAll(Access access) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      if (access.isTopLevel())
        return DAORecord.modelsFrom(Program.class, DAORecord.DSL(connection).select(PROGRAM.fields()).from(PROGRAM)
          .where(PROGRAM.STATE.notEqual(String.valueOf(ProgramState.Erase)))
          .orderBy(PROGRAM.TYPE, PROGRAM.NAME)
          .fetch());
      else
        return DAORecord.modelsFrom(Program.class, DAORecord.DSL(connection).select(PROGRAM.fields()).from(PROGRAM)
          .join(LIBRARY).on(LIBRARY.ID.eq(PROGRAM.LIBRARY_ID))
          .where(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
          .and(PROGRAM.STATE.notEqual(String.valueOf(ProgramState.Erase)))
          .orderBy(PROGRAM.TYPE, PROGRAM.NAME)
          .fetch());
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  public void update(Access access, UUID id, Program entity) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      entity.validate();
      requireLibrary(access);
      executeUpdate(connection, PROGRAM, id, entity);

    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  public void destroy(Access access, UUID id) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      DSLContext db = DAORecord.DSL(connection);

      if (!access.isTopLevel())
        requireExists("Program belonging to you", db.selectCount().from(PROGRAM)
          .join(LIBRARY).on(PROGRAM.LIBRARY_ID.eq(LIBRARY.ID))
          .where(PROGRAM.ID.eq(id))
          .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
          .fetchOne(0, int.class));

      db.deleteFrom(PROGRAM)
        .where(PROGRAM.ID.eq(id))
        .execute();
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  public Program newInstance() {
    return new Program();
  }

  @Override
  public Collection<Program> readAllInAccount(Access access, UUID accountId) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      if (access.isTopLevel())
        return DAORecord.modelsFrom(Program.class, DAORecord.DSL(connection).select(PROGRAM.fields()).from(PROGRAM)
          .join(LIBRARY).on(PROGRAM.LIBRARY_ID.eq(LIBRARY.ID))
          .where(LIBRARY.ACCOUNT_ID.eq(accountId))
          .and(PROGRAM.STATE.notEqual(String.valueOf(ProgramState.Erase)))
          .orderBy(PROGRAM.TYPE, PROGRAM.NAME)
          .fetch());
      else
        return DAORecord.modelsFrom(Program.class, DAORecord.DSL(connection).select(PROGRAM.fields()).from(PROGRAM)
          .join(LIBRARY).on(PROGRAM.LIBRARY_ID.eq(LIBRARY.ID))
          .where(LIBRARY.ACCOUNT_ID.in(accountId))
          .and(PROGRAM.STATE.notEqual(String.valueOf(ProgramState.Erase)))
          .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
          .orderBy(PROGRAM.TYPE, PROGRAM.NAME)
          .fetch());
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  public Collection<Program> readAllInState(Access access, ProgramState state) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      requireRole("platform access", access, UserRoleType.Admin, UserRoleType.Engineer);
      // FUTURE: engineer should only see programs in account?

      return DAORecord.modelsFrom(Program.class, DAORecord.DSL(connection).select(PROGRAM.fields())
        .from(PROGRAM)
        .where(PROGRAM.STATE.eq(state.toString()))
        .or(PROGRAM.STATE.eq(state.toString().toLowerCase(Locale.ENGLISH)))
        .fetch());
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  public void erase(Access access, UUID id) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      if (access.isTopLevel())
        requireExists("Program", DAORecord.DSL(connection).selectCount().from(PROGRAM)
          .where(PROGRAM.ID.eq(id))
          .fetchOne(0, int.class));
      else requireExists("Program", DAORecord.DSL(connection).selectCount().from(PROGRAM)
        .join(Library.LIBRARY).on(Library.LIBRARY.ID.eq(PROGRAM.LIBRARY_ID))
        .where(PROGRAM.ID.eq(id))
        .and(Library.LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne(0, int.class));

      // Update program state to Erase
      Program program = readOne(connection, access, id);
      program.setStateEnum(ProgramState.Erase);

      executeUpdate(connection, PROGRAM, id, program);
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  public Collection<UUID> readIdsInLibraries(Access access, Collection<UUID> parentIds) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      requireLibrary(access);
      return DAORecord.idsFrom(DAORecord.DSL(connection).select(PROGRAM.ID)
        .from(PROGRAM)
        .where(PROGRAM.LIBRARY_ID.in(parentIds))
        .and(PROGRAM.STATE.equal(ProgramState.Published.toString()))
        .fetch());
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  public void destroyChildEntities(Access access, Collection<UUID> programIds) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      requireTopLevel(access);
      DSLContext db = DAORecord.DSL(connection);
      db.deleteFrom(PROGRAM_SEQUENCE_PATTERN_EVENT).where(PROGRAM_SEQUENCE_PATTERN_EVENT.PROGRAM_ID.in(programIds)).execute();
      db.deleteFrom(PROGRAM_SEQUENCE_PATTERN).where(PROGRAM_SEQUENCE_PATTERN.PROGRAM_ID.in(programIds)).execute();
      db.deleteFrom(PROGRAM_SEQUENCE_BINDING_MEME).where(PROGRAM_SEQUENCE_BINDING_MEME.PROGRAM_ID.in(programIds)).execute();
      db.deleteFrom(PROGRAM_SEQUENCE_BINDING).where(PROGRAM_SEQUENCE_BINDING.PROGRAM_ID.in(programIds)).execute();
      db.deleteFrom(PROGRAM_SEQUENCE_CHORD).where(PROGRAM_SEQUENCE_CHORD.PROGRAM_ID.in(programIds)).execute();
      db.deleteFrom(PROGRAM_SEQUENCE).where(PROGRAM_SEQUENCE.PROGRAM_ID.in(programIds)).execute();
      db.deleteFrom(PROGRAM_VOICE_TRACK).where(PROGRAM_VOICE_TRACK.PROGRAM_ID.in(programIds)).execute();
      db.deleteFrom(PROGRAM_VOICE).where(PROGRAM_VOICE.PROGRAM_ID.in(programIds)).execute();
      db.deleteFrom(PROGRAM_MEME).where(PROGRAM_MEME.PROGRAM_ID.in(programIds)).execute();

    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }

  }


}

