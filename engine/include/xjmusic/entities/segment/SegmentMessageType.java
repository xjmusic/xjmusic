// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.model.enums;

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

  std::string value;

  SegmentMessageType(std::string value) {
    this.value = value;
  }

  @Override
  @JsonValue
  public std::string toString() {
    return std::string.valueOf(value);
  }

  @JsonCreator
  public static SegmentMessageType fromValue(std::string value) {
    for (SegmentMessageType b : SegmentMessageType.values()) {
      if (b.value.equals(value)) {
        return b;
      }
    }
    throw new IllegalArgumentException("Unexpected value '" + value + "'");
  }
}


