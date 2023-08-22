// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.dub;

import io.xj.lib.filestore.FileStoreException;
import io.xj.nexus.NexusException;

import java.io.IOException;

public interface DubAudioCache {

  /**
   * Get bytes of audio for a particular key
   * <p>
   * NO LONGER using Caffeine in-memory caching-- just caching on disk originally loading from S3
   * <p>
   * Advanced audio caching during fabrication https://www.pivotaltracker.com/story/show/176642679
   *
   * @param key              to retrieve
   * @param targetFrameRate  to resample if necessary
   * @param targetSampleBits
   * @param targetChannels
   * @return stream if cached; null if not
   */
  String load(String key, int targetFrameRate, int targetSampleBits, int targetChannels) throws FileStoreException, IOException, NexusException;

}
