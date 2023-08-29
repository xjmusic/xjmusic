// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.workstation.service;

import io.xj.nexus.work.WorkFactory;
import io.xj.workstation.service.api.WorkstationServiceHealthController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkstationServiceHealthControllerTest {
  WorkstationServiceHealthController subject;

  @Mock
  WorkFactory workFactory;

  @BeforeEach
  void setUp() {
    subject = new WorkstationServiceHealthController(workFactory);
  }

  @Test
  void healthcheck() {
    when(workFactory.isHealthy()).thenReturn(true);

    assertTrue(subject.index().getStatusCode().is2xxSuccessful());
  }

  @Test
  void healthcheck_failsWhenUnhealthy() {
    when(workFactory.isHealthy()).thenReturn(false);

    assertFalse(subject.index().getStatusCode().is2xxSuccessful());
  }


}
