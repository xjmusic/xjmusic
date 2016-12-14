// Copyright (c) 2016, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.application.server;

import io.outright.xj.core.application.exception.ConfigException;

public interface BaseUrlProvider {
  String getUrl() throws ConfigException;
}
