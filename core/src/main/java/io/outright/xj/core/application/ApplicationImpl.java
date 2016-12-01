// Copyright (c) 2016, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.application;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;

public class ApplicationImpl implements Application {
  static Logger log = LogManager.getLogger(Application.class);

  // Packages of JAX-RS resources and providers
  private final String[] packages;

  // Hostname the Grizzly HTTP server will answer as
  private final String host;

  // Port the Grizzly HTTP server will listen on
  private final int port;

  // Grizzly HTTP server
  private HttpServer server;

  /**
   * Application Constructor
   */
  public ApplicationImpl(
    final String[] _packages
  ) {
    // Use specified packages plus default resource package
    packages = new String[_packages.length+1];
    packages[0] = "io.outright.xj.core.application.resource";
    System.arraycopy(_packages, 0, packages, 1, _packages.length);

    host = System.getProperty("host","localhost");
    port = Integer.parseInt(System.getProperty("port","8080"));
    server = null;
  }

  /**
   * Starts Grizzly HTTP server exposing JAX-RS resources defined in this application.
   */
  @Override
  public void Start() {
    // create a resource config that scans for
    // in io.outright.xj.ship package
    final ResourceConfig rc = new ResourceConfig().packages(packages);

    // create and start a new instance of grizzly http server
    // exposing the Jersey application at BASE_URI
    log.info("Server starting");
    server = GrizzlyHttpServerFactory.createHttpServer(URI.create(BaseURI()), rc);
    log.info("Server started at " + BaseURI());
    log.info("Jersey app started with WADL available at " + BaseURI() + "application.wadl");
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
