// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.generation;

import io.xj.service.hub.HubException;
import io.xj.lib.util.CSV;
import io.xj.lib.util.Text;

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
   @throws HubException on failure
   */
  public static GenerationType validate(String value) throws HubException {
    if (Objects.isNull(value))
      throw new HubException("type is required (" + CSV.joinEnum(values()) + ").");

    try {
      return valueOf(value);
    } catch (Exception ignored) {
      throw new HubException("'" + value + "' is not a valid type (" + CSV.joinEnum(values()) + ").");
    }
  }
}
