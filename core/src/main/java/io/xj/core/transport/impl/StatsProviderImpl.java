// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.transport.impl;

import io.xj.core.cache.audio.AudioCacheProvider;
import io.xj.core.payload.PayloadObject;
import io.xj.core.transport.StatsProvider;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import com.github.benmanes.caffeine.cache.stats.CacheStats;

@Singleton
public class StatsProviderImpl implements StatsProvider {
  private final AudioCacheProvider audioCacheProvider;

  @Inject
  StatsProviderImpl(
    AudioCacheProvider audioCacheProvider
  ) {
    this.audioCacheProvider = audioCacheProvider;
  }

  @Override
  public PayloadObject toPayloadObject() {
    PayloadObject status = new PayloadObject();

    // audio cache: estimated size
    status.setAttribute("cache.audio.estimatedSize", audioCacheProvider.estimatedSize());

    // audio cache: all stats
    CacheStats cacheStats = audioCacheProvider.stats();
    status.setAttribute("cache.audio.stats.averageLoadPenalty", cacheStats.averageLoadPenalty());
    status.setAttribute("cache.audio.stats.evictionCount", cacheStats.evictionCount());
    status.setAttribute("cache.audio.stats.evictionWeight", cacheStats.evictionWeight());
    status.setAttribute("cache.audio.stats.hitCount", cacheStats.hitCount());
    status.setAttribute("cache.audio.stats.hitRate", cacheStats.hitRate());
    status.setAttribute("cache.audio.stats.loadCount", cacheStats.loadCount());
    status.setAttribute("cache.audio.stats.loadFailureCount", cacheStats.loadFailureCount());
    status.setAttribute("cache.audio.stats.loadFailureRate", cacheStats.loadFailureRate());
    status.setAttribute("cache.audio.stats.loadSuccessCount", cacheStats.loadSuccessCount());
    status.setAttribute("cache.audio.stats.averageLoadPenalty", cacheStats.averageLoadPenalty());
    status.setAttribute("cache.audio.stats.missCount", cacheStats.missCount());
    status.setAttribute("cache.audio.stats.missRate", cacheStats.missRate());
    status.setAttribute("cache.audio.stats.requestCount", cacheStats.requestCount());
    status.setAttribute("cache.audio.stats.totalLoadTime", cacheStats.totalLoadTime());

    return status;
  }
}
