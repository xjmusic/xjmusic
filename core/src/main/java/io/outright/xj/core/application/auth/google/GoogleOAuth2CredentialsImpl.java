// Copyright (c) 2016, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.application.auth.google;

import io.outright.xj.core.application.exception.ConfigException;
import io.outright.xj.core.util.RequiredProperty;

public class GoogleOAuth2CredentialsImpl implements GoogleOAuth2Credentials {
  @Override
  public String getClientId() throws ConfigException {
    return RequiredProperty.get("auth.google.id");
  }

  @Override
  public String getClientSecret() throws ConfigException {
    return RequiredProperty.get("auth.google.secret");
  }
}
