// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.telemetry;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClientBuilder;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.MetricDatum;
import com.amazonaws.services.cloudwatch.model.PutMetricDataRequest;
import com.amazonaws.services.cloudwatch.model.StandardUnit;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import io.xj.lib.app.Environment;

import java.util.Collection;

class TelemetryProviderImpl implements TelemetryProvider {
  private final AmazonCloudWatch client;
  private final String namespace;
  private final Collection<Dimension> dimensions;
  private final Boolean enabled;

  @Inject
  public TelemetryProviderImpl(
    Environment env
  ) {
    String awsAccessKeyId = env.getAwsAccessKeyID();
    String awsDefaultRegion = env.getAwsDefaultRegion();
    String awsSecretKey = env.getAwsSecretKey();
    enabled = env.isTelemetryEnabled();
    namespace = env.getTelemetryNamespace();
    if (!enabled) {
      client = null;
      dimensions = null;
      return;
    }
    BasicAWSCredentials credentials = new BasicAWSCredentials(awsAccessKeyId, awsSecretKey);
    client = AmazonCloudWatchClientBuilder.standard()
      .withRegion(awsDefaultRegion)
      .withCredentials(new AWSStaticCredentialsProvider(credentials))
      .build();
    dimensions = ImmutableList.of(
      new Dimension()
        .withName("Env")
        .withValue(env.getPlatformEnvironment()));
  }

  @Override
  public void put(String name, StandardUnit unit, double value) {
    if (!enabled) return;
    client.putMetricData(new PutMetricDataRequest()
      .withNamespace(namespace)
      .withMetricData(
        new MetricDatum()
          .withMetricName(name)
          .withUnit(unit)
          .withDimensions(dimensions)
          .withValue(value)));
  }
}
