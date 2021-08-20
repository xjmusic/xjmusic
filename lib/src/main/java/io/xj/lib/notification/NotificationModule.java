// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.notification;

import com.google.inject.AbstractModule;

/**
 Module for injecting the cloud files store implementation
 */
public class NotificationModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(NotificationProvider.class).to(NotificationProviderImpl.class);
  }
}
