// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.lib.notification;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsAsyncClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

import java.util.Objects;

/**
 * Implementation of Amazon SNS publisher
 */
@Service
public class NotificationProviderImpl implements NotificationProvider {
  static final Logger LOG = LoggerFactory.getLogger(NotificationProviderImpl.class);
  final String awsDefaultRegion;
  final String awsAccessKeyId;
  final String awsSecretKey;

  /**
   * Optional-- when null, no notifications are sent (as in testing)
   */
  @Nullable
  final String topicArn;

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
      LOG.info("Will not publish notifications.");
    }
  }

  /**
   * Get an Amazon S3 client
   *
   * @return S3 client
   */
  SnsAsyncClient snsClient() {
    AwsBasicCredentials awsCreds = AwsBasicCredentials.create(awsAccessKeyId, awsSecretKey);
    return SnsAsyncClient.builder()
      .region(Region.of(awsDefaultRegion))
      .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
      .build();
  }

  @Override
  public void publish(String subject, String message) {
    if (Objects.nonNull(topicArn))
      try {
        PublishRequest publishRequest = PublishRequest.builder()
          .topicArn(topicArn)
          .message(message)
          .subject(subject)
          .build();

        try (var snsClient = snsClient()) {
          snsClient.publish(publishRequest);
        }

      } catch (Exception e) {
        LOG.error("Failed to publish SNS message", e);
      }
    else
      LOG.warn("Did not publish {}: {}", subject, message);
  }

}
