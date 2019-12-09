// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model;

import com.google.common.collect.ImmutableList;
import io.xj.core.entity.Entity;
import io.xj.core.exception.CoreException;

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
  public static final ImmutableList<String> RESOURCE_ATTRIBUTE_NAMES = ImmutableList.<String>builder()
    .addAll(Entity.RESOURCE_ATTRIBUTE_NAMES)
    .build();
  public static final ImmutableList<Class> RESOURCE_BELONGS_TO = ImmutableList.<Class>builder()
    .add(Account.class)
    .add(User.class)
    .build();
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

  @Override
  public ImmutableList<String> getResourceAttributeNames() {
    return RESOURCE_ATTRIBUTE_NAMES;
  }

  @Override
  public ImmutableList<Class> getResourceBelongsTo() {
    return RESOURCE_BELONGS_TO;
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
  public void validate() throws CoreException {
    require(accountId, "Account ID");
    require(userId, "User ID");
  }

}
