// Copyright (c) 1999-2022, XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.nexus.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Gets or Sets SegmentState
 */
public enum SegmentState {

  PLANNED("Planned"),

  CRAFTING("Crafting"),

  CRAFTED("Crafted"),

  FAILED("Failed");

  String value;

  SegmentState(String value) {
    this.value = value;
  }

  @Override
  @JsonValue
  public String toString() {
    return String.valueOf(value);
  }

  @JsonCreator
  public static SegmentState fromValue(String value) {
    for (SegmentState b : SegmentState.values()) {
      if (b.value.equals(value)) {
        return b;
      }
    }
    throw new IllegalArgumentException("Unexpected value '" + value + "'");
  }
}


