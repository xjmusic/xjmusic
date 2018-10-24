//  Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.mixer.util;

import io.xj.mixer.impl.exception.MixerException;

/**
 Math utilities for mixer
 */
public interface MathUtil {

  /**
   Quick implementation of "Mixing two digital audio streams
   with on the fly Loudness Normalization
   by Logarithmic Dynamic Range Compression" by Paul Vögler

   @param v input value
   @return output value
   */
  static double logarithmicCompression(double v) {
    if (-1 > v)
      return -StrictMath.log(-v - 0.85) / 14 - 0.75;
    else if (1 < v)
      return StrictMath.log(v - 0.85) / 14 + 0.75;
    else
      return v / 1.61803398875;
  }

  /**
   Apply logarithmic dynamic range compression to each channels of a frame

   @param frame to operate on
   @return compressed frame
   */
  static double[] logarithmicCompression(double[] frame) {
    int iL = frame.length;
    double[] out = new double[iL];
    for (int i = 0; i < iL; i++)
      out[i] = logarithmicCompression(frame[i]);

    return out;
  }

  /**
   Enforce a maximum

   @param valueMax   maximum allowable value
   @param entityName name of entity, for error message
   @param value      actual
   @throws MixerException if value greater than allowable
   */
  static void enforceMax(int valueMax, String entityName, int value) throws MixerException {
    if (value > valueMax)
      throw new MixerException("more than " + valueMax + " " + entityName + " not allowed");
  }

  /**
   Enforce a minimum

   @param valueMin   minimum allowable value
   @param entityName name of entity, for error message
   @param value      actual
   @throws MixerException if value less than allowable
   */
  static void enforceMin(int valueMin, String entityName, int value) throws MixerException {
    if (value < valueMin)
      throw new MixerException("less than " + valueMin + " " + entityName + " not allowed");
  }

  /**
   Get the maximum absolute value found in an entire two-dimensional buffer of double values

   @param buf to search for maximum absolute value
   @return maximum absolute value found in buffer
   */
  static double maxAbs(double[][] buf) {
    return maxAbs(buf, 0, buf.length);
  }

  /**
   Get the maximum absolute value found in a section of a two-dimensional buffer of double values,
   from/to specified index

   @param buf to search for maximum absolute value
   @param iFr index to search from
   @param iTo index to search to
   @return maximum absolute value found in buffer
   */
  static double maxAbs(double[][] buf, int iFr, int iTo) {
    double M = 0;
    int iF = Math.max(iFr, 0);
    int iT = Math.min(iTo, buf.length);
    for (int i = iF; i < iT; i++) {
      for (double v : buf[i])
        M = Math.max(M, Math.abs(v));
    }
    return M;
  }

  /**
   Get an incremental delta from actual to target value

   @param target    to move towards
   @param actual    moving from
   @param dividedBy total # of increments
   @return incremental value delta
   */
  static double delta(double target, double actual, long dividedBy) {
    if (Double.isInfinite(actual) || Double.isInfinite(target)) return 0;
    if (0 == dividedBy) return 0;
    return (target - actual) / dividedBy;
  }

  /**
   Limit an input value within a floor and ceiling

   @param floor lower limit
   @param ceil upper limit
   @param value to limit
   @return limited value
   */
  static double limit(double floor, double ceil, double value) {
    return Math.min(ceil, Math.max(floor, value));
  }

  /**
   Volume ratio for right channel for a given pan.

   @param pan -1 to +1
   @return ratio
   */
  static double right(double pan) {
    if (0 <= pan) {
      // 0 to +1 (right) = full volume
      return 1;
    } else {
      // <0 to -1 = decay to zero;
      return 1 - Math.abs(pan);
    }
  }

  /**
   Volume ratio for left channel for a given pan.

   @param pan -1 to +1
   @return ratio
   */
  static double left(double pan) {
    if (0 >= pan) {
      // 0 to -1 (left) = full volume
      return 1;
    } else {
      // >0 to +1 = decay to zero;
      return 1 - pan;
    }
  }

}
