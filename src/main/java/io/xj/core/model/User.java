// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model;

import com.google.common.collect.ImmutableList;
import io.xj.core.entity.Entity;
import io.xj.core.exception.CoreException;

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
  public static final ImmutableList<String> RESOURCE_ATTRIBUTE_NAMES = ImmutableList.<String>builder()
    .addAll(Entity.RESOURCE_ATTRIBUTE_NAMES)
    .add("name")
    .add("roles")
    .add("email")
    .add("avatarUrl")
    .build();
  public static final ImmutableList<Class> RESOURCE_BELONGS_TO = ImmutableList.<Class>builder()
    .build();
  public static final ImmutableList<Class> RESOURCE_HAS_MANY = ImmutableList.<Class>builder()
    .add(UserAuth.class)
    .add(UserAuthToken.class)
    .build();
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

  @Override
  public ImmutableList<String> getResourceAttributeNames() {
    return RESOURCE_ATTRIBUTE_NAMES;
  }

  @Override
  public ImmutableList<Class> getResourceBelongsTo() {
    return RESOURCE_BELONGS_TO;
  }

  @Override
  public ImmutableList<Class> getResourceHasMany() {
    return RESOURCE_HAS_MANY;
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
  public void validate() throws CoreException {
    require(roles, "User roles");
  }

}
