// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.core.model.user;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.model.EntityWrapper;

/**
 Wrapper for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an entity inside this object
 */
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
   Validate data.

   @throws BusinessException if invalid.
   */
  public User validate() throws BusinessException {
    if (this.user == null) {
      throw new BusinessException("User is required.");
    }
    this.user.validate();
    return this.user;
  }

}
