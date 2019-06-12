//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.instrument;

import io.xj.core.exception.CoreException;
import io.xj.core.transport.CSV;
import io.xj.core.util.Text;

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
   Maybe in future: [#294] Instrument can be created in draft state, then published
   </p>

   @param value to cast to enum
   @return config state enum
   @throws CoreException on failure
   */
  public static InstrumentState validate(String value) throws CoreException {
    if (Objects.isNull(value))
      return Published;

    try {
      return valueOf(Text.toProperSlug(value));
    } catch (Exception ignored) {
      throw new CoreException("'" + value + "' is not a valid state (" + CSV.joinEnum(InstrumentState.values()) + ").");
    }
  }
}
