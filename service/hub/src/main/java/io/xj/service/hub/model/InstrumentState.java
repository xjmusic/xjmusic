// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.model;

import io.xj.lib.util.CSV;
import io.xj.lib.util.Text;
import io.xj.lib.util.ValueException;

import java.util.List;
import java.util.Objects;

public enum InstrumentState {
  Draft,
  Published,
  Erase;

  /**
   String Values

   @return ImmutableList of string values
   */
  public static List<String> stringValues() {
    return Text.toStrings(values());
  }

  /**
   cast string to enum
   <p>
   Maybe in future: [#294] Instrument can be createdin draft state, then published
   </p>

   @param value to cast to enum
   @return config state enum
   @throws ValueException on failure
   */
  public static InstrumentState validate(String value) throws ValueException {
    if (Objects.isNull(value))
      return Published;

    try {
      return valueOf(Text.toProperSlug(value));
    } catch (Exception ignored) {
      throw new ValueException("'" + value + "' is not a valid state (" + CSV.joinEnum(InstrumentState.values()) + ").");
    }
  }
}
