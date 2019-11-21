// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.transport.impl;

import io.xj.core.transport.ResourceConfigProvider;

import org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJsonProvider;
import org.glassfish.jersey.server.ResourceConfig;

public class ResourceConfigProviderImpl implements ResourceConfigProvider {
  @Override
  public ResourceConfig get(String... packages) {
    ResourceConfig resourceConfig = new ResourceConfig().packages(packages);
    resourceConfig.register(JacksonJsonProvider.class);

    return resourceConfig;
  }
}
