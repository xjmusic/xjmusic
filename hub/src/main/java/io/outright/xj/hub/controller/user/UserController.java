// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.hub.controller.user;

import io.outright.xj.core.app.exception.AccessException;
import io.outright.xj.core.app.exception.ConfigException;
import io.outright.xj.core.app.exception.DatabaseException;
import io.outright.xj.core.tables.records.UserRecord;

import org.jooq.types.ULong;

import javax.annotation.Nullable;

public interface UserController {
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
   * @return UserRecord confirmed authenticated
   * @throws ConfigException if authentication fails internally
   */
  String authenticate(String authType, String account, String externalAccessToken, String externalRefreshToken, String name, String avatarUrl, String email) throws DatabaseException, AccessException, ConfigException;

  /**
   * Fetch one User by id
   *
   * @param userId to fetch.
   * @return UserRecord.
   */
  @Nullable
  UserRecord fetchOneUser(ULong userId);

  /**
   * Destroy all access tokens for a specified user
   *
   * @param userId to destroy all access tokens for.
   * @return Boolean if any tokens were found and destroyed
   */
  void destroyAllTokens(ULong userId) throws AccessException;
}
