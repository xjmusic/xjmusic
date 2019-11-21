// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.transport;

import org.glassfish.jersey.server.ResourceConfig;

public interface ResourceConfigProvider {
  ResourceConfig get(final String... packages);
}
