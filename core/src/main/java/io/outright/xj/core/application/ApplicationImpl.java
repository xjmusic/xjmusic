// Copyright (c) 2016, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.application;

import io.outright.xj.core.application.server.HttpServerProvider;
import io.outright.xj.core.application.server.LogFilterProvider;
import io.outright.xj.core.application.server.ResourceConfigProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import java.io.IOException;
import java.net.URI;

public class ApplicationImpl implements Application {
  private final static Logger log = LoggerFactory.getLogger(Application.class);
  private final HttpServerProvider httpServerProvider;
  private final ResourceConfigProvider resourceConfig;
  private final LogFilterProvider logFilter;

  // Hostname the Grizzly HTTP server will answer as
  private String host = System.getProperty("app.host", "0.0.0.0");

  // Port the Grizzly HTTP server will listen on
  private Integer port = Integer.parseInt(System.getProperty("app.port", "80"));


  @Inject
  public ApplicationImpl(
    HttpServerProvider httpServerProvider,
    ResourceConfigProvider resourceConfig,
    LogFilterProvider logFilter
  ) {
    this.httpServerProvider = httpServerProvider;
    this.resourceConfig = resourceConfig;
    this.logFilter = logFilter;
  }

  /**
   * @param packages    containing JAX-RS resources and providers
   */
  @Override
  public void Configure(
    String[] packages
  ) {
    // Use specified packages plus default resource package
    String[] finalPackages = new String[packages.length + 1];
    finalPackages[0] = "io.outright.xj.core.application.resource";
    System.arraycopy(packages, 0, finalPackages, 1, packages.length);

    // Path to write Access Log [System Property]
    String pathToWriteAccessLog = System.getProperty("log.access.filename", "/tmp/access.log");

    // Access Log Entities can be boolean (log all/none entities) or int (bytes max size entity to log)
    Integer maxSizeEntitiesInAccessLog = Integer.valueOf(System.getProperty("log.access.entities.maxsize", "0"));
    Boolean showEntitiesInAccessLog = Boolean.valueOf(System.getProperty("log.access.entities.all", "false"));

    // scans for all resource classes in packages
    resourceConfig.setup(finalPackages);

    // access log
    log.info("Writing access log to {}", pathToWriteAccessLog);
    try {
      if (maxSizeEntitiesInAccessLog > 0) {
        logFilter.setup(pathToWriteAccessLog, maxSizeEntitiesInAccessLog);
      } else {
        logFilter.setup(pathToWriteAccessLog, showEntitiesInAccessLog);
      }
      resourceConfig.get().register(logFilter.get());
    } catch (IOException e) {
      log.error("Failed to register access log writer", e);
    }
  }


  /**
   * Starts Grizzly HTTP server exposing JAX-RS resources defined in this application.
   */
  @Override
  public void Start() throws IOException {
    log.info("Server starting now");
    httpServerProvider.setup(URI.create(BaseURI()), resourceConfig.get());
    httpServerProvider.get().start();
    log.info("Server up");
  }

  @Override
  public void Stop() {
    log.info("Server shutting down now");
    httpServerProvider.get().shutdownNow();
    log.info("Server gone");
  }

  @Override
  public String BaseURI() {
    return "http://" + host + ":" + port + "/";
  }

}
