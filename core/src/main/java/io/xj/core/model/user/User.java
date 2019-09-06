// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.user;

import com.google.common.collect.ImmutableList;
import io.xj.core.exception.CoreException;
import io.xj.core.model.entity.impl.EntityImpl;
import io.xj.core.model.user.access_token.UserAccessToken;
import io.xj.core.model.user.auth.UserAuth;

import java.math.BigInteger;

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
public class User extends EntityImpl {
  private String roles;
  private String email;
  private String avatarUrl;
  private String name;

  /**
   get AvatarUrl

   @return AvatarUrl
   */
  public String getAvatarUrl() {
    return avatarUrl;
  }

  /**
   get Email

   @return Email
   */
  public String getEmail() {
    return email;
  }

  /**
   get Name

   @return Name
   */
  public String getName() {
    return name;
  }

  @Override
  public ImmutableList<String> getResourceAttributeNames() {
    return ImmutableList.<String>builder()
      .addAll(super.getResourceAttributeNames())
      .add("name")
      .add("roles")
      .add("email")
      .add("avatarUrl")
      .build();
  }

  @Override
  public ImmutableList<Class> getResourceHasMany() {
    return ImmutableList.<Class>builder()
      .addAll(super.getResourceHasMany())
      .add(UserAuth.class)
      .add(UserAccessToken.class)
      .build();
  }

  /**
   get Roles

   @return Roles
   */
  public String getRoles() {
    return roles;
  }

  @Override
  public BigInteger getParentId() {
    return new BigInteger(""); // no parent
  }

  /**
   set AvatarUrl

   @param avatarUrl to set
   @return this User (for chaining methods)
   */
  public User setAvatarUrl(String avatarUrl) {
    this.avatarUrl = avatarUrl;
    return this;
  }

  /**
   set Email

   @param email to set
   @return this User (for chaining methods)
   */
  public User setEmail(String email) {
    this.email = email;
    return this;
  }

  /**
   set Id

   @param id to set
   @return this User (for chaining methods)
   */
  public User setId(BigInteger id) {
    this.id = id;
    return this;
  }

  /**
   set Name

   @param name to set
   @return this User (for chaining methods)
   */
  public User setName(String name) {
    this.name = name;
    return this;
  }

  /**
   set Roles

   @param roles to set
   @return this User (for chaining methods)
   */
  public User setRoles(String roles) {
    this.roles = roles;
    return this;
  }

  @Override
  public User validate() throws CoreException {
    require(roles, "User roles");
    return this;
  }

}
