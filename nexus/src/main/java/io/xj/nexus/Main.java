// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus;

import ch.qos.logback.classic.LoggerContext;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Module;
import com.google.inject.util.Modules;
import io.xj.lib.app.AppException;
import io.xj.lib.app.Environment;
import io.xj.lib.filestore.FileStoreModule;
import io.xj.lib.jsonapi.JsonapiModule;
import io.xj.lib.mixer.MixerModule;
import io.xj.lib.secret.Secrets;
import io.xj.nexus.craft.CraftModule;
import io.xj.nexus.dub.DubModule;
import io.xj.nexus.fabricator.NexusFabricatorModule;
import io.xj.nexus.hub_client.client.HubClientModule;
import io.xj.nexus.persistence.NexusPersistenceModule;
import io.xj.nexus.work.NexusWorkModule;
import org.slf4j.LoggerFactory;

import java.net.UnknownHostException;
import java.util.Set;

/**
 Nexus service
 */
public interface Main {
  Set<Module> injectorModules = ImmutableSet.of(
    new CraftModule(),
    new HubClientModule(),
    new NexusPersistenceModule(),
    new DubModule(),
    new NexusFabricatorModule(),
    new FileStoreModule(),
    new JsonapiModule(),
    new MixerModule(),
    new NexusPersistenceModule(),
    new NexusWorkModule()
  );

  /**
   Main method.

   @param args arguments-- the first argument must be the path to the configuration file
   */
  @SuppressWarnings("DuplicatedCode")
  static void main(String[] args) throws AppException, UnknownHostException {
    final var env = Secrets.environment();

    var injector = Guice.createInjector(Modules.override(injectorModules).with(new AbstractModule() {
      @Override
      protected void configure() {
        bind(Environment.class).toInstance(env);
      }
    }));

    // Add context to logs
    LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
    lc.setPackagingDataEnabled(true);
    lc.putProperty("source", "java");
    lc.putProperty("service", "nexus");
    lc.putProperty("host", env.getHostname());
    lc.putProperty("env", env.getPlatformEnvironment());

    // Instantiate app
    NexusApp app = injector.getInstance(NexusApp.class);

    // Shutdown Hook
    Runtime.getRuntime().addShutdownHook(new Thread(app::finish));

    // start
    app.start();

    // do work-- this blocks until work quits
    app.getWork().work();
  }
}
