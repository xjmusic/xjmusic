// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.workstation.service;

import io.xj.nexus.work.WorkManager;
import io.xj.workstation.service.api.WorkstationServiceHealthController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkstationServiceHealthControllerTest {
  WorkstationServiceHealthController subject;

  @Mock
  WorkManager workManager;

  @BeforeEach
  void setUp() {
    subject = new WorkstationServiceHealthController(workManager);
  }

  @Test
  void healthcheck() {
    when(workManager.isHealthy()).thenReturn(true);

    assertTrue(subject.index().getStatusCode().is2xxSuccessful());
  }

  @Test
  void healthcheck_failsWhenUnhealthy() {
    when(workManager.isHealthy()).thenReturn(false);

    assertFalse(subject.index().getStatusCode().is2xxSuccessful());
  }


}
