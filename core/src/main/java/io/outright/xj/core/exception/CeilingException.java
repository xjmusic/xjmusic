// Copyright (c) 2016, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.exception;

public class CeilingException extends Throwable {
  private float limit;

  public CeilingException(float limit) {
    this.limit = limit;
  }

  public String toString() {
    return "the ceiling is " + Float.toString(limit);
  }

  /**
   * @param value (float) to be validated
   * @throws CeilingException if the value is too low
   */
  public static void validate(float value, float limit) throws CeilingException {
    if (value > limit) {
      throw new CeilingException(limit);
    }
  }

  /**
   * @param value (int) to be validated
   * @throws CeilingException if the value is too low
   */
  public static void validate(int value, int limit) throws CeilingException {
    validate((float) value, (float) limit);
  }

}
