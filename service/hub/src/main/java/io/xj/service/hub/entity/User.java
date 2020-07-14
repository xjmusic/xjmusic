// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.entity;

import io.xj.lib.entity.Entity;
import io.xj.lib.util.Value;
import io.xj.lib.util.ValueException;

import java.util.UUID;

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


  private String roles;
  private String email;
  private String avatarUrl;
  private String name;

  /**
   Create a new User

   @return new User
   */
  public static User create() {
    return new User().setId(UUID.randomUUID());
  }

  /**
   Create a new User

   @param name      of User
   @param email     of User
   @param avatarUrl of User
   @return new User
   */
  public static User create(String name, String email, String avatarUrl) {
    return create()
      .setName(name)
      .setEmail(email)
      .setAvatarUrl(avatarUrl);
  }

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

  /**
   get Roles

   @return Roles
   */
  public String getRoles() {
    return roles;
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
  public User setId(UUID id) {
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
  public void validate() throws ValueException {
    Value.require(roles, "User roles");
  }

}
