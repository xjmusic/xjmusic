// Copyright (c) 2016, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.application.server;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.server.ResourceConfig;

import java.net.URI;

public interface ResourceConfigProvider {
  void setup(final String... packages);
  ResourceConfig get();
}
