// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.craftworker;

import io.outright.xj.core.CoreModule;
import io.outright.xj.core.app.App;
import io.outright.xj.core.app.config.Config;
import io.outright.xj.core.app.exception.ConfigException;
import io.outright.xj.core.db.sql.SQLDatabaseProvider;
import io.outright.xj.core.migration.MigrationService;
import io.outright.xj.core.model.link.Link;
import io.outright.xj.core.work.WorkFactory;
import io.outright.xj.core.work.WorkerOperation;
import io.outright.xj.core.work.impl.link_work.LinkWorkFactoryModule;
import io.outright.xj.core.work.impl.pilot_work.PilotWorkFactoryModule;
import io.outright.xj.craftworker.work.CraftLinkWorkerModule;

import com.google.inject.Guice;
import com.google.inject.Injector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 Main class.
 */
public class Main {
  private final static Logger log = LoggerFactory.getLogger(Main.class);
  private static final Injector injector = Guice.createInjector(new CoreModule());
  private static App app;


  /**
   Main method.

   @param args arguments
   @throws IOException if execution fails
   */
  public static void main(String[] args) throws IOException, ConfigException {
    // Default port
    Config.setDefault("app.port", "8043");

    // Default # seconds ahead of time to perform work
    Config.setDefault("work.buffer.seconds", "300");

    // Database migration validation check, to avoid operations on wrong database.
    try {
      MigrationService.validate(injector.getInstance(SQLDatabaseProvider.class));
    } catch (ConfigException e) {
      log.error("Migrations failed! App will not start.", e);
      System.exit(1);
    }

    // Server App
    app = injector.getInstance(App.class);
    app.configureServer("io.outright.xj.craftworker");

    // Pilot-type Workload
    WorkFactory pilotWorkFactory = Guice.createInjector(new CoreModule(),
      new PilotWorkFactoryModule()).getInstance(WorkFactory.class);
    app.registerWorkload(
      "Create New Links",
      pilotWorkFactory.createLeader(Config.workAheadSeconds(), Config.workBatchSize()),
      pilotWorkFactory.createWorker(Link.PLANNED)
    );

    // Link-type Workload
    WorkerOperation linkOperation = Guice.createInjector(new CoreModule(),
      new CraftLinkWorkerModule()).getInstance(WorkerOperation.class);
    WorkFactory linkWorkFactory = Guice.createInjector(new CoreModule(),
      new LinkWorkFactoryModule()).getInstance(WorkFactory.class);
    app.registerWorkload(
      "Craft Links",
      linkWorkFactory.createLeader(Link.PLANNED, Config.workAheadSeconds(), Config.workBatchSize()),
      linkWorkFactory.createWorker(Link.CRAFTING, Link.CRAFTED, linkOperation)
    );

    // Shutdown Hook
    Runtime.getRuntime().addShutdownHook(new Thread(Main::shutdown));

    // start
    app.start();
  }

  private static void shutdown() {
    app.stop();
  }

}
