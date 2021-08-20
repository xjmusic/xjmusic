//  Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.xj.lib.app.AppConfiguration;

/**
 App test configuration utility
 */
public class NexusTestConfiguration {

  /**
   Get default configuration for tests

   @return default configuration for tests
   */
  public static Config getDefault() {

    return ConfigFactory.parseResources("config/test.conf")
      .withFallback(ConfigFactory.parseResources("config/default.conf"))
      .withFallback(AppConfiguration.getDefault())
      .resolve();
  }

}
