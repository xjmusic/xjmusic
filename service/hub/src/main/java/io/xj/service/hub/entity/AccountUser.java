// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.entity;

import com.google.common.collect.Lists;
import io.xj.lib.entity.Entity;
import io.xj.lib.util.Value;
import io.xj.lib.util.ValueException;

import java.util.Collection;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

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
   extract collection of account ids of collection of account users

   @param accountUsers to get account ids of
   @return collection of account ids
   */
  public static Collection<UUID> accountIdsFromAccountUsers(Collection<AccountUser> accountUsers) {
    Collection<UUID> result = Lists.newArrayList();

    if (Objects.nonNull(accountUsers) && !accountUsers.isEmpty()) {
      result = accountUsers.stream().map(AccountUser::getAccountId).collect(Collectors.toList());
    }

    return result;
  }

  /**
   Get Account ID

   @return Account ID
   */
  public UUID getAccountId() {
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
