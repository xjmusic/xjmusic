package io.xj.gui.services;

import io.xj.nexus.entity.EntityFactory;
import io.xj.nexus.entity.EntityFactoryImpl;
import io.xj.nexus.http.HttpClientProvider;
import io.xj.nexus.http.HttpClientProviderImpl;
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

  private final int downloadAudioRetries;
  private ProjectManager projectManager;

  public ProjectManagerConfig(
    @Value("${download.audio.retries}") int downloadAudioRetries
  ) {
    this.downloadAudioRetries = downloadAudioRetries;
  }

  @Bean
  public ProjectManager projectManager() {
    if (Objects.isNull(projectManager)) {
      HttpClientProvider httpClientProvider = new HttpClientProviderImpl();
      JsonProvider jsonProvider = new JsonProviderImpl();
      EntityFactory entityFactory = new EntityFactoryImpl(jsonProvider);
      projectManager = new ProjectManagerImpl(httpClientProvider, jsonProvider, entityFactory, downloadAudioRetries);
    }
    return projectManager;
  }
}
