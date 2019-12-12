// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.app;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;
import com.google.inject.Injector;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigValue;
import io.xj.core.access.Access;
import io.xj.core.dao.PlatformMessageDAO;
import io.xj.core.entity.MessageType;
import io.xj.core.exception.CoreException;
import io.xj.core.model.PlatformMessage;
import io.xj.core.persistence.SQLDatabaseProvider;
import io.xj.core.persistence.Migration;
import io.xj.core.util.Text;
import io.xj.core.work.WorkManager;
import io.xj.core.work.RobustWorkerPool;
import net.greghaines.jesque.worker.JobFactory;
import net.greghaines.jesque.worker.WorkerEvent;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

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
public class App {
  private static final Collection<String> commonResourcesPackages = ImmutableList.of("io.xj.core.app");
  private final org.slf4j.Logger log = LoggerFactory.getLogger(App.class);
  private final int port;
  private final String name;
  private final WorkManager workManager;
  private final PlatformMessageDAO platformMessageDAO;
  private final String host;
  private final String platformRelease;
  private final ResourceConfig resourceConfig;
  private final Injector injector;
  private final SQLDatabaseProvider sqlDatabaseProvider;
  private JobFactory jobFactory;
  private RobustWorkerPool workerPool;
  private HttpServer resourceServer;
  private int workConcurrency;

  /**
   Construct a new application by providing
   - a config,
   - a set of resource packages to add to the core set, and
   - an injector to create a child injector of in order to add the core set.@param resourcePackages to add to the core set of packages for the new application

   @param injector to add to the core set of modules for the new application
   */
  public App(
    Iterable<String> resourcePackages,
    Injector injector
  ) {
    this.injector = injector;
    Config config = injector.getInstance(Config.class);

    // 1. Configuration
    port = config.getInt("app.port");
    name = config.getString("app.name");
    host = config.getString("app.host");
    workConcurrency = config.getInt("work.concurrency");
    platformRelease = config.getString("platform.release");

    // 3. non-static logger for this class, because app must init first
    log.info("{} configuration:\n{}", name, toReport(config));

    // 5. core delegates
    workManager = injector.getInstance(WorkManager.class);
    platformMessageDAO = injector.getInstance(PlatformMessageDAO.class);
    sqlDatabaseProvider = injector.getInstance(SQLDatabaseProvider.class);

    // 4. resources configuration for jetty/server server
    resourceConfig = initJerseyResources(resourcePackages, injector);

    // 6. Job factory
    try {
      jobFactory = injector.getInstance(JobFactory.class);
    } catch (Exception ignored) {
      log.info("{} has no JobFactory", name);
    }
  }

  /**
   Format a config into multiline key => value, padded to align into two columns, sorted alphabetically by key name

   @param config to format
   @return multiline formatted config
   */
  private static String toReport(Config config) {
    Set<Map.Entry<String, ConfigValue>> entries = config.entrySet();

    // there must be one longest entry, and we'll use its length as the column width for printing the whole list
    Optional<String> longest = entries.stream().map(Map.Entry::getKey).max(Comparator.comparingInt(String::length));
    if (longest.isEmpty()) return "";
    int padding = longest.get().length();

    // each line in the entry is padded to align into two columns, sorted alphabetically by key name
    List<String> lines = entries.stream()
      .map(c -> String.format("    %-" + padding + "s => %s", c.getKey(), c.getValue().unwrapped()))
      .sorted(Ordering.natural())
      .collect(Collectors.toList());

    // join lines into one multiline output
    return String.join("\n", lines);
  }

  /**
   Scans for all resource classes in package
   Use specified packages plus common resources@param packages to register@param addPackages containing JAX-RS resources to serve

   @param injector to resolve Guice bindings in JAX-RS resource classes
   @return resource configuration
   */
  private ResourceConfig initJerseyResources(Iterable<String> addPackages, Injector injector) {
    Collection<String> packages = ImmutableList.<String>builder()
      .addAll(commonResourcesPackages)
      .addAll(addPackages)
      .build();

    // scans for all resource classes in packages
    ResourceConfig rc = new AppResourceConfig(injector, packages);

    // TODO replace this with an event handler for Jersey server that shows all REST endpoints
    packages.forEach(pkg -> log.info("{} will serve resources from package {}", name, pkg));

    return rc;
  }

  /**
   Create a Jetty server from the configuration

   @return Jetty server
   */
  private HttpServer startResourceServer() throws AppException {
    URI serverURI = URI.create(getBaseURI());
    log.info("{} will start resource server at {}", name, serverURI);
    HttpServer server = GrizzlyHttpServerFactory.createHttpServer(serverURI, resourceConfig);
    try {
      server.start();
    } catch (IOException e) {
      throw new AppException("Failed to start resource server", e);
    }
    log.info("{} resource server up", name);
    return server;
  }

  /**
   Starts Grizzly HTTP server
   exposing JAX-RS resources defined in this app.
   */
  public void start() throws AppException {
    log.info("{} will start", name);

    if (Objects.nonNull(jobFactory)) {
      workerPool = new RobustWorkerPool(() -> workManager.getWorker(jobFactory),
        workConcurrency, Executors.defaultThreadFactory());

      // Handle errors of the worker pool
      workerPool.getWorkerEventEmitter().addListener(
        (event, worker, queue, errorJob, runner, result, t) ->
          log.error("{} error in worker pool: {}",
            name, ImmutableList.of(event, worker, queue, errorJob, runner, result, t)),
        WorkerEvent.WORKER_ERROR
      );

      workerPool.run();
    }

    resourceServer = startResourceServer();

    sendPlatformMessage(MessageType.Debug, String.format(
      "%s (%s) is up at %s",
      name, platformRelease, getBaseURI()
    ));

    log.info("{} did start OK, listening at {}", name, getBaseURI());
  }

  /**
   Get application name

   @return name of this application
   */
  public String getName() {
    return name;
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
    log.info("{} will stop", name);

    // shutdown resource server
    try {
      log.info("{} resource server will shutdown now", name);
      resourceServer.shutdownNow();
      log.info("Server did shutdown OK");
    } catch (Exception e) {
      log.error("{} failed to stop resource server", name, e);
    }

    // shutdown worker pool if present
    if (Objects.nonNull(workerPool)) {
      workerPool.end(false);
      log.info("Worker pool did shutdown OK");
    }

    // send messages about successful shutdown
    sendPlatformMessage(MessageType.Debug, String.format(
      "%s (%s) did exit OK at %s",
      name, platformRelease, getBaseURI()
    ));

    // shutdown SQL database connection pool
    sqlDatabaseProvider.shutdown();
    log.info("SQL database connection pool did shutdown OK");
  }

  /**
   Get base URI

   @return base URI
   */
  public String getBaseURI() {
    return "http://" + host + ":" + port + "/";
  }


  /**
   [#153539503] Developer wants any app to send PlatformMessage on startup, including code version, region, ip

   @param type of message
   @param body of message
   */
  private void sendPlatformMessage(MessageType type, String body) {
    try {
      platformMessageDAO.create(Access.internal(), new PlatformMessage().setType(String.valueOf(type)).setBody(body));
    } catch (Exception e) {
      log.error("failed to send startup platform message", e);
    }
  }

  /**
   Run database migrations
   */
  public void migrate() {
    // Database migrations
    try {
      injector.getInstance(Migration.class).migrate();
    } catch (CoreException e) {
      System.out.println(String.format("Migrations failed! App will not start. %s: %s\n%s", e.getClass().getSimpleName(), e.getMessage(), Text.formatStackTrace(e)));
      System.exit(1);
    }
  }
}
