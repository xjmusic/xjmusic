// Copyright (c) 2016, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.primitive.note;

import io.outright.xj.core.exception.CeilingException;
import io.outright.xj.core.exception.EmptyException;
import io.outright.xj.core.exception.FloorException;

public class Note {
  private final String value;

  public Note(String value) throws EmptyException {
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

