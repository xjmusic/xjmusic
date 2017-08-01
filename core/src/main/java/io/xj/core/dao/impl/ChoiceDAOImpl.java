// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.dao.impl;

import io.xj.core.app.access.impl.Access;
import io.xj.core.app.exception.BusinessException;
import io.xj.core.app.exception.ConfigException;
import io.xj.core.dao.ChoiceDAO;
import io.xj.core.database.sql.impl.SQLConnection;
import io.xj.core.database.sql.SQLDatabaseProvider;
import io.xj.core.model.choice.Choice;
import io.xj.core.model.idea.IdeaType;
import io.xj.core.tables.records.ChoiceRecord;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SelectOffsetStep;
import org.jooq.types.ULong;

import com.google.inject.Inject;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static io.xj.core.Tables.CHOICE;
import static io.xj.core.Tables.PHASE;
import static io.xj.core.tables.Arrangement.ARRANGEMENT;
import static io.xj.core.tables.Chain.CHAIN;
import static io.xj.core.tables.Link.LINK;
import static org.jooq.impl.DSL.groupConcat;

public class ChoiceDAOImpl extends DAOImpl implements ChoiceDAO {

  @Inject
  public ChoiceDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public ChoiceRecord create(Access access, Choice entity) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(createRecord(tx.getContext(), access, entity));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public ChoiceRecord readOne(Access access, ULong choiceId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOneRecord(tx.getContext(), access, choiceId));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Nullable
  @Override
  public ChoiceRecord readOneLinkIdea(Access access, ULong linkId, ULong ideaId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOneLinkIdea(tx.getContext(), access, linkId, ideaId));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public Choice readOneLinkTypeWithAvailablePhaseOffsets(Access access, ULong linkId, IdeaType ideaType) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(new Choice().setFromRecord(readOneLinkTypeWithAvailablePhaseOffsets(tx.getContext(), access, linkId, ideaType)));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public Result<ChoiceRecord> readAll(Access access, ULong linkId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAll(tx.getContext(), access, linkId));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Result<ChoiceRecord> readAllInLinks(Access access, List<ULong> linkIds) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAllInLinks(tx.getContext(), access, linkIds));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void update(Access access, ULong choiceId, Choice entity) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      update(tx.getContext(), access, choiceId, entity);
      tx.success();
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void delete(Access access, ULong choiceId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      delete(tx.getContext(), access, choiceId);
      tx.success();
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  /**
   Create a new record

   @param db     context
   @param access control
   @param entity for new record
   @return newly readMany record
   @throws BusinessException if a Business Rule is violated
   */
  private ChoiceRecord createRecord(DSLContext db, Access access, Choice entity) throws BusinessException {
    entity.validate();

    Map<Field, Object> fieldValues = entity.updatableFieldValueMap();

    requireTopLevel(access);

    requireExists("Link", db.select(LINK.ID).from(LINK)
      .where(LINK.ID.eq(entity.getLinkId()))
      .fetchOne());

    return executeCreate(db, CHOICE, fieldValues);
  }

  /**
   Read one record

   @param db     context
   @param access control
   @param id     of record
   @return record
   */
  private ChoiceRecord readOneRecord(DSLContext db, Access access, ULong id) {
    if (access.isTopLevel())
      return db.selectFrom(CHOICE)
        .where(CHOICE.ID.eq(id))
        .fetchOne();
    else
      return recordInto(CHOICE, db.select(CHOICE.fields())
        .from(CHOICE)
        .join(LINK).on(LINK.ID.eq(CHOICE.LINK_ID))
        .join(CHAIN).on(CHAIN.ID.eq(LINK.CHAIN_ID))
        .where(CHOICE.ID.eq(id))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne());
  }

  /**
   Read one record binding an idea to a link

   @param db     context
   @param access control
   @param linkId to get choice for
   @param ideaId to get choice for
   @return record
   */
  private ChoiceRecord readOneLinkIdea(DSLContext db, Access access, ULong linkId, ULong ideaId) throws BusinessException {
    requireTopLevel(access);
    return db.selectFrom(CHOICE)
      .where(CHOICE.LINK_ID.eq(linkId))
      .and(CHOICE.IDEA_ID.eq(ideaId))
      .fetchOne();
  }

  /**
   Read one record

   @return record
    @param db     context
   @param access control
   @param linkId of record
   @param ideaType of which to read one link with available offsets
   */
  private Record readOneLinkTypeWithAvailablePhaseOffsets(DSLContext db, Access access, ULong linkId, IdeaType ideaType) throws BusinessException {
    requireTopLevel(access);

    SelectOffsetStep<?> query = db.select(
      CHOICE.ID,
      CHOICE.IDEA_ID,
      CHOICE.LINK_ID,
      CHOICE.TYPE,
      CHOICE.PHASE_OFFSET,
      CHOICE.TRANSPOSE,
      groupConcat(PHASE.OFFSET, ",").as(Choice.KEY_AVAILABLE_PHASE_OFFSETS)
    )
      .from(PHASE)
      .join(CHOICE).on(CHOICE.IDEA_ID.eq(PHASE.IDEA_ID))
      .where(CHOICE.LINK_ID.eq(linkId))
      .and(CHOICE.TYPE.eq(ideaType.toString()))
      .groupBy(CHOICE.ID, CHOICE.LINK_ID, CHOICE.IDEA_ID, CHOICE.PHASE_OFFSET, CHOICE.TRANSPOSE)
      .limit(1);

    return query.fetchOne();
  }


  /**
   Read all records in parent by id

   @param db     context
   @param access control
   @param linkId of parent
   @return array of records
   */
  private Result<ChoiceRecord> readAll(DSLContext db, Access access, ULong linkId) {
    if (access.isTopLevel())
      return resultInto(CHOICE, db.select(CHOICE.fields())
        .from(CHOICE)
        .where(CHOICE.LINK_ID.eq(linkId))
        .fetch());
    else
      return resultInto(CHOICE, db.select(CHOICE.fields())
        .from(CHOICE)
        .join(LINK).on(LINK.ID.eq(CHOICE.LINK_ID))
        .join(CHAIN).on(CHAIN.ID.eq(LINK.CHAIN_ID))
        .where(CHOICE.LINK_ID.eq(linkId))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccounts()))
        .fetch());
  }

  /**
   Read all records in parent records by ids

   @return array of records
    @param db      context
   @param access  control
   @param linkIds id of parent's parent (the chain)
   */
  private Result<ChoiceRecord> readAllInLinks(DSLContext db, Access access, Collection<ULong> linkIds) throws Exception {
    if (access.isTopLevel())
      return resultInto(CHOICE, db.select(CHOICE.fields())
        .from(CHOICE)
        .join(LINK).on(LINK.ID.eq(CHOICE.LINK_ID))
        .where(LINK.ID.in(linkIds))
        .orderBy(CHOICE.TYPE)
        .fetch());
    else
      return resultInto(CHOICE, db.select(CHOICE.fields())
        .from(CHOICE)
        .join(LINK).on(LINK.ID.eq(CHOICE.LINK_ID))
        .join(CHAIN).on(CHAIN.ID.eq(LINK.CHAIN_ID))
        .where(LINK.ID.in(linkIds))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccounts()))
        .orderBy(CHOICE.TYPE)
        .fetch());
  }

  /**
   Update a record

   @param db     context
   @param access control
   @param id     of record
   @param entity to update with
   @throws BusinessException if a Business Rule is violated
   */
  private void update(DSLContext db, Access access, ULong id, Choice entity) throws BusinessException {
    entity.validate();

    Map<Field, Object> fieldValues = entity.updatableFieldValueMap();
    fieldValues.put(CHOICE.ID, id);

    requireTopLevel(access);

    requireExists("existing Choice with immutable Link membership",
      db.selectFrom(CHOICE)
        .where(CHOICE.ID.eq(id))
        .and(CHOICE.LINK_ID.eq(entity.getLinkId()))
        .fetchOne());

    if (0 == executeUpdate(db, CHOICE, fieldValues))
      throw new BusinessException("No records updated.");
  }

  /**
   Delete a Choice

   @param db     context
   @param access control
   @param id     to delete
   @throws Exception         if database failure
   @throws ConfigException   if not configured properly
   @throws BusinessException if fails business rule
   */
  private void delete(DSLContext db, Access access, ULong id) throws Exception {
    requireTopLevel(access);

    requireExists("Choice", db.selectFrom(CHOICE)
      .where(CHOICE.ID.eq(id))
      .fetchOne());

    requireNotExists("Arrangement in Choice", db.select(ARRANGEMENT.ID)
      .from(ARRANGEMENT)
      .where(ARRANGEMENT.CHOICE_ID.eq(id))
      .fetch());

    db.deleteFrom(CHOICE)
      .where(CHOICE.ID.eq(id))
      .andNotExists(
        db.select(ARRANGEMENT.ID)
          .from(ARRANGEMENT)
          .where(ARRANGEMENT.CHOICE_ID.eq(id))
      )
      .execute();
  }
}
