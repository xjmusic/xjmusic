// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.account_user;

import io.xj.core.exception.BusinessException;
import io.xj.core.model.Entity;

import java.math.BigInteger;

/**
 POJO for persisting data in memory while performing business logic,
or decoding messages received by JAX-RS resources.
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
  private BigInteger accountId;
  // User ID
  private BigInteger userId;

  public BigInteger getAccountId() {
    return accountId;
  }

  public AccountUser setAccountId(BigInteger accountId) {
    this.accountId = accountId;
    return this;
  }

  public BigInteger getUserId() {
    return userId;
  }

  public AccountUser setUserId(BigInteger userId) {
    this.userId = userId;
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

}
