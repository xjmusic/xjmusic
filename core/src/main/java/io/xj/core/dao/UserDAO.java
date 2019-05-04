// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.access.impl.Access;
import io.xj.core.exception.CoreException;
import io.xj.core.model.user.User;
import io.xj.core.model.user_access_token.UserAccessToken;
import io.xj.core.model.user_auth.UserAuth;
import io.xj.core.model.user_auth.UserAuthType;
import io.xj.core.model.user_role.UserRole;
import io.xj.core.model.user_role.UserRoleType;

import java.math.BigInteger;

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
   create a new user and user_auth record
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
  void destroyAllTokens(BigInteger userId) throws CoreException;

  /**
   (ADMIN ONLY)
   Update a specified User's roles, and destroy all their tokens.

   @param userId of specific User to update.
   @param entity for the updated User.
   */
  void updateUserRolesAndDestroyTokens(Access access, BigInteger userId, User entity) throws CoreException;

  /**
   (ADMIN ONLY) read one user access token

   @param access      control
   @param accessToken to read
   @return model
   */
  UserAccessToken readOneAccessToken(Access access, String accessToken) throws CoreException;

  /**
   (ADMIN ONLY) read one user auth

   @param access     control
   @param userAuthId to read
   @return model
   */
  UserAuth readOneAuth(Access access, BigInteger userAuthId) throws CoreException;

  /**
   (ADMIN ONLY) read one user role

   @param access control
   @param userId having role
   @param type   of role
   @return model
   */
  UserRole readOneRole(Access access, BigInteger userId, UserRoleType type) throws CoreException;
}
