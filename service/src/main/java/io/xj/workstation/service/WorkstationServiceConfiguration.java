// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.workstation.service;

import io.xj.lib.app.AppConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WorkstationServiceConfiguration {

  @Bean
  public AppConfiguration appConfiguration() {
    return new AppConfiguration("nexus");
  }
}
