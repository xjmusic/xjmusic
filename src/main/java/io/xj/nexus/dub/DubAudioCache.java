// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.dub;

import com.github.benmanes.caffeine.cache.stats.CacheStats;

import java.io.BufferedInputStream;

public interface DubAudioCache {

  /**
   Get bytes of audio for a particular key
   <p>
   [#176642679] Advanced audio caching during fabrication
   <p>
   Original DubAudioCacheItem should not be implemented with Caffeine-- this is the mechanism we use only for downloading files not already present to disk.
   <p>
   Implement Caffeine after loading the audio data from disk into memory-- the real speed lift here is from keeping the audio in memory

   @return stream if cached; null if not
   @param key to retrieve
   */
  BufferedInputStream get(String key);

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
