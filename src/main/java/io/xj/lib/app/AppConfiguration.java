// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.app;

import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.util.Objects;
import java.util.Set;

/**
 Utility for parsing command-line arguments passed in to a main application.
 <p>
 Assumptions:
 - there will only be one argument
 - this argument will be the path to a configuration file
 - the configuration file exists and can be read
 <p>
 Returns a Typesafe Config
 */
public class AppConfiguration {
  private static final String DEFAULT_CONFIGURATION_RESOURCE_FILENAME = "config/default.conf";

  AppConfiguration() {
    throw new IllegalStateException("Utility classes cannot be created");
  }

  /**
   Create a Guice injector from the given modules, but first binding the given instance to Config.class

   @param config  instance to bind to Config.class
   @param modules to inject
   @return injector with Config.class bound
   */
  public static Injector inject(final Config config, final Environment env, Set<Module> modules) throws AppException {
    if (Objects.isNull(config)) throw new AppException("Config cannot be null!");
    if (config.isEmpty()) throw new AppException("Config cannot be empty!");
    if (Objects.isNull(env)) throw new AppException("Environment cannot be null!");
    return Guice.createInjector(
      ImmutableList.<Module>builder()
        .add(new AbstractModule() {
          @Override
          protected void configure() {
            bind(Config.class).toInstance(config);
            bind(Environment.class).toInstance(env);
          }
        })
        .addAll(modules)
        .build());
  }

  /**
   Get default configuration

   @return default configuration
   */
  public static Config getDefault() {
    return ConfigFactory.parseResources(DEFAULT_CONFIGURATION_RESOURCE_FILENAME);
  }

}
