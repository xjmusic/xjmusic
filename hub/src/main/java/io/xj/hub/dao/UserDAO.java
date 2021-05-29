// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.dao;

import com.google.common.collect.Lists;
import io.xj.User;
import io.xj.UserAuth;
import io.xj.UserAuthToken;
import io.xj.UserRole;
import io.xj.hub.access.HubAccess;
import io.xj.lib.util.CSV;
import io.xj.lib.util.Text;
import io.xj.lib.util.ValueException;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

public interface UserDAO extends DAO<User> {

  /**
   cast string to enum

   @param value to cast to enum
   @return enum
   @throws ValueException on failure
   */
  static UserRole.Type validateUserRoleType(String value) throws ValueException {
    if (Objects.isNull(value))
      throw new ValueException("Role is required");

    try {
      return UserRole.Type.valueOf(Text.toProperSlug(value));
    } catch (Exception ignored) {
      throw new ValueException("'" + value + "' is not a valid role (" + CSV.joinEnum(UserRole.Type.values()) + ").");
    }
  }

  /**
   extract a collection of ids of a string CSV

   @param csv to parse
   @return collection of ids
   */
  static Collection<UserRole.Type> userRoleTypesFromCsv(String csv) {
    Collection<UserRole.Type> result = Lists.newArrayList();

    if (Objects.nonNull(csv) && !csv.isEmpty()) {
      result = CSV.split(csv).stream().map(type -> UserRole.Type.valueOf(Text.toProperSlug(type))).collect(Collectors.toList());
    }

    return result;
  }

  /**
   Get CSV of a collection of user role types

   @param roleTypes to get CSV of
   @return CSV of user role types
   */
  static String csvOfUserRoleTypes(Collection<UserRole.Type> roleTypes) {
    return CSV.join(roleTypes.stream().map(Enum::toString).collect(Collectors.toList()));
  }

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
  String authenticate(UserAuth.Type authType, String account, String externalAccessToken, String externalRefreshToken, String name, String avatarUrl, String email) throws DAOException;

  /**
   (ADMIN ONLY)
   Destroy all access tokens for a specified User

   @param userId to destroy all access tokens for.
   */
  void destroyAllTokens(String userId) throws DAOException;

  /**
   (ADMIN ONLY)
   Update a specified User's roles, and destroy all their tokens.

   @param userId of specific User to update.
   @param entity for the updated User.
   */
  void updateUserRolesAndDestroyTokens(HubAccess hubAccess, String userId, User entity) throws DAOException, ValueException;

  /**
   (ADMIN ONLY) read one user hubAccess token

   @param hubAccess   control
   @param accessToken to read
   @return model
   */
  UserAuthToken readOneAuthToken(HubAccess hubAccess, String accessToken) throws DAOException;

  /**
   (ADMIN ONLY) read one user auth

   @param hubAccess  control
   @param userAuthId to read
   @return model
   */
  UserAuth readOneAuth(HubAccess hubAccess, String userAuthId) throws DAOException;

  /**
   (ADMIN ONLY) read one user role

   @param hubAccess control
   @param userId    having role
   @param type      of role
   @return model
   */
  UserRole readOneRole(HubAccess hubAccess, String userId, UserRole.Type type) throws DAOException;
}
