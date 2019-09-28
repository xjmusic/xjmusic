// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao.impl;

import com.google.api.client.util.Maps;
import com.google.inject.Inject;
import io.xj.core.access.impl.Access;
import io.xj.core.dao.ProgramDAO;
import io.xj.core.exception.CoreException;
import io.xj.core.model.program.Program;
import io.xj.core.model.program.ProgramFactory;
import io.xj.core.model.program.ProgramState;
import io.xj.core.model.user.role.UserRoleType;
import io.xj.core.persistence.sql.SQLDatabaseProvider;
import io.xj.core.persistence.sql.impl.SQLConnection;
import io.xj.core.tables.Library;
import io.xj.core.transport.GsonProvider;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.types.ULong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static io.xj.core.Tables.LIBRARY;
import static io.xj.core.Tables.PROGRAM;

public class ProgramDAOImpl extends DAOImpl implements ProgramDAO {
  private final Logger log = LoggerFactory.getLogger(ProgramDAOImpl.class);
  private final ProgramFactory programFactory;
  private final GsonProvider gsonProvider;

  @Inject
  public ProgramDAOImpl(
    ProgramFactory programFactory,
    SQLDatabaseProvider dbProvider,
    GsonProvider gsonProvider
  ) {
    this.programFactory = programFactory;
    this.gsonProvider = gsonProvider;
    this.dbProvider = dbProvider;
  }

  /**
   Destroy a Program

   @param db context
   @param id to delete
   @throws CoreException if database failure
   @throws CoreException if not configured properly
   @throws CoreException if fails business rule
   */
  private static void destroy(DSLContext db, Access access, ULong id) throws CoreException {
    if (!access.isTopLevel())
      requireExists("Program belonging to you", db.selectCount().from(PROGRAM)
        .join(LIBRARY).on(PROGRAM.LIBRARY_ID.eq(LIBRARY.ID))
        .where(PROGRAM.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(idCollection(access.getAccountIds())))
        .fetchOne(0, int.class));

    db.deleteFrom(PROGRAM)
      .where(PROGRAM.ID.eq(id))
      .execute();
  }

  /**
   Update a program to Erase state

   @param db context
   @param id to delete
   @throws CoreException if database failure
   @throws CoreException if not configured properly
   @throws CoreException if fails business rule
   */
  private static void erase(Access access, DSLContext db, ULong id) throws CoreException {
    if (access.isTopLevel())
      requireExists("Program", db.selectCount().from(PROGRAM)
        .where(PROGRAM.ID.eq(id))
        .fetchOne(0, int.class));
    else requireExists("Program", db.selectCount().from(PROGRAM)
      .join(Library.LIBRARY).on(Library.LIBRARY.ID.eq(PROGRAM.LIBRARY_ID))
      .where(PROGRAM.ID.eq(id))
      .and(Library.LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
      .fetchOne(0, int.class));

    // Update program state to Erase
    Map<Field, Object> fieldValues = com.google.common.collect.Maps.newHashMap();
    fieldValues.put(PROGRAM.ID, id);
    fieldValues.put(PROGRAM.STATE, ProgramState.Erase);

    if (0 == executeUpdate(db, PROGRAM, fieldValues))
      throw new CoreException("No records updated.");
  }

  /**
   Read one record

   @param db     context
   @param access control
   @param id     of record
   @return record
   */
  @Nullable
  private Program readOne(DSLContext db, Access access, ULong id) throws CoreException {
    if (access.isTopLevel())
      return modelFrom(db.selectFrom(PROGRAM)
        .where(PROGRAM.ID.eq(id))
        .fetchOne(), programFactory);
    else {
      Record programRecord = db.select(PROGRAM.fields())
        .from(PROGRAM)
        .join(LIBRARY).on(LIBRARY.ID.eq(PROGRAM.LIBRARY_ID))
        .where(PROGRAM.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne();
      return modelFrom(programRecord, programFactory);
    }
  }

  /**
   Read all records in parent record by id

   @param db        context
   @param access    control
   @param accountId to get programs in
   @return array of records
   */
  private Collection<Program> readAllInAccount(DSLContext db, Access access, ULong accountId) throws CoreException {
    if (access.isTopLevel())
      return modelsFrom(db.select(PROGRAM.fields()).from(PROGRAM)
        .join(LIBRARY).on(PROGRAM.LIBRARY_ID.eq(LIBRARY.ID))
        .where(LIBRARY.ACCOUNT_ID.eq(accountId))
        .and(PROGRAM.STATE.notEqual(String.valueOf(ProgramState.Erase)))
        .orderBy(PROGRAM.TYPE, PROGRAM.NAME)
        .fetch(), programFactory);
    else
      return modelsFrom(db.select(PROGRAM.fields()).from(PROGRAM)
        .join(LIBRARY).on(PROGRAM.LIBRARY_ID.eq(LIBRARY.ID))
        .where(LIBRARY.ACCOUNT_ID.in(accountId))
        .and(PROGRAM.STATE.notEqual(String.valueOf(ProgramState.Erase)))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .orderBy(PROGRAM.TYPE, PROGRAM.NAME)
        .fetch(), programFactory);
  }

  /**
   Read all records in parent record by id

   @param db         context
   @param access     control
   @param libraryIds of parent
   @return array of records
   */
  private Collection<Program> readAllInLibraries(DSLContext db, Access access, Collection<ULong> libraryIds) throws CoreException {
    if (access.isTopLevel())
      return modelsFrom(db.select(PROGRAM.fields()).from(PROGRAM)
        .where(PROGRAM.LIBRARY_ID.in(libraryIds))
        .and(PROGRAM.STATE.notEqual(String.valueOf(ProgramState.Erase)))
        .orderBy(PROGRAM.TYPE, PROGRAM.NAME)
        .fetch(), programFactory);
    else
      return modelsFrom(db.select(PROGRAM.fields()).from(PROGRAM)
        .join(LIBRARY).on(LIBRARY.ID.eq(PROGRAM.LIBRARY_ID))
        .where(PROGRAM.LIBRARY_ID.in(libraryIds))
        .and(PROGRAM.STATE.notEqual(String.valueOf(ProgramState.Erase)))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .orderBy(PROGRAM.TYPE, PROGRAM.NAME)
        .fetch(), programFactory);
  }

  /**
   Read all records in parent record by id

   @param db     context
   @param access control
   @return array of records
   */
  private Collection<Program> readAll(DSLContext db, Access access) throws CoreException {
    if (access.isTopLevel())
      return modelsFrom(db.select(PROGRAM.fields()).from(PROGRAM)
        .where(PROGRAM.STATE.notEqual(String.valueOf(ProgramState.Erase)))
        .orderBy(PROGRAM.TYPE, PROGRAM.NAME)
        .fetch(), programFactory);
    else
      return modelsFrom(db.select(PROGRAM.fields()).from(PROGRAM)
        .join(LIBRARY).on(LIBRARY.ID.eq(PROGRAM.LIBRARY_ID))
        .where(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .and(PROGRAM.STATE.notEqual(String.valueOf(ProgramState.Erase)))
        .orderBy(PROGRAM.TYPE, PROGRAM.NAME)
        .fetch(), programFactory);
  }

  /**
   Read all records in a given state

   @param db     context
   @param access control
   @param state  to read programs in
   @return array of records
   */
  private Collection<Program> readAllInState(DSLContext db, Access access, ProgramState state) throws CoreException {
    requireRole("platform access", access, UserRoleType.Admin, UserRoleType.Engineer);
    // FUTURE: engineer should only see programs in account?

    return modelsFrom(db.select(PROGRAM.fields())
      .from(PROGRAM)
      .where(PROGRAM.STATE.eq(state.toString()))
      .or(PROGRAM.STATE.eq(state.toString().toLowerCase(Locale.ENGLISH)))
      .fetch(), programFactory);
  }

  /**
   Clone a Program into a new Program

   @param db      context
   @param access  control
   @param cloneId of program to clone
   @param entity  for the new program
   @return newly readMany record
   @throws CoreException on failure
   */
  private Program clone(DSLContext db, Access access, BigInteger cloneId, Program entity) throws CoreException {
    Program from = readOne(db, access, ULong.valueOf(cloneId));
    if (Objects.isNull(from))
      throw new CoreException("Can't clone nonexistent Program");

    // When null, inherits, type, state, key, and tempo
    if (Objects.isNull(entity.getType())) entity.setTypeEnum(from.getType());
    if (Objects.isNull(entity.getState())) entity.setStateEnum(from.getState());
    if (Objects.isNull(entity.getKey())) entity.setKey(from.getKey());
    if (Objects.isNull(entity.getTempo())) entity.setTempo(from.getTempo());

    // Program must be created (have id) before adding content and updating
    Program program = create(db, access, entity).setContentCloned(from);
    update(db, access, ULong.valueOf(program.getId()), program);
    return program;
  }

  /**
   Create a record

   @param db     context
   @param access control
   @param entity for new record
   @return newly readMany record
   @throws CoreException on failure
   */
  private Program create(DSLContext db, Access access, Program entity) throws CoreException {
    entity.validate();

    Map<Field, Object> fieldValues = fieldValueMap(entity);

    if (Objects.nonNull(entity.getId()))
      fieldValues.put(PROGRAM.ID, entity.getId());

    // This entity's parent is a Library
    requireLibraryAccess(db, access, entity);

    return modelFrom(executeCreate(db, PROGRAM, fieldValues), programFactory);
  }

  /**
   Update a record

   @param db     context
   @param access control
   @param id     of record
   @param entity to update with
   @throws CoreException if a Business Rule is violated
   @throws CoreException on database failure
   */
  private void update(DSLContext db, Access access, ULong id, Program entity) throws CoreException {
    entity.validate();

    Map<Field, Object> fieldValues = fieldValueMap(entity);
    fieldValues.put(PROGRAM.ID, id);

    // This entity's parent is a Library
    requireLibraryAccess(db, access, entity);

    if (0 == executeUpdate(db, PROGRAM, fieldValues))
      throw new CoreException("No records updated.");

    log.info("Updated Program(id={}) by User(id={})", id, access.getUserId());
  }

  /**
   Only certain (writable) fields are mapped back to jOOQ records--
   Read-only fields are excluded from here.

   @param entity to source values from
   @return values mapped to record fields
   */
  private Map<Field, Object> fieldValueMap(Program entity) {
    Map<Field, Object> fieldValues = Maps.newHashMap();
    fieldValues.put(PROGRAM.CONTENT, gsonProvider.gson().toJson(entity.getContent()));
    fieldValues.put(PROGRAM.NAME, entity.getName());
    fieldValues.put(PROGRAM.USER_ID, ULong.valueOf(entity.getUserId()));
    fieldValues.put(PROGRAM.LIBRARY_ID, ULong.valueOf(entity.getLibraryId()));
    fieldValues.put(PROGRAM.KEY, entity.getKey());
    fieldValues.put(PROGRAM.TYPE, entity.getType());
    fieldValues.put(PROGRAM.STATE, entity.getState());
    fieldValues.put(PROGRAM.TEMPO, entity.getTempo());
    return fieldValues;
  }

  @Override
  public Program create(Access access, Program entity) throws CoreException {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(create(tx.getContext(), access, entity));
    } catch (CoreException e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Program clone(Access access, BigInteger cloneId, Program entity) throws CoreException {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(clone(tx.getContext(), access, cloneId, entity));
    } catch (CoreException e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public Program readOne(Access access, BigInteger id) throws CoreException {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOne(tx.getContext(), access, ULong.valueOf(id)));
    } catch (CoreException e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Collection<Program> readMany(Access access, Collection<BigInteger> parentIds) throws CoreException {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAllInLibraries(tx.getContext(), access, uLongValuesOf(parentIds)));
    } catch (CoreException e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Collection<Program> readAll(Access access) throws CoreException {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAll(tx.getContext(), access));
    } catch (CoreException e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void update(Access access, BigInteger id, Program entity) throws CoreException {
    SQLConnection tx = dbProvider.getConnection();
    try {
      update(tx.getContext(), access, ULong.valueOf(id), entity);
      tx.success();
    } catch (CoreException e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void destroy(Access access, BigInteger id) throws CoreException {
    SQLConnection tx = dbProvider.getConnection();
    try {
      destroy(tx.getContext(), access, ULong.valueOf(id));
      tx.success();
    } catch (CoreException e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Program newInstance() {
    return programFactory.newProgram();
  }

  @Override
  public Collection<Program> readAllInAccount(Access access, BigInteger accountId) throws CoreException {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAllInAccount(tx.getContext(), access, ULong.valueOf(accountId)));
    } catch (CoreException e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Collection<Program> readAllInState(Access access, ProgramState state) throws CoreException {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAllInState(tx.getContext(), access, state));
    } catch (CoreException e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void erase(Access access, BigInteger id) throws CoreException {
    SQLConnection tx = dbProvider.getConnection();
    try {
      erase(access, tx.getContext(), ULong.valueOf(id));
      tx.success();
    } catch (CoreException e) {
      throw tx.failure(e);
    }
  }

}
