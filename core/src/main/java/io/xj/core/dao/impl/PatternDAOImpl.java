// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao.impl;

import io.xj.core.access.impl.Access;
import io.xj.core.dao.PatternDAO;
import io.xj.core.exception.BusinessException;
import io.xj.core.exception.ConfigException;
import io.xj.core.model.pattern.Pattern;
import io.xj.core.model.pattern.PatternType;
import io.xj.core.persistence.sql.SQLDatabaseProvider;
import io.xj.core.persistence.sql.impl.SQLConnection;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.types.ULong;

import com.google.api.client.util.Maps;
import com.google.inject.Inject;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Map;

import static io.xj.core.Tables.CHAIN_LIBRARY;
import static io.xj.core.Tables.CHOICE;
import static io.xj.core.Tables.LIBRARY;
import static io.xj.core.Tables.PATTERN;
import static io.xj.core.Tables.PATTERN_MEME;
import static io.xj.core.Tables.PHASE;
import static io.xj.core.tables.ChainPattern.CHAIN_PATTERN;

public class PatternDAOImpl extends DAOImpl implements PatternDAO {

  @Inject
  public PatternDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public Pattern create(Access access, Pattern entity) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(create(tx.getContext(), access, entity));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public Pattern readOne(Access access, BigInteger id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOne(tx.getContext(), access, ULong.valueOf(id)));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Nullable
  @Override
  public Pattern readOneTypeInLink(Access access, BigInteger linkId, PatternType patternType) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOneTypeInLink(tx.getContext(), access, ULong.valueOf(linkId), patternType));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Collection<Pattern> readAllBoundToChain(Access access, BigInteger chainId, PatternType patternType) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAllBoundToChain(tx.getContext(), access, ULong.valueOf(chainId), patternType));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Collection<Pattern> readAllBoundToChainLibrary(Access access, BigInteger chainId, PatternType patternType) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAllBoundToChainLibrary(tx.getContext(), access, ULong.valueOf(chainId), patternType));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Collection<Pattern> readAllInAccount(Access access, BigInteger accountId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAllInAccount(tx.getContext(), access, ULong.valueOf(accountId)));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Collection<Pattern> readAllInLibrary(Access access, BigInteger libraryId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAllInLibrary(tx.getContext(), access, ULong.valueOf(libraryId)));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void update(Access access, BigInteger patternId, Pattern entity) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      update(tx.getContext(), access, ULong.valueOf(patternId), entity);
      tx.success();
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void delete(Access access, BigInteger id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      delete(tx.getContext(), access, ULong.valueOf(id));
      tx.success();
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  /**
   Create a record

   @param db     context
   @param access control
   @param entity for new record
   @return newly readMany record
   @throws BusinessException on failure
   */
  private static Pattern create(DSLContext db, Access access, Pattern entity) throws BusinessException {
    entity.validate();

    Map<Field, Object> fieldValues = fieldValueMap(entity);

    if (access.isTopLevel())
      requireExists("Library",
        db.selectCount().from(LIBRARY)
          .where(LIBRARY.ID.eq(ULong.valueOf(entity.getLibraryId())))
          .fetchOne(0, int.class));
    else
      requireExists("Library",
        db.selectCount().from(LIBRARY)
          .where(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
          .and(LIBRARY.ID.eq(ULong.valueOf(entity.getLibraryId())))
          .fetchOne(0, int.class));
    fieldValues.put(PATTERN.USER_ID, access.getUserId());

    return modelFrom(executeCreate(db, PATTERN, fieldValues), Pattern.class);
  }

  /**
   Read one record

   @param db     context
   @param access control
   @param id     of record
   @return record
   */
  @Nullable
  private static Pattern readOne(DSLContext db, Access access, ULong id) throws BusinessException {
    if (access.isTopLevel())
      return modelFrom(db.selectFrom(PATTERN)
        .where(PATTERN.ID.eq(id))
        .fetchOne(), Pattern.class);
    else
      return modelFrom(db.select(PATTERN.fields())
        .from(PATTERN)
        .join(LIBRARY).on(LIBRARY.ID.eq(PATTERN.LIBRARY_ID))
        .where(PATTERN.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne(), Pattern.class);
  }

  /**
   Read one record of a given type for a given link

   @param db         context
   @param access     control
   @param linkId     of link
   @param choiceType of pattern
   @return record
   */
  @Nullable
  private static Pattern readOneTypeInLink(DSLContext db, Access access, ULong linkId, PatternType choiceType) throws BusinessException {
    requireTopLevel(access);
    return modelFrom(db.select(PATTERN.fields())
      .from(PATTERN)
      .join(CHOICE).on(CHOICE.PATTERN_ID.eq(PATTERN.ID))
      .where(CHOICE.LINK_ID.eq(linkId))
      .and(CHOICE.TYPE.eq(choiceType.toString()))
      .fetchOne(), Pattern.class);
  }


  /**
   Read all records in parent record by id

   @param db        context
   @param access    control
   @param accountId of parent
   @return array of records
   */
  private static Collection<Pattern> readAllInAccount(DSLContext db, Access access, ULong accountId) throws BusinessException {
    if (access.isTopLevel())
      return modelsFrom(db.select(PATTERN.fields()).from(PATTERN)
        .join(LIBRARY).on(PATTERN.LIBRARY_ID.eq(LIBRARY.ID))
        .where(LIBRARY.ACCOUNT_ID.eq(accountId))
        .orderBy(PATTERN.TYPE, PATTERN.NAME)
        .fetch(), Pattern.class);
    else
      return modelsFrom(db.select(PATTERN.fields()).from(PATTERN)
        .join(LIBRARY).on(PATTERN.LIBRARY_ID.eq(LIBRARY.ID))
        .where(LIBRARY.ACCOUNT_ID.in(accountId))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .orderBy(PATTERN.TYPE, PATTERN.NAME)
        .fetch(), Pattern.class);
  }

  /**
   Read all records in parent record by id

   @param db        context
   @param access    control
   @param libraryId of parent
   @return array of records
   */
  private static Collection<Pattern> readAllInLibrary(DSLContext db, Access access, ULong libraryId) throws BusinessException {
    if (access.isTopLevel())
      return modelsFrom(db.select(PATTERN.fields()).from(PATTERN)
        .where(PATTERN.LIBRARY_ID.eq(libraryId))
        .orderBy(PATTERN.TYPE, PATTERN.NAME)
        .fetch(), Pattern.class);
    else
      return modelsFrom(db.select(PATTERN.fields()).from(PATTERN)
        .join(LIBRARY).on(LIBRARY.ID.eq(PATTERN.LIBRARY_ID))
        .where(PATTERN.LIBRARY_ID.eq(libraryId))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .orderBy(PATTERN.TYPE, PATTERN.NAME)
        .fetch(), Pattern.class);
  }

  /**
   Read all pattern records bound to a Chain via ChainPattern records

   @param db          context
   @param access      control
   @param chainId     of parent
   @param patternType of which to read all bound to chain
   @return array of records
   */
  private static Collection<Pattern> readAllBoundToChain(DSLContext db, Access access, ULong chainId, PatternType patternType) throws Exception {
    requireTopLevel(access);
    return modelsFrom(db.select(PATTERN.fields()).from(PATTERN)
      .join(CHAIN_PATTERN).on(CHAIN_PATTERN.PATTERN_ID.eq(PATTERN.ID))
      .where(CHAIN_PATTERN.CHAIN_ID.eq(chainId))
      .and(PATTERN.TYPE.eq(patternType.toString()))
      .fetch(), Pattern.class);
  }

  /**
   Read all pattern records bound to a Chain via ChainLibrary records

   @param db          context
   @param access      control
   @param chainId     of parent
   @param patternType of which to read all bound to chain library
   @return array of records
   */
  private static Collection<Pattern> readAllBoundToChainLibrary(DSLContext db, Access access, ULong chainId, PatternType patternType) throws Exception {
    requireTopLevel(access);
    return modelsFrom(db.select(PATTERN.fields()).from(PATTERN)
      .join(CHAIN_LIBRARY).on(CHAIN_LIBRARY.LIBRARY_ID.eq(PATTERN.LIBRARY_ID))
      .where(CHAIN_LIBRARY.CHAIN_ID.eq(chainId))
      .and(PATTERN.TYPE.eq(patternType.toString()))
      .fetch(), Pattern.class);
  }

  /**
   Update a record

   @param db     context
   @param access control
   @param id     of record
   @param entity to update with
   @throws BusinessException if a Business Rule is violated
   @throws Exception         on database failure
   */
  private static void update(DSLContext db, Access access, ULong id, Pattern entity) throws Exception {
    entity.validate();

    Map<Field, Object> fieldValues = fieldValueMap(entity);
    fieldValues.put(PATTERN.ID, id);

    if (access.isTopLevel())
      requireExists("Library",
        db.selectCount().from(LIBRARY)
          .where(LIBRARY.ID.eq(ULong.valueOf(entity.getLibraryId())))
          .fetchOne(0, int.class));
    else
      requireExists("Library",
        db.selectCount().from(LIBRARY)
          .where(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
          .and(LIBRARY.ID.eq(ULong.valueOf(entity.getLibraryId())))
          .fetchOne(0, int.class));
    fieldValues.put(PATTERN.USER_ID, access.getUserId());

    if (0 == executeUpdate(db, PATTERN, fieldValues))
      throw new BusinessException("No records updated.");
  }

  /**
   Delete an Pattern

   @param db context
   @param id to delete
   @throws Exception         if database failure
   @throws ConfigException   if not configured properly
   @throws BusinessException if fails business rule
   */
  private static void delete(DSLContext db, Access access, ULong id) throws Exception {
    if (!access.isTopLevel())
      requireExists("Pattern belonging to you", db.selectCount().from(PATTERN)
        .join(LIBRARY).on(PATTERN.LIBRARY_ID.eq(LIBRARY.ID))
        .where(PATTERN.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(idCollection(access.getAccountIds())))
        .and(PATTERN.USER_ID.eq(ULong.valueOf(access.getUserId())))
        .fetchOne(0, int.class));

    requireNotExists("Phase in Pattern", db.selectCount().from(PHASE)
      .where(PHASE.PATTERN_ID.eq(id))
      .fetchOne(0, int.class));

    requireNotExists("Choice in Pattern", db.selectCount().from(CHOICE)
      .where(CHOICE.PATTERN_ID.eq(id))
      .fetchOne(0, int.class));

    requireNotExists("Meme in Pattern", db.selectCount().from(PATTERN_MEME)
      .where(PATTERN_MEME.PATTERN_ID.eq(id))
      .fetchOne(0, int.class));

    db.deleteFrom(PATTERN)
      .where(PATTERN.ID.eq(id))
      .andNotExists(
        db.select(PHASE.ID)
          .from(PHASE)
          .where(PHASE.PATTERN_ID.eq(id))
      )
      .andNotExists(
        db.select(CHOICE.ID)
          .from(CHOICE)
          .where(CHOICE.PATTERN_ID.eq(id))
      )
      .andNotExists(
        db.select(PATTERN_MEME.ID)
          .from(PATTERN_MEME)
          .where(PATTERN_MEME.PATTERN_ID.eq(id))
      )
      .execute();
  }

  /**
   Only certain (writable) fields are mapped back to jOOQ records--
   Read-only fields are excluded from here.

   @param entity to source values from
   @return values mapped to record fields
   */
  private static Map<Field, Object> fieldValueMap(Pattern entity) {
    Map<Field, Object> fieldValues = Maps.newHashMap();
    fieldValues.put(PATTERN.NAME, entity.getName());
    fieldValues.put(PATTERN.LIBRARY_ID, ULong.valueOf(entity.getLibraryId()));
    fieldValues.put(PATTERN.USER_ID, ULong.valueOf(entity.getUserId()));
    fieldValues.put(PATTERN.KEY, entity.getKey());
    fieldValues.put(PATTERN.TYPE, entity.getType());
    fieldValues.put(PATTERN.TEMPO, entity.getTempo());
    fieldValues.put(PATTERN.DENSITY, entity.getDensity());
    return fieldValues;
  }

}
