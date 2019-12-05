// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.app;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;

/**
 Utility for parsing command-line arguments passed in to a main application.
 <p>
 Assumptions:
 - there will only be one argument
 - this argument will be the path to a configuration file
 - the configuration file exists and can be read
 <p>
 Returns a BufferedReader, which allows for configuration to be mocked out in app tests (unwieldly with an actual file)
 */
public class AppConfiguration {
  /**
   @param args array of arguments passed to program
   @return buffered file reader comprising the ingest config file
   */
  public static Config parseArgs(String[] args) throws AppException {
    if (1 > args.length)
      throw new AppException("Requires path to configuration file as first argument.");

    BufferedReader buf;
    try {
      buf = new BufferedReader(new FileReader(args[0]));
    } catch (FileNotFoundException e) {
      throw new AppException("Cannot find configuration file");
    }

    return loadConfig(buf);
  }

  /**
   Load a configuration from file

   @param confFile comprising configuration file
   @return configuration
   */
  private static Config loadConfig(Reader confFile) throws AppException {
    try {
      Preconditions.checkArgument(confFile.ready(), "Unable to read configuration file at given path");
    } catch (Exception e) {
      throw new AppException("Unable to parse configuration", e);
    }

    return ConfigFactory.parseReader(confFile)
      .withFallback(getDefault())
      .resolve();
  }

  /**
   Create a Guice injector from the given modules, but first binding the given instance to Config.class

   @param config  instance to bind to Config.class
   @param modules to inject
   @return injector with Config.class bound
   */
  public static Injector inject(Config config, Iterable<Module> modules) {
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
    return ConfigFactory.parseResources("default.conf");
  }
}
