// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.dao.impl;

import io.outright.xj.core.app.access.impl.AccessControl;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.app.exception.ConfigException;
import io.outright.xj.core.app.exception.DatabaseException;
import io.outright.xj.core.dao.ChoiceDAO;
import io.outright.xj.core.db.sql.SQLConnection;
import io.outright.xj.core.db.sql.SQLDatabaseProvider;
import io.outright.xj.core.model.choice.Choice;
import io.outright.xj.core.model.choice.ChoiceWrapper;
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

import static io.outright.xj.core.Tables.CHOICE;
import static io.outright.xj.core.tables.Arrangement.ARRANGEMENT;
import static io.outright.xj.core.tables.Chain.CHAIN;
import static io.outright.xj.core.tables.Link.LINK;

public class ChoiceDAOImpl extends DAOImpl implements ChoiceDAO {

  @Inject
  public ChoiceDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public JSONObject create(AccessControl access, ChoiceWrapper data) throws Exception {
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
  public void update(AccessControl access, ULong id, ChoiceWrapper data) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      update(tx.getContext(), access, id, data);
      tx.success();
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void delete(AccessControl access, ULong choiceId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      delete(tx.getContext(), access, choiceId);
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
  private JSONObject create(DSLContext db, AccessControl access, ChoiceWrapper data) throws BusinessException {
    Choice model = data.validate();
    Map<Field, Object> fieldValues = model.intoFieldValueMap();

    requireTopLevel(access);

    requireRecordExists("Link", db.select(LINK.ID).from(LINK)
      .where(LINK.ID.eq(model.getLinkId()))
      .fetchOne());

    return JSON.objectFromRecord(executeCreate(db, CHOICE, fieldValues));
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
      return JSON.objectFromRecord(db.selectFrom(CHOICE)
        .where(CHOICE.ID.eq(id))
        .fetchOne());
    } else {
      return JSON.objectFromRecord(db.select(CHOICE.fields())
        .from(CHOICE)
        .join(LINK).on(LINK.ID.eq(CHOICE.LINK_ID))
        .join(CHAIN).on(CHAIN.ID.eq(LINK.CHAIN_ID))
        .where(CHOICE.ID.eq(id))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne());
    }
  }

  /**
   * Read all records in parent by id
   *
   * @param db      context
   * @param access  control
   * @param linkId of parent
   * @return array of records
   */
  private JSONArray readAllIn(DSLContext db, AccessControl access, ULong linkId) throws SQLException {
    if (access.isTopLevel()) {
      return JSON.arrayFromResultSet(db.select(CHOICE.fields())
        .from(CHOICE)
        .where(CHOICE.LINK_ID.eq(linkId))
        .fetchResultSet());
    } else {
      return JSON.arrayFromResultSet(db.select(CHOICE.fields())
        .from(CHOICE)
        .join(LINK).on(LINK.ID.eq(CHOICE.LINK_ID))
        .join(CHAIN).on(CHAIN.ID.eq(LINK.CHAIN_ID))
        .where(CHOICE.LINK_ID.eq(linkId))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccounts()))
        .fetchResultSet());
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
  private void update(DSLContext db, AccessControl access, ULong id, ChoiceWrapper data) throws BusinessException, DatabaseException {
    Choice model = data.validate();
    Map<Field, Object> fieldValues = model.intoFieldValueMap();
    fieldValues.put(CHOICE.ID, id);

    requireTopLevel(access);

    requireRecordExists("existing Choice with immutable Link membership",
      db.selectFrom(CHOICE)
        .where(CHOICE.ID.eq(id))
        .and(CHOICE.LINK_ID.eq(model.getLinkId()))
        .fetchOne());

    if (executeUpdate(db, CHOICE, fieldValues) == 0) {
      throw new BusinessException("No records updated.");
    }
  }

  /**
   * Delete a Choice
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

    requireRecordExists("Choice", db.selectFrom(CHOICE)
      .where(CHOICE.ID.eq(id))
      .fetchOne());

    requireEmptyResultSet(db.select(ARRANGEMENT.ID)
      .from(ARRANGEMENT)
      .where(ARRANGEMENT.CHOICE_ID.eq(id))
      .fetchResultSet());

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
