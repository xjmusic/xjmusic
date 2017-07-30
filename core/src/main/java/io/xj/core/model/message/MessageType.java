// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.model.message;

import com.google.common.collect.ImmutableList;

public enum MessageType {
  Debug,
  Info,
  Warning,
  Error;

  /**
   String Values
   @return ImmutableList of string values
   */
  public static ImmutableList<String> stringValues() {
    ImmutableList.Builder<String> valuesBuilder = ImmutableList.builder();
    for (MessageType value : values()) {
      valuesBuilder.add(value.toString());
    }
    return valuesBuilder.build();
  }

}
