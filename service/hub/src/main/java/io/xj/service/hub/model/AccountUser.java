// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.model;

import io.xj.lib.util.Value;
import io.xj.lib.util.ValueException;
import io.xj.service.hub.entity.Entity;

import java.util.UUID;

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
public class AccountUser extends Entity {
  private UUID accountId;
  private UUID userId;

  /**
   Create an AccountUser

   @param account of AccountUser
   @param user    of AccountUser
   @return new AccountUser
   */
  public static AccountUser create(Account account, User user) {
    return create()
      .setAccountId(account.getId())
      .setUserId(user.getId());
  }

  /**
   Create an AccountUser

   @return new AccountUser
   */
  public static AccountUser create() {
    return (AccountUser) new AccountUser()
      .setId(UUID.randomUUID());
  }

  /**
   Get Account ID

   @return Account ID
   */
  public UUID getAccountId() {
    return accountId;
  }

  @Override
  public UUID getParentId() {
    return accountId;
  }

  /**
   Get User ID

   @return User ID
   */
  public UUID getUserId() {
    return userId;
  }

  /**
   set Account ID

   @param accountId to set
   @return this AccountUser (for chaining methods)
   */
  public AccountUser setAccountId(UUID accountId) {
    this.accountId = accountId;
    return this;
  }

  /**
   set User ID

   @param userId to set
   @return this AccountUser (for chaining methods)
   */
  public AccountUser setUserId(UUID userId) {
    this.userId = userId;
    return this;
  }

  @Override
  public void validate() throws ValueException {
    Value.require(accountId, "Account ID");
    Value.require(userId, "User ID");
  }

}
