// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model;

import io.xj.core.exception.CoreException;
import io.xj.core.util.CSV;
import io.xj.core.util.Text;

import java.util.List;
import java.util.Objects;

public enum WorkState {
  Queued,
  Expected;

  /**
   String Values

   @return ImmutableList of string values
   */
  public static List<String> stringValues() {
    return Text.toStrings(values());
  }

  /**
   cast string to enum

   @param value to cast to enum
   @return enum
   @throws CoreException on failure
   */
  public static WorkState validate(String value) throws CoreException {
    if (Objects.isNull(value))
      return Expected;

    try {
      return valueOf(Text.toProperSlug(value));
    } catch (Exception e) {
      throw new CoreException("'" + value + "' is not a valid state (" + CSV.joinEnum(values()) + ").", e);
    }
  }

}
