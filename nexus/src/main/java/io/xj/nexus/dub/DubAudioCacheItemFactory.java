// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.dub;


import io.xj.lib.filestore.FileStoreException;
import io.xj.nexus.NexusException;

import java.io.IOException;

/**
 Load audio from disk to memory, or if necessary, from S3 to disk (for future caching), then to memory.
 <p>
 NO LONGER using Caffeine in-memory caching-- just caching on disk originally loading from S3
 <p>
 Advanced audio caching during fabrication https://www.pivotaltracker.com/story/show/176642679
 */
public interface DubAudioCacheItemFactory {

  /**
   @param key  of item
   @param path of waveform data on disk
   @throws IOException        on failure to read or write from disk
   @throws FileStoreException on failure to load from filestore
   */
  DubAudioCacheItem load(
     String key,
     String path
  ) throws IOException, FileStoreException, NexusException;
}
