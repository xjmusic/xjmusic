// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.mixer;

import com.google.inject.assistedinject.Assisted;

import java.io.BufferedInputStream;
import java.io.IOException;

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
    @Assisted("mixerConfig") MixerConfig mixerConfig
  ) throws MixerException;


  /**
   Create a single Put to represent a single audio source playing at a specific time in the future.

   @param bus           to output into
   @param sourceId      to reference source by
   @param startAtMicros duration from beginning of mix
   @param stopAtMicros  duration from beginning of mix
   @param velocity      0 to 1
   @return Put
   @throws PutException on failure
   */
  Put createPut(
    @Assisted("bus") int bus,
    @Assisted("sourceId") String sourceId,
    @Assisted("startAtMicros") long startAtMicros,
    @Assisted("stopAtMicros") long stopAtMicros,
    @Assisted("velocity") double velocity
  ) throws PutException;

  /**
   models a single audio source
   Source stores a series of Samples in Channels across Time, for audio playback.
   attempt to source audio file from input stream

   @param sourceId    to reference source by
   @param inputStream to read audio file from
   @return Source
   @throws SourceException if something is wrong with the source audio
   @throws FormatException on failure interpret format
   @throws IOException     on failure to read file
   */
  Source createSource(
    @Assisted("sourceId") String sourceId,
    @Assisted("inputStream") BufferedInputStream inputStream
  ) throws SourceException, FormatException, IOException;
}
