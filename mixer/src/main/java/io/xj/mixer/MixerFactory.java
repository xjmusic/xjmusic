// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.mixer;

import io.xj.mixer.impl.exception.FormatException;
import io.xj.mixer.impl.exception.MixerException;
import io.xj.mixer.impl.exception.PutException;
import io.xj.mixer.impl.exception.SourceException;

import com.google.inject.assistedinject.Assisted;

import javax.sound.sampled.AudioFormat;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.time.Duration;

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

   @param outputFormat to write final output audio
   @return Mixer
   @throws MixerException if unable to create Mixer
   */
  Mixer createMixer(
    @Assisted("outputContainer") OutputContainer outputContainer,
    @Assisted("outputFormat") AudioFormat outputFormat,
    @Assisted("outputLength") Duration outputLength
  ) throws MixerException;


  /**
   Create a single Put to represent a single audio source playing at a specific time in the future.

   @param sourceId      to reference source by
   @param startAtMicros duration from beginning of mix
   @param stopAtMicros  duration from beginning of mix
   @param velocity      0 to 1
   @param pitchRatio    relative to original = 1.0
   @param pan           -1 to +1 = Left to Right (stereo), or however many channels there actually are
   @return Mix
   @throws PutException on failure
   */
  Put createPut(
    @Assisted("sourceId") String sourceId,
    @Assisted("startAtMicros") long startAtMicros,
    @Assisted("stopAtMicros") long stopAtMicros,
    @Assisted("velocity") double velocity,
    @Assisted("pitchRatio") double pitchRatio,
    @Assisted("pan") double pan
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
