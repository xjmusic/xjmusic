// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.core.model.account_user;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.model.Entity;

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.types.ULong;

import com.google.api.client.util.Maps;

import java.math.BigInteger;
import java.util.Map;
import java.util.Objects;

import static io.outright.xj.core.Tables.ACCOUNT_USER;

/**
 Entity for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an instance of this object

 Business logic ought to be performed beginning with an instance of this object,
 to implement common methods.

 NOTE: There can only be ONE of any getter/setter (with the same # of input params)
 */
public class AccountUser extends Entity {

  /**
   For use in maps.
   */
  public static final String KEY_ONE = "accountUser";
  public static final String KEY_MANY = "accountUsers";
  // Account ID
  private ULong accountId;
  // User ID
  private ULong userId;

  public ULong getAccountId() {
    return accountId;
  }

  public AccountUser setAccountId(BigInteger accountId) {
    this.accountId = ULong.valueOf(accountId);
    return this;
  }

  public ULong getUserId() {
    return userId;
  }

  public AccountUser setUserId(BigInteger userId) {
    this.userId = ULong.valueOf(userId);
    return this;
  }

  @Override
  public void validate() throws BusinessException {
    if (this.accountId == null) {
      throw new BusinessException("Account ID is required.");
    }
    if (this.userId == null) {
      throw new BusinessException("User ID is required.");
    }
  }

  @Override
  public AccountUser setFromRecord(Record record) {
    if (Objects.isNull(record)) {
      return null;
    }
    id = record.get(ACCOUNT_USER.ID);
    accountId = record.get(ACCOUNT_USER.ACCOUNT_ID);
    userId = record.get(ACCOUNT_USER.USER_ID);
    createdAt = record.get(ACCOUNT_USER.CREATED_AT);
    updatedAt = record.get(ACCOUNT_USER.UPDATED_AT);
    return this;
  }

  @Override
  public Map<Field, Object> updatableFieldValueMap() {
    Map<Field, Object> fieldValues = Maps.newHashMap();
    fieldValues.put(ACCOUNT_USER.ACCOUNT_ID, accountId);
    fieldValues.put(ACCOUNT_USER.USER_ID, userId);
    return fieldValues;
  }


}
