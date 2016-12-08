// Copyright (c) 2016, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.application;

import java.io.IOException;
import java.net.URI;

import io.outright.xj.core.application.logger.FileLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.server.ResourceConfig;

public class ApplicationImpl implements Application {
  private final static Logger log = LoggerFactory.getLogger(Application.class);

  // Packages of JAX-RS resources and providers
  private final String[] packages;

  // Hostname the Grizzly HTTP server will answer as
  private final String host;

  // Port the Grizzly HTTP server will listen on
  private final int port;

  // Path to write access.log of Grizzly HTTP server
  private final String pathToWriteAccessLog;

  // Grizzly HTTP server
  private HttpServer server = null;

  /**
   * Application Constructor
   */
  public ApplicationImpl(
    final String[] _packages,
    int defaultPort
  ) {
    // Use specified packages plus default resource package
    packages = new String[_packages.length+1];
    packages[0] = "io.outright.xj.core.application.resource";
    System.arraycopy(_packages, 0, packages, 1, _packages.length);

    // Parse system properties
    pathToWriteAccessLog = System.getProperty("log.access.filename","/tmp/access.log");
    host = System.getProperty("app.host","0.0.0.0");
    port = Integer.parseInt(System.getProperty("app.port",String.valueOf(defaultPort)));
  }

  /**
   * Starts Grizzly HTTP server exposing JAX-RS resources defined in this application.
   */
  @Override
  public void Start() {
    // create a resource config that scans for
    // in io.outright.xj.ship package
    final ResourceConfig config = new ResourceConfig().packages(packages);

    // access log
    log.info("Writing access log to {}", pathToWriteAccessLog);
    try {
      config.register(new LoggingFilter(FileLogger.getLogger(Application.class, pathToWriteAccessLog), true));
    } catch (IOException e) {
      log.error("Failed to register access log writer", e);
    }

    // create a new instance of grizzly http server
    // exposing the Jersey application at BASE_URI
    log.info("STARTING");
    server = GrizzlyHttpServerFactory.createHttpServer(URI.create(BaseURI()), config);
    log.info("UP");
  }

  @Override
  public void Stop() {
    log.info("Server shutting down now");
    server.shutdownNow();
    log.info("Server gone");
  }

  @Override
  public String BaseURI() {
    return "http://" + host + ":" + port + "/";
  }

}
