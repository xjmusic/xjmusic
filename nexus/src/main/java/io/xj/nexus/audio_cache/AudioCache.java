// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.audio_cache;

import io.xj.nexus.NexusException;
import io.xj.nexus.filestore.FileStoreException;

import java.io.IOException;
import java.util.UUID;

public interface AudioCache {

  /**
   Workstation caches all audio as float array in memory
   https://www.pivotaltracker.com/story/show/186440598

   @param contentStoragePathPrefix to retrieve from
   @param audioBaseUrl             to retrieve from
   @param instrumentId             to retrieve
   @param key                      to retrieve
   @param targetFrameRate          to resample if necessary
   @param targetSampleBits         to resample if necessary
   @param targetChannels           to resample if necessary
   @return stream if cached; null if not
   */
  CachedAudio load(String contentStoragePathPrefix, String audioBaseUrl, UUID instrumentId, String key, int targetFrameRate, int targetSampleBits, int targetChannels) throws FileStoreException, IOException, NexusException;

  /**
   Prepare audio on disk -- download and resample

   * @param contentStoragePathPrefix to retrieve from
   * @param audioBaseUrl             to retrieve from
   * @param instrumentId             to retrieve
   * @param waveformKey              to retrieve
   * @param targetFrameRate          to resample if necessary
   * @param targetSampleBits         to resample if necessary
   * @param targetChannels           to resample if necessary
   */
  void prepare(String contentStoragePathPrefix, String audioBaseUrl, UUID instrumentId, String waveformKey, int targetFrameRate, int targetSampleBits, int targetChannels) throws FileStoreException, IOException, NexusException;
}
