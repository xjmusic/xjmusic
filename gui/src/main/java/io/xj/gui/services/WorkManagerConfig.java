package io.xj.gui.services;

import io.xj.nexus.project.ProjectManager;
import io.xj.nexus.work.FabricationManager;
import io.xj.nexus.work.FabricationManagerImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WorkManagerConfig {

  private FabricationManager fabricationManager = null;

  private final ProjectManager projectManager;

  public WorkManagerConfig(
    ProjectManager projectManager
  ) {
    this.projectManager = projectManager;
  }

  @Bean
  public FabricationManager workManager() {
    if (fabricationManager == null) {
      fabricationManager = FabricationManagerImpl.createInstance(projectManager);
    }
    return fabricationManager;
  }
}
