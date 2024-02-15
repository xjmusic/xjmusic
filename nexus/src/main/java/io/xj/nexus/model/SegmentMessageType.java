// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.nexus.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 Gets or Sets SegmentMessageType
 */
public enum SegmentMessageType {

  DEBUG("Debug"),

  INFO("Info"),

  WARNING("Warning"),

  ERROR("Error");

  String value;

  SegmentMessageType(String value) {
    this.value = value;
  }

  @JsonCreator
  public static SegmentMessageType fromValue(String value) {
    for (SegmentMessageType b : SegmentMessageType.values()) {
      if (b.value.equals(value)) {
        return b;
      }
    }
    throw new IllegalArgumentException("Unexpected value '" + value + "'");
  }

  @Override
  @JsonValue
  public String toString() {
    return String.valueOf(value);
  }
}


