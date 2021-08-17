// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.telemetry;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClientBuilder;
import com.amazonaws.services.cloudwatch.model.MetricDatum;
import com.amazonaws.services.cloudwatch.model.PutMetricDataRequest;
import com.amazonaws.services.cloudwatch.model.PutMetricDataResult;
import com.google.inject.Inject;
import io.xj.lib.app.Environment;

class TelemetryProviderImpl implements TelemetryProvider {
  private final AmazonCloudWatch client;
  private final String namespace;

  @Inject
  public TelemetryProviderImpl(
    Environment env
  ) {
    String awsDefaultRegion = env.getAwsDefaultRegion();
    String awsAccessKeyId = env.getAwsAccessKeyID();
    String awsSecretKey = env.getAwsSecretKey();
    namespace = env.getNamespace();
    BasicAWSCredentials credentials = new BasicAWSCredentials(awsAccessKeyId, awsSecretKey);
    client = AmazonCloudWatchClientBuilder.standard()
      .withRegion(awsDefaultRegion)
      .withCredentials(new AWSStaticCredentialsProvider(credentials))
      .build();
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
}
