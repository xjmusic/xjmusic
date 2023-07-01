// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.notification;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSAsyncClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 Implementation of Amazon SNS publisher
 */
@Service
public class NotificationProviderImpl implements NotificationProvider {
  private static final Logger LOG = LoggerFactory.getLogger(NotificationProviderImpl.class);
  private final String awsDefaultRegion;
  private final String awsAccessKeyId;
  private final String awsSecretKey;

  /**
   Optional-- when null, no notifications are sent (as in testing)
   */
  @Nullable
  private final String topicArn;

  @Autowired
  public NotificationProviderImpl(
    @Value("${aws.default.region}")
    String awsDefaultRegion,
    @Value("${aws.access.key.id}")
    String awsAccessKeyId,
    @Value("${aws.secret.key}")
    String awsSecretKey,
    @Value("${aws.sns.topic.arn}")
    String awsSnsTopicArn
  ) {
    this.awsAccessKeyId = awsAccessKeyId;
    this.awsDefaultRegion = awsDefaultRegion;
    this.awsSecretKey = awsSecretKey;
    // If not configured, will warn instead of publishing
    if (0 < awsSnsTopicArn.length()) {
      topicArn = awsSnsTopicArn;
      LOG.info("Will publish notifications to {}", topicArn);
    } else {
      topicArn = null;
      LOG.info("Will not publish notifications because environment has no aws snsTopicArn");
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
  public void publish(String subject, String message) {
    if (Objects.nonNull(topicArn))
      try {
        snsClient().publish(topicArn, message, subject);

      } catch (Exception e) {
        LOG.error("Failed to publish SNS message", e);
      }
    else
      LOG.warn("Did not publish {}: {}", subject, message);
  }

}
