// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.app.server;

import org.glassfish.jersey.server.ResourceConfig;

public interface ResourceConfigProvider {
  ResourceConfig get(final String... packages);
}
