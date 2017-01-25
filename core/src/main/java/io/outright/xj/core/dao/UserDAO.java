// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.dao;

import io.outright.xj.core.app.exception.AccessException;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.app.exception.ConfigException;
import io.outright.xj.core.app.exception.DatabaseException;
import io.outright.xj.core.model.user.UserWrapper;

import org.jooq.types.ULong;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.annotation.Nullable;

public interface UserDAO {
  /**
   * Authenticates a User using external credentials:
   *
   *   1. Select existing UserAuth by type + account
   *
   *     a. If user_auth exists for this account,
   *        retrieve its user record and return the user
   *
   *     b. if no user_auth exists for this account,
   *        create a new user and user_auth record
   *        (storing access_token and refresh_token),
   *        and return the user
   *
   * @param authType of external auth
   * @param account identifier in external system
   * @param externalAccessToken for OAuth2 access
   * @param externalRefreshToken for refreshing OAuth2 access
   * @param name to call user
   * @param avatarUrl to display for user
   * @param email to contact user
   * @return access token
   */
  String authenticate(String authType, String account, String externalAccessToken, String externalRefreshToken, String name, String avatarUrl, String email) throws DatabaseException, AccessException;

  /**
   * Fetch one User by id
   *
   * @param userId to fetch.
   * @return User as JSON object
   */
  @Nullable
  JSONObject readOne(ULong userId) throws DatabaseException;

  /**
   * Fetch one User by id
   *
   * @param fromUserId of User from which user must be visible
   * @param userId to fetch
   * @return User if found and visible, as JSON object
   * @throws DatabaseException on failure
   */
  @Nullable
  JSONObject readOneVisible(ULong fromUserId, ULong userId) throws DatabaseException;

  /**
   * Read all Users and their roles
   *
   * @return Users as JSON array.
   */
  @Nullable
  JSONArray readAll() throws DatabaseException;

  /**
   * Read Users visible-to the specified user, and their roles
   *
   * @param fromUserId to whom resulting-users are visible.
   * @return Users as JSON array.
   */
  @Nullable
  JSONArray readAllVisible(ULong fromUserId) throws DatabaseException;

  /**
   * Destroy all access tokens for a specified User
   *
   * @param userId to destroy all access tokens for.
   */
  void destroyAllTokens(ULong userId) throws ConfigException, DatabaseException;

  /**
   * Update a specified User's roles, and destroy all their tokens.
   * @param userId of specific User to update.
   * @param data for the updated User.
   */
  void updateUserRolesAndDestroyTokens(ULong userId, UserWrapper data) throws DatabaseException, ConfigException, BusinessException;

}
