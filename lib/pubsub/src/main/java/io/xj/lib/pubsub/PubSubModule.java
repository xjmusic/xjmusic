// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.pubsub;

import com.google.inject.AbstractModule;

/**
 Module for injecting the cloud files store implementation
 */
public class PubSubModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(PubSubProvider.class).to(PubSubProviderImpl.class);
  }
}
