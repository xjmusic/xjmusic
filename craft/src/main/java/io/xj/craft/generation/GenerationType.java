// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.generation;

import io.xj.core.exception.CoreException;
import io.xj.core.transport.CSV;
import io.xj.core.util.Text;

import java.util.List;
import java.util.Objects;

/**
 Type of Macro-Craft, depending on previous segment existence and choices
 */
public enum GenerationType {
  LibrarySupersequence; // the first macro and main sequences in the chain


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
  public static GenerationType validate(String value) throws CoreException {
    if (Objects.isNull(value))
      throw new CoreException("type is required (" + CSV.joinEnum(values()) + ").");

    try {
      return valueOf(value);
    } catch (Exception ignored) {
      throw new CoreException("'" + value + "' is not a valid type (" + CSV.joinEnum(values()) + ").");
    }
  }
}
