// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub;

import ch.qos.logback.classic.LoggerContext;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValueFactory;
import io.xj.hub.access.HubAccessControlModule;
import io.xj.hub.dao.DAOModule;
import io.xj.hub.ingest.HubIngestModule;
import io.xj.hub.persistence.HubPersistenceModule;
import io.xj.lib.app.AppConfiguration;
import io.xj.lib.app.AppException;
import io.xj.lib.entity.EntityModule;
import io.xj.lib.filestore.FileStoreModule;
import io.xj.lib.jsonapi.JsonApiModule;
import io.xj.lib.mixer.MixerModule;
import org.slf4j.LoggerFactory;

import java.net.UnknownHostException;
import java.util.Set;

/**
 Hub service
 */
public interface Main {
  String DEFAULT_CONFIGURATION_RESOURCE_FILENAME = "default.conf";
  Set<Module> injectorModules = ImmutableSet.of(
    new FileStoreModule(),
    new MixerModule(),
    new EntityModule(),
    new JsonApiModule(),
    new HubAccessControlModule(),
    new DAOModule(),
    new HubIngestModule(),
    new HubPersistenceModule()
  );
  int defaultPort = 3001;

  /**
   Main method.

   @param args arguments-- the first argument must be the path to the configuration file
   */
  @SuppressWarnings("DuplicatedCode")
  static void main(String[] args) throws AppException, UnknownHostException {

    // Get default configuration
    Config defaults = ConfigFactory.parseResources(DEFAULT_CONFIGURATION_RESOURCE_FILENAME)
      .withFallback(AppConfiguration.getDefault())
      .withValue("app.port", ConfigValueFactory.fromAnyRef(defaultPort));

    // Read configuration from arguments to program, with default fallbacks
    Config config = AppConfiguration.parseArgs(args, defaults);

    // Add context to logs
    LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
    lc.setPackagingDataEnabled(true);
    lc.putProperty("source", "java");
    lc.putProperty("service", "hub");
    lc.putProperty("host", config.getString("app.hostname"));
    lc.putProperty("env", config.getString("app.env"));

    // Instantiate app
    HubApp app = new HubApp(AppConfiguration.inject(config, injectorModules));

    // Shutdown Hook
    Runtime.getRuntime().addShutdownHook(new Thread(app::finish));

    // run database migrations
    // FUTURE create a separate service (top level, besides hub) only for migration
    app.migrate();

    // start
    app.start();
  }
}
