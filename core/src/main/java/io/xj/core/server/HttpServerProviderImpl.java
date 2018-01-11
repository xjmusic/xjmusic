// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.server;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.net.URI;

public class HttpServerProviderImpl implements HttpServerProvider {
  private static HttpServer instance;

  @Override
  public void configure(URI uri, ResourceConfig config) {
    instance = GrizzlyHttpServerFactory.createHttpServer(uri, config);
  }

  @Override
  public HttpServer get() {
    return instance;
  }
}
