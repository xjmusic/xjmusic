// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.lib.mixer;


import org.springframework.beans.factory.annotation.Qualifier;

import java.io.IOException;
import java.util.UUID;

/**
 * Mix Factory provides encapsulated modules
 * (in the case of Source, in-memory audio-file-caching)
 * that operate together in order to mix a single
 * finite group of source audio in a particular
 * fashion to an output stream.
 */
public interface MixerFactory {

  /**
   * Create a single Mix instance which mixes to a single output stream and format.
   *
   * @param mixerConfig configuration for mixer
   * @return Mixer
   * @throws MixerException if unable to of Mixer
   */
  Mixer createMixer(
    @Qualifier("mixerConfig") MixerConfig mixerConfig
  ) throws MixerException;


  /**
   * Create a single Put to represent a single audio source playing at a specific time in the future.
   *
   * @param id            to uniquely identify this put
   * @param audioId       by which to reference source
   * @param bus           to output into
   * @param startAtMicros duration from beginning of mix
   * @param stopAtMicros  duration from beginning of mix
   * @param velocity      0 to 1
   * @return Put
   * @throws PutException on failure
   */
  Put createPut(
    UUID id,
    UUID audioId,
    int bus,
    long startAtMicros,
    long stopAtMicros,
    double velocity,
    int attackMillis,
    int releaseMillis
  ) throws PutException;

  /**
   * models a single audio source, a series of Samples in Channels across Time, for audio playback.
   * attempt to source audio file from input stream
   * <p>
   * Fabrication should not completely fail because of one bad source audio https://www.pivotaltracker.com/story/show/182575665
   *
   * @param audioId      by which to reference source
   * @param absolutePath from which to read audio file
   * @param description  in case of failure logs
   * @param frameRate    to conform all audio
   * @return Source
   * @throws SourceException if something is wrong with the source audio
   * @throws FormatException on failure interpret format
   * @throws IOException     on failure to read file
   */
  Source createSource(
    UUID audioId,
    String absolutePath,
    String description,
    float frameRate
  ) throws SourceException, FormatException, IOException;
}
