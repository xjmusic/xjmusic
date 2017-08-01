// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.worker;

import com.google.inject.Guice;
import com.google.inject.Injector;

import io.xj.core.CoreModule;
import io.xj.core.app.App;
import io.xj.core.app.config.Config;
import io.xj.core.app.exception.ConfigException;
import io.xj.core.database.sql.SQLDatabaseProvider;
import io.xj.core.migration.MigrationService;
import net.greghaines.jesque.worker.JobFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 Main class.
 */
public class Main {
  private static final Logger log = LoggerFactory.getLogger(Main.class);
  private static final Injector injector = Guice.createInjector(new CoreModule(), new WorkerModule());
  private static final App app = injector.getInstance(App.class);
  private static final SQLDatabaseProvider sqlDatabaseProvider = injector.getInstance(SQLDatabaseProvider.class);
  private static final JobFactory jobFactory = injector.getInstance(JobFactory.class);

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
      MigrationService.validate(sqlDatabaseProvider);
    } catch (ConfigException e) {
      log.error("Migrations failed! App will not start.", e);
      System.exit(1);
    }

    // Server App
    app.configureServer("io.xj.worker");

    // Set App Job Factory
    app.setJobFactory(jobFactory);

/*
    // Start a worker to run jobs from the queue
    Thread workerThread = new Thread(worker);
    workerThread.start();

    // Try to join that thread
    try {
      workerThread.join();
    } catch (Exception e) {
      log.error("Joining worker thread", e);
    }
*/

/*

    // Pilot-type Workload
    ChainGangFactory pilotChainGangFactory = Guice.createInjector(new CoreModule(),
      new LinkPilotWorkFactoryModule()).getInstance(ChainGangFactory.class);
    app.registerGangWorkload(
      "Create New Links",
      pilotChainGangFactory.createLeader(Config.workBufferSeconds(), Config.workBatchSize()),
      pilotChainGangFactory.createFollower(LinkState.Planned)
    );

    // Link-type Workload
    ChainGangOperation craftOperation = Guice.createInjector(new CoreModule(),
      new CraftworkerModule()).getInstance(ChainGangOperation.class);
    ChainGangFactory craftFactory = Guice.createInjector(new CoreModule(),
      new LinkWorkFactoryModule()).getInstance(ChainGangFactory.class);
    app.registerGangWorkload(
      "Craft Links",
      craftFactory.createLeader(LinkState.Planned, Config.workBufferSeconds(), Config.workBatchSize()),
      craftFactory.createFollower(LinkState.Crafting, LinkState.Crafted, craftOperation)
    );

    // Link-type Workload
    ChainGangOperation dubOperation = Guice.createInjector(new CoreModule(), new MixerModule(),
      new DubworkerModule()).getInstance(ChainGangOperation.class);
    ChainGangFactory dubFactory = Guice.createInjector(new CoreModule(), new MixerModule(),
      new LinkWorkFactoryModule()).getInstance(ChainGangFactory.class);
    app.registerGangWorkload(
      "Dub Links",
      dubFactory.createLeader(LinkState.Crafted, Config.workBufferSeconds(), Config.workBatchSize()),
      dubFactory.createFollower(LinkState.Dubbing, LinkState.Dubbed, dubOperation)
    );

    // Worker (Guice) factory
    EraseFactory eraseFactory = Guice.createInjector(new CoreModule(),
      new EraseworkerModule()).getInstance(EraseFactory.class);

    // Chain-Erase Workload
    app.registerSimpleWorkload(
      "Erase Chains",
      eraseFactory.chainEraseWorker(Config.workBatchSize()));

    // Audio-Erase Workload
    app.registerSimpleWorkload(
      "Erase Audio",
      eraseFactory.audioEraseWorker(Config.workBatchSize()));

*/

    // Shutdown Hook
    Runtime.getRuntime().addShutdownHook(new Thread(Main::shutdown));

    // start
    app.start();
  }

  private static void shutdown() {
    app.stop();
  }

}
