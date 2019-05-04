// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.worker;

import io.xj.core.CoreModule;
import io.xj.core.app.App;
import io.xj.core.config.Config;
import io.xj.core.exception.CoreException;
import io.xj.core.persistence.sql.migration.MigrationService;
import io.xj.core.persistence.sql.SQLDatabaseProvider;
import io.xj.craft.CraftModule;
import io.xj.dub.DubModule;

import com.google.inject.Guice;
import com.google.inject.Injector;

import net.greghaines.jesque.worker.JobFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 Main class.
 */
public class Main {
  private static final Logger log = LoggerFactory.getLogger(Main.class);
  private static final Injector injector = Guice.createInjector(new CoreModule(), new WorkerModule(), new CraftModule(), new DubModule());
  private static final App app = injector.getInstance(App.class);
  private static final SQLDatabaseProvider sqlDatabaseProvider = injector.getInstance(SQLDatabaseProvider.class);
  private static final JobFactory jobFactory = injector.getInstance(JobFactory.class);

  /**
   Main method.

   @param args arguments
   @throws IOException if execution fails
   */
  public static void main(String[] args) throws IOException, CoreException {
    // Default port
    Config.setDefault("app.port", "8043");

    // Default # seconds ahead of time to perform work
    Config.setDefault("work.buffer.seconds", "120");

    // Database migration validation check, to avoid operations on wrong database.
    try {
      MigrationService.validate(sqlDatabaseProvider);
    } catch (CoreException e) {
      log.error("Migration validation failed! Worker App will not start.", e);
      System.exit(1);
    }

    // Server App
    app.configureServer("io.xj.worker");

    // Set App Job Factory - Note that this is the only line that makes this app a worker!
    app.setJobFactory(jobFactory);

    // Shutdown Hook
    Runtime.getRuntime().addShutdownHook(new Thread(Main::shutdown));

    // start
    app.start();
  }

  private static void shutdown() {
    app.stop();
  }

}
