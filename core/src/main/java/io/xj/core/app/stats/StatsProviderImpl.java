// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.app.stats;

import io.xj.core.cache.audio.AudioCacheProvider;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import com.github.benmanes.caffeine.cache.stats.CacheStats;
import org.json.JSONObject;

@Singleton
public class StatsProviderImpl implements StatsProvider {
  private AudioCacheProvider audioCacheProvider;

  @Inject
  StatsProviderImpl(
    AudioCacheProvider audioCacheProvider
  ) {
    this.audioCacheProvider = audioCacheProvider;
  }

  @Override
  public JSONObject getJSON() {
    JSONObject status = new JSONObject();

    // audio cache: estimated size
    status.put("cache.audio.estimatedSize", audioCacheProvider.estimatedSize());

    // audio cache: all stats
    CacheStats cacheStats = audioCacheProvider.stats();
    status.put("cache.audio.stats.averageLoadPenalty", cacheStats.averageLoadPenalty());
    status.put("cache.audio.stats.evictionCount", cacheStats.evictionCount());
    status.put("cache.audio.stats.evictionWeight", cacheStats.evictionWeight());
    status.put("cache.audio.stats.hitCount", cacheStats.hitCount());
    status.put("cache.audio.stats.hitRate", cacheStats.hitRate());
    status.put("cache.audio.stats.loadCount", cacheStats.loadCount());
    status.put("cache.audio.stats.loadFailureCount", cacheStats.loadFailureCount());
    status.put("cache.audio.stats.loadFailureRate", cacheStats.loadFailureRate());
    status.put("cache.audio.stats.loadSuccessCount", cacheStats.loadSuccessCount());
    status.put("cache.audio.stats.averageLoadPenalty", cacheStats.averageLoadPenalty());
    status.put("cache.audio.stats.missCount", cacheStats.missCount());
    status.put("cache.audio.stats.missRate", cacheStats.missRate());
    status.put("cache.audio.stats.requestCount", cacheStats.requestCount());
    status.put("cache.audio.stats.totalLoadTime", cacheStats.totalLoadTime());

    return status;
  }
}
