// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.user;

import io.xj.core.exception.BusinessException;
import io.xj.core.model.entity.Entity;

import java.math.BigInteger;
import java.util.Objects;

/**
 POJO for persisting data in memory while performing business logic,
 or decoding messages received by JAX-RS resources.
 a.k.a. JSON input will be stored into an instance of this object
 <p>
 Business logic ought to be performed beginning with an instance of this object,
 to implement common methods.
 <p>
 NOTE: There can only be ONE of any getter/setter (with the same # of input params)
 */
public class User extends Entity {

  // JSON output keys
  public static final String KEY_ONE = "user";
  public static final String KEY_MANY = "users";
  public static final String KEY_ROLES = "roles";

  // Roles
  private String roles;
  private String email;
  private String avatarUrl;
  private String name;

  public String getRoles() {
    return roles;
  }

  public User setRoles(String roles) {
    this.roles = roles;
    return this;
  }

  public String getEmail() {
    return email;
  }

  public User setEmail(String email) {
    this.email = email;
    return this;
  }

  public String getAvatarUrl() {
    return avatarUrl;
  }

  public User setAvatarUrl(String avatarUrl) {
    this.avatarUrl = avatarUrl;
    return this;
  }

  public String getName() {
    return name;
  }

  public User setName(String name) {
    this.name = name;
    return this;
  }

  @Override
  public BigInteger getParentId() {
    return new BigInteger(""); // no parent
  }

  @Override
  public void validate() throws BusinessException {
    if (Objects.isNull(getRoles()) || getRoles().isEmpty()) {
      throw new BusinessException("User roles required.");
    }
  }

}
