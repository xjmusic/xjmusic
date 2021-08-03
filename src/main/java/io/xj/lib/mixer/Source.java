// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.mixer;

import javax.sound.sampled.AudioFormat;

/**
 models a single audio source
 Source stores a series of Samples in Channels across Time, for audio playback.
 */
public interface Source {
  String STAGED = "staged";
  String LOADING = "loading";
  String READY = "ready";

  /**
   lengthMicros of the source audio

   @return lengthMicros
   */
  long lengthMicros();

  /**
   Audio file format of the source audio
   */
  AudioFormat getInputFormat();

  /**
   Get state

   @return state
   */
  String getState();

  /**
   Get reference source id

   @return source id
   */
  String getSourceId();

  /**
   Get frame rate

   @return rate
   */
  float getFrameRate();

  /**
   Get data, array of samples

   @return samples[frame][channel]
   */
  double[][] getData();

  /**
   Get the value for a given frame and channel

   @param atMicros to get frame
   @param c channel
   @return value
   */
  double getValue(long atMicros, int c);
}
