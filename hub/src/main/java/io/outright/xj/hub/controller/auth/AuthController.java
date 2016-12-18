// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.hub.controller.auth;

import io.outright.xj.core.app.exception.AccessException;
import io.outright.xj.core.app.exception.ConfigException;
import io.outright.xj.core.app.exception.DatabaseException;

public interface AuthController {
  /**
   * Authenticate a user by OAuth2 Access Code,
   * store the access token for the remote (authenticating) system
   * and return a token for access to this system.
   *
   * @param accessCode to authenticate against remote system via OAuth2
   * @return token for access to this system.
   * @throws AccessException if user is not authenticated.
   * @throws ConfigException if the application is not configured properly.
   * @throws DatabaseException if database connection(s) fail.
   */
  String authenticate(String accessCode) throws AccessException, ConfigException, DatabaseException;
}
