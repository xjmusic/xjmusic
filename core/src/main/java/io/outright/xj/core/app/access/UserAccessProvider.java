// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.app.access;

import io.outright.xj.core.app.exception.AccessException;
import io.outright.xj.core.app.exception.DatabaseException;
import io.outright.xj.core.tables.records.AccountUserRoleRecord;
import io.outright.xj.core.tables.records.UserAuthRecord;
import io.outright.xj.core.tables.records.UserRoleRecord;

import javax.ws.rs.core.NewCookie;
import java.util.Collection;

public interface UserAccessProvider {
  /**
   * Create a token to grant a user access to resources.
   *
   * @param userAuthRecord to create a token for.
   * @param userRoleRecords roles that this user has access to.
   * @param userAccountRoleRecords accounts that this user has access to.
   * @return access token
   */
  String create(UserAuthRecord userAuthRecord, Collection<AccountUserRoleRecord> userAccountRoleRecords, Collection<UserRoleRecord> userRoleRecords) throws AccessException;

  /**
   * Expire an access token.
   *
   * @param token to expire.
   */
  void expire(String token) throws DatabaseException;

  /**
   * Fetch a token to determine if it is valid,
   * and what user it grants access to.
   *
   * @param token to fetch.
   * @return User who is granted access by this token
   * @throws DatabaseException if something goes wrong with storage access.
   */
  UserAccess get(String token) throws DatabaseException;

  /**
   * Create a new cookie to set access token.
   *
   * @param accessToken to set a cookie for.
   * @return new cookie to set access token.
   */
  NewCookie newCookie(String accessToken);

  /**
   * Create a new cookie to set an expired access token.
   *
   * @return new cookie to set expired access token.
   */
  NewCookie newExpiredCookie();
}
