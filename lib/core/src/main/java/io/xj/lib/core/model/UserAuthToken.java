// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.core.model;

import com.google.common.collect.ImmutableList;
import io.xj.lib.core.entity.Entity;
import io.xj.lib.core.exception.CoreException;

import java.util.UUID;

/**
 *
 */
public class UserAuthToken extends Entity {
  public static final ImmutableList<String> RESOURCE_ATTRIBUTE_NAMES = ImmutableList.<String>builder()
    .addAll(Entity.RESOURCE_ATTRIBUTE_NAMES)
    .add("accessToken")
    .build();
  public static final ImmutableList<Class> RESOURCE_BELONGS_TO = ImmutableList.<Class>builder()
    .add(User.class)
    .add(UserAuth.class)
    .build();
  private UUID userId;
  private UUID userAuthId;
  private String accessToken;

  /**
   Insert UserAuthToken to database

   @param userAuth to get token of
   @return userAccessToken
   */
  public static UserAuthToken create(UserAuth userAuth, String accessToken) {
    return UserAuthToken.create()
      .setUserId(userAuth.getUserId())
      .setUserAuthId(userAuth.getId())
      .setAccessToken(accessToken);
  }

  /**
   Creaet a new Platform Message

   @return new Platform Message
   */
  public static UserAuthToken create() {
    return (UserAuthToken) new UserAuthToken().setId(UUID.randomUUID());
  }


  @Override
  public ImmutableList<String> getResourceAttributeNames() {
    return RESOURCE_ATTRIBUTE_NAMES;
  }

  @Override
  public ImmutableList<Class> getResourceBelongsTo() {
    return RESOURCE_BELONGS_TO;
  }

  /**
   get AccessToken

   @return AccessToken
   */
  public String getAccessToken() {
    return accessToken;
  }


  @Override
  public UUID getParentId() {
    return userId;
  }

  /**
   get UserAuthId

   @return UserAuthId
   */
  public UUID getUserAuthId() {
    return userAuthId;
  }

  /**
   get UserId

   @return UserId
   */
  public UUID getUserId() {
    return userId;
  }

  /**
   set AccessToken

   @param accessToken to set
   @return this UserAuthToken (for chaining methods)
   */
  public UserAuthToken setAccessToken(String accessToken) {
    this.accessToken = accessToken;
    return this;
  }

  /**
   set UserAuthId

   @param userAuthId to set
   @return this UserAuthToken (for chaining methods)
   */
  public UserAuthToken setUserAuthId(UUID userAuthId) {
    this.userAuthId = userAuthId;
    return this;
  }

  /**
   set UserId

   @param userId to set
   @return this UserAuthToken (for chaining methods)
   */
  public UserAuthToken setUserId(UUID userId) {
    this.userId = userId;
    return this;
  }

  @Override
  public void validate() throws CoreException {
    require(userId, "User ID");
    require(userAuthId, "UserAuth ID");

    require(accessToken, "Access token");
  }
}
