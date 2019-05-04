// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub;

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.xj.core.CoreModule;
import io.xj.core.app.App;
import io.xj.core.config.Config;
import io.xj.core.exception.CoreException;
import io.xj.core.persistence.sql.SQLDatabaseProvider;
import io.xj.core.persistence.sql.migration.MigrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 Main class.
 */
public enum Main {
  ;
  private static final Injector injector = Guice.createInjector(new CoreModule());
  private static final App app = injector.getInstance(App.class);
  private static final Logger log = LoggerFactory.getLogger(Main.class);

  /**
   Main method.

   @param args arguments
   @throws IOException if execution fails
   */
  public static void main(String[] args) throws IOException, CoreException {
    // Default port
    Config.setDefault("app.port", "8042");

    // Default # seconds ahead of time to perform work
    Config.setDefault("work.buffer.seconds", "300");

    // App
    app.configureServer("io.xj.hub");

    // Database migrations
    try {
      MigrationService.migrate(injector.getInstance(SQLDatabaseProvider.class));
    } catch (CoreException e) {
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

