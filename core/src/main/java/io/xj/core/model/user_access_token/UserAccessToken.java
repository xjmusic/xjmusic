// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.user_access_token;

import io.xj.core.exception.CoreException;
import io.xj.core.model.entity.impl.EntityImpl;

import java.math.BigInteger;

public class UserAccessToken extends EntityImpl {
  private BigInteger userAuthId;
  private BigInteger userId;

  @Override
  public BigInteger getParentId() {
    return userId;
  }

  @Override
  public void validate() throws CoreException {

  }

  public BigInteger getUserAuthId() {
    return userAuthId;
  }

  public void setUserAuthId(BigInteger userAuthId) {
    this.userAuthId = userAuthId;
  }

  public BigInteger getUserId() {
    return userId;
  }

  public void setUserId(BigInteger userId) {
    this.userId = userId;
  }
}
