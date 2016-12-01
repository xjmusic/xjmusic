// Copyright (c) 2016, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.primitive.name;

import io.outright.xj.core.exception.EmptyException;

public class Name {
  private final String value;

  public Name(String value) throws EmptyException {
    EmptyException.validate(value);
    this.value = value;
  }

  /**
   * Value
   * @return string
   */
  public String Value() {
    return value;
  }
}

