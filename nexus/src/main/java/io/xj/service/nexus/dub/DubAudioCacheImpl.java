// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.dub;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.typesafe.config.Config;
import datadog.trace.api.Trace;
import io.xj.lib.filestore.FileStoreException;
import io.xj.lib.filestore.FileStoreProvider;
import io.xj.lib.util.TempFile;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

@Singleton
class DubAudioCacheImpl implements DubAudioCache {
  final Logger log = LoggerFactory.getLogger(DubAudioCacheImpl.class);
  final String pathPrefix;
  private final FileStoreProvider fileStoreProvider;
  private final LoadingCache<String, DubAudioCacheItem> items;
  private final String audioFileBucket;

  @Inject
  DubAudioCacheImpl(
    FileStoreProvider fileStoreProvider,
    Config config
  ) {
    this.fileStoreProvider = fileStoreProvider;
    audioFileBucket = config.getString("audio.fileBucket");
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
        .removalListener(this::remove)
        .build(this::fetchAndWrite);
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
  public DubAudioCacheItem get(String key) {
    return items.get(key);
  }

  @Override
  public void refresh(String key) {
    items.refresh(key);
  }

  /**
   item has been evicted of cache

   @param key   of item to remove
   @param item  to remove
   @param cause of removal
   */
  private void remove(String key, DubAudioCacheItem item, RemovalCause cause) {
    item.remove();
    log.info("Removed {} because {}", key, cause);
  }

  /**
   Compute the value of a cache item

   @param key to compute value for
   @return computed item
   */
  @Trace(resourceName = "nexus/dub/cache", operationName = "fetchAndWrite")
  private DubAudioCacheItem fetchAndWrite(String key) throws IOException, FileStoreException {
    String path = String.format("%s%s", pathPrefix, key);
    DubAudioCacheItem item = new DubAudioCacheItem(key, path);
    try (InputStream stream = fileStoreProvider.streamS3Object(audioFileBucket, key)) {
      item.writeFrom(stream);
    }
    return item;
  }
}
