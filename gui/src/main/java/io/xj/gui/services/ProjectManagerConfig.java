package io.xj.gui.services;

import io.xj.nexus.project.ProjectManager;
import io.xj.nexus.project.ProjectManagerImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProjectManagerConfig {

  private ProjectManager projectManager = null;

  @Bean
  public ProjectManager projectManager() {
    if (projectManager == null) {
      projectManager = ProjectManagerImpl.createInstance();
    }
    return projectManager;
  }
}
