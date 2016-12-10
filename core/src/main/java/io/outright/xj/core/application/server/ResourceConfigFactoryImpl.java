// Copyright (c) 2016, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.application.server;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.net.URI;

public class ResourceConfigFactoryImpl implements ResourceConfigFactory {
  @Override
  public final ResourceConfig createResourceConfig(final String... packages) {
    return new ResourceConfig().packages(packages);
  }
}
