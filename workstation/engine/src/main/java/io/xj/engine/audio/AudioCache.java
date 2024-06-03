// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.engine.audio;

import io.xj.model.pojos.InstrumentAudio;
import io.xj.engine.FabricationException;

import java.io.IOException;
import java.util.List;

public interface AudioCache {
  /**
   Workstation caches all audio as float array in memory
   https://github.com/xjmusic/workstation/issues/232

   @param audio to retrieve
   @return stream if cached; null if not
   */
  AudioInMemory load(InstrumentAudio audio) throws AudioCacheException, IOException, FabricationException;

  /**
   Load all the given audios into memory and forget the rest

   @param audios the audios to load
   */
  void loadTheseAndForgetTheRest(List<InstrumentAudio> audios);

  /**
   Prepare audio on disk -- download and resample

   @param audio to prepare
   */
  AudioCacheImpl.AudioPreparedOnDisk prepare(InstrumentAudio audio) throws AudioCacheException, IOException, FabricationException;

  /**
   Initialize the audio cache with the given parameters@param targetFrameRate          to resample if necessary

   @param targetSampleBits to resample if necessary
   @param targetChannels   to resample if necessary
   */
  void initialize(
    int targetFrameRate,
    int targetSampleBits,
    int targetChannels
  );

  /**
   Invalidate all cache entries
   */
  void invalidateAll();
}
