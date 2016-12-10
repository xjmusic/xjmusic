// Copyright (c) 2016, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.application.server;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.net.URI;

public class HttpServerProviderImpl implements HttpServerProvider {
  @Override
  public HttpServer createHttpServer(URI uri, ResourceConfig config) {
    return GrizzlyHttpServerFactory.createHttpServer(uri, config);
  }
}
