// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.core.app;

import io.outright.xj.core.app.access.AccessLogFilterProvider;
import io.outright.xj.core.app.access.AccessTokenAuthFilter;
import io.outright.xj.core.app.config.Config;
import io.outright.xj.core.app.exception.ConfigException;
import io.outright.xj.core.app.server.HttpServerProvider;
import io.outright.xj.core.app.server.ResourceConfigProvider;
import io.outright.xj.core.app.work.Worker;
import io.outright.xj.core.app.work.Workload;
import io.outright.xj.core.app.work.impl.ChainGangWorkload;
import io.outright.xj.core.app.work.impl.SimpleWorkload;
import io.outright.xj.core.chain_gang.Follower;
import io.outright.xj.core.chain_gang.Leader;

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
  private final static Logger log = LoggerFactory.getLogger(App.class);
  private final HttpServerProvider httpServerProvider;
  private final ResourceConfigProvider resourceConfigProvider;
  private final AccessTokenAuthFilter accessTokenAuthFilter;
  private final AccessLogFilterProvider accessLogFilterProvider;
  private final String host = Config.appHost();
  private final Integer port = Config.appPort();
  private ResourceConfig resourceConfig;
  private List<Workload> workloads = Lists.newArrayList();

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
    if (Config.workConcurrency()>1 || Config.workBatchSize() > 1)
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
    workloads.forEach((Workload::stop));

    log.info("Server will shutdown now");
    if (Objects.nonNull(httpServerProvider.get()))
      httpServerProvider.get().shutdownNow();
    log.info("Server did shutdown OK");
  }

  @Override
  public String baseURI() {
    return "http://" + host + ":" + port + "/";
  }


}
