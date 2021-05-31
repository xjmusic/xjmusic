// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.app;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
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
   @param args     array of arguments passed to program
   @param defaults to fallback on
   @return buffered file reader comprising the ingest config file
   */
  public static Config parseArgs(String[] args, Config defaults) throws AppException {
    if (0 == args.length) return defaults;

    Config config;
    try (BufferedReader buf = new BufferedReader(new FileReader(args[0]))) {
      try {
        Preconditions.checkArgument(buf.ready(), "Unable to read configuration file at given path");
        config = ConfigFactory.parseReader(buf)
          .withFallback(defaults)
          .resolve();
      } catch (Exception e) {
        throw new AppException("Unable to parse configuration", e);
      }

    } catch (IOException e) {
      throw new AppException("Error reading configuration file", e);
    }

    return config;
  }

  /**
   Create a Guice injector from the given modules, but first binding the given instance to Config.class

   @param config  instance to bind to Config.class
   @param modules to inject
   @return injector with Config.class bound
   */
  public static Injector inject(final Config config, Set<Module> modules) throws AppException {
    if (Objects.isNull(config)) throw new AppException("Config cannot be null!");
    if (config.isEmpty()) throw new AppException("Config cannot be empty!");
    return Guice.createInjector(
      ImmutableList.<Module>builder()
        .add(new AbstractModule() {
          @Override
          protected void configure() {
            bind(Config.class).toInstance(config);
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
