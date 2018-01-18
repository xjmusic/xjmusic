// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.model.user_auth;

import io.xj.core.exception.BusinessException;
import io.xj.core.model.entity.Entity;

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
public class UserAuth extends Entity {
  private UserAuthType type;
  private String externalAccessToken;
  private String externalRefreshToken;
  private String externalAccount;
  private BigInteger userId;
  private String _type; // to hold value before validation

  @Override
  public BigInteger getParentId() {
    return userId;
  }

  @Override
  public void validate() throws BusinessException {
    // throws its own BusinessException on failure
    type = UserAuthType.validate(_type);

    if (Objects.isNull(type)) {
      throw new BusinessException("type is required.");
    }
  }

  public void setTypeEnum(UserAuthType type) {
    this.type = type;
  }

  public void setType(String type) {
    _type = type;
  }

  public String getExternalAccessToken() {
    return externalAccessToken;
  }

  public void setExternalAccessToken(String externalAccessToken) {
    this.externalAccessToken = externalAccessToken;
  }

  public String getExternalRefreshToken() {
    return externalRefreshToken;
  }

  public void setExternalRefreshToken(String externalRefreshToken) {
    this.externalRefreshToken = externalRefreshToken;
  }

  public String getExternalAccount() {
    return externalAccount;
  }

  public void setExternalAccount(String externalAccount) {
    this.externalAccount = externalAccount;
  }

  public BigInteger getUserId() {
    return userId;
  }

  public void setUserId(BigInteger userId) {
    this.userId = userId;
  }

  public UserAuthType getType() {
    return type;
  }
}
