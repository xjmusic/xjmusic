// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.app;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

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
   Get default configuration

   @return default configuration
   */
  public static Config getDefault() {
    return ConfigFactory.parseResources(DEFAULT_CONFIGURATION_RESOURCE_FILENAME);
  }

}
