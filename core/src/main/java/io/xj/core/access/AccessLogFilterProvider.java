// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.access;

import org.glassfish.jersey.server.ResourceConfig;

public interface AccessLogFilterProvider {
  void registerTo(ResourceConfig resourceConfig);
}
