// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.pubsub;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSAsyncClientBuilder;
import com.google.inject.Inject;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 Implementation of Amazon SNS publisher
 */
class PubSubProviderImpl implements PubSubProvider {
  private static final Logger log = LoggerFactory.getLogger(PubSubProviderImpl.class);
  private final String awsDefaultRegion;
  private final String awsAccessKeyId;
  private final String awsSecretKey;

  @Inject
  public PubSubProviderImpl(
    Config config
  ) {
    awsDefaultRegion = config.getString("aws.defaultRegion");
    awsAccessKeyId = config.getString("aws.accessKeyID");
    awsSecretKey = config.getString("aws.secretKey");
  }

  /**
   Get an Amazon S3 client

   @return S3 client
   */
  private AmazonSNS snsClient() {
    BasicAWSCredentials credentials = new BasicAWSCredentials(awsAccessKeyId, awsSecretKey);
    return AmazonSNSAsyncClientBuilder.standard()
      .withRegion(awsDefaultRegion)
      .withCredentials(new AWSStaticCredentialsProvider(credentials))
      .build();
  }

  @Override
  public void publish(String topicArn, String message, String subject) {
    try {
      snsClient().publish(topicArn, message, subject);

    } catch (Exception e) {
      log.error("Failed to publish SNS message", e);
    }
  }

}
