// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.model.user;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.model.EntityWrapper;

import org.jooq.Record;

public class UserWrapper extends EntityWrapper {

  // User
  private User user;
  public User getUser() {
    return user;
  }
  public UserWrapper setUser(User user) {
    this.user = user;
    return this;
  }

  /**
   * Validate data.
   * @throws BusinessException if invalid.
   */
  public void validate() throws BusinessException{
    if (this.user == null) {
      throw new BusinessException("User is required.");
    }
    this.user.validate();
  }

  @Override
  public String toString() {
    return "{" +
      User.KEY_ONE + ":" + this.user +
      "}";
  }

  public Record intoRecord() {
    return null;
  }
}
