// Copyright (c) 2016, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.application.auth.google;

import io.outright.xj.core.application.exception.ConfigException;

public interface GoogleOAuth2Credentials {
  String getClientId() throws ConfigException;
  String getClientSecret() throws ConfigException;
  String getEmail() throws ConfigException;
}
