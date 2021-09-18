// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.ship;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.typesafe.config.Config;
import io.xj.lib.app.App;
import io.xj.lib.app.AppException;
import io.xj.lib.app.Environment;
import io.xj.lib.entity.EntityFactory;
import io.xj.hub.HubTopology;
import io.xj.lib.filestore.FileStoreProvider;
import io.xj.lib.json.JsonProvider;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.util.TempFile;
import io.xj.lib.util.Text;
import io.xj.ship.api.ShipAppHealthEndpoint;
import io.xj.ship.persistence.ShipEntityStore;
import io.xj.ship.work.ShipWork;
import org.slf4j.LoggerFactory;

import java.io.File;

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
  private final EntityFactory entityFactory;
  private final Environment env;
  private final FileStoreProvider fileStoreProvider;
  private final int rehydrateFabricatedAheadThreshold;
  private final JsonapiPayloadFactory jsonapiPayloadFactory;
  private final JsonProvider jsonProvider;
  private final ShipEntityStore entityStore;
  private final ShipWork work;
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
    Config config,
    Environment env,
    ShipAppHealthEndpoint shipAppHealthEndpoint
  ) {
    super(config, env);
    this.env = env;

    // Configuration
    platformRelease = env.getPlatformEnvironment();
    rehydrateFabricatedAheadThreshold = config.getInt("work.rehydrateFabricatedAheadThreshold");

    // non-static logger for this class, because app must init first
    LOG.info("{} configuration:\n{}", getName(), Text.toReport(config));

    // core delegates
    work = injector.getInstance(ShipWork.class);
    jsonProvider = injector.getInstance(JsonProvider.class);
    fileStoreProvider = injector.getInstance(FileStoreProvider.class);
    entityStore = injector.getInstance(ShipEntityStore.class);
    jsonapiPayloadFactory = injector.getInstance(JsonapiPayloadFactory.class);

    // Setup Entity topology
    entityFactory = injector.getInstance(EntityFactory.class);
    HubTopology.buildHubApiTopology(entityFactory);
    ShipTopology.buildShipApiTopology(entityFactory);

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
   Stop the work
   */
  public void stop() throws AppException {
    work.stop();
  }

  /**
   Get the current work manager

   @return work manager
   */
  public ShipWork getWork() {
    return work;
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
    return "http://" + getRestHostname() + ":" + getRestPort() + "/";
  }

}
