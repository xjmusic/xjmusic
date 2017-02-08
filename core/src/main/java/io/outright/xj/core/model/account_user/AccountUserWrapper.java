// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.model.account_user;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.model.EntityWrapper;

import org.jooq.Record;

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
   * Validate data.
   * @throws BusinessException if invalid.
   */
  public void validate() throws BusinessException{
    if (this.accountUser == null) {
      throw new BusinessException("Account User is required.");
    }
    this.accountUser.validate();
  }

  @Override
  public String toString() {
    return "{" +
      AccountUser.KEY_ONE + ":" + this.accountUser +
      "}";
  }

  public Record intoRecord() {
    return null;
  }
}
