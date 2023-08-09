package io.xj.workstation.service;

import static org.junit.jupiter.api.Assertions.*;

class WorkstationServiceConfigurationTest {
  @org.junit.jupiter.api.Test
  void appConfiguration() {
    WorkstationServiceConfiguration configuration = new WorkstationServiceConfiguration();
    assertNotNull(configuration.appConfiguration());
    assertEquals("nexus", configuration.appConfiguration().getName());
  }
}
