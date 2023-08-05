// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.lib.notification;

/**
 * Requires these typesafe configurations to be set:
 * - aws.defaultRegion
 * - aws.accessKeyID
 * - aws.secretKey
 */
public interface NotificationProvider {

  /**
   * Publish a message on Amazon SNS
   * Messages that result from Chain fabrication are persisted@param message  to publish@param subject of message https://www.pivotaltracker.com/story/show/173968289
   */
  void publish(String subject, String message);

}
