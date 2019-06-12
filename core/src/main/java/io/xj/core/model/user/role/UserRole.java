// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.user.role;

import io.xj.core.exception.CoreException;
import io.xj.core.model.entity.impl.EntityImpl;

import java.math.BigInteger;
import java.util.Objects;

/**
 POJO for persisting data in memory while performing business logic.
 <p>
 Business logic ought to be performed beginning with an instance of this object,
 to implement common methods.
 <p>
 NOTE: There can only be ONE of any getter/setter (with the same # of input params)
 */
public class UserRole extends EntityImpl {
  private static final String RESOURCE_TYPE = "user-roles";
  private UserRoleType type;
  private BigInteger userId;
  private Exception typeException;

  @Override
  public BigInteger getParentId() {
    return userId;
  }

  @Override
  public String getResourceType() {
    return RESOURCE_TYPE;
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
  public BigInteger getUserId() {
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
    } catch (CoreException e) {
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
  public UserRole setUserId(BigInteger userId) {
    this.userId = userId;
    return this;
  }

  @Override
  public UserRole validate() throws CoreException {
    if (Objects.nonNull(typeException))
      throw new CoreException("Invalid type value.", typeException);

    require(type, "type");

    return this;
  }
}
