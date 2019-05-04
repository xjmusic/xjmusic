// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao.impl;

import com.google.api.client.util.Maps;
import com.google.inject.Inject;
import io.xj.core.access.impl.Access;
import io.xj.core.dao.AccountDAO;
import io.xj.core.exception.CoreException;
import io.xj.core.model.account.Account;
import io.xj.core.persistence.sql.SQLDatabaseProvider;
import io.xj.core.persistence.sql.impl.SQLConnection;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.types.ULong;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.Collection;
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

  /**
   Create a new record

   @param db     context
   @param entity for new record
   @return newly readMany record
   @throws CoreException if a business rule is violated
   */
  private static Account create(DSLContext db, Access access, Account entity) throws CoreException {
    entity.validate();

    Map<Field, Object> fieldValues = fieldMap(entity);

    requireTopLevel(access);

    return modelFrom(executeCreate(db, ACCOUNT, fieldValues), Account.class);
  }

  /**
   Read one record, if accessible

   @param db     context
   @param access control
   @param id     of record
   @return record
   */
  private static Account readOne(DSLContext db, Access access, ULong id) throws CoreException {
    if (access.isTopLevel())
      return modelFrom(db.selectFrom(ACCOUNT)
        .where(ACCOUNT.ID.eq(id))
        .fetchOne(), Account.class);
    else
      return modelFrom(db.select(ACCOUNT.fields())
        .from(ACCOUNT)
        .where(ACCOUNT.ID.eq(id))
        .and(ACCOUNT.ID.in(access.getAccountIds()))
        .fetchOne(), Account.class);
  }

  /**
   Read all records accessible

   @param db     context
   @param access control
   @return array of records
   */
  private static Collection<Account> readAll(DSLContext db, Access access) throws CoreException {
    if (access.isTopLevel())
      return modelsFrom(db.selectFrom(ACCOUNT)
        .fetch(), Account.class);
    else
      return modelsFrom(db.selectFrom(ACCOUNT)
        .where(ACCOUNT.ID.in(access.getAccountIds()))
        .fetch(), Account.class);
  }

  /**
   Update a record

   @param db     context
   @param access control
   @param id     of record to update
   @param entity to update record with
   @throws CoreException if a business rule is violated
   */
  private static void update(DSLContext db, Access access, ULong id, Account entity) throws CoreException {
    requireTopLevel(access);

    entity.validate();

    Map<Field, Object> fieldValues = fieldMap(entity);
    fieldValues.put(ACCOUNT.ID, id);

    if (0 == executeUpdate(db, ACCOUNT, fieldValues))
      throw new CoreException("No records updated.");
  }

  /**
   Delete an Account

   @param db        context
   @param accountId to delete
   @throws CoreException if database failure
   @throws CoreException if not configured properly
   @throws CoreException if fails business rule
   */
  private static void delete(DSLContext db, Access access, ULong accountId) throws CoreException {
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

  /**
   Only certain (writable) fields are mapped back to jOOQ records--
   Read-only fields are excluded from here.

   @param model to source values from
   @return values mapped to record fields
   */
  private static Map<Field, Object> fieldMap(Account model) {
    Map<Field, Object> fieldValues = Maps.newHashMap();
    fieldValues.put(ACCOUNT.NAME, model.getName());
    return fieldValues;
  }

  @Override
  public Account create(Access access, Account entity) throws CoreException {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(create(tx.getContext(), access, entity));
    } catch (CoreException e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public Account readOne(Access access, BigInteger id) throws CoreException {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOne(tx.getContext(), access, ULong.valueOf(id)));
    } catch (CoreException e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public Collection<Account> readAll(Access access, Collection<BigInteger> parentIds) throws CoreException {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAll(tx.getContext(), access));
    } catch (CoreException e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void update(Access access, BigInteger id, Account entity) throws CoreException {
    SQLConnection tx = dbProvider.getConnection();
    try {
      update(tx.getContext(), access, ULong.valueOf(id), entity);
      tx.success();
    } catch (CoreException e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void destroy(Access access, BigInteger id) throws CoreException {
    SQLConnection tx = dbProvider.getConnection();
    try {
      delete(tx.getContext(), access, ULong.valueOf(id));
      tx.success();
    } catch (CoreException e) {
      throw tx.failure(e);
    }
  }

}
