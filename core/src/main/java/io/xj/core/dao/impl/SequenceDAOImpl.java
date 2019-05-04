// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao.impl;

import com.google.api.client.util.Maps;
import com.google.inject.Inject;
import io.xj.core.access.impl.Access;
import io.xj.core.dao.SequenceDAO;
import io.xj.core.exception.CoreException;
import io.xj.core.model.sequence.Sequence;
import io.xj.core.model.sequence.SequenceState;
import io.xj.core.model.user_role.UserRoleType;
import io.xj.core.persistence.sql.SQLDatabaseProvider;
import io.xj.core.persistence.sql.impl.SQLConnection;
import io.xj.core.tables.Library;
import io.xj.core.work.WorkManager;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.types.ULong;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static io.xj.core.Tables.LIBRARY;
import static io.xj.core.Tables.PATTERN_EVENT;
import static io.xj.core.Tables.SEQUENCE;
import static io.xj.core.Tables.SEQUENCE_MEME;
import static io.xj.core.Tables.SEQUENCE_PATTERN;
import static io.xj.core.Tables.VOICE;
import static io.xj.core.tables.ChainSequence.CHAIN_SEQUENCE;
import static io.xj.core.tables.Pattern.PATTERN;

public class SequenceDAOImpl extends DAOImpl implements SequenceDAO {
  //  private static final Logger log = LoggerFactory.getLogger(SequenceDAOImpl.class);
  private final WorkManager workManager;

  @Inject
  public SequenceDAOImpl(
    SQLDatabaseProvider dbProvider,
    WorkManager workManager
  ) {
    this.workManager = workManager;
    this.dbProvider = dbProvider;
  }

  /**
   Create a record

   @param db     context
   @param access control
   @param entity for new record
   @return newly readMany record
   @throws CoreException on failure
   */
  private static Sequence create(DSLContext db, Access access, Sequence entity) throws CoreException {
    entity.validate();

    Map<Field, Object> fieldValues = fieldValueMap(entity);

    // This entity's parent is a Library
    requireLibraryAccess(db, access, entity);

    fieldValues.put(SEQUENCE.USER_ID, access.getUserId());

    return modelFrom(executeCreate(db, SEQUENCE, fieldValues), Sequence.class);
  }

  /**
   Read one record

   @param db     context
   @param access control
   @param id     of record
   @return record
   */
  @Nullable
  private static Sequence readOne(DSLContext db, Access access, ULong id) throws CoreException {
    if (access.isTopLevel())
      return modelFrom(db.selectFrom(SEQUENCE)
        .where(SEQUENCE.ID.eq(id))
        .fetchOne(), Sequence.class);
    else
      return modelFrom(db.select(SEQUENCE.fields())
        .from(SEQUENCE)
        .join(LIBRARY).on(LIBRARY.ID.eq(SEQUENCE.LIBRARY_ID))
        .where(SEQUENCE.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne(), Sequence.class);
  }

  /**
   Read all records in parent record by id

   @param db        context
   @param access    control
   @param accountId to get sequences in
   @return array of records
   */
  private static Collection<Sequence> readAllInAccount(DSLContext db, Access access, ULong accountId) throws CoreException {
    if (access.isTopLevel())
      return modelsFrom(db.select(SEQUENCE.fields()).from(SEQUENCE)
        .join(LIBRARY).on(SEQUENCE.LIBRARY_ID.eq(LIBRARY.ID))
        .where(LIBRARY.ACCOUNT_ID.eq(accountId))
        .and(SEQUENCE.STATE.notEqual(String.valueOf(SequenceState.Erase)))
        .orderBy(SEQUENCE.TYPE, SEQUENCE.NAME)
        .fetch(), Sequence.class);
    else
      return modelsFrom(db.select(SEQUENCE.fields()).from(SEQUENCE)
        .join(LIBRARY).on(SEQUENCE.LIBRARY_ID.eq(LIBRARY.ID))
        .where(LIBRARY.ACCOUNT_ID.in(accountId))
        .and(SEQUENCE.STATE.notEqual(String.valueOf(SequenceState.Erase)))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .orderBy(SEQUENCE.TYPE, SEQUENCE.NAME)
        .fetch(), Sequence.class);
  }

  /**
   Read all records in parent record by id

   @param db         context
   @param access     control
   @param libraryIds of parent
   @return array of records
   */
  private static Collection<Sequence> readAllInLibraries(DSLContext db, Access access, Collection<ULong> libraryIds) throws CoreException {
    if (access.isTopLevel())
      return modelsFrom(db.select(SEQUENCE.fields()).from(SEQUENCE)
        .where(SEQUENCE.LIBRARY_ID.in(libraryIds))
        .and(SEQUENCE.STATE.notEqual(String.valueOf(SequenceState.Erase)))
        .orderBy(SEQUENCE.TYPE, SEQUENCE.NAME)
        .fetch(), Sequence.class);
    else
      return modelsFrom(db.select(SEQUENCE.fields()).from(SEQUENCE)
        .join(LIBRARY).on(LIBRARY.ID.eq(SEQUENCE.LIBRARY_ID))
        .where(SEQUENCE.LIBRARY_ID.in(libraryIds))
        .and(SEQUENCE.STATE.notEqual(String.valueOf(SequenceState.Erase)))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .orderBy(SEQUENCE.TYPE, SEQUENCE.NAME)
        .fetch(), Sequence.class);
  }

  /**
   Read all records in parent record by id

   @param db     context
   @param access control
   @return array of records
   */
  private static Collection<Sequence> readAll(DSLContext db, Access access) throws CoreException {
    if (access.isTopLevel())
      return modelsFrom(db.select(SEQUENCE.fields()).from(SEQUENCE)
        .where(SEQUENCE.STATE.notEqual(String.valueOf(SequenceState.Erase)))
        .orderBy(SEQUENCE.TYPE, SEQUENCE.NAME)
        .fetch(), Sequence.class);
    else
      return modelsFrom(db.select(SEQUENCE.fields()).from(SEQUENCE)
        .join(LIBRARY).on(LIBRARY.ID.eq(SEQUENCE.LIBRARY_ID))
        .where(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .and(SEQUENCE.STATE.notEqual(String.valueOf(SequenceState.Erase)))
        .orderBy(SEQUENCE.TYPE, SEQUENCE.NAME)
        .fetch(), Sequence.class);
  }

  /**
   Read all sequence records bound to a Chain via ChainSequence records

   @param db      context
   @param access  control
   @param chainId of parent
   @return array of records
   */
  private static Collection<Sequence> readAllBoundToChain(DSLContext db, Access access, ULong chainId) throws CoreException {
    requireTopLevel(access);
    return modelsFrom(db.select(SEQUENCE.fields()).from(SEQUENCE)
      .join(CHAIN_SEQUENCE).on(CHAIN_SEQUENCE.SEQUENCE_ID.eq(SEQUENCE.ID))
      .where(CHAIN_SEQUENCE.CHAIN_ID.eq(chainId))
      .and(SEQUENCE.STATE.notEqual(String.valueOf(SequenceState.Erase)))
      .fetch(), Sequence.class);
  }

  /**
   Read all records in a given state

   @param db     context
   @param access control
   @param state  to read sequences in
   @return array of records
   */
  private static Collection<Sequence> readAllInState(DSLContext db, Access access, SequenceState state) throws CoreException {
    requireRole("platform access", access, UserRoleType.Admin, UserRoleType.Engineer);
    // FUTURE: engineer should only see sequences in account?

    return modelsFrom(db.select(SEQUENCE.fields())
      .from(SEQUENCE)
      .where(SEQUENCE.STATE.eq(state.toString()))
      .or(SEQUENCE.STATE.eq(state.toString().toLowerCase(Locale.ENGLISH)))
      .fetch(), Sequence.class);
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
  private static void update(DSLContext db, Access access, ULong id, Sequence entity) throws CoreException {
    entity.validate();

    Map<Field, Object> fieldValues = fieldValueMap(entity);
    fieldValues.put(SEQUENCE.ID, id);

    // This entity's parent is a Library
    requireLibraryAccess(db, access, entity);

    // Never update user id! [#156030760] Artist expects owner of Sequence or Instrument to always remain the same as when it was created, even after being updated by another user.
    fieldValues.remove(SEQUENCE.USER_ID);

    if (0 == executeUpdate(db, SEQUENCE, fieldValues))
      throw new CoreException("No records updated.");
  }

  /**
   Destroy a Sequence

   @param db context
   @param id to delete
   @throws CoreException if database failure
   @throws CoreException if not configured properly
   @throws CoreException if fails business rule
   */
  private static void destroy(DSLContext db, Access access, ULong id) throws CoreException {
    if (!access.isTopLevel())
      requireExists("Sequence belonging to you", db.selectCount().from(SEQUENCE)
        .join(LIBRARY).on(SEQUENCE.LIBRARY_ID.eq(LIBRARY.ID))
        .where(SEQUENCE.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(idCollection(access.getAccountIds())))
        .and(SEQUENCE.USER_ID.eq(ULong.valueOf(access.getUserId())))
        .fetchOne(0, int.class));

    requireNotExists("Pattern in Sequence", db.selectCount().from(PATTERN)
      .where(PATTERN.SEQUENCE_ID.eq(id))
      .fetchOne(0, int.class));

    requireNotExists("SequencePattern in Sequence", db.selectCount().from(SEQUENCE_PATTERN)
      .where(SEQUENCE_PATTERN.SEQUENCE_ID.eq(id))
      .fetchOne(0, int.class));

    requireNotExists("Pattern Events of Voice in Sequence", db.selectCount().from(PATTERN_EVENT)
      .join(VOICE).on(PATTERN_EVENT.VOICE_ID.eq(VOICE.ID))
      .where(VOICE.SEQUENCE_ID.eq(id))
      .fetchOne(0, int.class));

    db.deleteFrom(VOICE)
      .where(VOICE.SEQUENCE_ID.eq(id))
      .execute();

    db.deleteFrom(SEQUENCE_MEME)
      .where(SEQUENCE_MEME.SEQUENCE_ID.eq(id))
      .execute();

    db.deleteFrom(SEQUENCE)
      .where(SEQUENCE.ID.eq(id))
      .execute();
  }

  /**
   Only certain (writable) fields are mapped back to jOOQ records--
   Read-only fields are excluded from here.

   @param entity to source values from
   @return values mapped to record fields
   */
  private static Map<Field, Object> fieldValueMap(Sequence entity) {
    Map<Field, Object> fieldValues = Maps.newHashMap();
    fieldValues.put(SEQUENCE.NAME, entity.getName());
    fieldValues.put(SEQUENCE.LIBRARY_ID, ULong.valueOf(entity.getLibraryId()));
    fieldValues.put(SEQUENCE.USER_ID, ULong.valueOf(entity.getUserId()));
    fieldValues.put(SEQUENCE.KEY, entity.getKey());
    fieldValues.put(SEQUENCE.TYPE, entity.getType());
    fieldValues.put(SEQUENCE.STATE, entity.getState());
    fieldValues.put(SEQUENCE.TEMPO, entity.getTempo());
    fieldValues.put(SEQUENCE.DENSITY, entity.getDensity());
    return fieldValues;
  }

  /**
   Update a sequence to Erase state

   @param db context
   @param id to delete
   @throws CoreException if database failure
   @throws CoreException if not configured properly
   @throws CoreException if fails business rule
   */
  private void erase(Access access, DSLContext db, ULong id) throws CoreException {
    if (access.isTopLevel())
      requireExists("Sequence", db.selectCount().from(SEQUENCE)
        .where(SEQUENCE.ID.eq(id))
        .fetchOne(0, int.class));
    else requireExists("Sequence", db.selectCount().from(SEQUENCE)
      .join(Library.LIBRARY).on(Library.LIBRARY.ID.eq(SEQUENCE.LIBRARY_ID))
      .where(SEQUENCE.ID.eq(id))
      .and(Library.LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
      .fetchOne(0, int.class));

    requireNotExists("Meme in Sequence", db.selectCount().from(SEQUENCE_MEME)
      .where(SEQUENCE_MEME.SEQUENCE_ID.eq(id))
      .fetchOne(0, int.class));

    // Update sequence state to Erase
    Map<Field, Object> fieldValues = com.google.common.collect.Maps.newHashMap();
    fieldValues.put(SEQUENCE.ID, id);
    fieldValues.put(SEQUENCE.STATE, SequenceState.Erase);

    if (0 == executeUpdate(db, SEQUENCE, fieldValues))
      throw new CoreException("No records updated.");

    // Schedule sequence deletion job
    workManager.doSequenceErase(id.toBigInteger());
  }

  @Override
  public Sequence create(Access access, Sequence entity) throws CoreException {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(create(tx.getContext(), access, entity));
    } catch (CoreException e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Sequence clone(Access access, BigInteger cloneId, Sequence entity) throws CoreException {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(clone(tx.getContext(), access, cloneId, entity));
    } catch (CoreException e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public Sequence readOne(Access access, BigInteger id) throws CoreException {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOne(tx.getContext(), access, ULong.valueOf(id)));
    } catch (CoreException e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Collection<Sequence> readAllBoundToChain(Access access, BigInteger chainId) throws CoreException {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAllBoundToChain(tx.getContext(), access, ULong.valueOf(chainId)));
    } catch (CoreException e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Collection<Sequence> readAllInAccount(Access access, BigInteger accountId) throws CoreException {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAllInAccount(tx.getContext(), access, ULong.valueOf(accountId)));
    } catch (CoreException e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Collection<Sequence> readAll(Access access, Collection<BigInteger> parentIds) throws CoreException {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAllInLibraries(tx.getContext(), access, uLongValuesOf(parentIds)));
    } catch (CoreException e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Collection<Sequence> readAll(Access access) throws CoreException {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAll(tx.getContext(), access));
    } catch (CoreException e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void update(Access access, BigInteger id, Sequence entity) throws CoreException {
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
  public Collection<Sequence> readAllInState(Access access, SequenceState state) throws CoreException {
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


  /**
   Clone a Sequence into a new Sequence

   @param db      context
   @param access  control
   @param cloneId of sequence to clone
   @param entity  for the new sequence
   @return newly readMany record
   @throws CoreException on failure
   */
  private Sequence clone(DSLContext db, Access access, BigInteger cloneId, Sequence entity) throws CoreException {
    Sequence from = readOne(db, access, ULong.valueOf(cloneId));
    if (Objects.isNull(from))
      throw new CoreException("Can't clone nonexistent Sequence");

    entity.setUserId(from.getUserId());
    entity.setDensity(from.getDensity());
    entity.setKey(from.getKey());
    entity.setTempo(from.getTempo());
    entity.setTypeEnum(from.getType());

    Sequence result = create(db, access, entity);
    workManager.doSequenceClone(cloneId, result.getId());
    return result;
  }

}
