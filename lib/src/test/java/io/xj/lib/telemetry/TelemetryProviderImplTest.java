// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.lib.telemetry;

import io.xj.lib.app.AppConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TelemetryProviderImplTest {
  TelemetryProvider subject;
  final AppConfiguration config = new AppConfiguration("nexus");

  @BeforeEach
  public void setUp() throws IOException {
    subject = new TelemetryProviderImpl(config, "coolair", true);
  }

  @Test
  public void getStatsDClient() {
    assertNotNull(subject);
  }

  @Test
  public void prefixedLowerSnake() {
    assertEquals("coolair_nexus_segments", subject.prefixedLowerSnake("segments"));
  }

  @Test
  public void prefixedProperSpace() {
    assertEquals("Coolair Nexus Segments", subject.prefixedProperSpace("segments"));
  }
}
