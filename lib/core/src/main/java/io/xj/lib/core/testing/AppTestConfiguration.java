// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.core.testing;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.xj.lib.core.app.AppConfiguration;

/**
 App test configuration utility
 */
public class AppTestConfiguration {

  /**
   Get default configuration for tests

   @return default configuration for tests
   */
  public static Config getDefault() {

    return ConfigFactory.parseResources("test.conf")
      .withFallback(AppConfiguration.getDefault())
      .resolve();
  }

}
