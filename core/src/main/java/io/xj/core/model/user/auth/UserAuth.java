// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.user.auth;

import com.google.common.collect.ImmutableList;
import io.xj.core.exception.CoreException;
import io.xj.core.model.entity.impl.EntityImpl;
import io.xj.core.model.user.User;

import java.math.BigInteger;

/**
 POJO for persisting data in memory while performing business logic.
 <p>
 Business logic ought to be performed beginning with an instance of this object,
 to implement common methods.
 <p>
 NOTE: There can only be ONE of any getter/setter (with the same # of input params)
 */
public class UserAuth extends EntityImpl {
  private UserAuthType type;
  private String externalAccessToken;
  private String externalRefreshToken;
  private String externalAccount;
  private BigInteger userId;
  private Exception typeException;

  /**
   get ExternalAccessToken

   @return ExternalAccessToken
   */
  public String getExternalAccessToken() {
    return externalAccessToken;
  }

  /**
   get ExternalAccount

   @return ExternalAccount
   */
  public String getExternalAccount() {
    return externalAccount;
  }

  /**
   get ExternalRefreshToken

   @return ExternalRefreshToken
   */
  public String getExternalRefreshToken() {
    return externalRefreshToken;
  }

  @Override
  public BigInteger getParentId() {
    return userId;
  }

  /**
   get Type

   @return Type
   */
  public UserAuthType getType() {
    return type;
  }

  /**
   get UserId

   @return UserId
   */
  public BigInteger getUserId() {
    return userId;
  }

  /**
   set ExternalAccessToken

   @param externalAccessToken to set
   @return this UserAuth (for chaining methods)
   */
  public UserAuth setExternalAccessToken(String externalAccessToken) {
    this.externalAccessToken = externalAccessToken;
    return this;
  }

  /**
   set ExternalAccount

   @param externalAccount to set
   @return this UserAuth (for chaining methods)
   */
  public UserAuth setExternalAccount(String externalAccount) {
    this.externalAccount = externalAccount;
    return this;
  }

  /**
   set ExternalRefreshToken

   @param externalRefreshToken to set
   @return this UserAuth (for chaining methods)
   */
  public UserAuth setExternalRefreshToken(String externalRefreshToken) {
    this.externalRefreshToken = externalRefreshToken;
    return this;
  }

  @Override
  public ImmutableList<Class> getResourceBelongsTo() {
    return ImmutableList.<Class>builder()
      .addAll(super.getResourceBelongsTo())
      .add(User.class)
      .build();
  }

  /**
   Set type

   @param type to set
   @return this UserAuth (for chaining methods)
   */
  public UserAuth setType(String type) {
    try {
      this.type = UserAuthType.validate(type);
    } catch (CoreException e) {
      typeException = e;
    }
    return this;
  }

  /**
   set TypeEnum

   @param type to set
   @return this UserAuth (for chaining methods)
   */
  public UserAuth setTypeEnum(UserAuthType type) {
    this.type = type;
    return this;
  }

  /**
   set UserId

   @param userId to set
   @return this UserAuth (for chaining methods)
   */
  public UserAuth setUserId(BigInteger userId) {
    this.userId = userId;
    return this;
  }

  @Override
  public UserAuth validate() throws CoreException {
    requireNo(typeException, "Type");
    require(type, "type");
    return this;
  }
}
