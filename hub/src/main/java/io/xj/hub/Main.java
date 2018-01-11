// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub;

import io.xj.core.CoreModule;
import io.xj.core.app.App;
import io.xj.core.config.Config;
import io.xj.core.exception.ConfigException;
import io.xj.core.migration.MigrationService;
import io.xj.core.persistence.sql.SQLDatabaseProvider;
import io.xj.core.transport.CSV;

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
  private static final App app = injector.getInstance(App.class);
  private static final Logger log = LoggerFactory.getLogger(Main.class);

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

    // List of eitherOr docs is hard-coded!
    // [#215] Internal "Docs" section where users of different permissions can view static content that is stored in .md static files on the backend, for easy editing.
    // For each <key> there is a `src/main/resources/docs/<key>.md` file
    Config.setDefault("doc.eitherOr.keys", CSV.join(new String[]{
      "chain-link-choice"
    }));

    // App
    app.configureServer("io.xj.hub");

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

