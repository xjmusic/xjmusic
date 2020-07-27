// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.pubsub;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sns.AmazonSNSAsync;
import com.amazonaws.services.sns.AmazonSNSAsyncClientBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 Implementation of Amazon SNS publisher
 <p>
 Singleton to instantiate the client only once per application
 */
@Singleton
class PubSubProviderImpl implements PubSubProvider {
  private static final Logger log = LoggerFactory.getLogger(PubSubProviderImpl.class);
  private final AmazonSNSAsync client;

  @Inject
  public PubSubProviderImpl(
    Config config
  ) {
    String awsDefaultRegion = config.getString("aws.defaultRegion");
    String awsAccessKeyId = config.getString("aws.accessKeyID");
    String awsSecretKey = config.getString("aws.secretKey");
    BasicAWSCredentials credentials = new BasicAWSCredentials(awsAccessKeyId, awsSecretKey);
    client = AmazonSNSAsyncClientBuilder.standard()
      .withRegion(awsDefaultRegion)
      .withCredentials(new AWSStaticCredentialsProvider(credentials))
      .build();
  }

  @Override
  public void publish(String topicArn, String message, String subject) {
    try {
      client.publish(topicArn, message, subject);

    } catch (Exception e) {
      log.error("Failed to publish SNS message", e);
    }
  }

}
