// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.dao.impl;

import io.outright.xj.core.app.access.impl.AccessControl;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.app.exception.ConfigException;
import io.outright.xj.core.dao.LinkChordDAO;
import io.outright.xj.core.db.sql.SQLConnection;
import io.outright.xj.core.db.sql.SQLDatabaseProvider;
import io.outright.xj.core.model.link_chord.LinkChord;
import io.outright.xj.core.model.link_chord.LinkChordWrapper;
import io.outright.xj.core.transport.JSON;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.types.ULong;

import com.google.inject.Inject;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.annotation.Nullable;
import java.sql.SQLException;
import java.util.Map;

import static io.outright.xj.core.Tables.LINK;
import static io.outright.xj.core.Tables.LINK_CHORD;
import static io.outright.xj.core.tables.Chain.CHAIN;

public class LinkChordDAOImpl extends DAOImpl implements LinkChordDAO {

  @Inject
  public LinkChordDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public JSONObject create(AccessControl access, LinkChordWrapper data) throws Exception {
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

  @Override
  @Nullable
  public JSONArray readAllIn(AccessControl access, ULong linkId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAllIn(tx.getContext(), access, linkId));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void update(AccessControl access, ULong id, LinkChordWrapper data) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      update(tx.getContext(), access, id, data);
      tx.success();
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void delete(AccessControl access, ULong id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      delete(access, tx.getContext(), id);
      tx.success();
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  /**
   * Create a new Link Chord
   *
   * @param db     context
   * @param access control
   * @param data   for new link
   * @return newly created record
   * @throws BusinessException if failure
   */
  private JSONObject create(DSLContext db, AccessControl access, LinkChordWrapper data) throws BusinessException {
    LinkChord model = data.validate();
    Map<Field, Object> fieldValues = model.intoFieldValueMap();

    requireTopLevel(access);

    requireRecordExists("Link", db.select(LINK.ID).from(LINK)
      .where(LINK.ID.eq(model.getLinkId()))
      .fetchOne());

    return JSON.objectFromRecord(executeCreate(db, LINK_CHORD, fieldValues));
  }

  /**
   * Read one Chord if able
   *
   * @param db     context
   * @param access control
   * @param id     of link
   * @return link
   */
  private JSONObject readOne(DSLContext db, AccessControl access, ULong id) {
    if (access.isTopLevel()) {
      return JSON.objectFromRecord(db.selectFrom(LINK_CHORD)
        .where(LINK_CHORD.ID.eq(id))
        .fetchOne());
    } else {
      return JSON.objectFromRecord(db.select(LINK_CHORD.fields())
        .from(LINK_CHORD)
        .join(LINK).on(LINK.ID.eq(LINK_CHORD.LINK_ID))
        .join(CHAIN).on(CHAIN.ID.eq(LINK.CHAIN_ID))
        .where(LINK_CHORD.ID.eq(id))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne());
    }
  }

  /**
   * Read all Chord able for an Chain
   *
   * @param db     context
   * @param access control
   * @param linkId to read all link of
   * @return array of links
   * @throws SQLException on failure
   */
  private JSONArray readAllIn(DSLContext db, AccessControl access, ULong linkId) throws SQLException {
    if (access.isTopLevel()) {
      return JSON.arrayFromResultSet(db.select(LINK_CHORD.fields())
        .from(LINK_CHORD)
        .where(LINK_CHORD.LINK_ID.eq(linkId))
        .orderBy(LINK_CHORD.POSITION)
        .fetchResultSet());
    } else {
      return JSON.arrayFromResultSet(db.select(LINK_CHORD.fields())
        .from(LINK_CHORD)
        .join(LINK).on(LINK.ID.eq(LINK_CHORD.LINK_ID))
        .join(CHAIN).on(CHAIN.ID.eq(LINK.CHAIN_ID))
        .where(LINK_CHORD.LINK_ID.eq(linkId))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccounts()))
        .orderBy(LINK_CHORD.POSITION)
        .fetchResultSet());
    }
  }

  /**
   * Update a Chord record
   *
   * @param db     context
   * @param access control
   * @param id     to update
   * @param data   to update with
   * @throws BusinessException if failure
   */
  private void update(DSLContext db, AccessControl access, ULong id, LinkChordWrapper data) throws BusinessException {
    LinkChord model = data.validate();
    Map<Field, Object> fieldValues = model.intoFieldValueMap();
    fieldValues.put(LINK_CHORD.ID, id);

    requireTopLevel(access);

    requireRecordExists("existing LinkChord with immutable Link membership",
      db.select(LINK_CHORD.ID).from(LINK_CHORD)
      .where(LINK_CHORD.ID.eq(id))
        .and(LINK_CHORD.LINK_ID.eq(model.getLinkId()))
      .fetchOne());

    if (executeUpdate(db, LINK_CHORD, fieldValues) == 0) {
      throw new BusinessException("No records updated.");
    }
  }

  /**
   * Delete an Chord
   *
   * @param db context
   * @param id to delete
   * @throws Exception         if database failure
   * @throws ConfigException   if not configured properly
   * @throws BusinessException if fails business rule
   */
  private void delete(AccessControl access, DSLContext db, ULong id) throws Exception {
    requireTopLevel(access);

    db.deleteFrom(LINK_CHORD)
      .where(LINK_CHORD.ID.eq(id))
      .execute();
  }

}
