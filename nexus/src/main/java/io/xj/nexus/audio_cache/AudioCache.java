// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.audio_cache;

import io.xj.hub.tables.pojos.InstrumentAudio;
import io.xj.nexus.NexusException;
import io.xj.nexus.mixer.ActiveAudio;

import java.io.IOException;
import java.util.List;

public interface AudioCache {
  /**
   Workstation caches all audio as float array in memory
   https://www.pivotaltracker.com/story/show/186440598

   @param audio to retrieve
   @return stream if cached; null if not
   */
  CachedAudio load(InstrumentAudio audio) throws AudioCacheException, IOException, NexusException;

  /**
   Load all the given audios into memory and forget the rest

   @param audios the audios to load
   */
  void loadTheseAndForgetTheRest(List<InstrumentAudio> audios);

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

  /**
   Check if all the provided audios are loaded into memory

   @param activeAudios the audios to check
   @return true if all the audios are loaded into memory
   */
  boolean areAllReady(List<ActiveAudio> activeAudios);
}