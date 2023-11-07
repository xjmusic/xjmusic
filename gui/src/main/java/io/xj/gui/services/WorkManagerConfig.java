package io.xj.gui.services;

import io.xj.nexus.work.WorkManager;
import io.xj.nexus.work.WorkManagerImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WorkManagerConfig {

  private WorkManager workManager = null;

  @Bean
  public WorkManager myBean() {
    if (workManager == null) {
      workManager = WorkManagerImpl.createInstance();
    }
    return workManager;
  }
}
