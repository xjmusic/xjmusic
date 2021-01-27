// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.telemetry;

import com.google.inject.Inject;
import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.NonBlockingStatsDClientBuilder;
import com.typesafe.config.Config;

class TelemetryProviderImpl implements TelemetryProvider {
  private final NonBlockingStatsDClient statsDClient;

  @Inject
  public TelemetryProviderImpl(
    Config config
  ) {
    statsDClient = new NonBlockingStatsDClientBuilder()
      .prefix(config.getString("datadog.statsd.prefix"))
      .hostname(config.getString("datadog.statsd.hostname"))
      .port(config.getInt("datadog.statsd.port"))
      .build();
  }

  @Override
  public NonBlockingStatsDClient getStatsDClient() {
    return statsDClient;
  }

}
