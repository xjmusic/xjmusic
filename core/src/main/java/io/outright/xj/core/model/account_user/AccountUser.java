// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.model.account_user;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.model.Entity;

import org.jooq.Field;
import org.jooq.types.ULong;

import com.google.api.client.util.Maps;

import java.math.BigInteger;
import java.util.Map;

import static io.outright.xj.core.Tables.ACCOUNT_USER;

public class AccountUser extends Entity {

  // Account ID
  private BigInteger accountId;

  public ULong getAccountId() {
    return ULong.valueOf(accountId);
  }

  public AccountUser setAccountId(BigInteger accountId) {
    this.accountId = accountId;
    return this;
  }

  // User ID
  private BigInteger userId;

  public ULong getUserId() {
    return ULong.valueOf(userId);
  }

  public AccountUser setUserId(BigInteger userId) {
    this.userId = userId;
    return this;
  }

  /**
   Validate data.

   @throws BusinessException if invalid.
   */
  public void validate() throws BusinessException {
    if (this.accountId == null) {
      throw new BusinessException("Account ID is required.");
    }
    if (this.userId == null) {
      throw new BusinessException("User ID is required.");
    }
  }

  /**
   Model info jOOQ-field : Value map

   @return map
   */
  public Map<Field, Object> intoFieldValueMap() {
    Map<Field, Object> fieldValues = Maps.newHashMap();
    fieldValues.put(ACCOUNT_USER.ACCOUNT_ID, accountId);
    fieldValues.put(ACCOUNT_USER.USER_ID, userId);
    return fieldValues;
  }

  /**
   For use in maps.
   */
  public static final String KEY_ONE = "accountUser";
  public static final String KEY_MANY = "accountUsers";


}
