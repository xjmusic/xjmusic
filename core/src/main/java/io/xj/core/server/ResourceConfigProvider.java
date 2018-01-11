// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.server;

import org.glassfish.jersey.server.ResourceConfig;

public interface ResourceConfigProvider {
  ResourceConfig get(final String... packages);
}
