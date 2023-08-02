// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.nexus.ship;

import io.xj.lib.util.StringUtils;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

public enum ShipMode {
  Playback,
  HLS,
  WAV;

  public static Optional<ShipMode> from(String value) {
    return Arrays.stream(values())
      .filter((m) -> value.toLowerCase(Locale.ROOT).equals(m.toString().toLowerCase(Locale.ROOT)))
      .findAny();
  }

  public boolean equals(String value) {
    return
      !StringUtils.isNullOrEmpty(value) &&
        toString().toLowerCase(Locale.ROOT).equals(value.toLowerCase(Locale.ROOT));
  }
}
