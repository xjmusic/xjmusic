// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.dao.impl;

import io.outright.xj.core.app.access.AccessControl;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.app.exception.ConfigException;
import io.outright.xj.core.app.exception.DatabaseException;
import io.outright.xj.core.dao.PickDAO;
import io.outright.xj.core.db.sql.SQLConnection;
import io.outright.xj.core.db.sql.SQLDatabaseProvider;
import io.outright.xj.core.model.pick.Pick;
import io.outright.xj.core.model.pick.PickWrapper;
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

import static io.outright.xj.core.Tables.ARRANGEMENT;
import static io.outright.xj.core.Tables.LINK;
import static io.outright.xj.core.Tables.CHAIN;
import static io.outright.xj.core.Tables.CHOICE;
import static io.outright.xj.core.Tables.PICK;

public class PickDAOImpl extends DAOImpl implements PickDAO {

  @Inject
  public PickDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public JSONObject create(AccessControl access, PickWrapper data) throws Exception {
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
  public JSONArray readAllIn(AccessControl access, ULong arrangementId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAllIn(tx.getContext(), access, arrangementId));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void update(AccessControl access, ULong id, PickWrapper data) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      update(tx.getContext(), access, id, data);
      tx.success();
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void delete(AccessControl access, ULong pickId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      delete(tx.getContext(), access, pickId);
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
  private JSONObject create(DSLContext db, AccessControl access, PickWrapper data) throws BusinessException {
    Pick model = data.validate();
    Map<Field, Object> fieldValues = model.intoFieldValueMap();

    requireTopLevel(access);

    requireRecordExists("Arrangement", db.select(ARRANGEMENT.ID).from(ARRANGEMENT)
      .where(ARRANGEMENT.ID.eq(model.getArrangementId()))
      .fetchOne());

    return JSON.objectFromRecord(executeCreate(db, PICK, fieldValues));
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
      return JSON.objectFromRecord(db.selectFrom(PICK)
        .where(PICK.ID.eq(id))
        .fetchOne());
    } else {
      return JSON.objectFromRecord(db.select(PICK.fields())
        .from(PICK)
        .join(ARRANGEMENT).on(ARRANGEMENT.ID.eq(PICK.ARRANGEMENT_ID))
        .join(CHOICE).on(CHOICE.ID.eq(ARRANGEMENT.CHOICE_ID))
        .join(LINK).on(LINK.ID.eq(CHOICE.LINK_ID))
        .join(CHAIN).on(CHAIN.ID.eq(LINK.CHAIN_ID))
        .where(PICK.ID.eq(id))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne());
    }
  }

  /**
   * Read all records in parent by id
   *
   * @param db            context
   * @param access        control
   * @param arrangementId of parent
   * @return array of records
   */
  private JSONArray readAllIn(DSLContext db, AccessControl access, ULong arrangementId) throws SQLException {
    if (access.isTopLevel()) {
      return JSON.arrayFromResultSet(db.select(PICK.fields())
        .from(PICK)
        .where(PICK.ARRANGEMENT_ID.eq(arrangementId))
        .fetchResultSet());
    } else {
      return JSON.arrayFromResultSet(db.select(PICK.fields())
        .from(PICK)
        .join(ARRANGEMENT).on(ARRANGEMENT.ID.eq(PICK.ARRANGEMENT_ID))
        .join(CHOICE).on(CHOICE.ID.eq(ARRANGEMENT.CHOICE_ID))
        .join(LINK).on(LINK.ID.eq(CHOICE.LINK_ID))
        .join(CHAIN).on(CHAIN.ID.eq(LINK.CHAIN_ID))
        .where(PICK.ARRANGEMENT_ID.eq(arrangementId))
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
  private void update(DSLContext db, AccessControl access, ULong id, PickWrapper data) throws BusinessException, DatabaseException {
    Pick model = data.validate();
    Map<Field, Object> fieldValues = model.intoFieldValueMap();
    fieldValues.put(PICK.ID, id);

    requireTopLevel(access);

    requireRecordExists("existing Pick with immutable Arrangement membership",
      db.selectFrom(PICK)
        .where(PICK.ID.eq(id))
        .and(PICK.ARRANGEMENT_ID.eq(model.getArrangementId()))
        .fetchOne());

    if (executeUpdate(db, PICK, fieldValues) == 0) {
      throw new BusinessException("No records updated.");
    }
  }

  /**
   * Delete a Pick
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

    requireRecordExists("Pick", db.selectFrom(PICK)
      .where(PICK.ID.eq(id))
      .fetchOne());

    db.deleteFrom(PICK)
      .where(PICK.ID.eq(id))
      .execute();
  }

}
