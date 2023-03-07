// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.telemetry;

import io.xj.lib.app.AppConfiguration;
import io.xj.lib.app.AppEnvironment;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TelemetryProviderImplTest {
  private TelemetryProvider subject;

  private final AppEnvironment env = AppEnvironment.from(Map.of(
    "SHIP_KEY", "coolair",
    "APP_NAME", "nexus"
  ));

  private final AppConfiguration config = new AppConfiguration("nexus");

  @Before
  public void setUp() throws IOException {
    subject = new TelemetryProviderImpl(env, config);
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
