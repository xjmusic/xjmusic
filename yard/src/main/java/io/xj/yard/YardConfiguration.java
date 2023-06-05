package io.xj.yard;

import io.xj.lib.app.AppConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class YardConfiguration {

  @Bean
  public AppConfiguration appConfiguration() {
    return new AppConfiguration("yard");
  }
}
