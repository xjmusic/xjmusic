// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.model.user;

import io.outright.xj.core.app.exception.BusinessException;

public class User {

  // Roles
  private String roles;

  public String getRoles() {
    return roles;
  }

  public User setRoles(String roles) {
    this.roles = roles;
    return this;
  }

  /**
   * Validate data.
   *
   * @throws BusinessException if invalid.
   */
  void validate() throws BusinessException {
    if (this.getRoles() == null || this.getRoles().length() == 0) {
      throw new BusinessException("User roles required.");
    }
  }

  @Override
  public String toString() {
    return "{" +
      "roles:" + this.roles +
      "}";
  }

  /**
   * For use in maps.
   */
  public static final String KEY_ONE = "user";
  public static final String KEY_MANY = "users";

}
