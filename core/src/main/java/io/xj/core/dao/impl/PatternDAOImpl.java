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

import com.google.common.collect.Lists;
import com.google.inject.Inject;

import javax.annotation.Nullable;
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
  public Pattern readOne(Access access, ULong id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOne(tx.getContext(), access, id));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Nullable
  @Override
  public Pattern readOneRecordTypeInLink(Access access, ULong linkId, PatternType patternType) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOneTypeInLink(tx.getContext(), access, linkId, patternType));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Collection<Pattern> readAllBoundToChain(Access access, ULong chainId, PatternType patternType) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAllBoundToChain(tx.getContext(), access, chainId, patternType));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Collection<Pattern> readAllBoundToChainLibrary(Access access, ULong chainId, PatternType patternType) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAllBoundToChainLibrary(tx.getContext(), access, chainId, patternType));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Collection<Pattern> readAllInAccount(Access access, ULong accountId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAllInAccount(tx.getContext(), access, accountId));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Collection<Pattern> readAllInLibrary(Access access, ULong libraryId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAllInLibrary(tx.getContext(), access, libraryId));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void update(Access access, ULong patternId, Pattern entity) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      update(tx.getContext(), access, patternId, entity);
      tx.success();
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void delete(Access access, ULong id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      delete(tx.getContext(), access, id);
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
  private Pattern create(DSLContext db, Access access, Pattern entity) throws BusinessException {
    entity.validate();

    Map<Field, Object> fieldValues = entity.updatableFieldValueMap();

    if (access.isTopLevel())
      requireExists("Library",
        db.selectCount().from(LIBRARY)
          .where(LIBRARY.ID.eq(entity.getLibraryId()))
          .fetchOne(0, int.class));
    else
      requireExists("Library",
        db.selectCount().from(LIBRARY)
          .where(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
          .and(LIBRARY.ID.eq(entity.getLibraryId()))
          .fetchOne(0, int.class));
    fieldValues.put(PATTERN.USER_ID, access.getUserId());

    return new Pattern().setFromRecord(executeCreate(db, PATTERN, fieldValues));
  }

  /**
   Read one record

   @param db     context
   @param access control
   @param id     of record
   @return record
   */
  @Nullable
  private Pattern readOne(DSLContext db, Access access, ULong id) {
    if (access.isTopLevel())
      return new Pattern().setFromRecord(db.selectFrom(PATTERN)
        .where(PATTERN.ID.eq(id))
        .fetchOne());
    else
      return new Pattern().setFromRecord(recordInto(PATTERN, db.select(PATTERN.fields())
        .from(PATTERN)
        .join(LIBRARY).on(LIBRARY.ID.eq(PATTERN.LIBRARY_ID))
        .where(PATTERN.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne()));
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
  private Pattern readOneTypeInLink(DSLContext db, Access access, ULong linkId, PatternType choiceType) throws BusinessException {
    requireTopLevel(access);
    return new Pattern().setFromRecord(recordInto(PATTERN, db.select(PATTERN.fields())
      .from(PATTERN)
      .join(CHOICE).on(CHOICE.PATTERN_ID.eq(PATTERN.ID))
      .where(CHOICE.LINK_ID.eq(linkId))
      .and(CHOICE.TYPE.eq(choiceType.toString()))
      .fetchOne()));
  }


  /**
   Read all records in parent record by id

   @param db        context
   @param access    control
   @param accountId of parent
   @return array of records
   */
  private static Collection<Pattern> readAllInAccount(DSLContext db, Access access, ULong accountId) {
    Collection<Pattern> result = Lists.newArrayList();

    if (access.isTopLevel())
      db.select(PATTERN.fields()).from(PATTERN)
        .join(LIBRARY).on(PATTERN.LIBRARY_ID.eq(LIBRARY.ID))
        .where(LIBRARY.ACCOUNT_ID.eq(accountId))
        .orderBy(PATTERN.TYPE, PATTERN.NAME)
        .fetch().forEach((record) -> result.add(new Pattern().setFromRecord(record)));
    else
      db.select(PATTERN.fields()).from(PATTERN)
        .join(LIBRARY).on(PATTERN.LIBRARY_ID.eq(LIBRARY.ID))
        .where(LIBRARY.ACCOUNT_ID.in(accountId))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .orderBy(PATTERN.TYPE, PATTERN.NAME)
        .fetch().forEach((record) -> result.add(new Pattern().setFromRecord(record)));

    return result;
  }

  /**
   Read all records in parent record by id

   @param db        context
   @param access    control
   @param libraryId of parent
   @return array of records
   */
  private static Collection<Pattern> readAllInLibrary(DSLContext db, Access access, ULong libraryId) {
    Collection<Pattern> result = Lists.newArrayList();

    if (access.isTopLevel())
      db.select(PATTERN.fields()).from(PATTERN)
        .where(PATTERN.LIBRARY_ID.eq(libraryId))
        .orderBy(PATTERN.TYPE, PATTERN.NAME)
        .fetch().forEach((record) -> result.add(new Pattern().setFromRecord(record)));
    else
      db.select(PATTERN.fields()).from(PATTERN)
        .join(LIBRARY).on(LIBRARY.ID.eq(PATTERN.LIBRARY_ID))
        .where(PATTERN.LIBRARY_ID.eq(libraryId))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .orderBy(PATTERN.TYPE, PATTERN.NAME)
        .fetch().forEach((record) -> result.add(new Pattern().setFromRecord(record)));

    return result;
  }

  /**
   Read all pattern records bound to a Chain via ChainPattern records

   @param db          context
   @param access      control
   @param chainId     of parent
   @param patternType of which to read all bound to chain
   @return array of records
   */
  private Collection<Pattern> readAllBoundToChain(DSLContext db, Access access, ULong chainId, PatternType patternType) throws Exception {
    requireTopLevel(access);

    Collection<Pattern> result = Lists.newArrayList();

    db.select(PATTERN.fields()).from(PATTERN)
      .join(CHAIN_PATTERN).on(CHAIN_PATTERN.PATTERN_ID.eq(PATTERN.ID))
      .where(CHAIN_PATTERN.CHAIN_ID.eq(chainId))
      .and(PATTERN.TYPE.eq(patternType.toString()))
      .fetch().forEach((record) -> result.add(new Pattern().setFromRecord(record)));

    return result;
  }

  /**
   Read all pattern records bound to a Chain via ChainLibrary records

   @param db          context
   @param access      control
   @param chainId     of parent
   @param patternType of which to read all bound to chain library
   @return array of records
   */
  private Collection<Pattern> readAllBoundToChainLibrary(DSLContext db, Access access, ULong chainId, PatternType patternType) throws Exception {
    requireTopLevel(access);

    Collection<Pattern> result = Lists.newArrayList();

    db.select(PATTERN.fields()).from(PATTERN)
      .join(CHAIN_LIBRARY).on(CHAIN_LIBRARY.LIBRARY_ID.eq(PATTERN.LIBRARY_ID))
      .where(CHAIN_LIBRARY.CHAIN_ID.eq(chainId))
      .and(PATTERN.TYPE.eq(patternType.toString()))
      .fetch().forEach((record) -> result.add(new Pattern().setFromRecord(record)));

    return result;
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
  private void update(DSLContext db, Access access, ULong id, Pattern entity) throws Exception {
    entity.validate();

    Map<Field, Object> fieldValues = entity.updatableFieldValueMap();
    fieldValues.put(PATTERN.ID, id);

    if (access.isTopLevel())
      requireExists("Library",
        db.selectCount().from(LIBRARY)
          .where(LIBRARY.ID.eq(entity.getLibraryId()))
          .fetchOne(0, int.class));
    else
      requireExists("Library",
        db.selectCount().from(LIBRARY)
          .where(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
          .and(LIBRARY.ID.eq(entity.getLibraryId()))
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
  private void delete(DSLContext db, Access access, ULong id) throws Exception {
    if (!access.isTopLevel())
      requireExists("Pattern belonging to you", db.selectCount().from(PATTERN)
        .join(LIBRARY).on(PATTERN.LIBRARY_ID.eq(LIBRARY.ID))
        .where(PATTERN.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .and(PATTERN.USER_ID.eq(access.getUserId()))
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

}
