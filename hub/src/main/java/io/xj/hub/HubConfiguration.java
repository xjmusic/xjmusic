package io.xj.hub;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import io.xj.lib.app.AppConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HubConfiguration {

  @Bean
  public AppConfiguration appConfiguration() {
    return new AppConfiguration("hub");
  }

  @Bean
  public HttpTransport httpTransport() {
    return new NetHttpTransport();
  }

}
