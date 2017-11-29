// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.app;

import io.xj.core.app.access.AccessLogFilterProvider;
import io.xj.core.app.access.AccessTokenAuthFilter;
import io.xj.core.app.config.Config;
import io.xj.core.app.exception.ConfigException;
import io.xj.core.app.server.HttpServerProvider;
import io.xj.core.app.server.ResourceConfigProvider;
import io.xj.core.work.impl.RobustWorkerPool;
import io.xj.core.work.WorkManager;

import com.google.inject.Inject;

import net.greghaines.jesque.worker.JobFactory;
import net.greghaines.jesque.worker.WorkerEvent;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.Objects;
import java.util.concurrent.Executors;

public class AppImpl implements App {
  private static final Logger log = LoggerFactory.getLogger(AppImpl.class);
  private final HttpServerProvider httpServerProvider;
  private final ResourceConfigProvider resourceConfigProvider;
  private final AccessTokenAuthFilter accessTokenAuthFilter;
  private final AccessLogFilterProvider accessLogFilterProvider;
  private ResourceConfig resourceConfig;
  private JobFactory jobFactory;
  private RobustWorkerPool workerPool;
  private final WorkManager workManager;

  @Inject
  public AppImpl(
    HttpServerProvider httpServerProvider,
    ResourceConfigProvider resourceConfigProvider,
    AccessLogFilterProvider accessLogFilterProvider,
    AccessTokenAuthFilter accessTokenAuthFilter,
    WorkManager workManager
  ) {
    this.httpServerProvider = httpServerProvider;
    this.resourceConfigProvider = resourceConfigProvider;
    this.accessLogFilterProvider = accessLogFilterProvider;
    this.accessTokenAuthFilter = accessTokenAuthFilter;
    this.workManager = workManager;
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
  public void setJobFactory(JobFactory jobFactory) {
    this.jobFactory = jobFactory;
  }

  @Override
  public WorkManager getWorkManager() {
    return workManager;
  }

  /**
   Starts Grizzly HTTP server
   exposing JAX-RS resources defined in this application.
   */
  @Override
  public void start() throws IOException, ConfigException {
    if (null == resourceConfig) {
      throw new ConfigException("Failed to app.start(); must configureServer() first!");
    }

    if (Objects.nonNull(jobFactory)) {
      workerPool = new RobustWorkerPool(() -> workManager.getWorker(jobFactory),
        Config.workConcurrency(), Executors.defaultThreadFactory());

      // Handle errors from the worker pool
      workerPool.getWorkerEventEmitter().addListener(
        (event, worker, queue, errorJob, runner, result, t) -> {
          log.error("Worker Pool: event: {}, worker: {}, queue: {}, errorJob: {}, runner: {}, result: {}, t: {}", event, worker, queue, errorJob, runner, result, t);
        }, WorkerEvent.WORKER_ERROR
      );

      workerPool.run();
    }

    URI serverURI = URI.create(baseURI());
    log.info("Server starting now at {}", serverURI);
    httpServerProvider.configure(serverURI, resourceConfig);
    httpServerProvider.get().start();
    log.info("Server up");
  }

  @Override
  public void stop() {
    log.info("Server will shutdown now");

    if (Objects.nonNull(workerPool)) {
      workerPool.end(false);
    }

    if (Objects.nonNull(httpServerProvider.get()))
      httpServerProvider.get().shutdownNow();

    log.info("Server did shutdown OK");
  }

  @Override
  public String baseURI() {
    return "http://" + Config.appHost() + ":" + Config.appPort() + "/";
  }


}
