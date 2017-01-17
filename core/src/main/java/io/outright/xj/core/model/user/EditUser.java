// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.model.user;

import io.outright.xj.core.app.exception.BusinessException;

public class EditUser {

  User user;

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  @Override
  public String toString() {
    return "{\"user\":\"" + this.user + "\"}";
  }

  /**
   * Get User Roles, or throw BusinessException
   * @return roles
   * @throws BusinessException if unacceptable input
   */
  public String getUserRoles() throws BusinessException {
    if (user == null) {
      throw new BusinessException("No user specified");
    }
    return user.getRoles();
  }

  public class User {
    String roles;

    public String getRoles() {
      return roles;
    }

    public void setRoles(String roles) {
      this.roles = roles;
    }

    @Override
    public String toString() {
      return "{\"roles\":\"" + this.roles + "\"}";
    }
  }

}
