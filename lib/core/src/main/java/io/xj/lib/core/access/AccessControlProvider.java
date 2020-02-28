// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.core.access;

import io.xj.lib.core.exception.CoreException;
import io.xj.lib.core.model.AccountUser;
import io.xj.lib.core.model.UserAuth;
import io.xj.lib.core.model.UserRole;

import javax.ws.rs.core.NewCookie;
import java.util.Collection;
import java.util.Map;

public interface AccessControlProvider {
  /**
   Create a token to grant a user access to resources.

   @param userAuth         to of a token for.
   @param accountUsers accounts that this user has access to.
   @param userRoles        roles that this user has access to.
   @return access token
   */
  String create(UserAuth userAuth, Collection<AccountUser> accountUsers, Collection<UserRole> userRoles) throws CoreException;

  /**
   Update an access token to grant a user access to resources.

   @param userAuth         to of a token for.
   @param userAccountRoles accounts that this user has access to.
   @param userRoles        roles that this user has access to.
   @return map of cached properties for this user
   */
  Map<String, String> update(String accessToken, UserAuth userAuth, Collection<AccountUser> userAccountRoles, Collection<UserRole> userRoles) throws CoreException;

  /**
   Expire an access token.

   @param token to expire.
   */
  void expire(String token) throws CoreException;

  /**
   Fetch a token to determine if it is valid,
   and what user it grants access to.

   @param token to fetch.
   @return User who is granted access by this token
   @throws CoreException if something goes wrong with storage access.
   */
  Access get(String token) throws CoreException;

  /**
   Create a new cookie to set access token.

   @param accessToken to set a cookie for.
   @return new cookie to set access token.
   */
  NewCookie newCookie(String accessToken);

  /**
   Create a new cookie to set an expired access token.

   @return new cookie to set expired access token.
   */
  NewCookie newExpiredCookie();

  /**
   Authenticate a user by OAuth2 Access Code,
   store the access token for the remote (authenticating) system
   and return a token for access to this system.

   @param accessCode to authenticate against remote system via OAuth2
   @return token for access to this system.
   @throws CoreException if user is not authenticated.
   @throws CoreException if the application is not configured properly.
   @throws Exception     if database connection(s) fail.
   */
  String authenticate(String accessCode) throws Exception;
}
