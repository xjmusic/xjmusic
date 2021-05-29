// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.dub;

import com.google.inject.assistedinject.Assisted;
import io.xj.lib.filestore.FileStoreException;

import java.io.IOException;

/**
 Load audio from disk to memory, or if necessary, from S3 to disk (for future caching), then to memory.
 <p>
 [#176642679] Advanced audio caching during fabrication
 <p>
 Original DubAudioCacheItem should not be implemented with Caffeine-- this is the mechanism we use only for downloading files not already present to disk.
 <p>
 Implement Caffeine after loading the audio data from disk into memory-- the real speed lift here is from keeping the audio in memory
 */
public interface DubAudioCacheItemFactory {

  /**
   @param key  of item
   @param path of waveform data on disk
   @throws IOException        on failure to read or write from disk
   @throws FileStoreException on failure to load from filestore
   */
  DubAudioCacheItem load(
    @Assisted("key") String key,
    @Assisted("path") String path
  ) throws IOException, FileStoreException;
}
