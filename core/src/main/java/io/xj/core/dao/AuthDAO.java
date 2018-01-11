// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.exception.AccessException;
import io.xj.core.exception.ConfigException;

public interface AuthDAO {
  /**
   Authenticate a user by OAuth2 Access Code,
   store the access token for the remote (authenticating) system
   and return a token for access to this system.

   @param accessCode to authenticate against remote system via OAuth2
   @return token for access to this system.
   @throws AccessException if user is not authenticated.
   @throws ConfigException if the application is not configured properly.
   @throws Exception       if database connection(s) fail.
   */
  String authenticate(String accessCode) throws Exception;
}
