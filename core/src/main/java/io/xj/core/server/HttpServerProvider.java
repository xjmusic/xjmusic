// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.server;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.server.ResourceConfig;

import java.net.URI;

public interface HttpServerProvider {
  void configure(URI uri, ResourceConfig config);

  HttpServer get();
}
