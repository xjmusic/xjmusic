// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.dao.impl;

import io.outright.xj.core.Tables;
import io.outright.xj.core.app.access.impl.AccessControl;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.app.exception.ConfigException;
import io.outright.xj.core.app.exception.DatabaseException;
import io.outright.xj.core.dao.LinkDAO;
import io.outright.xj.core.db.sql.SQLConnection;
import io.outright.xj.core.db.sql.SQLDatabaseProvider;
import io.outright.xj.core.model.link.Link;
import io.outright.xj.core.model.link.LinkWrapper;
import io.outright.xj.core.tables.records.LinkRecord;
import io.outright.xj.core.transport.JSON;
import io.outright.xj.core.util.Purify;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.UpdateSetFirstStep;
import org.jooq.types.ULong;

import com.google.inject.Inject;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.annotation.Nullable;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Map;

import static io.outright.xj.core.tables.Chain.CHAIN;
import static io.outright.xj.core.tables.Choice.CHOICE;
import static io.outright.xj.core.tables.Link.LINK;
import static io.outright.xj.core.tables.LinkChord.LINK_CHORD;

public class LinkDAOImpl extends DAOImpl implements LinkDAO {

  @Inject
  public LinkDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public JSONObject create(AccessControl access, LinkWrapper data) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(create(tx.getContext(), access, data));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public JSONObject readOne(AccessControl access, ULong id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOne(tx.getContext(), access, id));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Nullable
  @Override
  public JSONObject readOneInState(AccessControl access, ULong chainId, String linkState, Timestamp linkBeginBefore) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOneInState(tx.getContext(), access, chainId, linkState, linkBeginBefore));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public JSONArray readAllIn(AccessControl access, ULong chainId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAllIn(tx.getContext(), access, chainId));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void update(AccessControl access, ULong id, LinkWrapper data) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      updateWrapper(tx.getContext(), access, id, data);
      tx.success();
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void delete(AccessControl access, ULong linkId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      delete(tx.getContext(), access, linkId);
      tx.success();
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  /**
   * Create a new record
   *
   * @param db     context
   * @param access control
   * @param data   for new record
   * @return newly created record
   * @throws BusinessException if a Business Rule is violated
   */
  private JSONObject create(DSLContext db, AccessControl access, LinkWrapper data) throws BusinessException {
    Link model = data.validate();
    Map<Field, Object> fieldValues = model.intoFieldValueMap();

    requireTopLevel(access);

//    prefer to let the database constraint fail on create, instead of blocking with a read beforehand

//    requireRecordExists("Chain", db.select(CHAIN.ID).from(CHAIN)
//      .where(CHAIN.ID.eq(model.getChainId()))
//      .fetchOne());
//
    return JSON.objectFromRecord(executeCreate(db, LINK, fieldValues));
  }

  /**
   * Read one record
   *
   * @param db     context
   * @param access control
   * @param id     of record
   * @return record
   */
  private JSONObject readOne(DSLContext db, AccessControl access, ULong id) {
    if (access.isTopLevel()) {
      return JSON.objectFromRecord(db.selectFrom(LINK)
        .where(LINK.ID.eq(id))
        .fetchOne());
    } else {
      return JSON.objectFromRecord(db.select(LINK.fields())
        .from(LINK)
        .join(CHAIN).on(CHAIN.ID.eq(LINK.CHAIN_ID))
        .where(LINK.ID.eq(id))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne());
    }
  }


  /**
   * Fetch one Link by chainId and state, if present
   *
   * @param db           context
   * @param access       control
   * @param chainId      to find link in
   * @param linkState    linkState to find link in
   * @param linkBeginBefore ahead to look for links
   * @return Link if found
   * @throws BusinessException on failure
   */
  private JSONObject readOneInState(DSLContext db, AccessControl access, ULong chainId, String linkState, Timestamp linkBeginBefore) throws BusinessException {
    requireTopLevel(access);

    return JSON.objectFromRecord(
      db.select(LINK.fields()).from(LINK)
        .where(LINK.CHAIN_ID.eq(chainId))
        .and(LINK.STATE.eq(Purify.LowerSlug(linkState)))
        .and(LINK.BEGIN_AT.lessOrEqual(linkBeginBefore))
        .orderBy(LINK.OFFSET.asc())
        .limit(1)
        .fetchOne());
  }


  /**
   * Read all records in parent by id
   *
   * @param db      context
   * @param access  control
   * @param chainId of parent
   * @return array of records
   */
  private JSONArray readAllIn(DSLContext db, AccessControl access, ULong chainId) throws SQLException {
    if (access.isTopLevel()) {
      return JSON.arrayFromResultSet(db.select(LINK.fields())
        .from(LINK)
        .where(LINK.CHAIN_ID.eq(chainId))
        .orderBy(LINK.OFFSET.desc())
        .fetchResultSet());
    } else {
      return JSON.arrayFromResultSet(db.select(LINK.fields())
        .from(LINK)
        .join(CHAIN).on(CHAIN.ID.eq(LINK.CHAIN_ID))
        .where(LINK.CHAIN_ID.eq(chainId))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccounts()))
        .orderBy(LINK.OFFSET.desc())
        .fetchResultSet());
    }
  }

  /**
   * Update a record using a model wrapper
   *
   * @param db     context
   * @param access control
   * @param id     of link to update
   * @param data   wrapper
   * @throws BusinessException on failure
   * @throws DatabaseException on failure
   */
  private void updateWrapper(DSLContext db, AccessControl access, ULong id, LinkWrapper data) throws Exception {
    Link model = data.validate();
    Map<Field, Object> fieldValues = model.intoFieldValueMap();
    fieldValues.put(Tables.LINK.ID, id);
    update(db, access, id, fieldValues);
  }

  /**
   * Update a record
   *
   * @param db     context
   * @param access control
   * @param id     of record
   * @param fieldValues   to update with
   * @throws BusinessException if a Business Rule is violated
   */
  private void update(DSLContext db, AccessControl access, ULong id, Map<Field, Object> fieldValues) throws BusinessException, DatabaseException {
    requireTopLevel(access);

    // validate and cache to-state
    String updateState = fieldValues.get(Tables.LINK.STATE).toString();
    Link.validateState(updateState);

    // fetch existing link; further logic is based on its current state
    LinkRecord link = db.selectFrom(Tables.LINK).where(Tables.LINK.ID.eq(id)).fetchOne();
    requireRecordExists("Link #" + id, link);
    switch (link.getState()) {

      case Link.PLANNED:
        onlyAllowTransitions(updateState, Link.PLANNED, Link.CRAFTING);
        break;

      case Link.CRAFTING:
        onlyAllowTransitions(updateState, Link.CRAFTING, Link.CRAFTED);
        break;

      case Link.CRAFTED:
        onlyAllowTransitions(updateState, Link.CRAFTED, Link.DUBBING);
        break;

      case Link.DUBBING:
        onlyAllowTransitions(updateState, Link.DUBBING, Link.DUBBED);
        break;

      case Link.DUBBED:
        onlyAllowTransitions(updateState, Link.DUBBED);
        break;

      default:
        onlyAllowTransitions(updateState, Link.PLANNED);
        break;
    }

    // [#128] cannot change chainId of a link
    Object updateChainId = fieldValues.get(Tables.LINK.CHAIN_ID);
    if (updateChainId != null
      && !updateChainId.equals(link.getChainId())
      ) {
      throw new BusinessException("cannot change chainId of a link");
    }

    // This "change from state to state" complexity
    // is required in order to prevent duplicate
    // state-changes of the same link
    UpdateSetFirstStep<LinkRecord> update = db.update(Tables.LINK);
    fieldValues.forEach(update::set);
    int rowsAffected = update.set(Tables.LINK.STATE, updateState)
      .where(Tables.LINK.ID.eq(id))
      .and(Tables.LINK.STATE.eq(link.getState()))
      .execute();
    if (rowsAffected == 0) {
      throw new BusinessException("No records updated.");
    }
  }

  /**
   * Delete a Link
   *
   * @param db     context
   * @param access control
   * @param id     to delete
   * @throws Exception         if database failure
   * @throws ConfigException   if not configured properly
   * @throws BusinessException if fails business rule
   */
  private void delete(DSLContext db, AccessControl access, ULong id) throws Exception {
    requireTopLevel(access);

    requireRecordExists("Link", db.selectFrom(LINK)
      .where(LINK.ID.eq(id))
      .fetchOne());

    requireEmptyResultSet(db.select(LINK_CHORD.ID)
      .from(LINK_CHORD)
      .where(LINK_CHORD.LINK_ID.eq(id))
      .fetchResultSet());

    requireEmptyResultSet(db.select(CHOICE.ID)
      .from(CHOICE)
      .where(CHOICE.LINK_ID.eq(id))
      .fetchResultSet());

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

}
