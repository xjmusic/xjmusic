// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.server;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import org.glassfish.jersey.server.ResourceConfig;

public class ResourceConfigProviderImpl implements ResourceConfigProvider {
  @Override
  public ResourceConfig get(final String... packages) {
    final ResourceConfig resourceConfig = new ResourceConfig().packages(packages);
    resourceConfig.register(JacksonJsonProvider.class);

    return resourceConfig;
  }
}
