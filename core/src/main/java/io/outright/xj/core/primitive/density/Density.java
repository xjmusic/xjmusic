// Copyright (c) 2016, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.primitive.density;

import io.outright.xj.core.exception.CeilingException;
import io.outright.xj.core.exception.FloorException;

public class Density {
  private final float value;

  public Density(float value) throws FloorException, CeilingException {
    FloorException.validate(value,0);
    CeilingException.validate(value,1);
    this.value = value;
  }

  /**
   * Value
   * @return float
   */
  public float Value() {
    return value;
  }
}

