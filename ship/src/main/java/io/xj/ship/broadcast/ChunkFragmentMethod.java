// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.ship.broadcast;

import java.util.Locale;

public enum ChunkFragmentMethod {
  MANUAL,
  MP4BOX;

  public static ChunkFragmentMethod fromString(String value) {
    try {
      return valueOf(value.toUpperCase(Locale.ROOT));
    } catch (Exception e) {
      return MANUAL;
    }
  }
}
