// Copyright (c) 2016, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.exception;

public class FloorException extends Throwable {
  private float limit;

  public FloorException(float limit) {
    this.limit = limit;
  }

  public String toString() {
    return "the floor is " + Float.toString(limit);
  }

  /**
   * @param value (float) to be validated
   * @throws FloorException if the value is too low
   */
  public static void validate(float value, float limit) throws FloorException {
    if (value < limit) {
      throw new FloorException(limit);
    }
  }

  /**
   * @param value (int) to be validated
   * @throws FloorException if the value is too low
   */
  public static void validate(int value, int limit) throws FloorException {
    validate((float) value, (float) limit);
  }

}
