// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.engine.mixer;


/**
 Mix Factory provides encapsulated modules
 (in the case of Source, in-memory audio-file-caching)
 that operate together in order to mix a single
 finite group of source audio in a particular
 fashion to an output stream.
 */
public interface MixerFactory {

  /**
   Create a single Mix instance which mixes to a single output stream and format.

   @param mixerConfig configuration for mixer
   @return Mixer
   @throws MixerException if unable to of Mixer
   */
  Mixer createMixer(
    MixerConfig mixerConfig
  ) throws MixerException;
}
