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
import io.xj.ship.work.ShipWork;
import io.xj.ship.work.ShipWorkModule;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 Ship service
 */
public interface Main {
  String APP_NAME = "ship";
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
    env.setAppName(APP_NAME);

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

    // Instantiate app, add its shutdown hook, and start it
    ShipApp app = injector.getInstance(ShipApp.class);
    Runtime.getRuntime().addShutdownHook(new Thread(app::finish));
    app.start();

    // Instantiate work, add its shutdown hook
    ShipWork work = injector.getInstance(ShipWork.class);
    Runtime.getRuntime().addShutdownHook(new Thread(work::finish));

    // Start work. This blocks until a graceful exit on interrupt signal
    work.start();
  }
}
