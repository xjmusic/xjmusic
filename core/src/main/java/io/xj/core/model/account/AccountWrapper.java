// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.account;

/**
 Wrapper for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an entity inside this object
 */
public class AccountWrapper {
  private Account account;

  public Account getAccount() {
    return account;
  }

  public AccountWrapper setAccount(Account account) {
    this.account = account;
    return this;
  }
}
