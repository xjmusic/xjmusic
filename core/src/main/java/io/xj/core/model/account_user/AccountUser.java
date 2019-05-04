// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.account_user;

import io.xj.core.exception.CoreException;
import io.xj.core.model.entity.impl.EntityImpl;

import java.math.BigInteger;
import java.util.Objects;

/**
 POJO for persisting data in memory while performing business logic,
 or decoding messages received by JAX-RS resources.
 a.k.a. JSON input will be stored into an instance of this object
 <p>
 Business logic ought to be performed beginning with an instance of this object,
 to implement common methods.
 <p>
 NOTE: There can only be ONE of any getter/setter (with the same # of input params)
 */
public class AccountUser extends EntityImpl {
  public static final String KEY_ONE = "accountUser";
  public static final String KEY_MANY = "accountUsers";
  private BigInteger accountId;
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
  public BigInteger getParentId() {
    return accountId;
  }

  @Override
  public void validate() throws CoreException {
    if (Objects.isNull(accountId)) {
      throw new CoreException("Account ID is required.");
    }
    if (Objects.isNull(userId)) {
      throw new CoreException("User ID is required.");
    }
  }

}
