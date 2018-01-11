// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.access;

import io.xj.core.access.impl.Access;
import io.xj.core.exception.AccessException;
import io.xj.core.exception.DatabaseException;
import io.xj.core.model.account_user.AccountUser;
import io.xj.core.model.user_auth.UserAuth;
import io.xj.core.model.user_role.UserRole;

import javax.ws.rs.core.NewCookie;
import java.util.Collection;
import java.util.Map;

public interface AccessControlProvider {
  /**
   Create a token to grant a user access to resources.

   @return access token
    @param userAuth         to create a token for.
   @param userAccountRoles accounts that this user has access to.
   @param userRoles        roles that this user has access to.
   */
  String create(UserAuth userAuth, Collection<AccountUser> userAccountRoles, Collection<UserRole> userRoles) throws AccessException;

  /**
   Update an access token to grant a user access to resources.

   @return map of cached properties for this user
    @param userAuth         to create a token for.
   @param userAccountRoles accounts that this user has access to.
   @param userRoles        roles that this user has access to.
   */
  Map<String, String> update(String accessToken, UserAuth userAuth, Collection<AccountUser> userAccountRoles, Collection<UserRole> userRoles) throws AccessException;

  /**
   Expire an access token.

   @param token to expire.
   */
  void expire(String token) throws DatabaseException;

  /**
   Fetch a token to determine if it is valid,
   and what user it grants access to.

   @param token to fetch.
   @return User who is granted access by this token
   @throws DatabaseException if something goes wrong with storage access.
   */
  Access get(String token) throws DatabaseException;

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
}
