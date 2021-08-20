// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.telemetry;

import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.model.MetricDatum;
import com.amazonaws.services.cloudwatch.model.PutMetricDataResult;
import com.amazonaws.services.cloudwatch.model.StandardUnit;

/**
 Send custom telemetry to CloudWatch #179247968
 */
public interface TelemetryProvider {

  /**
   Get Amazon CloudWatch Client

   @return cloudwatch client
   */
  AmazonCloudWatch getClient();

  /**
   Put a metric datum in this app's namespace

   @param datum to put
   @return result
   */
  PutMetricDataResult put(MetricDatum datum);

  /**
   Put  metric datum

   @param name  of datum
   @param unit  of datum
   @param value of datum
   */
  void put(String name, StandardUnit unit, double value);
}
