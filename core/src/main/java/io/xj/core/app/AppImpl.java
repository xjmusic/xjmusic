// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.app;

import io.xj.core.app.access.AccessLogFilterProvider;
import io.xj.core.app.access.AccessTokenAuthFilter;
import io.xj.core.app.config.Config;
import io.xj.core.app.exception.ConfigException;
import io.xj.core.app.server.HttpServerProvider;
import io.xj.core.app.server.ResourceConfigProvider;
import io.xj.core.work.Worker;
import io.xj.core.work.Workload;
import io.xj.core.work.impl.ChainGangWorkload;
import io.xj.core.work.impl.SimpleWorkload;
import io.xj.core.chain_gang.Follower;
import io.xj.core.chain_gang.Leader;

import com.google.api.client.util.Lists;
import com.google.inject.Inject;

import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Objects;

public class AppImpl implements App {
  private static final Logger log = LoggerFactory.getLogger(AppImpl.class);
  private final HttpServerProvider httpServerProvider;
  private final ResourceConfigProvider resourceConfigProvider;
  private final AccessTokenAuthFilter accessTokenAuthFilter;
  private final AccessLogFilterProvider accessLogFilterProvider;
  private ResourceConfig resourceConfig;
  private final List<Workload> workloads = Lists.newArrayList();

  @Inject
  public AppImpl(
    HttpServerProvider httpServerProvider,
    ResourceConfigProvider resourceConfigProvider,
    AccessLogFilterProvider accessLogFilterProvider,
    AccessTokenAuthFilter accessTokenAuthFilter
  ) throws ConfigException {
    this.httpServerProvider = httpServerProvider;
    this.resourceConfigProvider = resourceConfigProvider;
    this.accessLogFilterProvider = accessLogFilterProvider;
    this.accessTokenAuthFilter = accessTokenAuthFilter;
  }

  @Override
  public void configureServer(
    String[] packages
  ) {
    // Use specified packages plus default resource package
    String[] finalPackages = new String[packages.length + 1];
    finalPackages[0] = "io.xj.core.app.resource";
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

  @Override
  public void registerGangWorkload(String name, Leader leader, Follower follower) throws ConfigException {
    workloads.add(new ChainGangWorkload(name, leader, follower));
  }

  @Override
  public void registerSimpleWorkload(String name, Worker worker) throws ConfigException {
    workloads.add(new SimpleWorkload(name, worker));
  }

  /**
   Starts Grizzly HTTP server
   exposing JAX-RS resources defined in this application.
   */
  @Override
  public void start() throws IOException, ConfigException {
    if (resourceConfig == null) {
      throw new ConfigException("Failed to app.start(); must configureServer() first!");
    }

    // [#276] Chain work not safe for concurrent or batch use; link state transition errors should not cause chain failure
    if (Config.workConcurrency() > 1 || Config.workBatchSize() > 1)
      throw new ConfigException("Current XJ implementation is not safe for concurrent or batch use! See [#286] https://trello.com/c/KpoQOse1 true work management. After that implementation, concurrent and batch work should be safe.");

    URI serverURI = URI.create(baseURI());
    log.info("Server starting now at {}", serverURI);
    httpServerProvider.configure(serverURI, resourceConfig);
    httpServerProvider.get().start();
    log.info("Server up");

    workloads.forEach((workload -> {
      try {
        workload.start();
      } catch (ConfigException e) {
        log.error("Starting workload {}", workload, e);
      }
    }));
  }

  @Override
  public void stop() {
    log.info("Server will shutdown now");

    workloads.forEach((Workload::stop));

    if (Objects.nonNull(httpServerProvider.get()))
      httpServerProvider.get().shutdownNow();

    log.info("Server did shutdown OK");
  }

  @Override
  public String baseURI() {
    return "http://" + Config.appHost() + ":" + Config.appPort() + "/";
  }


}
