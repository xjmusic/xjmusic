// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.account_user;

import io.xj.core.exception.BusinessException;
import io.xj.core.model.entity.EntityWrapper;

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
  @Override
  public AccountUser validate() throws BusinessException {
    if (this.accountUser == null) {
      throw new BusinessException("Account User is required.");
    }
    this.accountUser.validate();
    return this.accountUser;
  }

}
