package io.xj.ship;

import io.xj.lib.app.AppConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ShipConfiguration {

  @Bean
  public AppConfiguration appConfiguration() {
    return new AppConfiguration("ship");
  }
}
