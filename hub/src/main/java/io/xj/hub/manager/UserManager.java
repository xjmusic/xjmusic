// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.manager;

import io.xj.hub.access.HubAccess;
import io.xj.hub.enums.UserAuthType;
import io.xj.hub.tables.pojos.User;
import io.xj.hub.tables.pojos.UserAuth;
import io.xj.hub.tables.pojos.UserAuthToken;

import java.util.UUID;

public interface UserManager extends Manager<User> {

  /**
   Authenticates a User using external credentials:
   <p>
   1. Select existing UserAuth by type + account
   <p>
   a. If user_auth exists for this account,
   retrieve its user record and return the user
   <p>
   b. if no user_auth exists for this account,
   of a new user and user_auth record
   (storing access_token and refresh_token),
   and return the user

   @param authType             of external auth
   @param account              identifier in external system
   @param externalAccessToken  for OAuth2 access
   @param externalRefreshToken for refreshing OAuth2 access
   @param name                 to call user
   @param avatarUrl            to display for user
   @param email                to contact user
   @return access token
   */
  String authenticate(UserAuthType authType, String account, String externalAccessToken, String externalRefreshToken, String name, String avatarUrl, String email) throws ManagerException;

  /**
   (ADMIN ONLY)
   Destroy all access tokens for a specified User@param userId to destroy all access tokens for.
   */
  void destroyAllTokens(UUID userId) throws ManagerException;

  /**
   (ADMIN ONLY) read one user access token

   @param access   control
   @param accessToken to read
   @return model
   */
  UserAuthToken readOneAuthToken(HubAccess access, String accessToken) throws ManagerException;

  /**
   (ADMIN ONLY) read one user auth

   @param access  control
   @param userAuthId to read
   @return model
   */
  UserAuth readOneAuth(HubAccess access, UUID userAuthId) throws ManagerException;
}
