// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.user_role;

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
  private UserRoleType type;
  private BigInteger userId;
  private String _type; // to hold value before validation

  @Override
  public BigInteger getParentId() {
    return userId;
  }

  @Override
  public void validate() throws CoreException {
    // throws its own CoreException on failure
    type = UserRoleType.validate(_type);

    if (Objects.isNull(type)) {
      throw new CoreException("type is required.");
    }
  }

  public void setTypeEnum(UserRoleType type) {
    this.type = type;
  }

  public UserRoleType getType() {
    return type;
  }

  public void setType(String value) {
    _type = value;
  }

  public BigInteger getUserId() {
    return userId;
  }

  public void setUserId(BigInteger userId) {
    this.userId = userId;
  }
}
