// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.telemetry;

import com.amazonaws.services.cloudwatch.model.MetricDatum;
import com.amazonaws.services.cloudwatch.model.StandardUnit;

/**
 Base telemetry implementation
 <p>
 Requires these typesafe configurations to be set:
 - aws.defaultRegion
 - aws.accessKeyID
 - aws.secretKey
 <p>
 [#171919183] Prometheus metrics reported by Java apps and consumed by CloudWatch
 <p>
 */
public interface TelemetryProvider {

  /**
   Build a CloudWatch Metric Datum with the given name, unit, and value

   @param name  of datum
   @param unit  of datum
   @param value of datum
   @return MetricDatum
   */
  MetricDatum datum(String name, StandardUnit unit, Double value);

  /**
   Send one datum of telemetry to Amazon CloudWatch

   @param namespace in which to send datum
   @param datum     to send
   */
  void send(String namespace, MetricDatum datum);

  /**
   Send one Datum with the given name, unit, and value

   @param namespace in which to send datum
   @param name      of datum
   @param unit      of datum
   @param value     of datum
   */
  void send(String namespace, String name, StandardUnit unit, Double value);
}
