// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.telemetry;

import com.google.inject.Inject;
import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.NonBlockingStatsDClientBuilder;
import io.xj.lib.app.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class TelemetryProviderImpl implements TelemetryProvider {
  final Logger LOG = LoggerFactory.getLogger(TelemetryProviderImpl.class);
  private final NonBlockingStatsDClient statsDClient;

  @Inject
  public TelemetryProviderImpl(
    Environment env
  ) {
    var prefix = env.getDatadogStatsdPrefix();
    var hostname = env.getDatadogStatsdHostname();
    var port = env.getDatadogStatsdPort();
    LOG.info("Will send telemetry to statsd client prefix:{} hostname:{} port:{}", prefix, hostname, port);
    statsDClient = new NonBlockingStatsDClientBuilder()
      .prefix(prefix)
      .hostname(hostname)
      .port(port)
      .build();
  }

  @Override
  public NonBlockingStatsDClient getStatsDClient() {
    return statsDClient;
  }

}
