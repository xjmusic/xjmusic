// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.telemetry;

import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Modules;
import io.xj.lib.app.Environment;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public class TelemetryProviderImplTest {
  @Mock
  private TelemetryProvider telemetryProvider;

  @Before
  public void setUp() {
    var env = Environment.getDefault();
    var injector = Guice.createInjector(ImmutableSet.of(Modules.override(new TelemetryModule()).with(
      new AbstractModule() {
        @Override
        public void configure() {
          bind(Environment.class).toInstance(env);
        }
      })));
    telemetryProvider = injector.getInstance(TelemetryProvider.class);
  }

  @Test
  public void getStatsDClient() {
    assertNotNull(telemetryProvider);
  }

}
