// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.dub;

import io.xj.lib.filestore.FileStoreException;
import io.xj.hub.util.StringUtils;
import io.xj.nexus.NexusException;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Service
public class DubAudioCacheImpl implements DubAudioCache {
  final Logger log = LoggerFactory.getLogger(DubAudioCacheImpl.class);
  String pathPrefix;
  final DubAudioCacheItemFactory dubAudioCacheItemFactory;

  @Autowired
  public DubAudioCacheImpl(
    DubAudioCacheItemFactory dubAudioCacheItemFactory,
    @Value("${audio.cache.file-prefix}") String audioCacheFilePrefix
  ) {
    this.dubAudioCacheItemFactory = dubAudioCacheItemFactory;

    try {
      pathPrefix = 0 < audioCacheFilePrefix.length() ?
        audioCacheFilePrefix :
        Files.createTempDirectory("cache").toAbsolutePath().toString();
      // make directory for cache files
      File dir = new File(pathPrefix);
      if (!dir.exists()) {
        FileUtils.forceMkdir(dir);
      }
      log.debug("Initialized audio cache directory: {}", pathPrefix);

    } catch (IOException e) {
      log.error("Failed to initialize audio cache directory", e);
    }
  }

  @Override
  public String load(String key, int targetFrameRate) throws FileStoreException, IOException, NexusException {
    if (StringUtils.isNullOrEmpty(key)) throw new FileStoreException("Can't load null or empty audio key!");
    return dubAudioCacheItemFactory.load(pathPrefix, key, targetFrameRate).getAbsolutePath();
  }
}
