// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.hub_client;

import jakarta.annotation.Nullable;

import java.util.UUID;

public class HubClientAccess {
  @Nullable
  String token = null;
  @Nullable
  UUID userId = null;
  @Nullable
  UUID userAuthId = null;

  /**
   Construct an HubClientAccess model
   */
  public HubClientAccess() {
  }

  /**
   Set User Id

   @param userId to set
   @return this HubClientAccess (for chaining setters)
   */
  public HubClientAccess setUserId(@Nullable UUID userId) {
    this.userId = userId;
    return this;
  }

  /**
   Set UserAuth ID

   @param userAuthId to set
   @return this HubClientAccess (for chaining setters)
   */
  public HubClientAccess setUserAuthId(@Nullable UUID userAuthId) {
    this.userAuthId = userAuthId;
    return this;
  }

  /**
   Get the access token string

   @return access token string
   */
  @Nullable
  public String getToken() {
    return token;
  }

  /**
   Set the access token string

   @param token to set
   @return this HubClientAccess (for chaining setters)
   */
  public HubClientAccess setToken(@Nullable String token) {
    this.token = token;
    return this;
  }

}
