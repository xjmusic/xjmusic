// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.lib.mixer;

import javax.sound.sampled.AudioFormat;
import java.util.Optional;
import java.util.UUID;

/**
 Models a single audio source.
 Stores a series of Samples in Channels across Time, for audio playback.
 <p>
 Dub mixes audio from disk (not memory) to avoid heap overflow https://www.pivotaltracker.com/story/show/180206211
 */
public interface Source {
  /**
   @return absolute path to audio file
   */
  String getAbsolutePath();

  /**
   @return source audio file format
   */
  Optional<AudioFormat> getAudioFormat();

  /**
   @return # of channels in source audio
   */
  int getChannels();

  /**
   @return # of available sample frames of source audio
   */
  long getFrameLength();

  /**
   @return source audio frame rate
   */
  float getFrameRate();

  /**
   @return length of the source audio, in microseconds
   */
  long getLengthMicros();

  /**
   @return length of source audio, in seconds
   */
  double getLengthSeconds();

  /**
   @return microseconds per sample frame
   */
  double getMicrosPerFrame();

  /**
   @return the sample size, in bytes
   */
  int getSampleSize();

  /**
   @return source id, for usage
   */
  UUID getAudioId();
}
