// Copyright (c) 2016, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.exception;

public class EmptyException extends Throwable {
  public String toString() {
    return "cannot be empty";
  }

  /**
   * @param value (float) to be validated
   * @throws EmptyException if the value is too low
   */
  public static void validate(String value) throws EmptyException {
    if (value.replaceAll("\\s+","").length() == 0) {
      throw new EmptyException();
    }
  }

}
