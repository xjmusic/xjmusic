// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub;

import com.google.inject.Injector;
import com.typesafe.config.Config;
import io.xj.hub.access.HubAccessControlProvider;
import io.xj.hub.access.HubAccessLogFilter;
import io.xj.hub.access.HubAccessTokenAuthFilter;
import io.xj.hub.persistence.HubDatabaseProvider;
import io.xj.hub.persistence.HubMigration;
import io.xj.hub.persistence.HubPersistenceException;
import io.xj.lib.app.App;
import io.xj.lib.app.AppException;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.entity.common.Topology;
import io.xj.lib.util.TempFile;
import io.xj.lib.util.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collections;

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
public class HubApp extends App {
  private final Logger log = LoggerFactory.getLogger(HubApp.class);
  private final String platformRelease;
  private final Injector injector;
  private final HubDatabaseProvider hubDatabaseProvider;

  /**
   Construct a new application by providing
   - a config,
   - a set of resource packages to add to the core set, and
   - an injector to create a child injector of in order to add the core set.@param resourcePackages to add to the core set of packages for the new application@param resourcePackages@param injector to add to the core set of modules for the new application
   */
  public HubApp(
    Injector injector
  ) {
    super(injector, Collections.singleton("io.xj.hub.api"));

    // Injection
    this.injector = injector;
    hubDatabaseProvider = injector.getInstance(HubDatabaseProvider.class);
    Config config = injector.getInstance(Config.class);

    // Configuration
    platformRelease = config.getString("platform.release");

    // Non-static logger for this class, because app must init first
    log.info("{} configuration:\n{}", getName(), Text.toReport(config));

    // Setup Entity topology
    Topology.buildHubApiTopology(injector.getInstance(EntityFactory.class));

    // Register JAX-RS filter for access log only registers if file succeeds to open for writing
    String pathToWriteAccessLog = config.hasPath("app.accessLogFile") ?
      config.getString("app.accessLogFile") :
      String.format("%s%s-access.log", TempFile.getTempFilePathPrefix(), File.separator);
    new HubAccessLogFilter(pathToWriteAccessLog).registerTo(getResourceConfig());

    // Register JAX-RS filter for reading access control token
    HubAccessControlProvider hubAccessControlProvider = injector.getInstance(HubAccessControlProvider.class);
    getResourceConfig().register(new HubAccessTokenAuthFilter(hubAccessControlProvider,
      config.getString("access.tokenName")));
  }

  /**
   Starts Grizzly HTTP server
   exposing JAX-RS resources defined in this app.
   */
  public void start() throws AppException {
    // start the underlying app
    super.start();
    log.info("{} ({}) is up and running at {}}", getName(), platformRelease, getBaseURI());
  }

  /**
   stop App Server
   */
  public void finish() {
    // stop the underlying app
    super.finish();
    log.info("{} ({}}) did exit OK at {}", getName(), platformRelease, getBaseURI());

    // shutdown SQL database connection pool
    hubDatabaseProvider.shutdown();
    log.debug("{} SQL database connection pool did shutdown OK", getName());
  }

  /**
   Get base URI

   @return base URI
   */
  public String getBaseURI() {
    return "http://" + getRestHostname() + ":" + getRestPort() + "/";
  }

  /**
   Run database migrations
   */
  public void migrate() {
    // Database migrations
    try {
      injector.getInstance(HubMigration.class).migrate();
    } catch (HubPersistenceException e) {
      System.out.printf("Migrations failed! HubApp will not start. %s: %s\n%s%n", e.getClass().getSimpleName(), e.getMessage(), Text.formatStackTrace(e));
      System.exit(1);
    }
  }

}
