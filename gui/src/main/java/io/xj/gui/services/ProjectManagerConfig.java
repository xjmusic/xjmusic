package io.xj.gui.services;

import io.xj.nexus.entity.EntityFactory;
import io.xj.nexus.entity.EntityFactoryImpl;
import io.xj.nexus.http.HttpClientProvider;
import io.xj.nexus.json.JsonProvider;
import io.xj.nexus.json.JsonProviderImpl;
import io.xj.nexus.project.ProjectManager;
import io.xj.nexus.project.ProjectManagerImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;

@Configuration
public class ProjectManagerConfig {
  private final HttpClientProvider httpClientProvider;
  private final int downloadAudioRetries;
  private ProjectManager projectManager;

  public ProjectManagerConfig(
    @Value("${download.audio.retries}") int downloadAudioRetries,
    HttpClientProvider httpClientProvider
  ) {
    this.httpClientProvider = httpClientProvider;
    this.downloadAudioRetries = downloadAudioRetries;
  }

  @Bean
  public ProjectManager projectManager() {
    if (Objects.isNull(projectManager)) {
      JsonProvider jsonProvider = new JsonProviderImpl();
      EntityFactory entityFactory = new EntityFactoryImpl(jsonProvider);
      projectManager = new ProjectManagerImpl(httpClientProvider, jsonProvider, entityFactory, downloadAudioRetries);
    }
    return projectManager;
  }
}
