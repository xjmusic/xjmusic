// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.model.account_user;

import io.outright.xj.core.app.exception.BusinessException;

import org.jooq.Field;

import com.google.common.collect.ImmutableMap;

import java.math.BigInteger;
import java.util.Map;

import static io.outright.xj.core.Tables.ACCOUNT_USER;
import static io.outright.xj.core.Tables.IDEA;

public class AccountUser {

  // Account ID
  private BigInteger accountId;
  public BigInteger getAccountId() {
    return accountId;
  }
  public AccountUser setAccountId(BigInteger accountId) {
    this.accountId = accountId;
    return this;
  }

  // User ID
  private BigInteger userId;
  public BigInteger getUserId() {
    return userId;
  }
  public AccountUser setUserId(BigInteger userId) {
    this.userId = userId;
    return this;
  }

  /**
   * Validate data.
   *
   * @throws BusinessException if invalid.
   */
  void validate() throws BusinessException {
    if (this.accountId == null) {
      throw new BusinessException("Account ID is required.");
    }
    if (this.userId == null) {
      throw new BusinessException("User ID is required.");
    }
  }

  /**
   * Model info jOOQ-field : Value map
   * @return map
   */
  public Map<Field, Object> intoFieldValueMap() {
    return new ImmutableMap.Builder<Field, Object>()
      .put(ACCOUNT_USER.ACCOUNT_ID, accountId)
      .put(ACCOUNT_USER.USER_ID, userId)
      .build();
  }

  @Override
  public String toString() {
    return "{" +
      "userId:" + this.userId +
      "accountId:" + this.accountId +
      "}";
  }

  /**
   * For use in maps.
   */
  public static final String KEY_ONE = "accountUser";
  public static final String KEY_MANY = "accountUsers";


}
