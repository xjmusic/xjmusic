// Copyright (c) 2016, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.primitive.key;

import io.outright.xj.core.exception.EmptyException;

public class Key {
  private final String value;

  public Key(String value) throws EmptyException {
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

