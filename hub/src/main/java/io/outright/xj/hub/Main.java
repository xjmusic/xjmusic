// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.hub;

import io.outright.xj.core.CoreModule;
import io.outright.xj.core.app.App;
import io.outright.xj.core.app.config.Config;
import io.outright.xj.core.app.exception.ConfigException;
import io.outright.xj.core.db.sql.SQLDatabaseProvider;
import io.outright.xj.core.migration.MigrationService;
import io.outright.xj.core.transport.CSV;

import com.google.inject.Guice;
import com.google.inject.Injector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 Main class.
 */
public class Main {
  private static final Injector injector = Guice.createInjector(new CoreModule());
  private static App app;
  private static Logger log = LoggerFactory.getLogger(Main.class);

  /**
   Main method.

   @param args arguments
   @throws IOException if execution fails
   */
  public static void main(String[] args) throws IOException, ConfigException {
    // Default port
    Config.setDefault("app.port", "8042");

    // Default # seconds ahead of time to perform work
    Config.setDefault("work.buffer.seconds", "300");

    // List of available docs is hard-coded!
    // [#215] Internal "Docs" section where users of different permissions can view static content that is stored in .md static files on the backend, for easy editing.
    // For each <key> there is a `src/main/resources/docs/<key>.md` file
    Config.setDefault("doc.available.keys", CSV.join(new String[]{
      "chain-link-choice"
    }));

    // App
    app = injector.getInstance(App.class);
    app.configureServer("io.outright.xj.hub");

    // Database migrations
    try {
      MigrationService.migrate(injector.getInstance(SQLDatabaseProvider.class));
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

