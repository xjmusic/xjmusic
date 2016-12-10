// Copyright (c) 2016, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.application.server;

import org.glassfish.jersey.server.ResourceConfig;

public class ResourceConfigProviderImpl implements ResourceConfigProvider {
  @Override
  public final ResourceConfig createResourceConfig(final String... packages) {
    return new ResourceConfig().packages(packages);
  }
}
