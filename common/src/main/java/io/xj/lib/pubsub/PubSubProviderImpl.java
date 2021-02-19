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

import javax.annotation.Nullable;
import java.util.Objects;

/**
 Implementation of Amazon SNS publisher
 */
class PubSubProviderImpl implements PubSubProvider {
  private static final Logger log = LoggerFactory.getLogger(PubSubProviderImpl.class);
  private final String awsDefaultRegion;
  private final String awsAccessKeyId;
  private final String awsSecretKey;

  /**
   Optional-- when null, no notifications are sent (as in testing)
   */
  @Nullable
  private final String topicArn;

  @Inject
  public PubSubProviderImpl(
    Config config
  ) {
    awsDefaultRegion = config.getString("aws.defaultRegion");
    awsAccessKeyId = config.getString("aws.accessKeyID");
    awsSecretKey = config.getString("aws.secretKey");

    // If not configured, will warn instead of publishing
    if (config.hasPath("aws.snsTopicArn")) {
      topicArn = config.getString("aws.snsTopicArn");
      log.info("Will publish notifications to {}", topicArn);
    } else {
      topicArn = null;
      log.warn("Will not publish notifications because no aws.snsTopicArn is configured.");
    }

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
  public void publish(String message, String subject) {
    if (Objects.nonNull(topicArn))
      try {
        snsClient().publish(topicArn, message, subject);

      } catch (Exception e) {
        log.error("Failed to publish SNS message", e);
      }
    else
      log.warn("Did not publish {}: {}", subject, message);
  }

}
