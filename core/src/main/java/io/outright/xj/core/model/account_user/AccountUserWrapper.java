// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.core.model.account_user;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.model.EntityWrapper;

/**
 Wrapper for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an entity inside this object
 */
public class AccountUserWrapper extends EntityWrapper {

  // Account
  private AccountUser accountUser;

  public AccountUser getAccountUser() {
    return accountUser;
  }

  public AccountUserWrapper setAccountUser(AccountUser accountUser) {
    this.accountUser = accountUser;
    return this;
  }

  /**
   Validate data.

   @throws BusinessException if invalid.
   */
  public AccountUser validate() throws BusinessException {
    if (this.accountUser == null) {
      throw new BusinessException("Account User is required.");
    }
    this.accountUser.validate();
    return this.accountUser;
  }

}
