package io.xj.gui.services;

import io.xj.nexus.http.HttpClientProvider;
import io.xj.nexus.http.HttpClientProviderImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;

@Configuration
public class HttpClientProviderConfig {
  private HttpClientProvider httpClientProvider;

  @Bean
  public HttpClientProvider httpClientProvider() {
    if (Objects.isNull(httpClientProvider)) {
      httpClientProvider = new HttpClientProviderImpl();
    }
    return httpClientProvider;
  }
}
