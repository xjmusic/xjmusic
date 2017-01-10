// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.hub;

import io.outright.xj.core.app.App;
import io.outright.xj.core.app.CoreModule;
import io.outright.xj.core.app.config.Config;
import io.outright.xj.hub.controller.migration.MigrationController;
import io.outright.xj.core.app.exception.ConfigException;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Main class.
 *
 */
public class Main {
  private static App app;
  private static final Injector injector = Guice.createInjector(new CoreModule());
  private static Logger log = LoggerFactory.getLogger(Main.class);

  /**
     * Main method.
     * @param args arguments
     * @throws IOException if execution fails
     */
    public static void main(String[] args) throws IOException, ConfigException {
      // Default port
      Config.setDefault("app.port", "8042");

      // App
      app = injector.getInstance(App.class);
      app.configure("io.outright.xj.hub");

      // Database migrations
      MigrationController migrationController = injector.getInstance(MigrationController.class);
      try {
        migrationController.migrate();
      } catch (ConfigException e) {
        log.error("Migrations failed! App will not start.", e);
        System.exit(1);
      }

      // Shutdown Hook
      Runtime.getRuntime().addShutdownHook(new Thread(Main::shutdown));

      // start
      app.start();
    }

  private static void shutdown() {
    app.stop();
  }

}

