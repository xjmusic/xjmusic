// Copyright (c) 2016, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.app.access;

import org.glassfish.jersey.server.ResourceConfig;

public interface AccessLogFilterProvider {
  void registerTo(ResourceConfig resourceConfig);
}
