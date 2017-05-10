// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.core.app.access;

import org.glassfish.jersey.server.ResourceConfig;

public interface AccessLogFilterProvider {
  void registerTo(ResourceConfig resourceConfig);
}
