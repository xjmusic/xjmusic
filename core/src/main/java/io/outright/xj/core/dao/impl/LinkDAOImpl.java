// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.dao.impl;

import io.outright.xj.core.app.access.impl.AccessControl;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.app.exception.ConfigException;
import io.outright.xj.core.app.exception.DatabaseException;
import io.outright.xj.core.dao.LinkDAO;
import io.outright.xj.core.db.sql.SQLConnection;
import io.outright.xj.core.db.sql.SQLDatabaseProvider;
import io.outright.xj.core.model.link.Link;
import io.outright.xj.core.model.link.LinkWrapper;
import io.outright.xj.core.transport.JSON;
import io.outright.xj.core.util.Purify;
import io.outright.xj.core.util.timestamp.TimestampUTC;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
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
import static org.jooq.impl.DSL.max;

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
  public JSONObject readOneInState(AccessControl access, ULong chainId, String linkState, int aheadSeconds) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOneInState(tx.getContext(), access, chainId, linkState, aheadSeconds));
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
  public JSONObject readPilotTemplateFor(AccessControl access, ULong chainId, Timestamp chainBeginAt, int aheadSeconds) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readPilotTemplateFor(tx.getContext(), access, chainId, chainBeginAt, aheadSeconds));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void update(AccessControl access, ULong id, LinkWrapper data) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      update(tx.getContext(), access, id, data);
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

    requireRecordExists("Chain", db.select(CHAIN.ID).from(CHAIN)
      .where(CHAIN.ID.eq(model.getChainId()))
      .fetchOne());

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
   * @param aheadSeconds ahead to look for links
   * @return Link if found
   * @throws BusinessException on failure
   */
  private JSONObject readOneInState(DSLContext db, AccessControl access, ULong chainId, String linkState, int aheadSeconds) throws BusinessException {
    requireTopLevel(access);

    return JSON.objectFromRecord(
      db.select(LINK.fields()).from(LINK)
        .where(LINK.CHAIN_ID.eq(chainId))
        .and(LINK.STATE.eq(Purify.LowerSlug(linkState)))
        .and(LINK.BEGIN_AT.lessThan(TimestampUTC.nowPlusSeconds(aheadSeconds)))
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
   * Read all records in parent by id
   *
   * @param db           context
   * @param chainId      to read pilot template link for
   * @param chainBeginAt when the chain begins
   * @param aheadSeconds ahead of end of Chain to do work  @return array of records
   */
  @Nullable
  private JSONObject readPilotTemplateFor(DSLContext db, AccessControl access, ULong chainId, Timestamp chainBeginAt, int aheadSeconds) throws SQLException, BusinessException {
    requireTopLevel(access);

    Record lastRecordWithNoEndAtTime = db.select(LINK.CHAIN_ID)
      .from(LINK)
      .where(LINK.END_AT.isNull())
      .and(LINK.CHAIN_ID.eq(chainId))
      .groupBy(LINK.CHAIN_ID, LINK.OFFSET, LINK.END_AT)
      .orderBy(LINK.OFFSET.desc())
      .limit(1)
      .fetchOne();

    // If there's already a no-endAt-time-having Link
    // at the end of this Chain, get outta here
    if (lastRecordWithNoEndAtTime != null) {
      return null;
    }

    Record pilotRecord = db.select(
      LINK.CHAIN_ID,
      LINK.END_AT.as(LINK.BEGIN_AT),
      max(LINK.OFFSET).plus(1).as(LINK.OFFSET)
    )
      .from(LINK)
      .where(LINK.END_AT.lessThan(TimestampUTC.nowPlusSeconds(aheadSeconds)))
      .and(LINK.CHAIN_ID.eq(chainId))
      .groupBy(LINK.CHAIN_ID, LINK.OFFSET, LINK.END_AT)
      .orderBy(LINK.OFFSET.desc())
      .limit(1)
      .fetchOne();

    if (pilotRecord != null) {
      return JSON.objectFromRecord(pilotRecord);

    } else {
      // the Chain must be empty. Create its first link
      JSONObject pilotTemplate = new JSONObject();
      pilotTemplate.put(Link.KEY_CHAIN_ID, chainId);
      pilotTemplate.put(Link.KEY_BEGIN_AT, chainBeginAt);
      pilotTemplate.put(Link.KEY_OFFSET, 0);
      return pilotTemplate;
    }
  }

  /**
   * Update a record
   *
   * @param db     context
   * @param access control
   * @param id     of record
   * @param data   to update with
   * @throws BusinessException if a Business Rule is violated
   */
  private void update(DSLContext db, AccessControl access, ULong id, LinkWrapper data) throws BusinessException, DatabaseException {
    Link model = data.validate();
    Map<Field, Object> fieldValues = model.intoFieldValueMap();
    fieldValues.put(LINK.ID, id);

    requireTopLevel(access);

    requireRecordExists("existing Link with immutable Chain membership",
      db.selectFrom(LINK)
        .where(LINK.ID.eq(id))
        .and(LINK.CHAIN_ID.eq(model.getChainId()))
        .fetchOne());

    if (executeUpdate(db, LINK, fieldValues) == 0) {
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
