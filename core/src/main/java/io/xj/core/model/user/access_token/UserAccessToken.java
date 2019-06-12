// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.user.access_token;

import com.google.common.collect.ImmutableList;
import io.xj.core.exception.CoreException;
import io.xj.core.model.entity.impl.EntityImpl;
import io.xj.core.model.instrument.Instrument;

import java.math.BigInteger;

/**
 *
 */
public class UserAccessToken extends EntityImpl {
  private BigInteger userAuthId;
  private BigInteger userId;
  private String accessToken;

  /**
   get AccessToken

   @return AccessToken
   */
  public String getAccessToken() {
    return accessToken;
  }

  @Override
  public ImmutableList<Class> getResourceBelongsTo() {
    return ImmutableList.<Class>builder()
      .addAll(super.getResourceBelongsTo())
      .add(Instrument.class)
      .build();
  }

  @Override
  public BigInteger getParentId() {
    return userId;
  }

  /**
   get UserAuthId

   @return UserAuthId
   */
  public BigInteger getUserAuthId() {
    return userAuthId;
  }

  /**
   get UserId

   @return UserId
   */
  public BigInteger getUserId() {
    return userId;
  }

  /**
   set AccessToken

   @param accessToken to set
   @return this UserAccessToken (for chaining methods)
   */
  public UserAccessToken setAccessToken(String accessToken) {
    this.accessToken = accessToken;
    return this;
  }

  /**
   set UserAuthId

   @param userAuthId to set
   @return this UserAccessToken (for chaining methods)
   */
  public UserAccessToken setUserAuthId(BigInteger userAuthId) {
    this.userAuthId = userAuthId;
    return this;
  }

  /**
   set UserId

   @param userId to set
   @return this UserAccessToken (for chaining methods)
   */
  public UserAccessToken setUserId(BigInteger userId) {
    this.userId = userId;
    return this;
  }

  @Override
  public UserAccessToken validate() throws CoreException {
    require(accessToken, "Access token");
    require(userId, "User ID");
    require(userAuthId, "UserAuth ID");
    return this;
  }
}
