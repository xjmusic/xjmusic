// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.cache;

import com.google.inject.AbstractModule;
import io.xj.core.cache.audio.AudioCacheProvider;
import io.xj.core.cache.audio.impl.AudioCacheProviderImpl;

public class CacheModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(AudioCacheProvider.class).to(AudioCacheProviderImpl.class);
  }
}
