// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.cache;

import com.google.inject.AbstractModule;

public class NexusCacheModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(AudioCacheProvider.class).to(AudioCacheProviderImpl.class);
  }
}
