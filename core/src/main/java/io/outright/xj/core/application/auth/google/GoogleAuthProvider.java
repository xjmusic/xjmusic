// Copyright (c) 2016, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.application.auth.google;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import io.outright.xj.core.application.exception.ConfigException;

import java.io.IOException;

public interface GoogleAuthProvider {
  /**
   * Requires these System Properties to be set:
   *   auth.google.id
   *   auth.google.secret
   *
   * @throws ConfigException if required system properties are not set
   * @return String authorization code request URL
   */
  String getAuthCodeRequestUrl() throws ConfigException;
}
