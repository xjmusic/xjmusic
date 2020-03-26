// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValueFactory;
import io.xj.lib.app.AppConfiguration;
import io.xj.lib.app.AppException;
import io.xj.service.hub.digest.DigestModule;
import io.xj.service.hub.generation.GenerationModule;

import java.util.Set;

/**
 Hub service
 */
public interface Main {
  String DEFAULT_CONFIGURATION_RESOURCE_FILENAME = "default.conf";
  Set<Module> injectorModules = ImmutableSet.of(new HubModule(), new DigestModule(), new GenerationModule());
  Set<String> resourcePackages = ImmutableSet.of("io.xj.service.hub");
  int defaultPort = 8042;

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
    HubApp app = new HubApp(resourcePackages, AppConfiguration.inject(config, injectorModules));

    // Shutdown Hook
    Runtime.getRuntime().addShutdownHook(new Thread(app::stop));

    // run database migrations
    // TODO create a separate service (top level, besides hub) only for migration
    app.migrate();

    // start
    app.start();
  }
}
