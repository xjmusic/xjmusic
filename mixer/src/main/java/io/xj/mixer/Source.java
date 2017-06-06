// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.mixer;

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
   audio frame at a specific Tz, volume (0 to 1), and pan (-1 to +1)

   @param atMicros    since beginning of source
   @param volume      to mix output to
   @param pan         to mix output to
   @param outChannels to mix output to
   @return array of samples
   */
  double[] frameAt(long atMicros, double volume, double pan, int outChannels);

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
}
