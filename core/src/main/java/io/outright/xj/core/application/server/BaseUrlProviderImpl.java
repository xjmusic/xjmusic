// Copyright (c) 2016, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.application.server;

import io.outright.xj.core.application.exception.ConfigException;
import io.outright.xj.core.util.RequiredProperty;

public class BaseUrlProviderImpl implements BaseUrlProvider {
  @Override
  public String getUrl() throws ConfigException {
    return RequiredProperty.get("app.url");
  }
}
