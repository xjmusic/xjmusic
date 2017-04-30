// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.dao.impl;

import io.outright.xj.core.app.access.impl.AccessControl;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.app.exception.ConfigException;
import io.outright.xj.core.app.exception.DatabaseException;
import io.outright.xj.core.dao.AccountDAO;
import io.outright.xj.core.db.sql.SQLConnection;
import io.outright.xj.core.db.sql.SQLDatabaseProvider;
import io.outright.xj.core.model.account.Account;
import io.outright.xj.core.model.account.AccountWrapper;
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

import static io.outright.xj.core.Tables.ACCOUNT;
import static io.outright.xj.core.tables.AccountUser.ACCOUNT_USER;
import static io.outright.xj.core.tables.Library.LIBRARY;

public class AccountDAOImpl extends DAOImpl implements AccountDAO {

  @Inject
  public AccountDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public JSONObject create(AccessControl access, AccountWrapper data) throws Exception {
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
      return tx.success(readOneAble(tx.getContext(), access, id));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public JSONArray readAll(AccessControl access) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAllAble(tx.getContext(), access));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void update(AccessControl access, ULong id, AccountWrapper data) throws Exception {
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
      delete(tx.getContext(), access, id);
      tx.success();
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  /**
   Create a new record

   @param db   context
   @param data for new record
   @return newly created record
   @throws BusinessException if a business rule is violated
   */
  private JSONObject create(DSLContext db, AccessControl access, AccountWrapper data) throws BusinessException {
    Account model = data.validate();
    Map<Field, Object> fieldValues = model.intoFieldValueMap();

    requireTopLevel(access);

    return JSON.objectFromRecord(executeCreate(db, ACCOUNT, fieldValues));
  }

  /**
   Read one record, if accessible

   @param db     context
   @param access control
   @param id     of record
   @return record
   */
  private JSONObject readOneAble(DSLContext db, AccessControl access, ULong id) throws SQLException {
    if (access.isTopLevel()) {
      return JSON.objectFromRecord(db.selectFrom(ACCOUNT)
        .where(ACCOUNT.ID.eq(id))
        .fetchOne());
    } else {
      return JSON.objectFromRecord(db.select(ACCOUNT.fields())
        .from(ACCOUNT)
        .where(ACCOUNT.ID.eq(id))
        .and(ACCOUNT.ID.in(access.getAccounts()))
        .fetchOne());
    }
  }

  /**
   Read all records accessible

   @param db     context
   @param access control
   @return array of records
   @throws SQLException on failure
   */
  private JSONArray readAllAble(DSLContext db, AccessControl access) throws SQLException {
    if (access.isTopLevel()) {
      return JSON.arrayFromResultSet(db.select(ACCOUNT.fields())
        .from(ACCOUNT)
        .fetchResultSet());
    } else {
      return JSON.arrayFromResultSet(db.select(ACCOUNT.fields())
        .from(ACCOUNT)
        .where(ACCOUNT.ID.in(access.getAccounts()))
        .fetchResultSet());
    }
  }

  /**
   Update a record

   @param db     context
   @param access control
   @param id     of record to update
   @param data   to update record with
   @throws BusinessException if a business rule is violated
   */
  private void update(DSLContext db, AccessControl access, ULong id, AccountWrapper data) throws BusinessException, DatabaseException {
    requireTopLevel(access);

    Map<Field, Object> fieldValues = data.validate().intoFieldValueMap();
    fieldValues.put(ACCOUNT.ID, id);
    if (executeUpdate(db, ACCOUNT, fieldValues) == 0) {
      throw new BusinessException("No records updated.");
    }
  }

  /**
   Delete an Account

   @param db        context
   @param accountId to delete
   @throws Exception         if database failure
   @throws ConfigException   if not configured properly
   @throws BusinessException if fails business rule
   */
  private void delete(DSLContext db, AccessControl access, ULong accountId) throws Exception {
    requireTopLevel(access);

    requireEmptyResultSet(db.select(LIBRARY.ID)
      .from(LIBRARY)
      .where(LIBRARY.ACCOUNT_ID.eq(accountId))
      .fetchResultSet());

    requireEmptyResultSet(db.select(ACCOUNT_USER.ID)
      .from(ACCOUNT_USER)
      .where(ACCOUNT_USER.ACCOUNT_ID.eq(accountId))
      .fetchResultSet());

    db.deleteFrom(ACCOUNT)
      .where(ACCOUNT.ID.eq(accountId))
      .andNotExists(
        db.select(LIBRARY.ID)
          .from(LIBRARY)
          .where(LIBRARY.ACCOUNT_ID.eq(accountId))
      )
      .andNotExists(
        db.select(ACCOUNT_USER.ID)
          .from(ACCOUNT_USER)
          .where(ACCOUNT_USER.ACCOUNT_ID.eq(accountId))
      )
      .execute();
  }

}
