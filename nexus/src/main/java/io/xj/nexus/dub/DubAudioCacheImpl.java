// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.dub;

import com.google.common.base.Strings;
import io.xj.lib.app.AppEnvironment;
import io.xj.lib.filestore.FileStoreException;
import io.xj.lib.util.Files;
import io.xj.nexus.NexusException;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
public class DubAudioCacheImpl implements DubAudioCache {
  final Logger log = LoggerFactory.getLogger(DubAudioCacheImpl.class);
  final String pathPrefix;
  private final DubAudioCacheItemFactory dubAudioCacheItemFactory;

  @Autowired
  public DubAudioCacheImpl(
    AppEnvironment env,
    DubAudioCacheItemFactory dubAudioCacheItemFactory
  ) {
    this.dubAudioCacheItemFactory = dubAudioCacheItemFactory;
    pathPrefix = 0 < env.getAudioCacheFilePrefix().length() ?
      env.getAudioCacheFilePrefix() :
      Files.getTempFilePathPrefix() + "cache" + File.separator;

    try {
      // make directory for cache files
      File dir = new File(pathPrefix);
      if (!dir.exists()) {
        FileUtils.forceMkdir(dir);
      }
      log.debug("Initialized audio cache directory: {}", pathPrefix);

    } catch (IOException e) {
      log.error("Failed to initialize audio cache directory: {}", pathPrefix, e);
    }
  }

  @Override
  public String getAbsolutePath(String key) throws FileStoreException, IOException, NexusException {
    if (Strings.isNullOrEmpty(key)) throw new FileStoreException("Can't load null or empty audio key!");
    return dubAudioCacheItemFactory.load(key, String.format("%s%s", pathPrefix, key)).getAbsolutePath();
  }
}
