package io.xj.gui.services;

import io.xj.nexus.project.ProjectManager;
import io.xj.nexus.work.WorkManager;
import io.xj.nexus.work.WorkManagerImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WorkManagerConfig {

  private WorkManager workManager = null;

  private final ProjectManager projectManager;

  public WorkManagerConfig(
    ProjectManager projectManager
  ) {
    this.projectManager = projectManager;
  }

  @Bean
  public WorkManager workManager() {
    if (workManager == null) {
      workManager = WorkManagerImpl.createInstance(projectManager);
    }
    return workManager;
  }
}
