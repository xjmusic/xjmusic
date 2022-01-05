// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.ship;

import com.google.inject.Inject;
import com.google.inject.Injector;
import io.xj.hub.HubTopology;
import io.xj.lib.app.App;
import io.xj.lib.app.AppException;
import io.xj.lib.app.Environment;
import io.xj.lib.entity.EntityFactory;
import io.xj.nexus.NexusTopology;
import io.xj.ship.api.ShipAppHealthEndpoint;
import org.slf4j.LoggerFactory;

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
public class ShipApp extends App {
  private final org.slf4j.Logger LOG = LoggerFactory.getLogger(ShipApp.class);
  private final String platformRelease;

  /**
   Construct a new application by providing
   - a config,
   - a set of resource packages to add to the core set, and
   - an injector to create a child injector of in order to add the core set.@param resourcePackages to add to the core set of packages for the new application@param resourcePackages@param injector to add to the core set of modules for the new application
   */
  @Inject
  public ShipApp(
    Injector injector,
    Environment env,
    ShipAppHealthEndpoint shipAppHealthEndpoint
  ) {
    super(env);

    // Configuration
    platformRelease = env.getPlatformEnvironment();

    // Setup Entity topology
    EntityFactory entityFactory = injector.getInstance(EntityFactory.class);
    HubTopology.buildHubApiTopology(entityFactory);
    NexusTopology.buildNexusApiTopology(entityFactory);

    // Register JAX-RS filter for reading access control token
    getResourceConfig()
      .register(shipAppHealthEndpoint);
  }

  /**
   Starts Grizzly HTTP server
   exposing JAX-RS resources defined in this app.
   */
  public void start() throws AppException {
    super.start();
    LOG.info("{} ({}) is up at {}}", getName(), platformRelease, getBaseURI());
  }

  /**
   stop App Server
   */
  public void finish() {
    super.finish();
    LOG.info("{} ({}}) did exit OK at {}", getName(), platformRelease, getBaseURI());
  }

  /**
   Get base URI

   @return base URI
   */
  public String getBaseURI() {
    //noinspection HttpUrlsUsage
    return "http://" + getHostname() + ":" + getPort() + "/";
  }

}
