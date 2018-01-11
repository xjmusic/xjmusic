// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao.impl;

import io.xj.core.access.impl.Access;
import io.xj.core.dao.AccountUserDAO;
import io.xj.core.exception.BusinessException;
import io.xj.core.exception.ConfigException;
import io.xj.core.model.account_user.AccountUser;
import io.xj.core.persistence.sql.SQLDatabaseProvider;
import io.xj.core.persistence.sql.impl.SQLConnection;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.types.ULong;

import com.google.api.client.util.Maps;
import com.google.inject.Inject;

import java.math.BigInteger;
import java.util.Collection;
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
  public AccountUser create(Access access, AccountUser entity) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(create(tx.getContext(), access, entity));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public AccountUser readOne(Access access, BigInteger id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOne(tx.getContext(), access, ULong.valueOf(id)));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Collection<AccountUser> readAll(Access access, BigInteger accountId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAll(tx.getContext(), access, ULong.valueOf(accountId)));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void delete(Access access, BigInteger id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      delete(tx.getContext(), access, ULong.valueOf(id));
      tx.success();
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  /**
   Create a new Account User record

   @return new record
   @throws Exception         if database failure
   @throws ConfigException   if not configured properly
   @throws BusinessException if fails business rule
    @param db     context
   @param entity for new AccountUser
   */
  private static AccountUser create(DSLContext db, Access access, AccountUser entity) throws Exception {
    entity.validate();

    Map<Field, Object> fieldValues = fieldValueMap(entity);

    requireTopLevel(access);

    if (null != db.selectFrom(ACCOUNT_USER)
      .where(ACCOUNT_USER.ACCOUNT_ID.eq(ULong.valueOf(entity.getAccountId())))
      .and(ACCOUNT_USER.USER_ID.eq(ULong.valueOf(entity.getUserId())))
      .fetchOne())
      throw new BusinessException("Account User already exists!");

    return modelFrom(executeCreate(db, ACCOUNT_USER, fieldValues), AccountUser.class);
  }

  /**
   Read one record

   @return record
    @param db     context
   @param access control
   @param id     of record
   */
  private static AccountUser readOne(DSLContext db, Access access, ULong id) throws BusinessException {
    if (access.isTopLevel())
      return modelFrom(db.selectFrom(ACCOUNT_USER)
        .where(ACCOUNT_USER.ID.eq(id))
        .fetchOne(), AccountUser.class);
    else
      return modelFrom(db.selectFrom(ACCOUNT_USER)
        .where(ACCOUNT_USER.ID.eq(id))
        .and(ACCOUNT_USER.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne(), AccountUser.class);
  }

  /**
   Read all records in parent record

   @param db        context
   @param access    control
   @param accountId of parent
   @return array of child records
   */
  private static Collection<AccountUser> readAll(DSLContext db, Access access, ULong accountId) throws BusinessException {
    if (access.isTopLevel())
      return modelsFrom(db.selectFrom(ACCOUNT_USER)
        .where(ACCOUNT_USER.ACCOUNT_ID.eq(accountId))
        .fetch(), AccountUser.class);
    else
      return modelsFrom(db.selectFrom(ACCOUNT_USER)
        .where(ACCOUNT_USER.ACCOUNT_ID.eq(accountId))
        .and(ACCOUNT_USER.ACCOUNT_ID.in(access.getAccountIds()))
        .fetch(), AccountUser.class);
  }

  /**
   Delete a record

   @param db     context
   @param access control
   @param id     of record
   @throws BusinessException on failure
   */
  private static void delete(DSLContext db, Access access, ULong id) throws BusinessException {
    requireTopLevel(access);
    db.deleteFrom(ACCOUNT_USER)
      .where(ACCOUNT_USER.ID.eq(id))
      .execute();
  }

  /**
   Only certain (writable) fields are mapped back to jOOQ records--
   Read-only fields are excluded from here.

   @param entity to source values from
   @return values mapped to record fields
   */
  private static Map<Field, Object> fieldValueMap(AccountUser entity) {
    Map<Field, Object> fieldValues = Maps.newHashMap();
    fieldValues.put(ACCOUNT_USER.ACCOUNT_ID, ULong.valueOf(entity.getAccountId()));
    fieldValues.put(ACCOUNT_USER.USER_ID, ULong.valueOf(entity.getUserId()));
    return fieldValues;
  }

}
