// Copyright (c) 2016, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.primitive.offset;

import io.outright.xj.core.exception.CeilingException;
import io.outright.xj.core.exception.FloorException;

public class Offset {
  private final int value;

  public Offset(int value) throws FloorException {
    FloorException.validate(value,0);
    this.value = value;
  }

  /**
   * Value
   * @return int
   */
  public int Value() {
    return value;
  }
}

