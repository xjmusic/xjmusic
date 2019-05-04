// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.account_user;

/**
 Wrapper for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an entity inside this object
 */
public class AccountUserWrapper {
  private AccountUser accountUser;

  public AccountUser getAccountUser() {
    return accountUser;
  }

  public AccountUserWrapper setAccountUser(AccountUser accountUser) {
    this.accountUser = accountUser;
    return this;
  }

}
