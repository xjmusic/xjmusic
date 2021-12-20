// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.ship;

import ch.qos.logback.classic.LoggerContext;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Module;
import com.google.inject.util.Modules;
import io.xj.lib.app.AppException;
import io.xj.lib.app.Environment;
import io.xj.lib.jsonapi.JsonapiModule;
import io.xj.lib.secret.Secrets;
import io.xj.ship.work.ShipWorkModule;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 Ship service
 */
public interface Main {
  Set<Module> injectorModules = ImmutableSet.of(
    new JsonapiModule(),
    new ShipWorkModule()
  );

  /**
   Main method.

   @param args arguments-- the first argument must be the path to the configuration file
   */
  @SuppressWarnings("DuplicatedCode")
  static void main(String[] args) throws AppException {
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
    lc.putProperty("service", "ship");
    lc.putProperty("host", env.getHostname());
    lc.putProperty("env", env.getPlatformEnvironment());

    // Instantiate app
    ShipApp app = injector.getInstance(ShipApp.class);

    // Shutdown Hook
    Runtime.getRuntime().addShutdownHook(new Thread(app::finish));

    // start
    app.start();

    // do work-- this blocks until work quits
    app.getWork().work();
  }
}
