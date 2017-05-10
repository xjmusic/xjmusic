// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.core.model.account;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.model.EntityWrapper;

/**
 Wrapper for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an entity inside this object
 */
public class AccountWrapper extends EntityWrapper {

  // Account
  private Account account;

  public Account getAccount() {
    return account;
  }

  public AccountWrapper setAccount(Account account) {
    this.account = account;
    return this;
  }

  /**
   Validate data.

   @throws BusinessException if invalid.
   */
  public Account validate() throws BusinessException {
    if (this.account == null) {
      throw new BusinessException("Account is required.");
    }
    this.account.validate();
    return this.account;
  }

}
