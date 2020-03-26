// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.model;

import io.xj.lib.util.Value;
import io.xj.lib.util.ValueException;
import io.xj.service.hub.entity.Entity;

import java.util.UUID;

/**
 POJO for persisting data in memory while performing business logic.
 <p>
 Business logic ought to be performed beginning with an instance of this object,
 to implement common methods.
 <p>
 NOTE: There can only be ONE of any getter/setter (with the same # of input params)
 */
public class UserRole extends Entity {


  private UserRoleType type;
  private UUID userId;
  private Exception typeException;

  /**
   Create a new UserRole

   @param user of UserRole
   @param type of UserRole
   @return UserRole
   */
  public static UserRole create(User user, UserRoleType type) {
    return create()
      .setUserId(user.getId())
      .setTypeEnum(type);
  }

  /**
   Create a new UserRole

   @param user of UserRole
   @param type of UserRole
   @return UserRole
   */
  public static UserRole create(User user, String type) {
    return create()
      .setUserId(user.getId())
      .setType(type);
  }

  /**
   Create a new UserRole

   @return UserRole
   */
  public static UserRole create() {
    return (UserRole) new UserRole().setId(UUID.randomUUID());
  }

  @Override
  public UUID getParentId() {
    return userId;
  }

  /**
   get Type

   @return Type
   */
  public UserRoleType getType() {
    return type;
  }

  /**
   get UserId

   @return User Id
   */
  public UUID getUserId() {
    return userId;
  }

  /**
   set Type

   @param value to set
   @return this UserRole (for chaining methods)
   */
  public UserRole setType(String value) {
    try {
      type = UserRoleType.validate(value);
    } catch (ValueException e) {
      typeException = e;
    }
    return this;
  }

  /**
   set TypeEnum

   @param type to set
   @return this UserRole (for chaining methods)
   */
  public UserRole setTypeEnum(UserRoleType type) {
    this.type = type;
    return this;
  }

  /**
   set UserId

   @param userId to set
   @return this UserRole (for chaining methods)
   */
  public UserRole setUserId(UUID userId) {
    this.userId = userId;
    return this;
  }

  @Override
  public void validate() throws ValueException {
    Value.require(userId, "User ID");
    Value.requireNo(typeException, "Type");
    Value.require(type, "type");
  }
}
