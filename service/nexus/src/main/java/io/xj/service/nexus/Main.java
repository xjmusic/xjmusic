// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigValueFactory;
import io.xj.lib.app.AppConfiguration;
import io.xj.lib.app.AppException;
import io.xj.service.hub.HubModule;
import io.xj.service.nexus.craft.CraftModule;
import io.xj.service.nexus.dub.DubModule;

import java.util.Set;

/**
 Nexus service
 */
public interface Main {
  Set<Module> injectorModules = ImmutableSet.of(new HubModule(), new NexusModule());
  Set<String> resourcePackages = ImmutableSet.of("io.xj.nexus");
  int defaultPort = 8042;

  /**
   Main method.

   @param args arguments-- the first argument must be the path to the configuration file
   */
  static void main(String[] args) throws AppException {
    // Get default configuration
    Config defaults = AppConfiguration.getDefault()
      .withValue("app.port", ConfigValueFactory.fromAnyRef(defaultPort));

    // Read configuration from arguments to program, with default fallbacks
    Config config = AppConfiguration.parseArgs(args, defaults);

    // Instantiate app
    NexusApp app = new NexusApp(resourcePackages, AppConfiguration.inject(config, injectorModules));

    // Shutdown Hook
    Runtime.getRuntime().addShutdownHook(new Thread(app::stop));

    // start
    app.start();
  }
}
