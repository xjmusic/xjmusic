// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.lib.notification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 Implementation of Amazon SNS publisher
 */
@Service
public class NotificationProviderImpl implements NotificationProvider {

  @Autowired
  public NotificationProviderImpl(
  ) {
    // no op
  }

  @Override
  public void publish(String subject, String message) {
    // no op
  }

}
