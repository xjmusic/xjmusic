// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.telemetry;

import com.google.inject.Inject;
import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.NonBlockingStatsDClientBuilder;
import io.xj.lib.app.Environment;

class TelemetryProviderImpl implements TelemetryProvider {
  private final NonBlockingStatsDClient statsDClient;

  @Inject
  public TelemetryProviderImpl(
    Environment env
  ) {
    statsDClient = new NonBlockingStatsDClientBuilder()
      .prefix(env.getDatadogStatsdPrefix())
      .hostname(env.getDatadogStatsdHostname())
      .port(env.getDatadogStatsdPort())
      .build();
  }

  @Override
  public NonBlockingStatsDClient getStatsDClient() {
    return statsDClient;
  }

}
