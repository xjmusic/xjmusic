// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.core.cache;

import com.google.inject.AbstractModule;

public class CacheModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(AudioCacheProvider.class).to(AudioCacheProviderImpl.class);
  }
}
