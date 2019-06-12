//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.account;

import com.google.common.collect.ImmutableList;
import io.xj.core.exception.CoreException;
import io.xj.core.model.entity.impl.EntityImpl;
import io.xj.core.model.user.User;

import java.math.BigInteger;

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
  private BigInteger accountId;
  private BigInteger userId;

  /**
   Get Account ID

   @return Account ID
   */
  public BigInteger getAccountId() {
    return accountId;
  }

  @Override
  public BigInteger getParentId() {
    return accountId;
  }

  @Override
  public ImmutableList<Class> getResourceBelongsTo() {
    return ImmutableList.<Class>builder()
      .addAll(super.getResourceBelongsTo())
      .add(Account.class)
      .add(User.class)
      .build();
  }

  /**
   Get User ID

   @return User ID
   */
  public BigInteger getUserId() {
    return userId;
  }

  /**
   set Account ID

   @param accountId to set
   @return this AccountUser (for chaining methods)
   */
  public AccountUser setAccountId(BigInteger accountId) {
    this.accountId = accountId;
    return this;
  }

  /**
   set User ID

   @param userId to set
   @return this AccountUser (for chaining methods)
   */
  public AccountUser setUserId(BigInteger userId) {
    this.userId = userId;
    return this;
  }

  @Override
  public AccountUser validate() throws CoreException {
    require(accountId, "Account ID");
    require(userId, "User ID");
    return this;
  }

}
