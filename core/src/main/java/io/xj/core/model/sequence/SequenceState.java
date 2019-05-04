// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.sequence;

import io.xj.core.exception.CoreException;
import io.xj.core.transport.CSV;
import io.xj.core.util.Text;

import java.util.Objects;

public enum SequenceState {
  Draft,
  Published,
  Erase;

  /**
   cast string to enum
   <p>
   FUTURE: Sequence can be created in draft state, then published
   </p>

   @param value to cast to enum
   @return config state enum
   @throws CoreException on failure
   */
  public static SequenceState validate(String value) throws CoreException {
    if (Objects.isNull(value))
      return Published;

    try {
      return valueOf(Text.toProperSlug(value));
    } catch (Exception ignored) {
      throw new CoreException("'" + value + "' is not a valid state (" + CSV.joinEnum(SequenceState.values()) + ").");
    }
  }
}
