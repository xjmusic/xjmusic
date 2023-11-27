// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.audio_cache;

import io.xj.hub.tables.pojos.InstrumentAudio;
import io.xj.nexus.NexusException;

import java.io.IOException;

public interface AudioCache {

  /**
   Workstation caches all audio as float array in memory
   https://www.pivotaltracker.com/story/show/186440598

   @param audio to retrieve
   @return stream if cached; null if not
   */
  CachedAudio load(InstrumentAudio audio) throws AudioCacheException, IOException, NexusException;

  /**
   Prepare audio on disk -- download and resample

   @param audio to prepare
   */
  void prepare(InstrumentAudio audio) throws AudioCacheException, IOException, NexusException;

  /**
   Initialize the audio cache with the given parameters

   @param contentStoragePathPrefix to retrieve from
   @param audioBaseUrl             to retrieve from
   @param targetFrameRate          to resample if necessary
   @param targetSampleBits         to resample if necessary
   @param targetChannels           to resample if necessary
   */
  void initialize(
    String contentStoragePathPrefix,
    String audioBaseUrl,
    int targetFrameRate,
    int targetSampleBits,
    int targetChannels
  );

  /**
   Invalidate all cache entries
   */
  void invalidateAll();
}
