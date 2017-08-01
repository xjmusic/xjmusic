// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.dao.impl;

import io.xj.core.app.access.impl.Access;
import io.xj.core.app.exception.BusinessException;
import io.xj.core.app.exception.ConfigException;
import io.xj.core.dao.AccountUserDAO;
import io.xj.core.database.sql.impl.SQLConnection;
import io.xj.core.database.sql.SQLDatabaseProvider;
import io.xj.core.model.account_user.AccountUser;
import io.xj.core.tables.records.AccountUserRecord;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Result;
import org.jooq.types.ULong;

import com.google.inject.Inject;

import java.sql.SQLException;
import java.util.Map;

import static io.xj.core.tables.AccountUser.ACCOUNT_USER;

public class AccountUserDAOImpl extends DAOImpl implements AccountUserDAO {

  @Inject
  public AccountUserDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public AccountUserRecord create(Access access, AccountUser entity) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(createRecord(tx.getContext(), access, entity));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public AccountUserRecord readOne(Access access, ULong id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOneRecord(tx.getContext(), access, id));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Result<AccountUserRecord> readAll(Access access, ULong accountId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAll(tx.getContext(), access, accountId));
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
   Create a new Account User record

   @param db     context
   @param entity for new AccountUser
   @return new record
   @throws Exception         if database failure
   @throws ConfigException   if not configured properly
   @throws BusinessException if fails business rule
   */
  private AccountUserRecord createRecord(DSLContext db, Access access, AccountUser entity) throws Exception {
    entity.validate();

    Map<Field, Object> fieldValues = entity.updatableFieldValueMap();

    requireTopLevel(access);

    if (db.selectFrom(ACCOUNT_USER)
      .where(ACCOUNT_USER.ACCOUNT_ID.eq(entity.getAccountId()))
      .and(ACCOUNT_USER.USER_ID.eq(entity.getUserId()))
      .fetchOne() != null)
      throw new BusinessException("Account User already exists!");

    return executeCreate(db, ACCOUNT_USER, fieldValues);
  }

  /**
   Read one record

   @param db     context
   @param access control
   @param id     of record
   @return record
   */
  private AccountUserRecord readOneRecord(DSLContext db, Access access, ULong id) {
    if (access.isTopLevel())
      return db.selectFrom(ACCOUNT_USER)
        .where(ACCOUNT_USER.ID.eq(id))
        .fetchOne();
    else
      return db.selectFrom(ACCOUNT_USER)
        .where(ACCOUNT_USER.ID.eq(id))
        .and(ACCOUNT_USER.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne();
  }

  /**
   Read all records in parent record

   @param db        context
   @param access    control
   @param accountId of parent
   @return array of child records
   @throws SQLException on failure
   */
  private Result<AccountUserRecord> readAll(DSLContext db, Access access, ULong accountId) throws SQLException {
    if (access.isTopLevel())
      return db.selectFrom(ACCOUNT_USER)
        .where(ACCOUNT_USER.ACCOUNT_ID.eq(accountId))
        .fetch();
    else
      return db.selectFrom(ACCOUNT_USER)
        .where(ACCOUNT_USER.ACCOUNT_ID.eq(accountId))
        .and(ACCOUNT_USER.ACCOUNT_ID.in(access.getAccounts()))
        .fetch();
  }

  /**
   Delete a record

   @param db     context
   @param access control
   @param id     of record
   @throws BusinessException on failure
   */
  private void delete(DSLContext db, Access access, ULong id) throws BusinessException {
    requireTopLevel(access);
    db.deleteFrom(ACCOUNT_USER)
      .where(ACCOUNT_USER.ID.eq(id))
      .execute();
  }

}
