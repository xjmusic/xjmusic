// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui;

import io.xj.lib.app.AppConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WorkstationConfiguration {

  @Bean
  public AppConfiguration appConfiguration() {
    return new AppConfiguration("gui");
  }
}
