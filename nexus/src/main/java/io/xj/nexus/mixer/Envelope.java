// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.nexus.mixer;

import java.util.stream.IntStream;

/**
 Use this to apply an exponential envelope fade to the ends of audio put into a mix.
 */
public class Envelope {
  static float HALF_PI = 1.57079632679f;
  final float[] exponential;

  /**
   Cached result of exponential envelope over N frames
   */
  public Envelope(Integer frames) {
    exponential = new float[frames];
    IntStream.range(1, frames).forEach(i -> exponential[i - 1] = (float) Math.sin(HALF_PI * i / frames));
  }

  /**
   Return the value at a given delta, fade in from zero frames forward

   @param delta frames progress of fade in
   @param value upon which to apply envelope
   @return value at envelope position
   */
  public float in(int delta, float value) {
    if (delta < 0) return 0;
    return delta < exponential.length ? exponential[delta] * value : value;
  }

  /**
   Return the value at a given delta, fade out from zero frames forward

   @param delta frames progress of fade out
   @param value upon which to apply envelope
   @return value at envelope position
   */
  public float out(int delta, float value) {
    if (delta > exponential.length) return 0;
    return delta > 0 ? exponential[exponential.length - delta] * value : value;
  }
}
