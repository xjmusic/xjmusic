// Copyright (c) 2016, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.app.server;

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
