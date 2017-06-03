// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.dubworker;

import io.outright.xj.core.CoreModule;
import io.outright.xj.core.app.App;
import io.outright.xj.core.app.config.Config;
import io.outright.xj.core.app.exception.ConfigException;
import io.outright.xj.core.db.sql.SQLDatabaseProvider;
import io.outright.xj.core.migration.MigrationService;
import io.outright.xj.core.model.link.Link;
import io.outright.xj.core.chain_gang.ChainGangFactory;
import io.outright.xj.core.chain_gang.ChainGangOperation;
import io.outright.xj.core.chain_gang.impl.link_work.LinkWorkFactoryModule;
import io.outright.xj.mixer.MixerModule;

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
    Config.setDefault("app.port", "8044");

    // Default # seconds ahead of time to perform work
    Config.setDefault("work.buffer.seconds", "180");

    // Database migration validation check, to avoid operations on wrong database.
    try {
      MigrationService.validate(injector.getInstance(SQLDatabaseProvider.class));
    } catch (ConfigException e) {
      log.error("Migrations failed! App will not start.", e);
      System.exit(1);
    }

    // App
    app = injector.getInstance(App.class);
    app.configureServer("io.outright.xj.dubworker");

    // Link-type Workload
    ChainGangOperation linkChainGangOperation = Guice.createInjector(new CoreModule(), new MixerModule(),
      new DubworkerModule()).getInstance(ChainGangOperation.class);
    ChainGangFactory chainGangFactory = Guice.createInjector(new CoreModule(), new MixerModule(),
      new LinkWorkFactoryModule()).getInstance(ChainGangFactory.class);
    app.registerGangWorkload(
      "Dub Links",
      chainGangFactory.createLeader(Link.CRAFTED, Config.workAheadSeconds(), Config.workBatchSize()),
      chainGangFactory.createFollower(Link.DUBBING, Link.DUBBED, linkChainGangOperation)
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
