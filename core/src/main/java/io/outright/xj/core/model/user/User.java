// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.model.user;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.model.Entity;

import org.jooq.Field;

import com.google.api.client.util.Maps;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

public class User extends Entity {

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
  public void validate() throws BusinessException {
    if (this.getRoles() == null || this.getRoles().length() == 0) {
      throw new BusinessException("User roles required.");
    }
  }

  /**
   * Model info jOOQ-field : Value map
   * @return map
   */
  @Override
  public Map<Field, Object> intoFieldValueMap() {
    return Maps.newHashMap();
  }

  /**
   * For use in maps.
   */
  public static final String KEY_ONE = "user";
  public static final String KEY_MANY = "users";

}
