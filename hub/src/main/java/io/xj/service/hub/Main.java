// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValueFactory;
import io.xj.lib.app.AppConfiguration;
import io.xj.lib.app.AppException;
import io.xj.lib.entity.EntityModule;
import io.xj.lib.filestore.FileStoreModule;
import io.xj.lib.jsonapi.JsonApiModule;
import io.xj.lib.mixer.MixerModule;
import io.xj.service.hub.access.HubAccessControlModule;
import io.xj.service.hub.dao.DAOModule;
import io.xj.service.hub.ingest.HubIngestModule;
import io.xj.service.hub.persistence.HubPersistenceModule;

import java.util.Set;

/**
 Hub service
 */
public interface Main {
  String DEFAULT_CONFIGURATION_RESOURCE_FILENAME = "default.conf";
  Set<Module> injectorModules = ImmutableSet.of(
    new FileStoreModule(),
    new MixerModule(),
    new EntityModule(),
    new JsonApiModule(),
    new HubAccessControlModule(),
    new DAOModule(),
    new HubIngestModule(),
    new HubPersistenceModule()
  );
  Set<String> resourcePackages = ImmutableSet.of("io.xj.service.hub");
  int defaultPort = 3000;

  /**
   Main method.

   @param args arguments-- the first argument must be the path to the configuration file
   */
  static void main(String[] args) throws AppException {

    // Get default configuration
    Config defaults = ConfigFactory.parseResources(DEFAULT_CONFIGURATION_RESOURCE_FILENAME)
      .withFallback(AppConfiguration.getDefault())
      .withValue("app.port", ConfigValueFactory.fromAnyRef(defaultPort));

    // Read configuration from arguments to program, with default fallbacks
    Config config = AppConfiguration.parseArgs(args, defaults);

    // Instantiate app
    HubApp app = new HubApp(AppConfiguration.inject(config, injectorModules));

    // Shutdown Hook
    Runtime.getRuntime().addShutdownHook(new Thread(app::finish));

    // run database migrations
    // FUTURE create a separate service (top level, besides hub) only for migration
    app.migrate();

    // start
    app.start();
  }
}
