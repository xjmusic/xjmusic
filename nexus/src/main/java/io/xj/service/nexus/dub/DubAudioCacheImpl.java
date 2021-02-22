// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.dub;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.typesafe.config.Config;
import datadog.trace.api.Trace;
import io.xj.lib.filestore.FileStoreException;
import io.xj.lib.util.TempFile;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

@Singleton
class DubAudioCacheImpl implements DubAudioCache {
  final Logger log = LoggerFactory.getLogger(DubAudioCacheImpl.class);
  final String pathPrefix;
  private final LoadingCache<String, DubAudioCacheItem> items;
  private final DubAudioCacheItemFactory dubAudioCacheItemFactory;

  @Inject
  DubAudioCacheImpl(
    Config config,
    DubAudioCacheItemFactory dubAudioCacheItemFactory
  ) {
    this.dubAudioCacheItemFactory = dubAudioCacheItemFactory;
    long allocateBytes = config.getLong("audio.cacheAllocateBytes");
    pathPrefix = config.hasPath("audio.cacheFilePrefix") ?
      config.getString("audio.cacheFilePrefix") :
      TempFile.getTempFilePathPrefix() + "cache" + File.separator;

    try {
      // make directory for cache files
      File dir = new File(pathPrefix);
      if (!dir.exists()) {
        FileUtils.forceMkdir(dir);
      }
      log.info("Initialized audio cache directory: {}", pathPrefix);

    } catch (IOException e) {
      log.error("Failed to initialize audio cache directory: {}", pathPrefix, e);
    }

    LoadingCache<String, DubAudioCacheItem> loadItems = null;
    try {
      loadItems = Caffeine.newBuilder()
        .maximumWeight(allocateBytes)
        .weigher((String key, DubAudioCacheItem item) -> item.size())
        .build(this::load);
      log.info("Initialized Caffeine cache, allocated {} bytes", allocateBytes);

    } catch (Exception e) {
      log.error("Failed to initialize Caffeine cache", e);
    }

    items = loadItems;
  }

  @Override
  public Long estimatedSize() {
    return items.estimatedSize();
  }

  @Override
  public CacheStats stats() {
    return items.stats();
  }

  @Override
  public BufferedInputStream get(String key) {
    return Objects.requireNonNull(items.get(key)).getBytes();
  }

  @Override
  public void refresh(String key) {
    items.refresh(key);
  }

  /**
   Compute the value of a cache item

   @param key to compute value for
   @return computed item
   */
  @Trace(resourceName = "nexus/dub/cache", operationName = "fetchAndWrite")
  private DubAudioCacheItem load(String key) throws IOException, FileStoreException {
    return dubAudioCacheItemFactory.load(key, String.format("%s%s", pathPrefix, key));
  }
}
