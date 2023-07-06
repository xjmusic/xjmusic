// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.nexus;

import io.xj.lib.app.AppConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NexusConfiguration {

  @Bean
  public AppConfiguration appConfiguration() {
    return new AppConfiguration("nexus");
  }
}
