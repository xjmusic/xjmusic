// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.telemetry;

import com.google.inject.AbstractModule;

/**
 Module for injecting the cloud files store implementation
 */
public class TelemetryModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(TelemetryProvider.class).to(TelemetryProviderImpl.class);
  }
}
