// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.engine.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 Gets or Sets ChainState
 */
public enum ChainState {

  DRAFT("Draft"),

  READY("Ready"),

  FABRICATE("Fabricate"),

  FAILED("Failed");

  final String value;

  ChainState(String value) {
    this.value = value;
  }

  @Override
  @JsonValue
  public String toString() {
    return String.valueOf(value);
  }

  @JsonCreator
  public static ChainState fromValue(String value) {
    for (ChainState b : ChainState.values()) {
      if (b.value.equals(value)) {
        return b;
      }
    }
    throw new IllegalArgumentException("Unexpected value '" + value + "'");
  }
}


