// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.cache.audio;

import io.xj.core.cache.audio.impl.Item;

import com.github.benmanes.caffeine.cache.stats.CacheStats;

public interface AudioCacheProvider {

  /**
   Get value for a particular key

   @param key to retrieve
   @return stream if cached; null if not
   */
  Item get(String key);

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
