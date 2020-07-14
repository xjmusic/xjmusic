// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.entity;

import io.xj.lib.util.CSV;
import io.xj.lib.util.Text;
import io.xj.lib.util.ValueException;

import java.util.List;
import java.util.Objects;

/**
 Type of Macro-Craft, depending on previous segment existence and choices
 */
public enum SegmentType {
  Pending, // Segments are created with this type before a real one is assigned.
  Initial, // Only the first Segment in a Chain: Choose first Macro and Main program.
  Continue, // Continue the Main Program (and implicitly, continue the Macro program too)
  NextMain, // Choose the next Main program (but continue the Macro program)
  NextMacro; // Choose the next Macro program (and implicitly, the next Main program too)

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
   @throws ValueException on failure
   */
  public static SegmentType validate(String value) throws ValueException {
    if (Objects.isNull(value))
      throw new ValueException("Type is required");

    try {

      return valueOf(Text.toProperSlug(value));
    } catch (Exception e) {
      throw new ValueException("'" + value + "' is not a valid type (" + CSV.joinEnum(values()) + ").", e);
    }
  }
}
