// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.dao.impl;

import io.xj.core.access.impl.Access;
import io.xj.core.exception.BusinessException;
import io.xj.core.exception.ConfigException;
import io.xj.core.exception.DatabaseException;
import io.xj.core.dao.AccountDAO;
import io.xj.core.database.sql.impl.SQLConnection;
import io.xj.core.database.sql.SQLDatabaseProvider;
import io.xj.core.model.account.Account;
import io.xj.core.tables.records.AccountRecord;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Result;
import org.jooq.types.ULong;

import com.google.inject.Inject;

import javax.annotation.Nullable;
import java.sql.SQLException;
import java.util.Map;

import static io.xj.core.Tables.ACCOUNT;
import static io.xj.core.tables.AccountUser.ACCOUNT_USER;
import static io.xj.core.tables.Chain.CHAIN;
import static io.xj.core.tables.Library.LIBRARY;

public class AccountDAOImpl extends DAOImpl implements AccountDAO {

  @Inject
  public AccountDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public AccountRecord create(Access access, Account entity) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(create(tx.getContext(), access, entity));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public AccountRecord readOne(Access access, ULong id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOne(tx.getContext(), access, id));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public Result<AccountRecord> readAll(Access access) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAll(tx.getContext(), access));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void update(Access access, ULong id, Account entity) throws Exception {
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
   Create a new record

   @param db     context
   @param entity for new record
   @return newly readMany record
   @throws BusinessException if a business rule is violated
   */
  private AccountRecord create(DSLContext db, Access access, Account entity) throws BusinessException {
    entity.validate();

    Map<Field, Object> fieldValues = entity.updatableFieldValueMap();

    requireTopLevel(access);

    return executeCreate(db, ACCOUNT, fieldValues);
  }

  /**
   Read one record, if accessible

   @param db     context
   @param access control
   @param id     of record
   @return record
   */
  private AccountRecord readOne(DSLContext db, Access access, ULong id) throws SQLException {
    if (access.isTopLevel())
      return db.selectFrom(ACCOUNT)
        .where(ACCOUNT.ID.eq(id))
        .fetchOne();
    else
      return recordInto(ACCOUNT, db.select(ACCOUNT.fields())
        .from(ACCOUNT)
        .where(ACCOUNT.ID.eq(id))
        .and(ACCOUNT.ID.in(access.getAccounts()))
        .fetchOne());
  }

  /**
   Read all records accessible

   @param db     context
   @param access control
   @return array of records
   @throws SQLException on failure
   */
  private Result<AccountRecord> readAll(DSLContext db, Access access) throws SQLException {
    if (access.isTopLevel())
      return db.selectFrom(ACCOUNT)
        .fetch();
    else
      return db.selectFrom(ACCOUNT)
        .where(ACCOUNT.ID.in(access.getAccounts()))
        .fetch();
  }

  /**
   Update a record

   @param db     context
   @param access control
   @param id     of record to update
   @param entity to update record with
   @throws BusinessException if a business rule is violated
   */
  private void update(DSLContext db, Access access, ULong id, Account entity) throws BusinessException, DatabaseException {
    requireTopLevel(access);

    entity.validate();

    Map<Field, Object> fieldValues = entity.updatableFieldValueMap();
    fieldValues.put(ACCOUNT.ID, id);

    if (executeUpdate(db, ACCOUNT, fieldValues) == 0)
      throw new BusinessException("No records updated.");
  }

  /**
   Delete an Account

   @param db        context
   @param accountId to delete
   @throws Exception         if database failure
   @throws ConfigException   if not configured properly
   @throws BusinessException if fails business rule
   */
  private void delete(DSLContext db, Access access, ULong accountId) throws Exception {
    requireTopLevel(access);

    requireNotExists("Library in Account", db.select(LIBRARY.ID)
      .from(LIBRARY)
      .where(LIBRARY.ACCOUNT_ID.eq(accountId))
      .fetch());

    requireNotExists("Chain in Account", db.select(CHAIN.ID)
      .from(CHAIN)
      .where(CHAIN.ACCOUNT_ID.eq(accountId))
      .fetch());

    requireNotExists("User in Account", db.select(ACCOUNT_USER.ID)
      .from(ACCOUNT_USER)
      .where(ACCOUNT_USER.ACCOUNT_ID.eq(accountId))
      .fetch());

    db.deleteFrom(ACCOUNT)
      .where(ACCOUNT.ID.eq(accountId))
      .andNotExists(
        db.select(LIBRARY.ID)
          .from(LIBRARY)
          .where(LIBRARY.ACCOUNT_ID.eq(accountId)))
      .andNotExists(
        db.select(CHAIN.ID)
          .from(CHAIN)
          .where(CHAIN.ACCOUNT_ID.eq(accountId)))
      .andNotExists(
        db.select(ACCOUNT_USER.ID)
          .from(ACCOUNT_USER)
          .where(ACCOUNT_USER.ACCOUNT_ID.eq(accountId)))
      .execute();
  }

}
