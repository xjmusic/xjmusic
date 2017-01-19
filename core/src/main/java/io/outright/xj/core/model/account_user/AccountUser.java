// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.model.account_user;

import io.outright.xj.core.app.exception.BusinessException;

import java.math.BigInteger;

public class AccountUser {

  // Account ID
  private BigInteger accountId;
  public BigInteger getAccountId() {
    return accountId;
  }
  public void setAccountId(BigInteger accountId) {
    this.accountId = accountId;
  }

  // User ID
  private BigInteger userId;
  public BigInteger getUserId() {
    return userId;
  }
  public void setUserId(BigInteger userId) {
    this.userId = userId;
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
