// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.app;

import io.outright.xj.core.app.access.AccessTokenAuthFilter;
import io.outright.xj.core.app.config.Config;
import io.outright.xj.core.app.exception.ConfigException;
import io.outright.xj.core.app.server.HttpServerProvider;
import io.outright.xj.core.app.access.AccessLogFilterProvider;
import io.outright.xj.core.app.server.ResourceConfigProvider;

import com.google.inject.Inject;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;

public class AppImpl implements App {
  private final static Logger log = LoggerFactory.getLogger(App.class);
  private final HttpServerProvider httpServerProvider;
  private final ResourceConfigProvider resourceConfigProvider;
  private final AccessTokenAuthFilter accessTokenAuthFilter;
  private final AccessLogFilterProvider accessLogFilterProvider;

  private final String host = Config.appHost();
  private final Integer port = Config.appPort();

  private ResourceConfig resourceConfig;

  @Inject
  public AppImpl(
    HttpServerProvider httpServerProvider,
    ResourceConfigProvider resourceConfigProvider,
    AccessLogFilterProvider accessLogFilterProvider,
    AccessTokenAuthFilter accessTokenAuthFilter
  ) {
    this.httpServerProvider = httpServerProvider;
    this.resourceConfigProvider = resourceConfigProvider;
    this.accessLogFilterProvider = accessLogFilterProvider;
    this.accessTokenAuthFilter = accessTokenAuthFilter;
  }

  /**
   * @param packages containing JAX-RS resources and providers
   */
  @Override
  public void configure(
    String[] packages
  ) {
    // Use specified packages plus default resource package
    String[] finalPackages = new String[packages.length + 1];
    finalPackages[0] = "io.outright.xj.core.app.resource";
    System.arraycopy(packages, 0, finalPackages, 1, packages.length);
    for (String finalPackage : finalPackages) {
      log.info("Resources: " + finalPackage);
    }

    // scans for all resource classes in packages
    resourceConfig = resourceConfigProvider.get(finalPackages);

    // access log only registers if file succeeds to open for writing
    accessLogFilterProvider.registerTo(resourceConfig);

    // access control filter
    resourceConfig.register(accessTokenAuthFilter);
  }

  /**
   * Starts Grizzly HTTP server exposing JAX-RS resources defined in this application.
   */
  @Override
  public void start() throws IOException, ConfigException {
    if (resourceConfig==null) {
      throw new ConfigException("Failed to app.start(); must configure() first!");
    }
    log.info("Server starting now");
    httpServerProvider.configure(URI.create(baseURI()), resourceConfig);
    httpServerProvider.get().start();
    log.info("Server up");
  }

  @Override
  public void stop() {
    log.info("Server shutting down now");
    httpServerProvider.get().shutdownNow();
    log.info("Server gone");
  }

  @Override
  public String baseURI() {
    return "http://" + host + ":" + port + "/";
  }

}
