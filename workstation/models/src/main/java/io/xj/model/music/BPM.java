// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.model.music;

/**
 Beats-Per-Minute
 (utilities)
 */
public class BPM {

  static final long NANOSECONDS_PER_SECOND = 1000000000;

  /**
   Nanoseconds length of a given number of beats at a given Beats Per Minute.

   @param beats total length
   @param bpm   beats per minute
   */
  public static long beatsNanos(long beats, double bpm) {
    return (long) (NANOSECONDS_PER_SECOND * beats * (60 / bpm));
  }

  /**
   Velocity in seconds-ber-beat for any BPM

   @param bpm to get seconds-per-beat for
   @return velocity in seconds-per-beat
   */
  public static double velocity(double bpm) {
    return 60 / bpm;
  }

}
