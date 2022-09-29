// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.mixer;

import java.util.stream.IntStream;

/**
 Use this to apply an exponential envelope fade to the ends of audio put into a mix.
 */
public class Envelope {
  static double HALF_PI = 1.57079632679;
  private final double[] exponential;

  /**
   Cached result of exponential envelope over N frames
   */
  public Envelope(Integer frames) {
    exponential = IntStream.range(1, frames).mapToDouble(i -> Math.sin(HALF_PI * i / frames)).toArray();
  }

  /**
   Return the value at a given delta, fade in from zero frames forward

   @param delta frames progress of fade in
   @param value upon which to apply envelope
   @return value at envelope position
   */
  public double in(int delta, double value) {
    if (delta < 0) return 0;
    return delta < exponential.length ? exponential[delta] * value : value;
  }

  /**
   Return the value at a given delta, fade out from zero frames forward

   @param delta frames progress of fade out
   @param value upon which to apply envelope
   @return value at envelope position
   */
  public double out(int delta, double value) {
    if (delta > exponential.length) return 0;
    return delta > 0 ? exponential[exponential.length - delta] * value : value;
  }
}
