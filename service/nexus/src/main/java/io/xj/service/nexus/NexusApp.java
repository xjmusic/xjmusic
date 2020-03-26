// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus;

import com.google.common.collect.ImmutableList;
import com.google.inject.Injector;
import com.typesafe.config.Config;
import io.xj.lib.app.App;
import io.xj.lib.app.AppException;
import io.xj.lib.rest_api.PayloadFactory;
import io.xj.lib.util.Text;
import io.xj.service.hub.HubApp;
import io.xj.service.hub.access.Access;
import io.xj.service.hub.dao.PlatformMessageDAO;
import io.xj.service.hub.entity.MessageType;
import io.xj.service.hub.model.PlatformMessage;
import io.xj.service.hub.persistence.SQLDatabaseProvider;
import io.xj.service.hub.work.RobustWorkerPool;
import io.xj.service.hub.work.WorkManager;
import net.greghaines.jesque.worker.JobFactory;
import net.greghaines.jesque.worker.WorkerEvent;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executors;

/**
 Base application for XJ services.
 <p>
 USAGE
 <p>
 + Create a Guice injector that will be used throughout the entire application, by means of:
 - Creating an application with new App(pathToConfigFile, resourcePackages, injector) <-- pass in Guice injector
 - Making that injector available to Jersey2-based resources for their injection
 - Ensuring all classes within the application are injected via their constructors (NOT creating another injector)
 - ensuring all classes rely on factory and provider modules (NOT creating another injector)
 <p>
 + Accept one runtime argument, pointing to the location of a TypeSafe config
 - ingest that configuration and make it available throughout the application
 <p>
 + Configure Jersey server resources
 <p>
 + Call application start()
 - Add shutdown hook that calls application stop()
 */
public class NexusApp extends App {
  private final org.slf4j.Logger log = LoggerFactory.getLogger(NexusApp.class);
  private final WorkManager workManager;
  private final PlatformMessageDAO platformMessageDAO;
  private final String platformRelease;
  private final SQLDatabaseProvider sqlDatabaseProvider;
  private JobFactory jobFactory;
  private RobustWorkerPool workerPool;
  private int workConcurrency;

  /**
   Construct a new application by providing
   - a config,
   - a set of resource packages to add to the core set, and
   - an injector to create a child injector of in order to add the core set.@param resourcePackages to add to the core set of packages for the new application@param resourcePackages

   @param injector to add to the core set of modules for the new application
   */
  public NexusApp(
    Set<String> resourcePackages,
    Injector injector
  ) {
    super(resourcePackages, injector, NexusApp.class.getSimpleName());

    Config config = injector.getInstance(Config.class);

    // Configuration
    workConcurrency = config.getInt("work.concurrency");
    platformRelease = config.getString("platform.release");

    // non-static logger for this class, because app must init first
    log.info("{} configuration:\n{}", getName(), Text.toReport(config));

    // core delegates
    workManager = injector.getInstance(WorkManager.class);
    platformMessageDAO = injector.getInstance(PlatformMessageDAO.class);
    sqlDatabaseProvider = injector.getInstance(SQLDatabaseProvider.class);

    // Setup REST API payload topology
    HubApp.buildApiTopology(injector.getInstance(PayloadFactory.class));

    // Job factory
    try {
      jobFactory = injector.getInstance(JobFactory.class);
    } catch (Exception ignored) {
      log.info("{} has no JobFactory", getName());
    }
  }

  /**
   Starts Grizzly HTTP server
   exposing JAX-RS resources defined in this app.
   */
  public void start() throws AppException {
    log.info("{} will start worker pool before resource servers", getName());

    if (Objects.nonNull(jobFactory)) {
      workerPool = new RobustWorkerPool(() -> workManager.getWorker(jobFactory),
        workConcurrency, Executors.defaultThreadFactory());

      // Handle errors of the worker pool
      workerPool.getWorkerEventEmitter().addListener(
        (event, worker, queue, errorJob, runner, result, t) ->
          log.error("{} error in worker pool: {}",
            getName(), ImmutableList.of(event, worker, queue, errorJob, runner, result, t)),
        WorkerEvent.WORKER_ERROR
      );

      workerPool.run();
    }

    super.start();

    sendPlatformMessage(String.format(
      "%s (%s) is up at %s",
      getName(), platformRelease, getBaseURI()
    ));
  }

  /**
   Get the current work manager

   @return work manager
   */
  public WorkManager getWorkManager() {
    return workManager;
  }


  /**
   stop App Server
   */
  public void stop() {
    log.info("{} will stop worker pool before resource servers", getName());

    // shutdown worker pool if present
    if (Objects.nonNull(workerPool)) {
      workerPool.end(false);
      log.info("Worker pool did shutdown OK");
    }

    super.stop();

    // send messages about successful shutdown
    sendPlatformMessage(String.format(
      "%s (%s) did exit OK at %s",
      getName(), platformRelease, getBaseURI()
    ));

    // shutdown SQL database connection pool
    sqlDatabaseProvider.shutdown();
    log.info("{} SQL database connection pool did shutdown OK", getName());
  }

  /**
   Get base URI

   @return base URI
   */
  public String getBaseURI() {
    return "http://" + getRestHostname() + ":" + getRestPort() + "/";
  }


  /**
   [#153539503] Developer wants any app to send PlatformMessage on startup, including code version, region, ip@param body of message
   */
  private void sendPlatformMessage(String body) {
    try {
      platformMessageDAO.create(Access.internal(), new PlatformMessage().setType(String.valueOf(MessageType.Debug)).setBody(body));
    } catch (Exception e) {
      log.error("failed to send startup platform message", e);
    }
  }
}
