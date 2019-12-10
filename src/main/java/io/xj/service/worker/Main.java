// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.worker;

import com.google.common.collect.ImmutableList;
import com.google.inject.Module;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigValueFactory;
import io.xj.core.CoreModule;
import io.xj.core.app.App;
import io.xj.core.app.AppConfiguration;
import io.xj.core.app.AppException;
import io.xj.craft.CraftModule;
import io.xj.dub.DubModule;

/**
 Worker service
 */
public class Main {
  private static final Iterable<Module> injectorModules = ImmutableList.of(new CoreModule(), new WorkerModule(), new CraftModule(), new DubModule());
  private static final Iterable<String> resourcePackages = ImmutableList.of("io.xj.worker");
  private static final int defaultPort = 8042;

  /**
   Main method.

   @param args arguments-- the first argument must be the path to the configuration file
   */
  public static void main(String[] args) throws AppException {
    // Get default configuration
    Config defaults = AppConfiguration.getDefault()
      .withValue("app.port", ConfigValueFactory.fromAnyRef(defaultPort));

    // Read configuration from arguments to program, with default fallbacks
    Config config = AppConfiguration.parseArgs(args, defaults);

    // Instantiate app
    App app = new App(resourcePackages, AppConfiguration.inject(config, injectorModules));

    // Shutdown Hook
    Runtime.getRuntime().addShutdownHook(new Thread(app::stop));

    // start
    app.start();
  }
}
