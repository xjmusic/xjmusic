// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.core.access;

import org.glassfish.jersey.server.ResourceConfig;

public interface AccessLogFilterProvider {
  void registerTo(ResourceConfig resourceConfig);
}
