// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.cache;

import io.xj.core.app.config.Config;

import com.google.common.collect.Maps;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Objects;

public enum StreamCache {
  CACHE;

  final String pathPrefix = Config.cacheFilePathPrefix();
  final Logger log = LoggerFactory.getLogger(StreamCache.class);
  final Map<String, Content> contents = Maps.newConcurrentMap();

  StreamCache() {
    // destroy contents of pathPrefix
    try {
      FileUtils.deleteDirectory(new File(pathPrefix));
    } catch (IOException e) {
      log.error(String.format("Initialization failed to delete directory: %s", pathPrefix), e);
      return;
    }

    try {
      FileUtils.forceMkdir(new File(pathPrefix));
    } catch (IOException e) {
      log.error(String.format("Initialization failed to make directory: %s", pathPrefix), e);
      return;
    }

    log.info(String.format("Initialized new cache: %s", pathPrefix));
  }

  /**
   Get stream data, if already cached

   @param key to retrieve
   @return stream if cached; null if not
   */
  @Nullable
  public static InputStream getContent(String key) {
    Content content = CACHE.contents.get(key);

    // If nothing is cached, return null
    if (Objects.isNull(content)) {
      return null;
    }

    try {
      return FileUtils.openInputStream(new File(content.getPath()));
    } catch (IOException e) {
      CACHE.log.error(String.format("Failed to READ data for: %s", content.getPath()), e);
      return null;
    }
  }

  /**
   Get stream data, if already cached

   @param key to retrieve
   @return stream if cached; null if not
   */
  public static Boolean containsKey(String key) {
    return CACHE.contents.containsKey(key);
  }

  /**
   Set stream data to save in cache

   @param key    to store
   @param stream data to cache
   */
  public static void setContent(String key, InputStream stream) {
    Content content = new Content(key);

    try {
      content.save(stream);
      CACHE.contents.put(key, content);
    } catch (IOException e) {
      CACHE.log.error(String.format("Failed to WRITE data for: %s", content.getPath()), e);
    }
  }

  /**
   Garbage collection routine
   */
  public static void cleanup() {
    // TODO cleanup routine
    // TODO for each contents of cache, sorted by last used date, most recent to most stale, tally up sum of total bytes stored
    // TODO when contents of cache is greater or equal to max cache file storage space, delete all remaining contents and delete corresponding stored files
  }

}
