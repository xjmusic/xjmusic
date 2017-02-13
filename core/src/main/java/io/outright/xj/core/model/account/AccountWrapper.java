// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.model.account;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.model.EntityWrapper;

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
   * Validate data.
   *
   * @throws BusinessException if invalid.
   */
  public Account validate() throws BusinessException {
    if (this.account == null) {
      throw new BusinessException("Account is required.");
    }
    this.account.validate();
    return this.account;
  }

  @Override
  public String toString() {
    return "{" +
      Account.KEY_ONE + ":" + this.account +
      "}";
  }
}
