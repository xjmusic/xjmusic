// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.craftworker;

import io.xj.core.CoreModule;
import io.xj.core.app.App;
import io.xj.core.app.config.Config;
import io.xj.core.app.exception.ConfigException;
import io.xj.core.db.sql.SQLDatabaseProvider;
import io.xj.core.migration.MigrationService;
import io.xj.core.model.link.Link;
import io.xj.core.chain_gang.ChainGangFactory;
import io.xj.core.chain_gang.ChainGangOperation;
import io.xj.core.chain_gang.impl.link_work.LinkWorkFactoryModule;
import io.xj.core.chain_gang.impl.link_pilot_work.LinkPilotWorkFactoryModule;

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
    Config.setDefault("work.buffer.seconds", "120");

    // Database migration validation check, to avoid operations on wrong database.
    try {
      MigrationService.validate(injector.getInstance(SQLDatabaseProvider.class));
    } catch (ConfigException e) {
      log.error("Migrations failed! App will not start.", e);
      System.exit(1);
    }

    // Server App
    app = injector.getInstance(App.class);
    app.configureServer("io.xj.craftworker");

    // Pilot-type Workload
    ChainGangFactory pilotChainGangFactory = Guice.createInjector(new CoreModule(),
      new LinkPilotWorkFactoryModule()).getInstance(ChainGangFactory.class);
    app.registerGangWorkload(
      "Create New Links",
      pilotChainGangFactory.createLeader(Config.workAheadSeconds(), Config.workBatchSize()),
      pilotChainGangFactory.createFollower(Link.PLANNED)
    );

    // Link-type Workload
    ChainGangOperation linkChainGangOperation = Guice.createInjector(new CoreModule(),
      new CraftworkerModule()).getInstance(ChainGangOperation.class);
    ChainGangFactory chainGangFactory = Guice.createInjector(new CoreModule(),
      new LinkWorkFactoryModule()).getInstance(ChainGangFactory.class);
    app.registerGangWorkload(
      "Craft Links",
      chainGangFactory.createLeader(Link.PLANNED, Config.workAheadSeconds(), Config.workBatchSize()),
      chainGangFactory.createFollower(Link.CRAFTING, Link.CRAFTED, linkChainGangOperation)
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
