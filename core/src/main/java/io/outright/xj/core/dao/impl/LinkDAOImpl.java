// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.dao.impl;

import io.outright.xj.core.Tables;
import io.outright.xj.core.app.access.impl.Access;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.app.exception.ConfigException;
import io.outright.xj.core.app.exception.DatabaseException;
import io.outright.xj.core.dao.LinkDAO;
import io.outright.xj.core.db.sql.SQLConnection;
import io.outright.xj.core.db.sql.SQLDatabaseProvider;
import io.outright.xj.core.model.link.Link;
import io.outright.xj.core.model.link.LinkChoice;
import io.outright.xj.core.tables.records.LinkRecord;
import io.outright.xj.core.util.Text;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SelectSelectStep;
import org.jooq.UpdateSetFirstStep;
import org.jooq.types.ULong;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;

import javax.annotation.Nullable;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Map;

import static io.outright.xj.core.Tables.PHASE;
import static io.outright.xj.core.tables.Chain.CHAIN;
import static io.outright.xj.core.tables.Choice.CHOICE;
import static io.outright.xj.core.tables.Link.LINK;
import static io.outright.xj.core.tables.LinkChord.LINK_CHORD;
import static org.jooq.impl.DSL.groupConcat;

public class LinkDAOImpl extends DAOImpl implements LinkDAO {

  @Inject
  public LinkDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public LinkRecord create(Access access, Link entity) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(create(tx.getContext(), access, entity));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public LinkRecord readOne(Access access, ULong id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOne(tx.getContext(), access, id));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public LinkRecord readOneAtChainOffset(Access access, ULong chainId, ULong offset) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOneAtChainOffset(tx.getContext(), access, chainId, offset));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Nullable
  @Override
  public LinkRecord readOneInState(Access access, ULong chainId, String linkState, Timestamp linkBeginBefore) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOneInState(tx.getContext(), access, chainId, linkState, linkBeginBefore));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public LinkChoice readLinkChoice(Access access, ULong linkId, String ideaType) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(LinkChoice.from(readLinkChoice(tx.getContext(), access, linkId, ideaType)));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Result<LinkRecord> readAll(Access access, ULong chainId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAll(tx.getContext(), access, chainId));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void update(Access access, ULong id, Link entity) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      update(tx.getContext(), access, id, entity);
      tx.success();
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void updateState(Access access, ULong id, String state) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      updateState(tx.getContext(), access, id, state);
      tx.success();
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void delete(Access access, ULong linkId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      delete(tx.getContext(), access, linkId);
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
  private LinkRecord create(DSLContext db, Access access, Link entity) throws BusinessException {
    entity.validate();

    Map<Field, Object> fieldValues = entity.updatableFieldValueMap();

    // [#126] Links are always readMany in PLANNED state
    fieldValues.put(LINK.STATE, Link.PLANNED);

    // top-level access
    requireTopLevel(access);

    return executeCreate(db, LINK, fieldValues);
  }

  /**
   Read one record

   @param db     context
   @param access control
   @param id     of record
   @return record
   */
  private LinkRecord readOne(DSLContext db, Access access, ULong id) {
    if (access.isTopLevel())
      return db.selectFrom(LINK)
        .where(LINK.ID.eq(id))
        .fetchOne();
    else
      return recordInto(LINK, db.select(LINK.fields())
        .from(LINK)
        .join(CHAIN).on(CHAIN.ID.eq(LINK.CHAIN_ID))
        .where(LINK.ID.eq(id))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne());
  }

  /**
   Read id for the Link in a Chain at a given offset, if present

   @param db      context
   @param access  control
   @param chainId to fetch a link for
   @param offset  to fetch link at
   @return record
   */
  @Nullable
  private LinkRecord readOneAtChainOffset(DSLContext db, Access access, ULong chainId, ULong offset) throws BusinessException {
    requireTopLevel(access);
    return db.selectFrom(LINK)
      .where(LINK.OFFSET.eq(offset))
      .and(LINK.CHAIN_ID.eq(chainId))
      .fetchOne();
  }

  /**
   Fetch one Link by chainId and state, if present

   @param db              context
   @param access          control
   @param chainId         to find link in
   @param linkState       linkState to find link in
   @param linkBeginBefore ahead to look for links
   @return Link if found
   @throws BusinessException on failure
   */
  private LinkRecord readOneInState(DSLContext db, Access access, ULong chainId, String linkState, Timestamp linkBeginBefore) throws BusinessException {
    requireTopLevel(access);

    return recordInto(LINK, db.select(LINK.fields()).from(LINK)
      .where(LINK.CHAIN_ID.eq(chainId))
      .and(LINK.STATE.eq(Text.LowerSlug(linkState)))
      .and(LINK.BEGIN_AT.lessOrEqual(linkBeginBefore))
      .orderBy(LINK.OFFSET.asc())
      .limit(1)
      .fetchOne());
  }

  /**
   Read one record

   @param db     context
   @param access control
   @param linkId of record
   @return record
   */
  private Record readLinkChoice(DSLContext db, Access access, ULong linkId, String ideaType) throws BusinessException {
    requireTopLevel(access);
    return selectLinkChoiceAndPhases(db)
      .from(PHASE)
      .join(CHOICE).on(CHOICE.IDEA_ID.eq(PHASE.IDEA_ID))
      .where(CHOICE.LINK_ID.eq(linkId))
      .and(CHOICE.TYPE.eq(ideaType))
      .groupBy(CHOICE.IDEA_ID, CHOICE.PHASE_OFFSET, CHOICE.TRANSPOSE)
      .limit(1)
      .fetchOne();
  }

  /**
   Read all records in parent by id

   @param db      context
   @param access  control
   @param chainId of parent
   @return array of records
   */
  private Result<LinkRecord> readAll(DSLContext db, Access access, ULong chainId) throws SQLException {
    if (access.isTopLevel())
      return resultInto(LINK, db.select(LINK.fields())
        .from(LINK)
        .where(LINK.CHAIN_ID.eq(chainId))
        .orderBy(LINK.OFFSET.desc())
        .fetch());
    else
      return resultInto(LINK, db.select(LINK.fields())
        .from(LINK)
        .join(CHAIN).on(CHAIN.ID.eq(LINK.CHAIN_ID))
        .where(LINK.CHAIN_ID.eq(chainId))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccounts()))
        .orderBy(LINK.OFFSET.desc())
        .fetch());
  }

  /**
   Update a record using a model wrapper

   @param db     context
   @param access control
   @param id     of link to update
   @param entity wrapper
   @throws BusinessException on failure
   @throws DatabaseException on failure
   */
  private void update(DSLContext db, Access access, ULong id, Link entity) throws Exception {
    entity.validate();

    Map<Field, Object> fieldValues = entity.updatableFieldValueMap();

    fieldValues.put(LINK.ID, id);
    update(db, access, id, fieldValues);
  }

  /**
   Update the state of a record

   @param db     context
   @param access control
   @param id     of record
   @throws BusinessException if a Business Rule is violated
   */
  private void updateState(DSLContext db, Access access, ULong id, String state) throws Exception {
    Map<Field, Object> fieldValues = ImmutableMap.of(
      Tables.LINK.ID, id,
      Tables.LINK.STATE, state
    );

    update(db, access, id, fieldValues);

    if (executeUpdate(db, Tables.LINK, fieldValues) == 0)
      throw new BusinessException("No records updated.");
  }

  /**
   Update a record

   @param db          context
   @param access      control
   @param id          of record
   @param fieldValues to update with
   @throws BusinessException if a Business Rule is violated
   */
  private void update(DSLContext db, Access access, ULong id, Map<Field, Object> fieldValues) throws BusinessException, DatabaseException {
    requireTopLevel(access);

    // validate and cache to-state
    String updateState = fieldValues.get(LINK.STATE).toString();
    Link.validateState(updateState);

    // fetch existing link; further logic is based on its current state
    LinkRecord link = db.selectFrom(LINK).where(LINK.ID.eq(id)).fetchOne();
    requireExists("Link #" + id, link);
    switch (link.getState()) {

      case Link.PLANNED:
        onlyAllowTransitions(updateState, Link.PLANNED, Link.CRAFTING);
        break;

      case Link.CRAFTING:
        onlyAllowTransitions(updateState, Link.CRAFTING, Link.CRAFTED, Link.FAILED);
        break;

      case Link.CRAFTED:
        onlyAllowTransitions(updateState, Link.CRAFTED, Link.DUBBING);
        break;

      case Link.DUBBING:
        onlyAllowTransitions(updateState, Link.DUBBING, Link.DUBBED, Link.FAILED);
        break;

      case Link.DUBBED:
        onlyAllowTransitions(updateState, Link.DUBBED);
        break;

      case Link.FAILED:
        onlyAllowTransitions(updateState, Link.FAILED);
        break;

      default:
        onlyAllowTransitions(updateState, Link.PLANNED);
        break;
    }

    // [#128] cannot change chainId of a link
    Object updateChainId = fieldValues.get(LINK.CHAIN_ID);
    if (exists(updateChainId) && !updateChainId.equals(link.getChainId()))
      throw new BusinessException("cannot change chainId of a link");

    // This "change from state to state" complexity
    // is required in order to prevent duplicate
    // state-changes of the same link
    UpdateSetFirstStep<LinkRecord> update = db.update(LINK);
    fieldValues.forEach(update::set);
    int rowsAffected = update.set(LINK.STATE, updateState)
      .where(LINK.ID.eq(id))
      .and(LINK.STATE.eq(link.getState()))
      .execute();

    if (rowsAffected == 0)
      throw new BusinessException("No records updated.");

  }

  /**
   Delete a Link

   @param db     context
   @param access control
   @param id     to delete
   @throws Exception         if database failure
   @throws ConfigException   if not configured properly
   @throws BusinessException if fails business rule
   */
  private void delete(DSLContext db, Access access, ULong id) throws Exception {
    requireTopLevel(access);

    requireExists("Link", db.selectFrom(LINK)
      .where(LINK.ID.eq(id))
      .fetchOne());

    requireNotExists("Chord in Link", db.select(LINK_CHORD.ID)
      .from(LINK_CHORD)
      .where(LINK_CHORD.LINK_ID.eq(id))
      .fetch());

    requireNotExists("Choice in Link", db.select(CHOICE.ID)
      .from(CHOICE)
      .where(CHOICE.LINK_ID.eq(id))
      .fetch());

    db.deleteFrom(LINK)
      .where(LINK.ID.eq(id))
      .andNotExists(
        db.select(CHOICE.ID)
          .from(CHOICE)
          .where(CHOICE.LINK_ID.eq(id))
      )
      .andNotExists(
        db.select(LINK_CHORD.ID)
          .from(LINK_CHORD)
          .where(LINK_CHORD.LINK_ID.eq(id))
      )
      .execute();
  }

  /**
   This is used to select many Idea records
   with a virtual column containing a CSV of its meme names

   @param db context
   @return jOOQ select step
   */
  private SelectSelectStep<?> selectLinkChoiceAndPhases(DSLContext db) {
    return db.select(
      CHOICE.IDEA_ID.as(LinkChoice.KEY_IDEA_ID),
      CHOICE.TYPE.as(LinkChoice.KEY_TYPE),
      CHOICE.PHASE_OFFSET.as(LinkChoice.KEY_PHASE_OFFSET),
      CHOICE.TRANSPOSE.as(LinkChoice.KEY_TRANSPOSE),
      groupConcat(PHASE.OFFSET, ",").as(LinkChoice.KEY_AVAILABLE_PHASE_OFFSETS)
    );
  }

}
