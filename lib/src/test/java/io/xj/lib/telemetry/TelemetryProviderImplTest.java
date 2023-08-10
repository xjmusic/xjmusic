// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.lib.telemetry;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TelemetryProviderImplTest {
  TelemetryProvider subject;

  @BeforeEach
  public void setUp() throws IOException {
    subject = new TelemetryProviderImpl("coolair", true);
  }

  @Test
  public void getStatsDClient() {
    assertNotNull(subject);
  }

  @Test
  public void prefixedLowerSnake() {
    assertEquals("coolair_segments", subject.prefixedLowerSnake("segments"));
  }

  @Test
  public void prefixedProperSpace() {
    assertEquals("Coolair Segments", subject.prefixedProperSpace("segments"));
  }
}
