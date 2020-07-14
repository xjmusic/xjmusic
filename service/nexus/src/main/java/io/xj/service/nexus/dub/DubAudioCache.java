// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.dub;

import com.github.benmanes.caffeine.cache.stats.CacheStats;

public interface DubAudioCache {

  /**
   Get value for a particular key

   @param key to retrieve
   @return stream if cached; null if not
   */
  DubAudioCacheItem get(String key);

  /**
   Refresh the cache for a particular key

   @param key to refresh
   */
  void refresh(String key);

  /**
   Get the estimated size currently in cache

   @return size estimate
   */
  Long estimatedSize();

  /**
   Get stats for the cache

   @return cache stats object
   */
  CacheStats stats();


}
