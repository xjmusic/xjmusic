// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.entity;

import io.xj.lib.util.CSV;
import io.xj.lib.util.Text;
import io.xj.lib.util.ValueException;

import java.util.List;
import java.util.Objects;

/**
 [#153976073] Artist wants Pattern to have type *Macro* or *Main* (for Macro- or Main-type sequences), or *Intro*, *Loop*, or *Outro* (for Rhythm or Detail-type Sequence) in order to of a composition that is dynamic when chosen to fill a Segment.
 */
public enum ProgramSequencePatternType {
  Intro,
  Loop,
  Outro;

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
  public static ProgramSequencePatternType validate(String value) throws ValueException {
    if (Objects.isNull(value))
      throw new ValueException("Type is required");

    try {
      return valueOf(Text.toProperSlug(value));
    } catch (Exception e) {
      throw new ValueException("'" + value + "' is not a valid type (" + CSV.joinEnum(values()) + ").", e);
    }
  }

  /**
   Pattern types available for detail (including Rhythm) sequences

   @return types
   */
  public static ProgramSequencePatternType[] valuesForDetailSequence() {
    return new ProgramSequencePatternType[]{Intro, Loop, Outro};
  }

  /**
   String Values

   @return ImmutableList of string values
   */
  public static List<String> stringValuesForDetailSequence() {
    return Text.toStrings(valuesForDetailSequence());
  }


}
