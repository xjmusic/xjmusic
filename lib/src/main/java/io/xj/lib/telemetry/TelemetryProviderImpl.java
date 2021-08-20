// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.telemetry;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClientBuilder;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.MetricDatum;
import com.amazonaws.services.cloudwatch.model.PutMetricDataRequest;
import com.amazonaws.services.cloudwatch.model.PutMetricDataResult;
import com.amazonaws.services.cloudwatch.model.StandardUnit;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import io.xj.lib.app.Environment;

import java.util.Collection;

class TelemetryProviderImpl implements TelemetryProvider {
  private final AmazonCloudWatch client;
  private final String namespace;
  private final Collection<Dimension> dimensions;

  @Inject
  public TelemetryProviderImpl(
    Environment env
  ) {
    String awsDefaultRegion = env.getAwsDefaultRegion();
    String awsAccessKeyId = env.getAwsAccessKeyID();
    String awsSecretKey = env.getAwsSecretKey();
    namespace = env.getTelemetryNamespace();
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
  public AmazonCloudWatch getClient() {
    return client;
  }

  @Override
  public PutMetricDataResult put(MetricDatum datum) {
    return client.putMetricData(new PutMetricDataRequest()
      .withNamespace(namespace)
      .withMetricData(datum));
  }

  @Override
  public void put(String name, StandardUnit unit, double value) {
    put(new MetricDatum()
      .withMetricName(name)
      .withUnit(unit)
      .withDimensions(dimensions)
      .withValue(value));
  }
}
