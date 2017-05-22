// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.dao.impl;

import io.outright.xj.core.app.access.impl.Access;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.app.exception.ConfigException;
import io.outright.xj.core.dao.IdeaDAO;
import io.outright.xj.core.db.sql.SQLConnection;
import io.outright.xj.core.db.sql.SQLDatabaseProvider;
import io.outright.xj.core.model.idea.Idea;
import io.outright.xj.core.model.MemeEntity;
import io.outright.xj.core.tables.records.IdeaRecord;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SelectSelectStep;
import org.jooq.types.ULong;

import com.google.inject.Inject;

import javax.annotation.Nullable;
import java.sql.SQLException;
import java.util.Map;

import static io.outright.xj.core.Tables.CHAIN_LIBRARY;
import static io.outright.xj.core.Tables.CHOICE;
import static io.outright.xj.core.Tables.IDEA;
import static io.outright.xj.core.Tables.IDEA_MEME;
import static io.outright.xj.core.Tables.LIBRARY;
import static io.outright.xj.core.Tables.PHASE;
import static io.outright.xj.core.tables.ChainIdea.CHAIN_IDEA;
import static org.jooq.impl.DSL.groupConcat;

public class IdeaDAOImpl extends DAOImpl implements IdeaDAO {

  @Inject
  public IdeaDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public IdeaRecord create(Access access, Idea entity) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(createRecord(tx.getContext(), access, entity));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public IdeaRecord readOne(Access access, ULong id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOneRecord(tx.getContext(), access, id));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Nullable
  @Override
  public IdeaRecord readOneRecordTypeInLink(Access access, ULong linkId, String ideaType) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOneRecordTypeInLink(tx.getContext(), access, linkId, ideaType));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Result<? extends Record> readAllBoundToChain(Access access, ULong chainId, String ideaType) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAllBoundToChain(tx.getContext(), access, chainId, ideaType));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Result<? extends Record> readAllBoundToChainLibrary(Access access, ULong chainId, String ideaType) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAllBoundToChainLibrary(tx.getContext(), access, chainId, ideaType));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Result<IdeaRecord> readAllInAccount(Access access, ULong accountId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAllInAccount(tx.getContext(), access, accountId));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Result<IdeaRecord> readAllInLibrary(Access access, ULong libraryId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAllInLibrary(tx.getContext(), access, libraryId));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void update(Access access, ULong id, Idea entity) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      update(tx.getContext(), access, id, entity);
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
  private IdeaRecord createRecord(DSLContext db, Access access, Idea entity) throws BusinessException {
    entity.validate();

    Map<Field, Object> fieldValues = entity.updatableFieldValueMap();

    if (access.isTopLevel())
      requireExists("Library",
        db.select(LIBRARY.ID).from(LIBRARY)
          .where(LIBRARY.ID.eq(entity.getLibraryId()))
          .fetchOne());
    else
      requireExists("Library",
        db.select(LIBRARY.ID).from(LIBRARY)
          .where(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
          .and(LIBRARY.ID.eq(entity.getLibraryId()))
          .fetchOne());
    fieldValues.put(IDEA.USER_ID, access.getUserId());

    return executeCreate(db, IDEA, fieldValues);
  }

  /**
   Read one record

   @param db     context
   @param access control
   @param id     of record
   @return record
   */
  @Nullable
  private IdeaRecord readOneRecord(DSLContext db, Access access, ULong id) {
    if (access.isTopLevel())
      return db.selectFrom(IDEA)
        .where(IDEA.ID.eq(id))
        .fetchOne();
    else
      return recordInto(IDEA, db.select(IDEA.fields())
        .from(IDEA)
        .join(LIBRARY).on(LIBRARY.ID.eq(IDEA.LIBRARY_ID))
        .where(IDEA.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne());
  }

  /**
   Read one record of a given type for a given link

   @param db         context
   @param access     control
   @param linkId     of link
   @param choiceType of idea
   @return record
   */
  @Nullable
  private IdeaRecord readOneRecordTypeInLink(DSLContext db, Access access, ULong linkId, String choiceType) throws BusinessException {
    requireTopLevel(access);
    return recordInto(IDEA, db.select(IDEA.fields())
      .from(IDEA)
      .join(CHOICE).on(CHOICE.IDEA_ID.eq(IDEA.ID))
      .where(CHOICE.LINK_ID.eq(linkId))
      .and(CHOICE.TYPE.eq(choiceType))
      .fetchOne());
  }


  /**
   Read all records in parent record by id

   @param db        context
   @param access    control
   @param accountId of parent
   @return array of records
   */
  private Result<IdeaRecord> readAllInAccount(DSLContext db, Access access, ULong accountId) throws SQLException {
    if (access.isTopLevel())
      return resultInto(IDEA, db.select(IDEA.fields())
        .from(IDEA)
        .join(LIBRARY).on(IDEA.LIBRARY_ID.eq(LIBRARY.ID))
        .where(LIBRARY.ACCOUNT_ID.eq(accountId))
        .fetch());
    else
      return resultInto(IDEA, db.select(IDEA.fields())
        .from(IDEA)
        .join(LIBRARY).on(IDEA.LIBRARY_ID.eq(LIBRARY.ID))
        .where(LIBRARY.ACCOUNT_ID.in(accountId))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetch());
  }

  /**
   Read all idea records bound to a Chain via ChainIdea records

   @param db      context
   @param access  control
   @param chainId of parent
   @return array of records
   */
  private Result<? extends Record> readAllBoundToChain(DSLContext db, Access access, ULong chainId, String ideaType) throws Exception {
    requireTopLevel(access);
    return selectIdeaAndMemes(db)
      .from(IDEA_MEME)
      .join(CHAIN_IDEA).on(CHAIN_IDEA.IDEA_ID.eq(IDEA_MEME.IDEA_ID))
      .join(IDEA).on(IDEA.ID.eq(IDEA_MEME.IDEA_ID))
      .where(CHAIN_IDEA.CHAIN_ID.eq(chainId))
      .and(IDEA.TYPE.eq(ideaType))
      .groupBy(IDEA.ID)
      .fetch();
  }

  /**
   Read all idea records bound to a Chain via ChainLibrary records

   @param db      context
   @param access  control
   @param chainId of parent
   @return array of records
   */
  private Result<? extends Record> readAllBoundToChainLibrary(DSLContext db, Access access, ULong chainId, String ideaType) throws Exception {
    requireTopLevel(access);
    return selectIdeaAndMemes(db)
      .from(IDEA_MEME)
      .join(IDEA).on(IDEA.ID.eq(IDEA_MEME.IDEA_ID))
      .join(CHAIN_LIBRARY).on(CHAIN_LIBRARY.LIBRARY_ID.eq(IDEA.LIBRARY_ID))
      .where(CHAIN_LIBRARY.CHAIN_ID.eq(chainId))
      .and(IDEA.TYPE.eq(ideaType))
      .groupBy(IDEA.ID)
      .fetch();
  }

  /**
   Read all records in parent record by id

   @param db        context
   @param access    control
   @param libraryId of parent
   @return array of records
   */
  private Result<IdeaRecord> readAllInLibrary(DSLContext db, Access access, ULong libraryId) throws SQLException {
    if (access.isTopLevel())
      return resultInto(IDEA, db.select(IDEA.fields())
        .from(IDEA)
        .where(IDEA.LIBRARY_ID.eq(libraryId))
        .fetch());
    else
      return resultInto(IDEA, db.select(IDEA.fields())
        .from(IDEA)
        .join(LIBRARY).on(LIBRARY.ID.eq(IDEA.LIBRARY_ID))
        .where(IDEA.LIBRARY_ID.eq(libraryId))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetch());
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
  private void update(DSLContext db, Access access, ULong id, Idea entity) throws Exception {
    entity.validate();

    Map<Field, Object> fieldValues = entity.updatableFieldValueMap();
    fieldValues.put(IDEA.ID, id);

    if (access.isTopLevel())
      requireExists("Library",
        db.select(LIBRARY.ID).from(LIBRARY)
          .where(LIBRARY.ID.eq(entity.getLibraryId()))
          .fetchOne());
    else
      requireExists("Library",
        db.select(LIBRARY.ID).from(LIBRARY)
          .where(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
          .and(LIBRARY.ID.eq(entity.getLibraryId()))
          .fetchOne());
    fieldValues.put(IDEA.USER_ID, access.getUserId());

    if (executeUpdate(db, IDEA, fieldValues) == 0)
      throw new BusinessException("No records updated.");
  }

  /**
   Delete an Idea

   @param db context
   @param id to delete
   @throws Exception         if database failure
   @throws ConfigException   if not configured properly
   @throws BusinessException if fails business rule
   */
  private void delete(DSLContext db, Access access, ULong id) throws Exception {
    if (!access.isTopLevel())
      requireExists("Idea belonging to you", db.select(IDEA.fields()).from(IDEA)
        .join(LIBRARY).on(IDEA.LIBRARY_ID.eq(LIBRARY.ID))
        .where(IDEA.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .and(IDEA.USER_ID.eq(access.getUserId()))
        .fetchOne());

    requireNotExists("Phase in Idea", db.select(PHASE.ID)
      .from(PHASE)
      .where(PHASE.IDEA_ID.eq(id))
      .fetch());

    requireNotExists("Choice in Idea", db.select(CHOICE.ID)
      .from(CHOICE)
      .where(CHOICE.IDEA_ID.eq(id))
      .fetch());

    requireNotExists("Meme in Idea", db.select(IDEA_MEME.ID)
      .from(IDEA_MEME)
      .where(IDEA_MEME.IDEA_ID.eq(id))
      .fetch());

    db.deleteFrom(IDEA)
      .where(IDEA.ID.eq(id))
      .andNotExists(
        db.select(PHASE.ID)
          .from(PHASE)
          .where(PHASE.IDEA_ID.eq(id))
      )
      .andNotExists(
        db.select(CHOICE.ID)
          .from(CHOICE)
          .where(CHOICE.IDEA_ID.eq(id))
      )
      .andNotExists(
        db.select(IDEA_MEME.ID)
          .from(IDEA_MEME)
          .where(IDEA_MEME.IDEA_ID.eq(id))
      )
      .execute();
  }

  /**
   This is used to select many Idea records
   with a virtual column containing a CSV of its meme names

   @param db context
   @return jOOQ select step
   */
  private SelectSelectStep<?> selectIdeaAndMemes(DSLContext db) {
    return db.select(
      IDEA.ID,
      IDEA.DENSITY,
      IDEA.KEY,
      IDEA.USER_ID,
      IDEA.LIBRARY_ID,
      IDEA.NAME,
      IDEA.TEMPO,
      IDEA.TYPE,
      IDEA.CREATED_AT,
      IDEA.UPDATED_AT,
      groupConcat(IDEA_MEME.NAME, ",").as(MemeEntity.KEY_MANY)
    );
  }


}
