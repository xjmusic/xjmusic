// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.transport;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.server.ResourceConfig;

import java.net.URI;

public interface HttpServerProvider {
  void configure(URI uri, ResourceConfig config);

  HttpServer get();
}
