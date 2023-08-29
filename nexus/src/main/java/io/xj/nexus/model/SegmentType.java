// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.nexus.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Gets or Sets SegmentType
 */
public enum SegmentType {

  PENDING("Pending"),

  INITIAL("Initial"),

  CONTINUE("Continue"),

  NEXTMAIN("NextMain"),

  NEXTMACRO("NextMacro");

  String value;

  SegmentType(String value) {
    this.value = value;
  }

  @Override
  @JsonValue
  public String toString() {
    return String.valueOf(value);
  }

  @JsonCreator
  public static SegmentType fromValue(String value) {
    for (SegmentType b : SegmentType.values()) {
      if (b.value.equals(value)) {
        return b;
      }
    }
    throw new IllegalArgumentException("Unexpected value '" + value + "'");
  }
}


