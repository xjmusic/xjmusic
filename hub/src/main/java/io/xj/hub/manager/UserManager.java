// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.manager;

import io.xj.hub.access.HubAccess;
import io.xj.hub.access.HubAccessException;
import io.xj.hub.enums.UserAuthType;
import io.xj.hub.tables.pojos.AccountUser;
import io.xj.hub.tables.pojos.User;
import io.xj.hub.tables.pojos.UserAuth;
import io.xj.hub.tables.pojos.UserAuthToken;

import javax.servlet.http.Cookie;
import java.util.Collection;
import java.util.UUID;

public interface UserManager extends Manager<User> {

  /**
   * Authenticates a User using external credentials:
   * <p>
   * 1. Select existing UserAuth by type + account
   * <p>
   * a. If user_auth exists for this account,
   * retrieve its user record and return the user
   * <p>
   * b. if no user_auth exists for this account,
   * of a new user and user_auth record
   * (storing access_token and refresh_token),
   * and return the user
   *
   * @param authType             of external auth
   * @param account              identifier in external system
   * @param externalAccessToken  for OAuth2 access
   * @param externalRefreshToken for refreshing OAuth2 access
   * @param name                 to call user
   * @param avatarUrl            to display for user
   * @param email                to contact user
   * @return access token
   */
  String authenticate(UserAuthType authType, String account, String externalAccessToken, String externalRefreshToken, String name, String avatarUrl, String email) throws ManagerException;

  /**
   * (ADMIN ONLY)
   * Destroy all access tokens for a specified User@param userId to destroy all access tokens for.
   */
  void destroyAllTokens(UUID userId) throws ManagerException;

  /**
   * (ADMIN ONLY) read one user access token
   *
   * @param access      control
   * @param accessToken to read
   * @return model
   */
  UserAuthToken readOneAuthToken(HubAccess access, String accessToken) throws ManagerException;

  /**
   * (ADMIN ONLY) read one user auth
   *
   * @param access     control
   * @param userAuthId to read
   * @return model
   */
  UserAuth readOneAuth(HubAccess access, UUID userAuthId) throws ManagerException;

  /**
   * Create a token to grant a user access to resources.
   *
   * @param user         roles that this user has access to.
   * @param userAuth     to of a token for.
   * @param accountUsers accounts that this user has access to.
   * @return access token
   */
  String create(User user, UserAuth userAuth, Collection<AccountUser> accountUsers) throws HubAccessException;

  /**
   * Expire an access token.
   *
   * @param token to expire.
   */
  void expire(String token) throws HubAccessException;

  /**
   * Fetch a token to determine if it is valid,
   * and what user it grants access to.
   *
   * @param token to fetch.
   * @return User who is granted access by this token
   * @throws HubAccessException if something goes wrong with storage access.
   */
  HubAccess get(String token) throws HubAccessException;

  /**
   * Create a new cookie to set access token.
   *
   * @param accessToken to set a cookie for.
   * @return new cookie to set access token.
   */
  Cookie newCookie(String accessToken);

  /**
   * Create a new cookie to set an expired access token.
   *
   * @return new cookie to set expired access token.
   */
  Cookie newExpiredCookie();

  /**
   * Authenticate a user by OAuth2 HubAccess Code,
   * store the access token for the remote (authenticating) system
   * and return a token for access to this system.
   *
   * @param accessCode to authenticate against remote system via OAuth2
   * @return token for access to this system.
   * @throws HubAccessException if user is not authenticated.
   * @throws HubAccessException if the application is not configured properly.
   * @throws Exception          if database connection(s) fail.
   */
  String authenticate(String accessCode) throws Exception;

  /**
   * Compute the Redis session persistence key for any given token (it's prefixed to have a xj session namespace)
   *
   * @param token to compute key of
   * @return key for token
   */
  String computeKey(String token);
}
