// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.telemetry;

import com.timgroup.statsd.NonBlockingStatsDClient;

/**
 Requires these typesafe configurations to be set:
 - datadog.apiKey
 */
public interface TelemetryProvider {

  /**
   Get the statsd client
   @return statsd client
   */
  NonBlockingStatsDClient getStatsDClient();
}
