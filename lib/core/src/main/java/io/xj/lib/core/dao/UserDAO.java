// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.core.dao;

import io.xj.lib.core.access.Access;
import io.xj.lib.core.exception.CoreException;
import io.xj.lib.core.model.User;
import io.xj.lib.core.model.UserAuth;
import io.xj.lib.core.model.UserAuthType;
import io.xj.lib.core.model.UserAuthToken;
import io.xj.lib.core.model.UserRole;
import io.xj.lib.core.model.UserRoleType;

import java.util.UUID;

public interface UserDAO extends DAO<User> {

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
  String authenticate(UserAuthType authType, String account, String externalAccessToken, String externalRefreshToken, String name, String avatarUrl, String email) throws CoreException;

  /**
   (ADMIN ONLY)
   Destroy all access tokens for a specified User

   @param userId to destroy all access tokens for.
   */
  void destroyAllTokens(UUID userId) throws CoreException;

  /**
   (ADMIN ONLY)
   Update a specified User's roles, and destroy all their tokens.

   @param userId of specific User to update.
   @param entity for the updated User.
   */
  void updateUserRolesAndDestroyTokens(Access access, UUID userId, User entity) throws CoreException;

  /**
   (ADMIN ONLY) read one user access token

   @param access      control
   @param accessToken to read
   @return model
   */
  UserAuthToken readOneAuthToken(Access access, String accessToken) throws CoreException;

  /**
   (ADMIN ONLY) read one user auth

   @param access     control
   @param userAuthId to read
   @return model
   */
  UserAuth readOneAuth(Access access, UUID userAuthId) throws CoreException;

  /**
   (ADMIN ONLY) read one user role

   @param access control
   @param userId having role
   @param type   of role
   @return model
   */
  UserRole readOneRole(Access access, UUID userId, UserRoleType type) throws CoreException;
}
