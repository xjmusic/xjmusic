// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.pubsub;

/**
 Requires these typesafe configurations to be set:
 - aws.defaultRegion
 - aws.accessKeyID
 - aws.secretKey
 */
public interface PubSubProvider {

  /**
   Publish a message on Amazon SNS
   [#173968289] Messages that result from Chain fabrication are persisted@param message  to publish
   @param subject  of message


   */
  void publish(String message, String subject);

}
