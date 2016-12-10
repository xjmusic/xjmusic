// Copyright (c) 2016, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.application;

import io.outright.xj.core.application.server.LogFilterProvider;
import io.outright.xj.core.application.server.HttpServerProvider;
import io.outright.xj.core.application.server.ResourceConfigProvider;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.server.ResourceConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;

public class ApplicationImpl implements Application {
  private final static Logger log = LoggerFactory.getLogger(Application.class);
  private final String[] packages;
  private final String host;
  private final Integer port;
  private final Integer maxSizeEntitiesInAccessLog;
  private final String pathToWriteAccessLog;
  private final Boolean showEntitiesInAccessLog;
  private final HttpServerProvider httpServerProvider;
  private final ResourceConfigProvider resourceConfigProvider;
  private final LogFilterProvider logFilterProvider;
  private HttpServer server = null;

  /**
   * ApplicationImpl Constructor
   *
   * @param packages    of JAX-RS resources and providers
   * @param defaultPort to serve HTTP endpoints
   */
  public ApplicationImpl(
    HttpServerProvider httpServerProvider,
    ResourceConfigProvider resourceConfigProvider,
    LogFilterProvider logFilterProvider,
    final String[] packages,
    int defaultPort
  ) {
    this.httpServerProvider = httpServerProvider;
    this.resourceConfigProvider = resourceConfigProvider;
    this.logFilterProvider = logFilterProvider;

    // Use specified packages plus default resource package
    this.packages = new String[packages.length + 1];
    this.packages[0] = "io.outright.xj.core.application.resource";
    System.arraycopy(packages, 0, this.packages, 1, packages.length);

    // Path to write Access Log [System Property]
    pathToWriteAccessLog = System.getProperty("log.access.filename", "/tmp/access.log");

    // Access Log Entities can be boolean (log all/none entities) or int (bytes max size entity to log)
    maxSizeEntitiesInAccessLog = Integer.valueOf(System.getProperty("log.access.entities.maxsize", "0"));
    showEntitiesInAccessLog = Boolean.valueOf(System.getProperty("log.access.entities.all", "false"));

    // Hostname the Grizzly HTTP server will answer as
    host = System.getProperty("app.host", "0.0.0.0");

    // Port the Grizzly HTTP server will listen on
    port = Integer.parseInt(System.getProperty("app.port", String.valueOf(defaultPort)));
  }

  /**
   * Starts Grizzly HTTP server exposing JAX-RS resources defined in this application.
   */
  @Override
  public void Start() {
    // create a resource config that scans for
    // in io.outright.xj.ship package
    final ResourceConfig config = resourceConfigProvider.createResourceConfig(packages);

    // access log
    log.info("Writing access log to {}", pathToWriteAccessLog);
    try {
      LoggingFilter loggingFilter;
      if (maxSizeEntitiesInAccessLog > 0) {
        loggingFilter = logFilterProvider.newFilter(pathToWriteAccessLog, maxSizeEntitiesInAccessLog);
      } else {
        loggingFilter = logFilterProvider.newFilter(pathToWriteAccessLog, showEntitiesInAccessLog);
      }
      config.register(loggingFilter);
    } catch (IOException e) {
      log.error("Failed to register access log writer", e);
    }

    // create a new instance of grizzly http server
    // exposing the Jersey application at BASE_URI
    log.info("Server starting now");
    server = httpServerProvider.createHttpServer(URI.create(BaseURI()), config);
    try {
      server.start();
    } catch (IOException e) {
      log.error("Failed to start server", e);
    }
    log.info("Server up");
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
