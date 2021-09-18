package io.xj.lib.mixer;

import java.util.stream.IntStream;

/**
 Use this to apply an exponential envelope fade to the ends of audio put into a mix.
 */
public enum Envelope {
  ;
  static double MAX = 62.0;
  static double HALF_PI = 1.57079632679;

  /**
   Return the value at a given delta from the end of an envelope

   @param delta distance from end of envelope
   @param value upon which to apply envelope
   @return value at envelope position
   */
  public static double at(int delta, double value) {
    return delta < exponential.length ? exponential[delta] * value : value;
  }

  /**
   Cached result of exponential envelope over 300 frames
   */
  public static double[] exponential = IntStream.range(1, (int) MAX)
    .mapToDouble(i ->
      Math.sin(HALF_PI * i / MAX)
    ).toArray();
}
